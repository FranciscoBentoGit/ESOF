package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.AnswerDetails;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswerItem;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemCombinationStatementAnswerDetailsDto extends StatementAnswerDetailsDto {
    private Map<Integer, List<Integer>> rightIconIds = new HashMap<>();
    private List<Integer> leftIconIds = new ArrayList<>();


    public ItemCombinationStatementAnswerDetailsDto() {
    }

    public ItemCombinationStatementAnswerDetailsDto(ItemCombinationAnswer questionAnswer) {
        for (IconRight ir: questionAnswer.getRightAnswer()) {
            if (ir != null) {
                rightIconIds.put(ir.getId(), ir.getMatch());
            }
        }
        for (IconLeft il: questionAnswer.getLeftAnswer()) {
            if (il != null) {
                leftIconIds.add(il.getId());
            }
        }
    }

    public List<Integer> getLeftIconIds() {
        return leftIconIds;
    }

    public void setLeftIconIds(List<Integer> leftIconsId) {
        this.leftIconIds = leftIconsId;
    }

    public void addLeftIconId(Integer leftIconId) {
        this.leftIconIds.add(leftIconId);
    }

    public Map<Integer, List<Integer>> getRightIconIds() {
        return rightIconIds;
    }

    public void addRightIconId(Integer rightIconId, List<Integer> match) {
        this.rightIconIds.put(rightIconId, match);
    }

    public void setRightIconIds(Map<Integer, List<Integer>> rightIconsId) {
        this.rightIconIds = rightIconsId;
    }

    private ItemCombinationAnswer createdItemCombinationAnswer;

    @Override
    public AnswerDetails getAnswerDetails(QuestionAnswer questionAnswer) {
        createdItemCombinationAnswer = new ItemCombinationAnswer(questionAnswer);
        questionAnswer.getQuestion().getQuestionDetails().update(this);
        return createdItemCombinationAnswer;
    }

    @Override
    public boolean emptyAnswer() {
        return rightIconIds == null;
    }

    @Override
    public QuestionAnswerItem getQuestionAnswerItem(String username, int quizId, StatementAnswerDto statementAnswerDto) {
        return new ItemCombinationAnswerItem(username, quizId, statementAnswerDto, this);
    }

    @Override
    public void update(ItemCombinationQuestion question) {
        createdItemCombinationAnswer.setRightAnswer(question, this);
        createdItemCombinationAnswer.setLeftAnswer(question, this);
    }

    @Override
    public String toString() {
        return "ItemCombinationStatementAnswerDto{" +
                "rightIconsId=" + rightIconIds +
                "leftIconsId=" + leftIconIds +
                '}';
    }
}