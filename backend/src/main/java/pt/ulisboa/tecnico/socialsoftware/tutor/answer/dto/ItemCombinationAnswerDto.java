package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconRightDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconLeftDto;


import java.util.List;
import java.util.ArrayList;

public class ItemCombinationAnswerDto extends AnswerDetailsDto {
    private List<IconRightDto> rightIconDtos = new ArrayList<>();
    private List<IconLeftDto> leftIconDtos = new ArrayList<>();

    public ItemCombinationAnswerDto() {
    }

    public ItemCombinationAnswerDto(ItemCombinationAnswer answer) {
        for (IconRight ir: answer.getRightAnswer()) {
            if (ir != null) {
                rightIconDtos.add(new IconRightDto(ir));
            }
        }
        for (IconLeft il: answer.getLeftAnswer()) {
            if (il != null) {
                leftIconDtos.add(new IconLeftDto(il));
            }
        }
    }

    public List<IconRightDto> getRightIconDtos() {
        return rightIconDtos;
    }

    public List<IconLeftDto> getLeftIconDtos() {
        return leftIconDtos;
    }

    public void setRightIconDtos(List<IconRightDto> rightIconDtos) {
        this.rightIconDtos = rightIconDtos;
    }

    public void setLeftIconDtos(List<IconLeftDto> leftIconDtos) {
        this.leftIconDtos = leftIconDtos;
    }
}