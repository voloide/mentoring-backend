package mz.org.fgh.mentoring.controller.healthfacility;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import mz.org.fgh.mentoring.api.PaginatedResponse;
import mz.org.fgh.mentoring.api.RESTAPIMapping;
import mz.org.fgh.mentoring.api.SuccessResponse;
import mz.org.fgh.mentoring.base.BaseController;
import mz.org.fgh.mentoring.dto.LifeCycleStatusDTO;
import mz.org.fgh.mentoring.dto.healthFacility.HealthFacilityDTO;
import mz.org.fgh.mentoring.entity.healthfacility.HealthFacility;
import mz.org.fgh.mentoring.service.healthfacility.HealthFacilityService;
import mz.org.fgh.mentoring.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(RESTAPIMapping.HEALTH_FACILITY)
public class HealthFacilityController extends BaseController {

    public static final Logger LOG = LoggerFactory.getLogger(HealthFacilityController.class);

    private final HealthFacilityService healthFacilityService;

    public HealthFacilityController(HealthFacilityService healthFacilityService) {
        this.healthFacilityService = healthFacilityService;
    }

    @Operation(summary = "Return a list off all HealthFacilities of specified District")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Tag(name = "HealthFacility")
    @Get("getAllOfDistrict/{districtId}")
    public List<HealthFacilityDTO> getAllOfDistrict(@PathVariable("districtId") Long districtId) {
        return this.healthFacilityService.findAllOfDistrict(districtId);
    }

    @Operation(summary = "Return a list off all HealthFacilities")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Tag(name = "HealthFacilities")
    @Get("/getall")
    public List<HealthFacilityDTO> getAll(@Nullable @QueryValue("limit") Long limit ,
                                          @Nullable @QueryValue("offset") Long offset) {
        return this.healthFacilityService.getAll(limit, offset);
    }

    @Operation(summary = "Return a list off all HealthFacilities")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Tag(name = "HealthFacilities")
    @Get("/getByDistricts")
    public List<HealthFacilityDTO> getByDistricts(@QueryValue("uuids") List<String> uuids) {
        List<HealthFacility> healthFacilities =  this.healthFacilityService.getByDistricts(uuids);
        if (Utilities.listHasElements((ArrayList<?>) healthFacilities)) {
            try {
                return Utilities.parseList(healthFacilities, HealthFacilityDTO.class);
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    @Operation(summary = "Return a list off all HealthFacilities")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Tag(name = "HealthFacilities")
    @Get("getAllOfMentor/{uuid}")
    public List<HealthFacilityDTO> getAllOfMentor(@NonNull @PathVariable("uuid") String uuid,
                                                  @Nullable @QueryValue("limit") Long limit ,
                                                  @Nullable @QueryValue("offset") Long offset) {
        return this.healthFacilityService.getAllOfMentor(uuid, limit, offset);
    }

    @Operation(summary = "Return a list off all HealthFacilities of specified Province")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Tag(name = "HealthFacility")
    @Get("getAllOfProvince/{provinceId}")
    public List<HealthFacilityDTO> getAllOfProvince(@PathVariable("provinceId") Long provinceId) {
        return this.healthFacilityService.findAllOfProvince(provinceId);
    }

    @Operation(summary = "Get HealthFacility from database")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Tag(name = "HealthFacility")
    @Get("/{id}")
    public HealthFacilityDTO findHealthFacilityById(@PathVariable("id") Long id){
        return new HealthFacilityDTO( this.healthFacilityService.findById(id));
    }

    @Operation(summary = "Return a list off all HealthFacilities")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Secured(SecurityRule.IS_ANONYMOUS)
    @Tag(name = "HealthFacilities")
    @Get("/getByPageAndSize")
    public Page<HealthFacilityDTO> getByPageAndSize(Pageable pageable) {
        return this.healthFacilityService.getByPageAndSize(pageable);
    }


    @Operation(summary = "List or search health facilities (paginated)")
    @Get
    public HttpResponse<?> listOrSearch(@Nullable @QueryValue("name") String name,
                                        @Nullable Pageable pageable) {

        Page<HealthFacility> data = !Utilities.stringHasValue(name)
                ? healthFacilityService.findAll(resolvePageable(pageable))
                : healthFacilityService.searchByName(name, resolvePageable(pageable));

        List<HealthFacilityDTO> dataDTOs = data.getContent().stream()
                .map(HealthFacilityDTO::new)
                .collect(Collectors.toList());

        String message = data.getTotalSize() == 0
                ? "Sem Dados para esta pesquisa"
                : "Dados encontrados";

        return HttpResponse.ok(
                PaginatedResponse.of(
                        dataDTOs,
                        data.getTotalSize(),
                        data.getPageable(),
                        message
                )
        );
    }

    @Operation(summary = "Create a new HealthFacility")
    @Post
    public HttpResponse<?> create(@Body HealthFacilityDTO dto, Authentication authentication) {
        String userUuid = (String) authentication.getAttributes().get("useruuid");
        HealthFacility created = healthFacilityService.create(dto, userUuid);
        return HttpResponse.created(SuccessResponse.of("Unidade sanitária criada com sucesso", new HealthFacilityDTO(created)));
    }


    @Operation(summary = "Update an existing HealthFacility")
    @Put
    public HttpResponse<?> update(@Body HealthFacilityDTO dto, Authentication authentication) {
        String userUuid = (String) authentication.getAttributes().get("useruuid");
        HealthFacility updated = healthFacilityService.update(dto, userUuid);
        return HttpResponse.ok(SuccessResponse.of("Unidade sanitária atualizada com sucesso", new HealthFacilityDTO(updated)));
    }

    @Operation(summary = "Activate or deactivate a HealthFacility by changing its LifeCycleStatus")
    @Put("/{uuid}/status")
    public HttpResponse<?> updateLifeCycleStatus(@PathVariable String uuid, @Body LifeCycleStatusDTO dto, Authentication authentication) {
        String userUuid = (String) authentication.getAttributes().get("useruuid");
        HealthFacility updated = healthFacilityService.updateLifeCycleStatus(uuid, dto.getLifeCycleStatus(), userUuid);
        return HttpResponse.ok(SuccessResponse.of("Estado da unidade sanitária atualizado com sucesso", new HealthFacilityDTO(updated)));
    }

    @Operation(summary = "Delete a HealthFacility by UUID")
    @Delete("/{uuid}")
    public HttpResponse<?> delete(@PathVariable String uuid) {
        healthFacilityService.delete(uuid);
        return HttpResponse.ok(SuccessResponse.messageOnly("Unidade sanitária eliminada com sucesso"));
    }

}
