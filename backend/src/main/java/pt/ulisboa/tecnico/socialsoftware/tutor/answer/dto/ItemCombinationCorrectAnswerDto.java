package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ItemCombinationCorrectAnswerDto extends CorrectAnswerDetailsDto {
    private Map<Integer, List<Integer>> rightIconIds = new HashMap<>();
    private List<Integer> leftIconIds = new ArrayList<>();

    public ItemCombinationCorrectAnswerDto(ItemCombinationQuestion question) {
        for(IconRight ir : question.getRightIcons()) {
            this.rightIconIds.put(ir.getId(), ir.getMatch());
        }
        for(IconLeft il : question.getLeftIcons()) {
            this.leftIconIds.add(il.getId());
        }
    }

    public Map<Integer, List<Integer>> getRightIconIds() {
        return rightIconIds;
    }

    public List<Integer> getLeftIconIds() {
        return leftIconIds;
    }

    public void setRightIconIds(Map<Integer, List<Integer>> rightIconIds) {
        this.rightIconIds = rightIconIds;
    }

    public void setLeftIconIds(List<Integer> leftIconIds) {
        this.leftIconIds = leftIconIds;
    }

    @Override
    public String toString() {
        return "ItemCombinationCorrectAnswerDto{" +
                "rightIconIds=" + rightIconIds +
                "leftIconIds=" + leftIconIds +
                '}';
    }
}