package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.swing.*;
import java.util.List;

import java.util.stream.Collectors;

public class ItemCombinationQuestionDto extends QuestionDetailsDto {


    private List<IconLeftDto> leftIcons;
    private List<IconRightDto> rightIcons;

    public ItemCombinationQuestionDto() {

    }

    public ItemCombinationQuestionDto(ItemCombinationQuestion question) {
        this.leftIcons = question.getLeftIcons().stream().map(IconLeftDto::new).collect(Collectors.toList());
        this.rightIcons = question.getRightIcons().stream().map(IconRightDto::new).collect(Collectors.toList());
    }

    public List<IconLeftDto> getLeftIcons() {
        return leftIcons;
    }

    public List<IconRightDto> getRightIcons() {
        return rightIcons;
    }

    public void setLeftIcons(List<IconLeftDto> leftIcons) {
        this.leftIcons = leftIcons;
    }

    public void setRightIcons(List<IconRightDto> rightIcons) {
        this.rightIcons = rightIcons;
    }

    @Override
    public void update(ItemCombinationQuestion question) {
        question.update(this);
    }

    @Override
    public QuestionDetails getQuestionDetails(Question question) {

        return new ItemCombinationQuestion(question, this);
    }
}
