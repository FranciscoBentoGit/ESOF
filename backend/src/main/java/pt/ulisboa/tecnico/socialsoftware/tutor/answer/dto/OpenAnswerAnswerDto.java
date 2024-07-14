package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import org.aspectj.apache.bcel.classfile.Module;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenAnswerAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

public class OpenAnswerAnswerDto extends AnswerDetailsDto {
    private String response = "";

    public OpenAnswerAnswerDto() {
    }

    public OpenAnswerAnswerDto(OpenAnswerAnswer answer) {
        if (answer.getResponse() != null)
            this.response = answer.getResponse();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String newResponse) {
        this.response = newResponse;
    }
}