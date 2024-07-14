package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;

public class OpenAnswerCorrectAnswerDto extends CorrectAnswerDetailsDto{
    private String correctResponse;

    public OpenAnswerCorrectAnswerDto(OpenAnswerQuestion question) {
        this.correctResponse = question.getResponse();
    }

    public String getCorrectResponse() {
        return correctResponse;
    }

    public void setCorrectResponse(String correctResponse) {
        this.correctResponse = correctResponse;
    }

    @Override
    public String toString() {
        return "OpenAnswerCorrectAnswerDto{" +
                "correctResponse=" + correctResponse +
                '}';
    }
}