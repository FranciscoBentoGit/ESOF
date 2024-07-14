package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight;

import java.util.ArrayList;
import java.io.Serializable;


public class StatementRightIconDto implements Serializable {
    private Integer rightIconId;
    private String content;
    private ArrayList<Integer> match;

    public StatementRightIconDto(IconRight iconRight) {
        this.rightIconId = iconRight.getId();
        this.content = iconRight.getContent();
        this.match = iconRight.getMatch();
    }

    public Integer getRightIconId() {
        return rightIconId;
    }

    public void setRightIconId(Integer rightIconId) {
        this.rightIconId = rightIconId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<Integer> getMatch() {
        return match;
    }

    public void setMatch(ArrayList<Integer> match) {
        this.match = match;
    }

    @Override
    public String toString() {
        return "StatementRightIconDto{" +
                "rightIconId=" + rightIconId +
                ", content='" + content + '\'' +
                ", content='" + match +
                '}';
    }
}
