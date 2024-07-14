package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(Question.QuestionTypes.OPEN_ANSWER_QUESTION)
public class OpenAnswerAnswer extends AnswerDetails {
    String studentResponse = "";

    public OpenAnswerAnswer() {
        super();
    }

    public OpenAnswerAnswer(QuestionAnswer questionAnswer){
        super(questionAnswer);
    }

    public OpenAnswerAnswer(QuestionAnswer questionAnswer, String response){
        super(questionAnswer);
        this.setResponse(response);
    }

    public String getResponse() {
        return studentResponse;
    }

    public void setResponse(String response) {
        this.studentResponse = response;
    }

    public void setResponse(OpenAnswerQuestion question, OpenAnswerStatementAnswerDetailsDto openAnswerStatementAnswerDetailsDto) {
        if(question.getResponse().equals(openAnswerStatementAnswerDetailsDto.getStudentResponse())) {
            this.studentResponse = question.getResponse();
        }

    }

    @Override
    public boolean isCorrect() {
        OpenAnswerQuestion question = (OpenAnswerQuestion) this.getQuestionAnswer().getQuestion().getQuestionDetails();
        return this.getResponse() != null && this.getResponse().equals(question.getResponse());
    }


    public void remove() {
        if (studentResponse != null) {
            studentResponse = null;
        }
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() {
        return new OpenAnswerAnswerDto(this);
    }

    @Override
    public boolean isAnswered() {
        return this.getResponse() != null;
    }

    @Override
    public String getAnswerRepresentation() {
        return this.getResponse();
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new OpenAnswerStatementAnswerDetailsDto(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnswerDetails(this);
    }
}