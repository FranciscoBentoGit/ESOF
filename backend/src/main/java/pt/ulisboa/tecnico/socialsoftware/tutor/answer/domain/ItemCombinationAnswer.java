package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import java.util.List;
import java.util.ArrayList;

import javax.persistence.*;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_OPTION_MISMATCH;


@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationAnswer extends AnswerDetails {
    @ManyToMany
    @JoinColumn(name = "right_answer_id")
    private List<IconRight> rightAnswer = new ArrayList<>();

    @ManyToMany
    @JoinColumn(name = "left_answer_id")
    private List<IconLeft> leftAnswer = new ArrayList<>();

    public ItemCombinationAnswer() {
        super();
    }

    public ItemCombinationAnswer(QuestionAnswer questionAnswer) {
        super(questionAnswer);
    }

    public ItemCombinationAnswer(QuestionAnswer questionAnswer, List<IconRight> rightAnswer, List<IconLeft> leftAnswer) {
        super(questionAnswer);
        this.rightAnswer = rightAnswer;
        this.leftAnswer = leftAnswer;
    }

    public List<IconRight> getRightAnswer() {
        return rightAnswer;
    }

    public List<IconLeft> getLeftAnswer() {
        return leftAnswer;
    }

    public void setLeftAnswer(IconLeft leftIcon) {
        this.leftAnswer.add(leftIcon);

        for (IconLeft il : leftAnswer) {
            if (il != null && il == leftIcon)
                il.addQuestionAnswer(this);
        }
    }

    public void setLeftAnswer(ItemCombinationQuestion itemQuestion, ItemCombinationStatementAnswerDetailsDto itemCombinationStatementAnswerDetailsDto) {
        if (itemCombinationStatementAnswerDetailsDto.getLeftIconIds() != null) {
            for (Integer i : itemCombinationStatementAnswerDetailsDto.getLeftIconIds()) {
                if (i != null) {
                    IconLeft iconLeft = itemQuestion.getLeftIcons().stream()
                            .filter(icon1 -> icon1.getId().equals(i))
                            .findAny()
                            .orElseThrow(() -> new TutorException(QUESTION_OPTION_MISMATCH, i));
                    this.setLeftAnswer(iconLeft);
                }
                else {
                    leftAnswer = null;
                }
            }
        }
    }

    public void setRightAnswer(IconRight rightIcon) {
        this.rightAnswer.add(rightIcon);

        for (IconRight ir : rightAnswer) {
            if(ir != null && ir == rightIcon)
                ir.addQuestionAnswer(this);
        }
    }

    public void setRightAnswer(ItemCombinationQuestion itemQuestion, ItemCombinationStatementAnswerDetailsDto itemCombinationStatementAnswerDetailsDto) {
        if (itemCombinationStatementAnswerDetailsDto.getRightIconIds() != null) {
            for (Integer i : itemCombinationStatementAnswerDetailsDto.getRightIconIds().keySet()) {
                if (i != null) {
                    IconRight iconRight = itemQuestion.getRightIcons().stream()
                            .filter(icon2 -> icon2.getId().equals(i))
                            .findAny()
                            .orElseThrow(() -> new TutorException(QUESTION_OPTION_MISMATCH, i));
                    this.setRightAnswer(iconRight);
                }
                else {
                    rightAnswer = null;
                }
            }
        }
    }

    public void remove() {
        if (rightAnswer != null) {
            for(IconRight ir : rightAnswer) {
                ir.getQuestionAnswers().remove(this);
            }
        }
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() {
        return new ItemCombinationAnswerDto(this);
    }

    @Override
    public boolean isAnswered() {
        return this.getRightAnswer() != null;
    }

    @Override
    public boolean isCorrect() {
        int x = 0;
        int y = 0;
        ItemCombinationQuestion question = (ItemCombinationQuestion) this.getQuestionAnswer().getQuestion().getQuestionDetails();
        for (IconRight ir1 : rightAnswer) {
            for (IconRight ir2 : question.getRightIcons()) {
                if (ir1.getId().equals(ir2.getId())) {
                    y = 0;
                    for (Integer match: ir1.getMatch()) {
                        if (ir2.getMatch().contains(match)) {
                            y++;
                        }
                    }
                    if (y == ir2.getMatch().size()) {
                        x++;
                    }
                }
            }
        }
        return x == question.getRightIcons().size();
    }

    @Override
    public String getAnswerRepresentation() {
        StringBuilder matchesRepresentation = new StringBuilder();
        for (IconRight ir: rightAnswer) {
            if (this.getRightAnswer() != null) {
                matchesRepresentation.append(ItemCombinationQuestion.convertSequenceToLetter(ir.getMatch()));
            }
            matchesRepresentation.append("\n");
        }
        return matchesRepresentation.toString();
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new ItemCombinationStatementAnswerDetailsDto(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnswerDetails(this);
    }
}