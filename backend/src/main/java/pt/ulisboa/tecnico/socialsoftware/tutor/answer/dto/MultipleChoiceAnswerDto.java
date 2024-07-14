package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;

import java.util.ArrayList;
import java.util.List;

public class MultipleChoiceAnswerDto extends AnswerDetailsDto {
    private ArrayList<OptionDto> optionsList;

    public MultipleChoiceAnswerDto() {
    }

    public MultipleChoiceAnswerDto(MultipleChoiceAnswer answer) {
        List<Option> list = answer.getOptions();
        for (Option option : list) {
            if (option != null)
                this.optionsList.add(new OptionDto(option));
        }
    }

    public List<OptionDto> getOptions() {
        return optionsList;
    }

    public void setOption(OptionDto option) {
        this.optionsList.add(option);
    }
}
