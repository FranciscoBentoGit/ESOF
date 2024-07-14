package pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.QuestionDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue(Question.QuestionTypes.ITEM_COMBINATION_QUESTION)
public class ItemCombinationAnswerItem extends QuestionAnswerItem {

    @ElementCollection(targetClass = Integer.class)
    private List<Integer> rightIconsId;

    @ElementCollection(targetClass = Integer.class)
    private List<Integer> leftIconsId;


    public ItemCombinationAnswerItem() {
    }

    public ItemCombinationAnswerItem(String username, int quizId, StatementAnswerDto answer, ItemCombinationStatementAnswerDetailsDto detailsDto) {
        super(username, quizId, answer);
        this.rightIconsId = new ArrayList<>(detailsDto.getRightIconIds().keySet());
        this.leftIconsId = detailsDto.getLeftIconIds();
    }

    @Override
    public String getAnswerRepresentation(QuestionDetails questionDetails) {
        return this.getRightIconIds() != null ? questionDetails.getAnswerRepresentation(rightIconsId) : "-";
    }

    public List<Integer> getRightIconIds() {
        return rightIconsId;
    }

    public List<Integer> getLeftIconIds() {
        return leftIconsId;
    }

    public void setRightIconIds(List<Integer> iconsId) {
        this.rightIconsId = iconsId;
    }

    public void setLeftIconIds(List<Integer> iconsId) {
        this.leftIconsId = iconsId;
    }
}