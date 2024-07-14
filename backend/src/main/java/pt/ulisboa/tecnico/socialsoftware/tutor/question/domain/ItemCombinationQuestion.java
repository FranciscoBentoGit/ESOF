package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;


@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationQuestion extends QuestionDetails {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<IconLeft> leftIcons = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.LAZY, orphanRemoval = true)
    private final List<IconRight> rightIcons = new ArrayList<>();

    public ItemCombinationQuestion() {

        super();
    }

    public ItemCombinationQuestion(Question question, ItemCombinationQuestionDto questionDto) {

        super(question);
        setLeftIcons(questionDto.getLeftIcons());
        setRightIcons(questionDto.getRightIcons());
    }

    public void setLeftIcons(List<IconLeftDto> icons) {
        int index = 0;
        for (IconLeftDto icon: icons) {
            if (icon.getId() == null) {
                icon.setSequence(index++);
                new IconLeft(icon).setQuestionDetails(this);
            } else {
                IconLeft iconLeft = getLeftIcons()
                        .stream()
                        .filter(op -> op.getId().equals(icon.getId()))
                        .findAny()
                        .orElseThrow(() -> new TutorException(OPTION_NOT_FOUND, icon.getId()));
                iconLeft.setContent(icon.getContent());
            }
        }
    }

    public void setRightIcons(List<IconRightDto> icons) {
        int index = 0;
        for (IconRightDto icon: icons) {
            if (icon.getId() == null) {
                icon.setSequence(index++);
                new IconRight(icon).setQuestionDetails(this);
            } else {
                IconRight iconRight = getRightIcons()
                        .stream()
                        .filter(op -> op.getId().equals(icon.getId()))
                        .findAny()
                        .orElseThrow(() -> new TutorException(OPTION_NOT_FOUND, icon.getId()));
                iconRight.setContent(icon.getContent());
                iconRight.setMatch(icon.getMatch());
            }
        }
    }

    public void addRightIcon(IconRight icon) { this.rightIcons.add(icon); }

    public void addLeftIcon(IconLeft icon) { this.leftIcons.add(icon); }

    public List<IconLeft> getLeftIcons() {
        return leftIcons;
    }

    public List<IconRight> getRightIcons() {
        return rightIcons;
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new ItemCombinationCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new ItemCombinationStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new ItemCombinationStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new ItemCombinationAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new ItemCombinationQuestionDto(this);
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return null;
    }

    public static String convertSequenceToLetter(List<Integer> correctAnswer) {
        StringBuilder sequence = new StringBuilder();
        for (Integer idx: correctAnswer) {
            if (idx != null) {
                sequence.append(Character.toString('A' + idx));
            }
        }
        return sequence.toString();
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        StringBuilder matchesRepresentation = new StringBuilder();
        for (IconRight ir: rightIcons) {
            if (this.getRightIcons() != null) {
                matchesRepresentation.append(ItemCombinationQuestion.convertSequenceToLetter(ir.getMatch()));
            }
            matchesRepresentation.append("\n");
        }
        return matchesRepresentation.toString();
    }

    @Override
    public void delete() {
        super.delete();
        for (var rightIcon : this.rightIcons) {
            rightIcon.delete();
        }
        this.rightIcons.clear();
        for (var leftIcon : this.leftIcons) {
            leftIcon.delete();
        }
        this.leftIcons.clear();
    }

    public void update(ItemCombinationQuestionDto questionDetails) {
        setLeftIcons(questionDetails.getLeftIcons());
        setRightIcons(questionDetails.getRightIcons());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitLeftIcons(Visitor visitor) {
        for (IconLeft icon : this.getLeftIcons()) {
            icon.accept(visitor);
        }
    }

    public void visitRightIcons(Visitor visitor) {
        for (IconRight icon : this.getRightIcons()) {
            icon.accept(visitor);
        }
    }

}
