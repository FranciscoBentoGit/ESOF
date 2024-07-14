package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto;

import javax.persistence.*;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ANSWER_QUESTION)
public class OpenAnswerQuestion extends QuestionDetails {

    // @Column(columnDefinition = "TEXT", nullable = false)
    private String response;

    public OpenAnswerQuestion() {
        super();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new OpenAnswerQuestionDto(this);
    }


    public OpenAnswerQuestion(Question question, OpenAnswerQuestionDto openAnswerQuestionDto) {
        super(question);
        setResponse(openAnswerQuestionDto.getResponse());
    }

    public String getResponse() {
        return this.response;
    }

    public void setResponse(String newResponse) {
        if (newResponse == null || newResponse.length() == 0){
            throw new TutorException(INVALID_RESPONSE_FOR_QUESTION);
        } else {
            this.response = newResponse;
        }

    }

    public void update(OpenAnswerQuestionDto questionDetails) {
        setResponse(questionDetails.getResponse());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }


    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }


    @Override
    public void delete() {
        super.delete();
        this.response = null;
    }


    @Override
    public String getCorrectAnswerRepresentation() {
        return this.getResponse();
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        return null;
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new OpenAnswerCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new OpenAnswerStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new OpenAnswerStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new OpenAnswerAnswerDto();
    }


}