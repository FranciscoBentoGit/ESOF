package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;


public class OpenAnswerQuestionDto extends QuestionDetailsDto {

    private String response;

    public OpenAnswerQuestionDto() {
    }

    public OpenAnswerQuestionDto(OpenAnswerQuestion question) {
        this.response = question.getResponse();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }


    @Override
    public QuestionDetails getQuestionDetails(Question question) {
        return new OpenAnswerQuestion(question, this);
    }

    public void update(OpenAnswerQuestion question) {
        question.update(this);
    }

    @Override
    public String toString() {
        return "OpenAnswerQuestionDto{" +
                "response=" + response +
                '}';
    }

}