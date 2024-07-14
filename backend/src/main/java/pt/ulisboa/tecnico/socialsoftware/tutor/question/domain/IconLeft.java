package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconLeftDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import javax.persistence.*;

import java.util.HashSet;
import java.util.Set;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_CONTENT_FOR_OPTION;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_SEQUENCE_FOR_OPTION;

@Entity
@Table(name = "icon_left")
public class IconLeft implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer sequence;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_details_id")
    private ItemCombinationQuestion questionDetails;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "rightAnswer", fetch = FetchType.LAZY)
    private final Set<ItemCombinationAnswer> questionAnswers = new HashSet<>();

    public IconLeft() {

    }

    public IconLeft(IconLeftDto icon) {
        setSequence(icon.getSequence());
        setContent(icon.getContent());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public String getContent() {
        return content;
    }

    public void setQuestionDetails(ItemCombinationQuestion question) {
        this.questionDetails = question;
        question.addLeftIcon(this);
    }

    public ItemCombinationQuestion getQuestionDetails() { return questionDetails; }

    public void setSequence(Integer sequence) {
        if (sequence == null || sequence < 0)
            throw new TutorException(INVALID_SEQUENCE_FOR_OPTION);

        this.sequence = sequence;
    }

    public void setContent(String content) {
        if (content == null || content.isBlank())
            throw new TutorException(INVALID_CONTENT_FOR_OPTION);

        this.content = content;
    }

    public void delete() {
        this.questionDetails = null;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitLeftIcon(this);
    }

    public Set<ItemCombinationAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void addQuestionAnswer(ItemCombinationAnswer questionAnswer) {
        questionAnswers.add(questionAnswer);
    }
}
