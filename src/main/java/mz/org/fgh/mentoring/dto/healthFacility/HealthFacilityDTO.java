package mz.org.fgh.mentoring.dto.healthFacility;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mz.org.fgh.mentoring.base.BaseEntityDTO;
import mz.org.fgh.mentoring.dto.district.DistrictDTO;
import mz.org.fgh.mentoring.entity.healthfacility.HealthFacility;
import mz.org.fgh.mentoring.entity.location.District;
import mz.org.fgh.mentoring.util.LifeCycleStatus;
import mz.org.fgh.mentoring.util.Utilities;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthFacilityDTO extends BaseEntityDTO {

    private String healthFacility;
    private DistrictDTO districtDTO;

    // ✅ NOVOS CAMPOS
    private Long clinicalPartnerId;       // Parceiro Clínico
    private List<Long> otherPartnerIds;   // Outros parceiros (IDs)

    public HealthFacilityDTO(HealthFacility healthFacility) {
        super(healthFacility);
        this.setHealthFacility(healthFacility.getHealthFacility());
        if (healthFacility.getDistrict() != null && healthFacility.getDistrict().getId() != null) {
            this.setDistrictDTO(new DistrictDTO(healthFacility.getDistrict()));
        }

        if (healthFacility.getClinicalPartner() != null) {
            this.setClinicalPartnerId(healthFacility.getClinicalPartner().getId());
        }

        if (healthFacility.getOtherPartners() != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.setOtherPartnerIds(
                        mapper.readValue(
                                healthFacility.getOtherPartners(),
                                new TypeReference<List<Long>>() {}
                        )
                );
            } catch (Exception e) {
                throw new RuntimeException("Erro ao converter otherPartners JSON", e);
            }
        }
    }

    @JsonIgnore
    public HealthFacility toEntity() {
        HealthFacility entity = new HealthFacility();
        entity.setUuid(this.getUuid());
        entity.setId(this.getId());
        entity.setCreatedAt(this.getCreatedAt());
        entity.setUpdatedAt(this.getUpdatedAt());

        // ✅ CORREÇÃO: estava a faltar
        entity.setHealthFacility(this.getHealthFacility());

        if (Utilities.stringHasValue(this.getLifeCycleStatus())) {
            entity.setLifeCycleStatus(LifeCycleStatus.valueOf(this.getLifeCycleStatus()));
        }
        if (this.getDistrictDTO() != null) {
            entity.setDistrict(new District(this.getDistrictDTO()));
        }
        return entity;
    }
}