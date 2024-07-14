package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenAnswerAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.AnswerDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.OpenAnswerAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswerItem;




public class OpenAnswerStatementAnswerDetailsDto extends StatementAnswerDetailsDto{
    private String studentResponse;

    public OpenAnswerStatementAnswerDetailsDto() {

    }

    public OpenAnswerStatementAnswerDetailsDto(OpenAnswerAnswer questionAnswer) {
        if(questionAnswer.getResponse() != null) {
            this.studentResponse = questionAnswer.getResponse();
        }
    }

    public String getStudentResponse() {
        return studentResponse;
    }

    public void setStudentResponse(String response) {
        this.studentResponse = response;
    }

    private OpenAnswerAnswer createdOpenAnswerAnswer;

    @Override
    public AnswerDetails getAnswerDetails(QuestionAnswer questionAnswer) {
        createdOpenAnswerAnswer = new OpenAnswerAnswer(questionAnswer);
        questionAnswer.getQuestion().getQuestionDetails().update(this);
        return createdOpenAnswerAnswer;
    }

    @Override
    public boolean emptyAnswer() {
        return studentResponse == null;
    }

    @Override
    public QuestionAnswerItem getQuestionAnswerItem(String username, int quizId, StatementAnswerDto statementAnswerDto) {
        return new OpenAnswerAnswerItem(username, quizId, statementAnswerDto, this);
    }

    @Override
    public void update(OpenAnswerQuestion question) {
        createdOpenAnswerAnswer.setResponse(question, this);
    }

    @Override
    public String toString() {
        return "OpenAnswerStatementAnswerDto{" +
                "response=" + studentResponse +
                '}';
    }

}