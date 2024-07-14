package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUESTION_OPTION_MISMATCH;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceAnswer extends AnswerDetails {
    @ManyToMany
    @JoinColumn(name = "option_id")
    //@ElementCollection
    private List<Option> optionsList = new ArrayList<>();

    public MultipleChoiceAnswer() {
        super();
    }

    public MultipleChoiceAnswer(QuestionAnswer questionAnswer){
        super(questionAnswer);
    }

    public MultipleChoiceAnswer(QuestionAnswer questionAnswer, List<Option> options){
        super(questionAnswer);
        this.optionsList = options;
    }

    public List<Option> getOptions() {
        return optionsList;
    }

    public void setOption(Option option) {
        this.optionsList.add(option);

        for (Option opt : optionsList) {
            if (opt != null && opt == option)
                opt.addQuestionAnswer(this);
        }

    }

    public void setOption(MultipleChoiceQuestion question, MultipleChoiceStatementAnswerDetailsDto multipleChoiceStatementAnswerDetailsDto) {
        if (!multipleChoiceStatementAnswerDetailsDto.getOptionsIds().isEmpty()) {
            for (Map.Entry<Integer, Integer> pair : multipleChoiceStatementAnswerDetailsDto.getOptionsIds().entrySet()) {
                if (pair.getKey() != null) {
                    Option option = question.getOptions().stream()
                            .filter(option1 -> option1.getId().equals(pair.getKey()))
                            .findAny()
                            .orElseThrow(() -> new TutorException(QUESTION_OPTION_MISMATCH, pair.getKey()));
                    this.setOption(option);
                }
                else {
                    this.setOption(null);
                }
            }
        }
    }

    @Override
    public boolean isCorrect() {
        MultipleChoiceQuestion question = (MultipleChoiceQuestion) this.getQuestionAnswer().getQuestion().getQuestionDetails();
        if (question.getIsToOrder()) {
            if(question.getCorrectOptionsList().size() == optionsList.size()){
                return !checkIfOptionIsCorrectWithOrder(question);
            }
        } else {
            if (question.getCorrectOptionsList().size() == optionsList.size()) {
                return checkIfOptionIsCorrectWithNoOrder(question);
            }
        }
        return false;
    }

    private boolean checkIfOptionIsCorrectWithNoOrder(MultipleChoiceQuestion question) {
        for (Option opt : question.getCorrectOptionsList()) {
            if(!optionsList.contains(opt)){
                return false;
            }
        }
        return true;
    }

    private boolean checkIfOptionIsCorrectWithOrder(MultipleChoiceQuestion question) {
        for(Option opt : question.getCorrectOptionsList()){
            if(!optionsList.contains(opt)){
                return true;
            }
            int index =optionsList.lastIndexOf(opt);
            if (!optionsList.get(index).getId().equals(opt.getOrder())) {
                return true;
            }
        }
        return false;
    }


    public void remove() {
        if (!optionsList.isEmpty()) {
           optionsList.clear();
        }
    }

    @Override
    public AnswerDetailsDto getAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto(this);
    }

    @Override
    public boolean isAnswered() {
        return !optionsList.isEmpty();
    }

    @Override
    public String getAnswerRepresentation() {
        StringBuilder rep= new StringBuilder();
        if(!optionsList.isEmpty()){
            for(Option opt : optionsList){
                rep.append(MultipleChoiceQuestion.convertSequenceToLetter(opt.getSequence()));
            }
            return rep.toString();
        }
        return "-";
    }

    @Override
    public StatementAnswerDetailsDto getStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto(this);
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitAnswerDetails(this);
    }
}
