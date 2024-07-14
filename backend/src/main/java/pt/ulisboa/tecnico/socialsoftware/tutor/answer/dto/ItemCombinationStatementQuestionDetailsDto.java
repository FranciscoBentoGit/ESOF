package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;

import java.util.List;
import java.util.stream.Collectors;

public class ItemCombinationStatementQuestionDetailsDto extends StatementQuestionDetailsDto {
    private List<StatementRightIconDto> rightIcons;
    private List<StatementLeftIconDto> leftIcons;

    public ItemCombinationStatementQuestionDetailsDto(ItemCombinationQuestion question) {
        this.rightIcons = question.getRightIcons().stream()
                .map(StatementRightIconDto::new)
                .collect(Collectors.toList());
        this.leftIcons = question.getLeftIcons().stream()
                .map(StatementLeftIconDto::new)
                .collect(Collectors.toList());
    }

    public List<StatementRightIconDto> getRightIcons() {
        return rightIcons;
    }

    public List<StatementLeftIconDto> getLeftIcons() {
        return leftIcons;
    }

    public void setRightIcons(List<StatementRightIconDto> rightIcons) {
        this.rightIcons = rightIcons;
    }

    public void setLeftIcons(List<StatementLeftIconDto> leftIcons) {
        this.leftIcons = leftIcons;
    }

    @Override
    public String toString() {
        return "ItemCombinationStatementQuestionDetailsDto{" +
                "rightIcons=" + rightIcons +
                "leftIcons=" + leftIcons +
                '}';
    }
}