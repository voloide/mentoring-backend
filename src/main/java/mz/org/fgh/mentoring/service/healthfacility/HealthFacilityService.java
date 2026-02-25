package mz.org.fgh.mentoring.service.healthfacility;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import mz.org.fgh.mentoring.dto.healthFacility.HealthFacilityDTO;
import mz.org.fgh.mentoring.entity.healthfacility.HealthFacility;
import mz.org.fgh.mentoring.entity.location.Location;
import mz.org.fgh.mentoring.entity.partner.Partner;
import mz.org.fgh.mentoring.entity.tutor.Tutor;
import mz.org.fgh.mentoring.error.MentoringBusinessException;
import mz.org.fgh.mentoring.error.RecordInUseException;
import mz.org.fgh.mentoring.repository.healthFacility.HealthFacilityRepository;
import mz.org.fgh.mentoring.repository.location.LocationRepository;
import mz.org.fgh.mentoring.repository.partner.PartnerRepository;
import mz.org.fgh.mentoring.repository.ronda.RondaRepository;
import mz.org.fgh.mentoring.repository.tutor.TutorRepository;
import mz.org.fgh.mentoring.util.Utilities;

import javax.transaction.Transactional;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Singleton
public class HealthFacilityService {

    private final HealthFacilityRepository healthFacilityRepository;

    @Inject
    private TutorRepository tutorRepository;

    private final LocationRepository locationRepository;
    private final RondaRepository rondaRepository;

    private final PartnerRepository partnerRepository;
    private final ObjectMapper objectMapper;

    public HealthFacilityService(HealthFacilityRepository healthFacilityRepository,
                                 LocationRepository locationRepository,
                                 RondaRepository rondaRepository,
                                 PartnerRepository partnerRepository,
                                 ObjectMapper objectMapper) {
        this.healthFacilityRepository = healthFacilityRepository;
        this.locationRepository = locationRepository;
        this.rondaRepository = rondaRepository;
        this.partnerRepository = partnerRepository;
        this.objectMapper = objectMapper;
    }

    public List<HealthFacility> findAllHealthFacilities() {
        return healthFacilityRepository.findAll();
    }

    public List<HealthFacilityDTO> findAllOfDistrict(Long districtId) {
        try {
            return Utilities.parseList(this.healthFacilityRepository.findByDistrictId(districtId), HealthFacilityDTO.class);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public List<HealthFacilityDTO> getAll(Long limit, Long offset) {
        try {
            List<HealthFacility> healthFacilities = new ArrayList<>();
            if(limit!=null && offset!=null && limit>0) {
                healthFacilities = healthFacilityRepository.findHealthFacilitiesWithLimit(limit, offset);
            } else {
                healthFacilities = healthFacilityRepository.findAll();
            }
            return Utilities.parseList(healthFacilities, HealthFacilityDTO.class);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    public List<HealthFacilityDTO> findAllOfProvince(Long provinceId) {
        return null;
    }

    public HealthFacility findById(Long id){
       return this.healthFacilityRepository.findById(id).get();
    }

    public List<HealthFacilityDTO> getAllOfMentor(String uuid, Long limit, Long offset) {
        Optional<Tutor> tutor = tutorRepository.findByUuid(uuid);
        List<HealthFacility> healthFacilities = new ArrayList<>();

        if (tutor.isPresent()) {
            for (Location location : tutor.get().getEmployee().getLocations()) {
                healthFacilities.addAll(healthFacilityRepository.findByDistrictId(location.getDistrict().getId()));
            }
        }
        try {
            return Utilities.parseList(healthFacilities, HealthFacilityDTO.class);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public List<HealthFacility> getByDistricts(List<String> uuids) {
        return healthFacilityRepository.getAllOfDistrict(uuids);
    }


    public Page<HealthFacilityDTO> getByPageAndSize(Pageable pageable) {
        Page<HealthFacility> pageHealthFacilities = this.healthFacilityRepository.findAll(pageable);

        List<HealthFacility> hfList = pageHealthFacilities.getContent();

        List<HealthFacilityDTO> healthFacilities = new ArrayList<HealthFacilityDTO>();
        for (HealthFacility healthFacility: hfList) {
            HealthFacilityDTO hfDTO = new HealthFacilityDTO(healthFacility);
            healthFacilities.add(hfDTO);
        }

        return pageHealthFacilities.map(this::hfToDTO);
    }

    private HealthFacilityDTO hfToDTO(HealthFacility healthFacility){
        return new HealthFacilityDTO(healthFacility);
    }

    public Page<HealthFacility> findAll(@Nullable Pageable pageable) {
        return healthFacilityRepository.findAll(pageable);
    }

    public Page<HealthFacility> searchByName(String name, Pageable pageable) {
        return healthFacilityRepository.findByHealthFacilityIlike("%" + name + "%", pageable);
    }

    @Transactional
    public HealthFacility create(HealthFacilityDTO dto, String userUuid) {
        HealthFacility entity = dto.toEntity();
        entity.setUuid(java.util.UUID.randomUUID().toString());
        entity.setCreatedBy(userUuid);
        entity.setCreatedAt(mz.org.fgh.mentoring.util.DateUtils.getCurrentDate());
        entity.setLifeCycleStatus(mz.org.fgh.mentoring.util.LifeCycleStatus.ACTIVE);

        applyPartners(entity, dto);

        return healthFacilityRepository.save(entity);
    }

    @Transactional
    public HealthFacility update(HealthFacilityDTO dto, String userUuid) {
        HealthFacility existing = healthFacilityRepository.findByUuid(dto.getUuid())
                .orElseThrow(() -> new MentoringBusinessException("Unidade sanitária não encontrada com UUID: " + dto.getUuid()));

        // atualiza campos normais
        existing.setHealthFacility(dto.getHealthFacility());
        if (dto.getDistrictDTO() != null) existing.setDistrict(new mz.org.fgh.mentoring.entity.location.District(dto.getDistrictDTO()));
        existing.setUpdatedBy(userUuid);
        existing.setUpdatedAt(mz.org.fgh.mentoring.util.DateUtils.getCurrentDate());

        // aplica parceiros
        applyPartners(existing, dto);

        return healthFacilityRepository.update(existing);
    }

    @Transactional
    public HealthFacility updateLifeCycleStatus(String uuid, mz.org.fgh.mentoring.util.LifeCycleStatus status, String userUuid) {
        HealthFacility facility = healthFacilityRepository.findByUuid(uuid)
                .orElseThrow(() -> new MentoringBusinessException("Unidade sanitária não encontrada com UUID: " + uuid));

        facility.setLifeCycleStatus(status);
        facility.setUpdatedAt(mz.org.fgh.mentoring.util.DateUtils.getCurrentDate());
        facility.setUpdatedBy(userUuid);

        return healthFacilityRepository.update(facility);
    }

    @Transactional
    public void delete(String uuid) {
        HealthFacility facility = healthFacilityRepository.findByUuid(uuid)
                .orElseThrow(() -> new MentoringBusinessException("Unidade sanitária não encontrada com UUID: " + uuid));

        long used = locationRepository.countByHealthFacility(facility) + rondaRepository.countByHealthFacility(facility);
        // Verificações adicionais podem ser colocadas aqui se necessário
        if (used > 0) throw new RecordInUseException("Unidade sanitária associada a outros registos, impossível apagar.");

        healthFacilityRepository.delete(facility);
    }

    private String toJson(List<Long> ids) {
        try {
            if (ids == null || ids.isEmpty()) return null;
            return objectMapper.writeValueAsString(ids);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao serializar outros parceiros.", e);
        }
    }

    private void applyPartners(HealthFacility entity, HealthFacilityDTO dto) {
        // Parceiro clínico
        if (dto.getClinicalPartnerId() != null) {
            Partner clinical = partnerRepository.findById(dto.getClinicalPartnerId())
                    .orElseThrow(() -> new RuntimeException("Parceiro Clínico não encontrado: " + dto.getClinicalPartnerId()));
            entity.setClinicalPartner(clinical);
        } else {
            entity.setClinicalPartner(null);
        }

        // Outros parceiros
        List<Long> others = dto.getOtherPartnerIds();

        if (others != null && dto.getClinicalPartnerId() != null && others.contains(dto.getClinicalPartnerId())) {
            throw new RuntimeException("Outros Parceiros não pode conter o Parceiro Clínico.");
        }

        if (others != null) {
            for (Long id : others) {
                if (id == null) continue;
                if (!partnerRepository.existsById(id)) {
                    throw new RuntimeException("Parceiro em Outros Parceiros não existe: " + id);
                }
            }
        }

        entity.setOtherPartners(toJson(others));
    }
}
