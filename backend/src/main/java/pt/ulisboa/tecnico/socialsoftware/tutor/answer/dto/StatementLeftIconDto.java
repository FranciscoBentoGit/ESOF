package pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft;


import java.io.Serializable;


public class StatementLeftIconDto implements Serializable {
    private Integer leftIconId;
    private String content;

    public StatementLeftIconDto(IconLeft iconLeft) {
        this.leftIconId = iconLeft.getId();
        this.content = iconLeft.getContent();
    }

    public Integer getLeftIconId() {
        return leftIconId;
    }

    public void setLeftIconId(Integer rightIconId) {
        this.leftIconId = leftIconId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "StatementRightIconDto{" +
                "rightIconId=" + leftIconId +
                ", content='" + content + '\'' +
                '}';
    }
}
