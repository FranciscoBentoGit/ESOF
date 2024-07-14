package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import java.util.HashMap;
import java.util.Map;

public class MultipleChoiceCorrectAnswerDto extends CorrectAnswerDetailsDto {
    @JsonProperty("correctOptions")
    private Map<Integer, Integer> correctOptions = new HashMap<>();

    public MultipleChoiceCorrectAnswerDto(MultipleChoiceQuestion question) {
        for (Option opt : question.getOptions()) {
            if (opt != null && opt.isCorrect())
                correctOptions.put(opt.getId(), opt.getOrder());
        }
    }

    public Map<Integer, Integer> getCorrectOptionId() {
        return correctOptions;
    }

    public void setCorrectOptionId(Integer optionId, Integer optionOrder) {
        this.correctOptions.put(optionId,optionOrder);
    }

    @Override
    public String toString() {
        return "MultipleChoiceCorrectAnswerDto{" +
                "correctOptions" + correctOptions +
                '}';
    }
}