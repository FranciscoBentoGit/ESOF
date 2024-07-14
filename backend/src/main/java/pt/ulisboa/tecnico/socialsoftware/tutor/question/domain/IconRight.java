package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconRightDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.DomainEntity;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import java.util.List;
import java.util.ArrayList;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_CONTENT_FOR_OPTION;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.INVALID_SEQUENCE_FOR_OPTION;

@Entity
@Table(name = "icon_right")
public class IconRight implements DomainEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer sequence;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column
    private ArrayList<Integer> match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_details_id")
    private ItemCombinationQuestion questionDetails;

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "rightAnswer", fetch = FetchType.LAZY)
    private final Set<ItemCombinationAnswer> questionAnswers = new HashSet<>();

    public IconRight() {

    }

    public IconRight(IconRightDto icon) {
        setSequence(icon.getSequence());
        setContent(icon.getContent());
        setMatch(icon.getMatch());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitRightIcon(this);
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
        question.addRightIcon(this);
    }

    public ItemCombinationQuestion getQuestionDetails() { return questionDetails; }

    public void setMatch(ArrayList<Integer> match) {
        this.match = match;
    }

    public ArrayList<Integer> getMatch() {
        return match;
    }

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

    public Set<ItemCombinationAnswer> getQuestionAnswers() {
        return questionAnswers;
    }

    public void addQuestionAnswer(ItemCombinationAnswer questionAnswer) {
        questionAnswers.add(questionAnswer);
    }

}

