package pt.ulisboa.tecnico.socialsoftware.tutor.question.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight;

import java.io.Serializable;
import java.util.ArrayList;

public class IconRightDto implements Serializable {

    private Integer id;
    private Integer sequence;
    private String content;
    private ArrayList<Integer> match;

    public IconRightDto() {
    }

    public IconRightDto(IconRight icon) {
        this.id = icon.getId();
        this.sequence = icon.getSequence();
        this.content = icon.getContent();
        this.match = icon.getMatch();
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

    public void setMatch(ArrayList<Integer> match) { this.match = match;}

    public ArrayList<Integer> getMatch() { return match;}

    @Override
    public String toString() {
        return "IconRightDto{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", sequence='" + sequence + '\'' +
                ", match='" + match + '\'' +
                '}';
    }
}

