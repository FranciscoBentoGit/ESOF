package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;


import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.MultipleChoiceStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Entity
@DiscriminatorValue(Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION)
public class MultipleChoiceAnswerItem extends QuestionAnswerItem {

    private HashMap<Integer, Integer> optionsIdsHash = new HashMap<>();

    public MultipleChoiceAnswerItem() {
    }

    public MultipleChoiceAnswerItem(String username, int quizId, StatementAnswerDto answer, MultipleChoiceStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.optionsIdsHash = (HashMap<Integer, Integer>) detailsDto.getOptionsIds();
    }

    @Override

    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        StringBuilder rep= new StringBuilder();
        if(!this.getOptionsIds().isEmpty()){
            for(Map.Entry<Integer, Integer> opt : this.getOptionsIds().entrySet()){
                rep.append(questionDetails.getAnswerRepresentation(Arrays.asList(opt.getKey())));
            }
            return rep.toString();
        }
        return "-";
    }

    public Map<Integer,Integer> getOptionsIds() {
        return optionsIdsHash;
    }

    public void setOptionId(Integer optionId,Integer optionOrder) {
        this.optionsIdsHash.put(optionId,optionOrder);
    }
}
