package mz.org.fgh.mentoring.controller.formSectionQuestion;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import mz.org.fgh.mentoring.api.RESTAPIMapping;
import mz.org.fgh.mentoring.base.BaseController;
import mz.org.fgh.mentoring.dto.form.FormSectionQuestionDTO;
import mz.org.fgh.mentoring.entity.formQuestion.FormSectionQuestion;
import mz.org.fgh.mentoring.entity.question.Question;
import mz.org.fgh.mentoring.error.MentoringAPIError;
import mz.org.fgh.mentoring.service.form.FormSectionQuestionService;
import mz.org.fgh.mentoring.service.tutor.TutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller(RESTAPIMapping.FORM_QUESTION_CONTROLLER)
@Tag(name = "FormSectionQuestion", description = "Operations related to FormSectionQuestion")
public class FormSectionQuestionController extends BaseController {

    @Inject
    FormSectionQuestionService formSectionQuestionService;
    @Inject
    TutorService tutorService;

    public FormSectionQuestionController() {}

    public static final Logger LOG = LoggerFactory.getLogger(FormSectionQuestionController.class);

    @Operation(summary = "Retrieve paginated FormQuestions", description = "Fetches paginated FormQuestions using the Pageable object.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved FormQuestions")
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Get
    public HttpResponse<Page<FormSectionQuestionDTO>> getAll(@Nullable Pageable pageable) {
        LOG.debug("Searching paginated FormQuestions");
        try {
            Page<FormSectionQuestion> formQuestions = formSectionQuestionService.findAll(pageable);
            Page<FormSectionQuestionDTO> formQuestionDTOs = formQuestions.map(FormSectionQuestionDTO::new); // Convert to DTO
            return HttpResponse.ok(formQuestionDTOs);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return HttpResponse.badRequest();
        }
    }


    @Operation(summary = "Find FormQuestion by ID", description = "Fetch a FormQuestion by its ID.")
    @ApiResponse(responseCode = "200", description = "FormQuestion found")
    @ApiResponse(responseCode = "404", description = "FormQuestion not found")
    @Get("/{id}")
    public Optional<FormSectionQuestion> findById(@PathVariable Long id) {
        return formSectionQuestionService.findById(id);
    }

    @Operation(summary = "Get paginated FormQuestions by Form ID", description = "Fetches paginated FormQuestions associated with a specific Form ID.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved FormQuestions")
    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Get("/getByFormId")
    public HttpResponse<Page<FormSectionQuestionDTO>> findFormQuestionByFormId(@NonNull @QueryValue("formId") Long formId, @Nullable Pageable pageable) {
        LOG.debug("Searching paginated FormQuestions for formId: {}", formId);
        try {
            Page<FormSectionQuestionDTO> formQuestionDTOs = formSectionQuestionService.fetchByForm(formId, pageable); // Convert entities to DTOs
            return HttpResponse.ok(formQuestionDTOs);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return HttpResponse.badRequest();
        }
    }


    @Operation(summary = "Create a new FormQuestion", description = "Create a new FormQuestion in the system.")
    @ApiResponse(responseCode = "201", description = "FormQuestion created successfully")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @Post(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    public FormSectionQuestion create(@Body FormSectionQuestion formSectionQuestion) {
        LOG.debug("Created FormQuestion {}", formSectionQuestion);
        return formSectionQuestionService.create(formSectionQuestion);
    }

    @Operation(summary = "Remove a FormQuestion", description = "Inactivate or remove a specific FormQuestion.")
    @ApiResponse(responseCode = "204", description = "FormQuestion removed successfully")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @Patch("/remove")
    public void remove(@NonNull @QueryValue("formId") Long formId, @Body FormSectionQuestionDTO formQuestionDTO, Authentication authentication) {
        LOG.debug("Removing FormQuestion with formId: {}", formId);
        formSectionQuestionService.inactivate((Long) authentication.getAttributes().get("userInfo"), formId, formQuestionDTO);
    }

    @Operation(summary = "Get FormQuestions by Forms UUIDs", description = "Retrieve all FormQuestions associated with specific Form UUIDs.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved FormQuestions")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @Get("/getByFormsUuids")
    public List<FormSectionQuestionDTO> findFormQuestionByFormsUuids(@NonNull @QueryValue("formsUuids") List<String> formsUuids, @QueryValue("offset") Long offset, @QueryValue("limit") Long limit) {
        return listAsDtos(formSectionQuestionService.fetchByFormsUuids(formsUuids, offset, limit), FormSectionQuestionDTO.class);
    }

    @Operation(summary = "Get FormQuestions by Forms UUIDs with pagination", description = "Retrieve paginated FormQuestions associated with specific Forms UUIDs.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved FormQuestions")
    @ApiResponse(responseCode = "400", description = "Bad request")
    @Get("/getByFormsUuidsAndPageAndSize")
    public List<FormSectionQuestionDTO> findFormQuestionByFormsUuidsAndPageAndSize(@NonNull @QueryValue("formsUuids") List<String> formsUuids, @QueryValue("page") Long page, @QueryValue("size") Long size) {
        return listAsDtos(formSectionQuestionService.fetchByFormsUuidsAndPageAndSize(formsUuids, page, size), FormSectionQuestionDTO.class);
    }

    @Operation(summary = "Delete a formSectionQuestion")
    @ApiResponse(content = @Content(mediaType = MediaType.APPLICATION_JSON))
    @Tag(name = "FormSectionQuestion")
    @Delete("/{id}")
    public HttpResponse<?> deleteFormSectionQuestion(@PathVariable("id") Long id) {
        try {
            Optional<FormSectionQuestion> formSectionQuestionServiceById = formSectionQuestionService.findById(id);
            if (formSectionQuestionServiceById.isPresent()) {
                formSectionQuestionService.destroy(formSectionQuestionServiceById.get());
                LOG.info("Deleted FormSectionQuestion with ID {}", id);
                return HttpResponse.ok();
            } else {
                return HttpResponse.notFound();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return HttpResponse.badRequest().body(
                    MentoringAPIError.builder()
                            .status(HttpStatus.BAD_REQUEST.getCode())
                            .error(e.getLocalizedMessage())
                            .message(e.getMessage())
                            .build()
            );
        }
    }


}
