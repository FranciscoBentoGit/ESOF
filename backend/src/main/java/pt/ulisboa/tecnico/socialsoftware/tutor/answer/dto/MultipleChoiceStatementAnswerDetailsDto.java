package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.AnswerDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;

import java.util.HashMap;
import java.util.Map;

public class MultipleChoiceStatementAnswerDetailsDto extends StatementAnswerDetailsDto {
    @JsonProperty("optionsIdsHash")
    private Map<Integer, Integer> optionsIdsHash = new HashMap<>();

    public MultipleChoiceStatementAnswerDetailsDto() {
    }

    public MultipleChoiceStatementAnswerDetailsDto(MultipleChoiceAnswer questionAnswer) {
        for (Option opt : questionAnswer.getOptions()) {
            if (opt != null)
                optionsIdsHash.put(opt.getId(), opt.getOrder());
        }
    }

    public Map<Integer,Integer> getOptionsIds() {
        return optionsIdsHash;
    }

    public void setOptionId(Integer optionId,Integer optionOrder) {
        this.optionsIdsHash.put(optionId,optionOrder);
    }

    private MultipleChoiceAnswer createdMultipleChoiceAnswer;

    @Override
    public AnswerDetails getAnswerDetails(QuestionAnswer questionAnswer) {
        createdMultipleChoiceAnswer = new MultipleChoiceAnswer(questionAnswer);
        questionAnswer.getQuestion().getQuestionDetails().update(this);
        return createdMultipleChoiceAnswer;
    }

    @Override
    public boolean emptyAnswer() {
        return optionsIdsHash.isEmpty();
    }

    @Override
    public QuestionAnswerItem getQuestionAnswerItem(String username, int quizId, StatementAnswerDto statementAnswerDto) {
        return new MultipleChoiceAnswerItem(username, quizId, statementAnswerDto, this);
    }

    @Override
    public void update(MultipleChoiceQuestion question) {
        createdMultipleChoiceAnswer.setOption(question, this);
    }

    @Override
    public String toString() {
        return "MultipleChoiceStatementAnswerDto{" +
                "optionsIdsHash=" + optionsIdsHash +
                '}';
    }
}
