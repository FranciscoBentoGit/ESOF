package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.OpenAnswerStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;

@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ANSWER_QUESTION)
public class OpenAnswerAnswerItem extends QuestionAnswerItem{
    private String response;

    public OpenAnswerAnswerItem() {
    }

    public OpenAnswerAnswerItem(String username, int quizId, StatementAnswerDto answer, OpenAnswerStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.response = detailsDto.getStudentResponse();
    }

    @Override
    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        return this.getResponse();
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String newResponse) {
        this.response = newResponse;
    }
}