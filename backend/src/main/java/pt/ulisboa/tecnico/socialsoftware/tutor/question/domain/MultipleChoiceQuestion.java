package pt.ulisboa.tecnico.socialsoftware.tutor.question.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.Visitor;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.Updator;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDetailsDto;

import java.util.Collections;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceQuestion extends QuestionDetails {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "questionDetails", fetch = FetchType.EAGER, orphanRemoval = true)
    private final List<Option> options = new ArrayList<>();

    //To distinguish if all options need to be ordered or not
    private boolean isToOrder = false;

    public MultipleChoiceQuestion() {
        super();
    }

    public MultipleChoiceQuestion(Question question, MultipleChoiceQuestionDto questionDto) {
        super(question);
        setIsToOrder(questionDto.getIsToOrder());
        setOptions(questionDto.getOptions());
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<OptionDto> options) {

        checkIfNonOrderedQuestionHasAtLeastOneCorrectOption(options);

        checkIfOrderedQuestionHasAtLeastTwoCorrectOptions(options);

        checkIfOrderedQuestionHasNoCorrectOptions(options);

        checkIfOrderedQuestionHasAtLeastTwoOptions(options);

        if (getIsToOrder()) {
            if (!checkIfAnyOptionHasNoOrderAssigned(options)) {
                throw new TutorException(INVALID_ORDER_FOR_CORRECT_OPTION);
            }
            if (!validOrderForOptions(options)) {
                throw new TutorException(WRONG_ORDER_FOR_OPTIONS);
            }
        }

        int index = 0;
        for (OptionDto optionDto : options) {
            if (optionDto.getId() == null) {
                optionDto.setSequence(index++);
                new Option(optionDto).setQuestionDetails(this);
            } else {
                Option option = getOptions()
                        .stream()
                        .filter(op -> op.getId().equals(optionDto.getId()))
                        .findAny()
                        .orElseThrow(() -> new TutorException(OPTION_NOT_FOUND, optionDto.getId()));

                option.setContent(optionDto.getContent());
                option.setCorrect(optionDto.isCorrect());

                setOptionOrder(optionDto, option);
            }
        }


    }

    public List<Option> getCorrectOptionsList() {
        List<Option> correctOptions = new ArrayList<>();
        for (Option opt : options){
            if(opt.isCorrect()){
                correctOptions.add(opt);
            }
        }
        return correctOptions;
    }

    private boolean checkIfAnyOptionHasNoOrderAssigned(List<OptionDto> options) {
        for (OptionDto optionDto : options){
            if(optionDto.isCorrect() && (optionDto.getOrder() == null)){
                return false;
            }
        }
        return true;
    }

    private boolean validOrderForOptions(List<OptionDto> options) {
        List<Integer> orders = new ArrayList<>();
        for (OptionDto optionDto : options) {
            if(optionDto.isCorrect()){
                orders.add(optionDto.getOrder());
            }
        }
        Collections.sort(orders);
        for (int i = 0; i<orders.size()-1 ;i++) {
            if(orders.get(i) != (orders.get(i+1)-1)){
                return false;
            }
        }
        return true;
    }

    private void setOptionOrder(OptionDto optionDto, Option option) {
        if (getIsToOrder() && option.isCorrect()){
            option.setOrder(optionDto.getOrder());
        } else {
            option.setOrder(null);
        }
    }

    private void checkIfOrderedQuestionHasAtLeastTwoOptions(List<OptionDto> options) {
        if (getIsToOrder() && (options.size() < 2)) {
            throw new TutorException(AT_LEAST_TWO_OPTION_NEEDED);
        }
    }

    private void checkIfOrderedQuestionHasNoCorrectOptions(List<OptionDto> options) {
        if (getIsToOrder() && (options.stream().filter(OptionDto::isCorrect).count() == 0)) {
            throw new TutorException(NO_CORRECT_OPTION);
        }
    }

    private void checkIfOrderedQuestionHasAtLeastTwoCorrectOptions(List<OptionDto> options) {
        if (getIsToOrder() && (options.stream().filter(OptionDto::isCorrect).count() == 1) && (options.size() >= 2)) {
            throw new TutorException(AT_LEAST_TWO_CORRECT_OPTION_NEEDED);
        }
    }

    private void checkIfNonOrderedQuestionHasAtLeastOneCorrectOption(List<OptionDto> options) {
        if (!getIsToOrder() && options.stream().filter(OptionDto::isCorrect).count() < 1) {
            throw new TutorException(ONE_CORRECT_OPTION_NEEDED);
        }
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public boolean getIsToOrder() {
        return isToOrder;
    }

    public void setIsToOrder(boolean isToOrder) {
        this.isToOrder = isToOrder;
    }

    public void update(MultipleChoiceQuestionDto questionDetails) {
        setIsToOrder(questionDetails.getIsToOrder());
        setOptions(questionDetails.getOptions());
    }

    @Override
    public void update(Updator updator) {
        updator.update(this);
    }

    @Override
    public String getCorrectAnswerRepresentation() {
        return convertSequenceToLetter(this.getCorrectAnswer());
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitQuestionDetails(this);
    }

    public void visitOptions(Visitor visitor) {
        for (Option option : this.getOptions()) {
            option.accept(visitor);
        }
    }

    @Override
    public CorrectAnswerDetailsDto getCorrectAnswerDetailsDto() {
        return new MultipleChoiceCorrectAnswerDto(this);
    }

    @Override
    public StatementQuestionDetailsDto getStatementQuestionDetailsDto() {
        return new MultipleChoiceStatementQuestionDetailsDto(this);
    }

    @Override
    public StatementAnswerDetailsDto getEmptyStatementAnswerDetailsDto() {
        return new MultipleChoiceStatementAnswerDetailsDto();
    }

    @Override
    public AnswerDetailsDto getEmptyAnswerDetailsDto() {
        return new MultipleChoiceAnswerDto();
    }

    @Override
    public QuestionDetailsDto getQuestionDetailsDto() {
        return new MultipleChoiceQuestionDto(this);
    }

    public Integer getCorrectAnswer() {
        return this.getOptions()
                .stream()
                .filter(Option::isCorrect)
                .findAny().orElseThrow(() -> new TutorException(NO_CORRECT_OPTION))
                .getSequence();
    }

    @Override
    public void delete() {
        super.delete();
        for (Option option : this.options) {
            option.remove();
        }
        this.options.clear();
    }

    @Override
    public String toString() {
        return "MultipleChoiceQuestion{" +
                "options=" + options +
                '}';
    }

    public static String convertSequenceToLetter(Integer correctAnswer) {
        return correctAnswer != null ? Character.toString('A' + correctAnswer) : "-";
    }

    @Override
    public String getAnswerRepresentation(List<Integer> selectedIds) {
        var result = this.options
                .stream()
                .filter(x -> selectedIds.contains(x.getId()))
                .map(x -> convertSequenceToLetter(x.getSequence()))
                .collect(Collectors.joining("|"));
        return !result.isEmpty() ? result : "-";
    }
}