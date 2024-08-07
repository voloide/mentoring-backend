package mz.org.fgh.mentoring.entity.question;

import io.swagger.v3.oas.annotations.media.Schema;
import io.micronaut.core.annotation.Creator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import mz.org.fgh.mentoring.base.BaseEntity;
import mz.org.fgh.mentoring.dto.question.QuestionDTO;
import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.util.HashSet;
import java.util.Set;
@Schema(name = "Question", description = "A professional that provide mentoring to the tutored individuals")
@Entity(name = "Question")
@Table(name = "questions")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@AllArgsConstructor
public class Question  extends BaseEntity {


    @NotEmpty
    @Column(name = "CODE", nullable = false, length = 50)
    private String code;

    @NotEmpty
    @Column(name = "QUESTION", nullable = false)
    private String question;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "QUESTION_CATEGORY_ID", nullable = false)
    private QuestionCategory questionCategory;

    @Creator
    public Question(){}
    public Question(QuestionDTO questionDTO){
        super(questionDTO);
        this.setCode(questionDTO.getCode());
        this.setQuestion(questionDTO.getQuestion());
        if(questionDTO.getQuestionCategoryDTO()!=null) this.setQuestionCategory(new QuestionCategory(questionDTO.getQuestionCategoryDTO()));
    }

    @Override
    public String toString() {
        return "Question{" +
                "code='" + code + '\'' +
                ", question='" + question + '\'' +
                ", questionCategory=" + questionCategory +
                '}';
    }
}
