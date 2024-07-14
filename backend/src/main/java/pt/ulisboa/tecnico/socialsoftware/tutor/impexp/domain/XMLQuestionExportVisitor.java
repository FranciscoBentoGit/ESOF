package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*;

import java.util.List;

public class XMLQuestionExportVisitor implements Visitor {
    private Element rootElement;
    private Element currentElement;

    public String export(List<Question> questions) {
        createHeader();

        exportQuestions(questions);

        XMLOutputter xml = new XMLOutputter();
        xml.setFormat(Format.getPrettyFormat());

        return xml.outputString(this.rootElement);
    }

    public void createHeader() {
        Document jdomDoc = new Document();
        rootElement = new Element("questions");

        jdomDoc.setRootElement(rootElement);
        this.currentElement = rootElement;
    }

    private void exportQuestions(List<Question> questions) {
        for (Question question : questions) {
            question.accept(this);
        }
    }

    @Override
    public void visitQuestion(Question question) {
        Element questionElement = new Element("question");
        questionElement.setAttribute("courseType", question.getCourse().getType().name());
        questionElement.setAttribute("courseName", question.getCourse().getName());
        questionElement.setAttribute("key", String.valueOf(question.getKey()));
        questionElement.setAttribute("content", question.getContent());
        questionElement.setAttribute("title", question.getTitle());
        questionElement.setAttribute("status", question.getStatus().name());

        if (question.getCreationDate() != null)
            questionElement.setAttribute("creationDate", DateHandler.toISOString(question.getCreationDate()));
        this.currentElement.addContent(questionElement);

        this.currentElement = questionElement;

        if (question.getImage() != null)
            question.getImage().accept(this);

        question.getQuestionDetails().accept(this);

        this.currentElement = this.rootElement;
    }

    @Override
    public void visitQuestionDetails(MultipleChoiceQuestion question) {
        this.currentElement.setAttribute("type", Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION);
        this.currentElement.setAttribute("isToOrder", String.valueOf(question.getIsToOrder()));

        Element optionsElement = new Element("options");
        this.currentElement.addContent(optionsElement);

        this.currentElement = optionsElement;
        question.visitOptions(this);
    }

    @Override
    public void visitQuestionDetails(CodeFillInQuestion question) {
        this.currentElement.setAttribute("type", Question.QuestionTypes.CODE_FILL_IN_QUESTION);

        Element codeElement = new Element("code");
        codeElement.setAttribute("language", question.getLanguage().toString());
        codeElement.addContent(question.getCode());
        this.currentElement.addContent(codeElement);

        Element spotsElement = new Element("fillInSpots");
        this.currentElement.addContent(spotsElement);

        this.currentElement = spotsElement;
        question.visitFillInSpots(this);
    }

    @Override
    public void visitQuestionDetails(OpenAnswerQuestion question) {
        this.currentElement.setAttribute("type", Question.QuestionTypes.OPEN_ANSWER_QUESTION);
        this.currentElement.setAttribute("response", question.getResponse());

    }

    @Override
    public void visitFillInSpot(CodeFillInSpot spot) {
        Element spotElement = new Element("fillInSpot");

        spotElement.setAttribute("sequence", String.valueOf(spot.getSequence()));
        this.currentElement.addContent(spotElement);
        var oldElement = this.currentElement;
        this.currentElement = spotElement;

        spot.visitOptions(this);

        this.currentElement = oldElement;
    }

    @Override
    public void visitFillInOption(CodeFillInOption option) {
        Element optionElement = new Element("fillInOption");

        optionElement.setAttribute("sequence", String.valueOf(option.getSequence()));
        optionElement.setAttribute("content", option.getContent());
        optionElement.setAttribute("correct", String.valueOf(option.isCorrect()));

        this.currentElement.addContent(optionElement);
    }

    @Override
    public void visitImage(Image image) {
        Element imageElement = new Element("image");
        if (image.getWidth() != null) {
            imageElement.setAttribute("width", String.valueOf(image.getWidth()));
        }
        imageElement.setAttribute("url", image.getUrl());

        this.currentElement.addContent(imageElement);
    }

    @Override
    public void visitOption(Option option) {
        Element optionElement = new Element("option");

        optionElement.setAttribute("sequence", String.valueOf(option.getSequence()));
        optionElement.setAttribute("content", option.getContent());
        optionElement.setAttribute("correct", String.valueOf(option.isCorrect()));
        optionElement.setAttribute("order", String.valueOf(option.getOrder()));

        this.currentElement.addContent(optionElement);
    }

    @Override
    public void visitQuestionDetails(CodeOrderQuestion question) {
        this.currentElement.setAttribute("type", Question.QuestionTypes.CODE_ORDER_QUESTION);

        Element codeElement = new Element("orderSlots");
        codeElement.setAttribute("language", question.getLanguage().toString());
        this.currentElement.addContent(codeElement);

        this.currentElement = codeElement;
        question.visitCodeOrderSlots(this);
    }

    @Override
    public void visitCodeOrderSlot(CodeOrderSlot codeOrderSlot) {
        Element spotElement = new Element("slot");

        spotElement.setAttribute("order", String.valueOf(codeOrderSlot.getOrder()));
        spotElement.setAttribute("sequence", String.valueOf(codeOrderSlot.getSequence()));
        spotElement.addContent(codeOrderSlot.getContent());
        this.currentElement.addContent(spotElement);
    }

    @Override
    public void visitQuestionDetails(ItemCombinationQuestion question) {
        this.currentElement.setAttribute("type", Question.QuestionTypes.ITEM_COMBINATION_QUESTION);

        Element oldElement = this.currentElement;

        Element leftIconElements = new Element("leftIcons");
        this.currentElement.addContent(leftIconElements);

        this.currentElement = leftIconElements;
        question.visitLeftIcons(this);

        this.currentElement = oldElement;

        Element rightIconElements = new Element("rightIcons");
        this.currentElement.addContent(rightIconElements);

        this.currentElement = rightIconElements;
        question.visitRightIcons(this);

        this.currentElement = oldElement;

    }

    @Override
    public void visitLeftIcon(IconLeft icon) {
        Element leftIconElement = new Element("iconLeft");

        leftIconElement.setAttribute("sequence", String.valueOf(icon.getSequence()));
        leftIconElement.setAttribute("content", icon.getContent());

        this.currentElement.addContent(leftIconElement);
    }

    @Override
    public void visitRightIcon(IconRight icon) {
        Element rightIconElement = new Element("iconRight");

        rightIconElement.setAttribute("sequence", String.valueOf(icon.getSequence()));
        rightIconElement.setAttribute("content", icon.getContent());
        rightIconElement.setAttribute("match", String.valueOf(icon.getMatch()));

        this.currentElement.addContent(rightIconElement);
    }
}
