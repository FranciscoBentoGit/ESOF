package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;

public class OpenAnswerStatementQuestionDetailsDto extends StatementQuestionDetailsDto {
    private String response;

    public OpenAnswerStatementQuestionDetailsDto(OpenAnswerQuestion question) {
        this.response = question.getResponse();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String newResponse) {
        response = newResponse;
    }

    @Override
    public String toString() {
        return "OpenAnswerStatementQuestionDetailsDto{" +
                "response=" + response +
                '}';
    }
}