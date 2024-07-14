package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;

import java.util.List;
import java.util.stream.Collectors;

public class MultipleChoiceStatementQuestionDetailsDto extends StatementQuestionDetailsDto {
    private List<StatementOptionDto> options;
    private boolean isToOrder;

    public MultipleChoiceStatementQuestionDetailsDto(MultipleChoiceQuestion question) {
        this.options = question.getOptions().stream()
                .map(StatementOptionDto::new)
                .collect(Collectors.toList());
        this.isToOrder = question.getIsToOrder();
    }

    public List<StatementOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<StatementOptionDto> options) {
        this.options = options;
    }

    public boolean getIsToOrder() {
        return this.isToOrder;
    }

    public void setIsToOder(boolean isToOrder) {
        this.isToOrder = isToOrder;
    }

    @Override
    public String toString() {
        return "MultipleChoiceStatementQuestionDetailsDto{" +
                "options=" + options +
                '}';
    }
}