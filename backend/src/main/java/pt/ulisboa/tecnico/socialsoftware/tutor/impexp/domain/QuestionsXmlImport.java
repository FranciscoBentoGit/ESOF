package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Languages;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*;

public class QuestionsXmlImport {
    private QuestionService questionService;
    private CourseRepository courseRepository;

    public void importQuestions(InputStream inputStream, QuestionService questionService, CourseRepository courseRepository) {
        this.questionService = questionService;
        this.courseRepository = courseRepository;

        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);

        Document doc;
        try {
            Reader reader = new InputStreamReader(inputStream, Charset.defaultCharset());
            doc = builder.build(reader);
        } catch (FileNotFoundException e) {
            throw new TutorException(QUESTIONS_IMPORT_ERROR, "File not found");
        } catch (JDOMException e) {
            throw new TutorException(QUESTIONS_IMPORT_ERROR, "Coding problem");
        } catch (IOException e) {
            throw new TutorException(QUESTIONS_IMPORT_ERROR, "File type or format");
        }

        if (doc == null) {
            throw new TutorException(QUESTIONS_IMPORT_ERROR, "File not found ot format error");
        }

        importQuestions(doc);
    }

    public void importQuestions(String questionsXml, QuestionService questionService, CourseRepository courseRepository) {
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringElementContentWhitespace(true);

        InputStream stream = new ByteArrayInputStream(questionsXml.getBytes());

        importQuestions(stream, questionService, courseRepository);
    }

    private void importQuestions(Document doc) {
        XPathFactory xpfac = XPathFactory.instance();
        XPathExpression<Element> xp = xpfac.compile("//questions/question", Filters.element());
        for (Element element : xp.evaluate(doc)) {
            importQuestion(element);
        }
    }

    private void importQuestion(Element questionElement) {
        String courseType = questionElement.getAttributeValue("courseType");
        String courseName = questionElement.getAttributeValue("courseName");
        Integer key = Integer.valueOf(questionElement.getAttributeValue("key"));
        String content = questionElement.getAttributeValue("content");
        String title = questionElement.getAttributeValue("title");
        String status = questionElement.getAttributeValue("status");
        String creationDate = questionElement.getAttributeValue("creationDate");
        String type = questionElement.getAttributeValue("type");

        QuestionDto questionDto = new QuestionDto();
        questionDto.setKey(key);
        questionDto.setContent(content);
        questionDto.setTitle(title);
        questionDto.setStatus(status);
        questionDto.setCreationDate(creationDate);

        Element imageElement = questionElement.getChild("image");
        if (imageElement != null) {
            Integer width = imageElement.getAttributeValue("width") != null ?
                    Integer.valueOf(imageElement.getAttributeValue("width")) : null;
            String url = imageElement.getAttributeValue("url");

            ImageDto imageDto = new ImageDto();
            imageDto.setWidth(width);
            imageDto.setUrl(url);

            questionDto.setImage(imageDto);
        }

        QuestionDetailsDto questionDetailsDto;
        switch (type) {
            case Question.QuestionTypes.MULTIPLE_CHOICE_QUESTION:
                questionDetailsDto = importMultipleChoiceQuestion(questionElement);
                break;
            case Question.QuestionTypes.CODE_FILL_IN_QUESTION:
                questionDetailsDto = importCodeFillInQuestion(questionElement);
                break;
            case Question.QuestionTypes.CODE_ORDER_QUESTION:
                questionDetailsDto = importCodeOrderQuestion(questionElement);
                break;
            case Question.QuestionTypes.ITEM_COMBINATION_QUESTION:
                questionDetailsDto = importItemCombinationQuestion(questionElement);
                break;
            case Question.QuestionTypes.OPEN_ANSWER_QUESTION:
                questionDetailsDto = importOpenAnswerQuestion(questionElement);
                break;
            default:
                throw new TutorException(QUESTION_TYPE_NOT_IMPLEMENTED, type);
        }

        questionDto.setQuestionDetailsDto(questionDetailsDto);

        Course course = courseRepository.findByNameType(courseName, courseType).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseName));
        questionService.createQuestion(course.getId(), questionDto);
    }

    private QuestionDetailsDto importMultipleChoiceQuestion(Element questionElement) {
        List<OptionDto> optionDtos = new ArrayList<>();
        boolean isToOrder = Boolean.parseBoolean(questionElement.getAttributeValue("isToOrder"));
        for (Element optionElement : questionElement.getChild("options").getChildren("option")) {
            Integer optionSequence = Integer.valueOf(optionElement.getAttributeValue("sequence"));
            String optionContent = optionElement.getAttributeValue("content");
            boolean correct = Boolean.parseBoolean(optionElement.getAttributeValue("correct"));

            OptionDto optionDto = new OptionDto();
            optionDto.setSequence(optionSequence);
            optionDto.setContent(optionContent);
            optionDto.setCorrect(correct);

            if (isToOrder) {
                Integer optionOrder = Integer.valueOf(optionElement.getAttributeValue("order"));
                optionDto.setOrder(optionOrder);
            }

            optionDtos.add(optionDto);
        }
        MultipleChoiceQuestionDto multipleChoiceQuestionDto = new MultipleChoiceQuestionDto();
        multipleChoiceQuestionDto.setOptions(optionDtos);
        multipleChoiceQuestionDto.setIsToOrder(isToOrder);
        return multipleChoiceQuestionDto;
    }

    private QuestionDetailsDto importCodeFillInQuestion(Element questionElement) {
        CodeFillInQuestionDto questionDto = new CodeFillInQuestionDto();
        questionDto.setCode(questionElement.getChildText("code"));
        questionDto.setLanguage(Languages.valueOf(questionElement.getChild("code").getAttributeValue("language")));
        var spots = new ArrayList<CodeFillInSpotDto>();
        for (Element spotElement : questionElement.getChild("fillInSpots").getChildren("fillInSpot")) {
            var spot = new CodeFillInSpotDto();
            spot.setSequence(Integer.valueOf(spotElement.getAttributeValue("sequence")));
            var options = new ArrayList<OptionDto>();
            for (Element optionElement : spotElement.getChildren("fillInOption")) {
                Integer optionSequence = Integer.valueOf(optionElement.getAttributeValue("sequence"));
                String optionContent = optionElement.getAttributeValue("content");
                boolean correct = Boolean.parseBoolean(optionElement.getAttributeValue("correct"));

                OptionDto optionDto = new OptionDto();
                optionDto.setSequence(optionSequence);
                optionDto.setContent(optionContent);
                optionDto.setCorrect(correct);

                options.add(optionDto);
            }

            spot.setOptions(options);
            spots.add(spot);
        }
        questionDto.setFillInSpots(spots);
        return questionDto;
    }

    private QuestionDetailsDto importCodeOrderQuestion(Element questionElement) {
        CodeOrderQuestionDto questionDto = new CodeOrderQuestionDto();
        questionDto.setLanguage(Languages.valueOf(questionElement.getChild("orderSlots").getAttributeValue("language")));
        var slots = new ArrayList<CodeOrderSlotDto>();
        for (Element slotElement : questionElement.getChild("orderSlots").getChildren("slot")) {
            var slot = new CodeOrderSlotDto();
            slot.setOrder(Integer.valueOf(slotElement.getAttributeValue("order")));
            slot.setSequence(Integer.valueOf(slotElement.getAttributeValue("sequence")));
            slot.setContent(slotElement.getValue());

            slots.add(slot);
        }
        questionDto.setCodeOrderSlots(slots);
        return questionDto;
    }


    private QuestionDetailsDto importOpenAnswerQuestion(Element questionElement) {
        OpenAnswerQuestionDto questionDto = new OpenAnswerQuestionDto();
        questionDto.setResponse(String.valueOf(questionElement.getAttributeValue("response")));

        return questionDto;
    }



    private QuestionDetailsDto importItemCombinationQuestion(Element questionElement) {
        List<IconLeftDto> leftIconsDto = new ArrayList<>();
        List<IconRightDto> rightIconsDto = new ArrayList<>();
        for (Element leftIconElement : questionElement.getChild("leftIcons").getChildren("iconLeft")) {
            Integer leftIconSequence = Integer.valueOf(leftIconElement.getAttributeValue("sequence"));
            String leftIconContent = leftIconElement.getAttributeValue("content");

            IconLeftDto iconLeftDto = new IconLeftDto();
            iconLeftDto.setSequence(leftIconSequence);
            iconLeftDto.setContent(leftIconContent);

            leftIconsDto.add(iconLeftDto);
        }
        for (Element rightIconElement : questionElement.getChild("rightIcons").getChildren("iconRight")) {
            Integer rightIconSequence = Integer.valueOf(rightIconElement.getAttributeValue("sequence"));
            String rightIconContent = rightIconElement.getAttributeValue("content");
            String rightIconMatchString = rightIconElement.getAttributeValue("match").replaceAll("]", "");
            String[] rightIconMatchSplit =  rightIconMatchString.substring(1).split(",");
            ArrayList<Integer> rightIconMatch = new ArrayList<>();

            for(String str: rightIconMatchSplit)  {
                rightIconMatch.add(Integer.valueOf(str));
            }

            IconRightDto iconRightDto = new IconRightDto();
            iconRightDto.setSequence(rightIconSequence);
            iconRightDto.setContent(rightIconContent);
            iconRightDto.setMatch(rightIconMatch);

            rightIconsDto.add(iconRightDto);
        }
        ItemCombinationQuestionDto itemCombinationQuestionDto = new ItemCombinationQuestionDto();
        itemCombinationQuestionDto.setLeftIcons(leftIconsDto);
        itemCombinationQuestionDto.setRightIcons(rightIconsDto);
        return itemCombinationQuestionDto;
    }

}
