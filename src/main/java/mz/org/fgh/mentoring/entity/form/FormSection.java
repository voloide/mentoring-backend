package mz.org.fgh.mentoring.entity.form;

import io.micronaut.core.annotation.Creator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import mz.org.fgh.mentoring.base.BaseEntity;
import mz.org.fgh.mentoring.dto.form.FormSectionDTO;
import mz.org.fgh.mentoring.entity.question.Section;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity(name = "FormSection")
@Table(name = "form_section")
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@AllArgsConstructor
public class FormSection extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FORM_ID", nullable = false)
    private Form form;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SECTION_ID", nullable = false)
    private Section section;

    // Sequence or order of sections in a form, optional field
    @Column(name = "SEQUENCE")
    private Integer sequence;

    @Creator
    public FormSection() {}

    public FormSection(FormSectionDTO formSection) {
        super(formSection);
        this.section = new Section(formSection.getSection());
        this.sequence = formSection.getSequence();
    }

    @Override
    public String toString() {
        return "FormSection{" +
                "form=" + form.getId() +
                ", section=" + section.getDescription() +
                ", sequence=" + sequence +
                '}';
    }
}
