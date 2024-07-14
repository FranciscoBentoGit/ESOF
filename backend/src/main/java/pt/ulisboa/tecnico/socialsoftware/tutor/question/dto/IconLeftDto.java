package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft;

import java.io.Serializable;

public class IconLeftDto implements Serializable {

    private Integer id;
    private Integer sequence;
    private String content;

    public IconLeftDto() {
    }

    public IconLeftDto(IconLeft icon) {
        this.id = icon.getId();
        this.sequence = icon.getSequence();
        this.content = icon.getContent();
    }

    public Integer getId() {
        return id;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "IconLeftDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", sequence='" + sequence + '\'' +
                '}';
    }
}
