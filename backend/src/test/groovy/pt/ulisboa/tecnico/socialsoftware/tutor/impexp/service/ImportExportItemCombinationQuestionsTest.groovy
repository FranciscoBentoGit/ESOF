package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconLeftDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconRightDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ImportExportItemCombinationQuestionsTest extends SpockTest {
    def questionId
    def teacher

    def "export and import questions to xml"() {
        given: "an item combination question"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "icons"
        def iconDtoLeft1 = new IconLeftDto()
        iconDtoLeft1.setContent(ICON_1_CONTENT)
        iconDtoLeft1.setSequence(0)
        def iconDtoLeft2 = new IconLeftDto()
        iconDtoLeft2.setContent(ICON_2_CONTENT)
        iconDtoLeft2.setSequence(1)
        def iconDtoRight1 = new IconRightDto()
        iconDtoRight1.setContent(ICON_1_CONTENT)
        iconDtoRight1.setSequence(0)
        def iconDtoRight2 = new IconRightDto()
        iconDtoRight2.setContent(ICON_2_CONTENT)
        iconDtoRight2.setSequence(1)

        List<Integer> matches1 = new ArrayList<>()
        List<Integer> matches2 = new ArrayList<>()

        matches1.add(iconDtoLeft1.getSequence())
        matches2.add(iconDtoLeft2.getSequence())

        iconDtoRight1.setMatch(matches1)
        iconDtoRight2.setMatch(matches2)

        List<IconLeftDto> leftIcons = new ArrayList<>()
        List<IconRightDto> rightIcons = new ArrayList<>()

        leftIcons.add(iconDtoLeft1)
        leftIcons.add(iconDtoLeft2)
        rightIcons.add(iconDtoRight1)
        rightIcons.add(iconDtoRight2)

        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)
        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)


        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()

        and: "a xml with questions"
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
        and: "a clean database"
        questionService.removeQuestion(questionId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(externalCourse.getId()).size() == 1
        def questionResult = questionService.findQuestions(externalCourse.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        questionResult.getQuestionDetailsDto().getLeftIcons().size() == 2
        questionResult.getQuestionDetailsDto().getRightIcons().size() == 2
        def iconOneResult = questionResult.getQuestionDetailsDto().getLeftIcons().get(0)
        def iconTwoResult = questionResult.getQuestionDetailsDto().getLeftIcons().get(1)
        def iconThreeResult = questionResult.getQuestionDetailsDto().getRightIcons().get(0)
        def iconFourResult = questionResult.getQuestionDetailsDto().getRightIcons().get(1)
        iconOneResult.getSequence() + iconTwoResult.getSequence() +
                iconThreeResult.getSequence() + iconFourResult.getSequence() == 2
        iconOneResult.getContent() == ICON_1_CONTENT
        iconTwoResult.getContent() == ICON_2_CONTENT
        iconThreeResult.getContent() == ICON_1_CONTENT
        iconFourResult.getContent() == ICON_2_CONTENT
        iconThreeResult.getMatch().get(0) == iconDtoLeft1.getSequence()
        iconFourResult.getMatch().get(0) == iconDtoLeft2.getSequence()
    }

    def "export an item combination question to latex"() {
        given: "an item combination question"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "icons"
        def iconDtoLeft1 = new IconLeftDto()
        iconDtoLeft1.setContent(ICON_1_CONTENT)
        iconDtoLeft1.setSequence(0)
        def iconDtoLeft2 = new IconLeftDto()
        iconDtoLeft2.setContent(ICON_2_CONTENT)
        iconDtoLeft2.setSequence(1)
        def iconDtoRight1 = new IconRightDto()
        iconDtoRight1.setContent(ICON_1_CONTENT)
        iconDtoRight1.setSequence(0)
        def iconDtoRight2 = new IconRightDto()
        iconDtoRight2.setContent(ICON_2_CONTENT)
        iconDtoRight2.setSequence(1)

        List<Integer> matches1 = new ArrayList<>()
        List<Integer> matches2 = new ArrayList<>()

        matches1.add(iconDtoLeft1.getSequence())
        matches2.add(iconDtoLeft2.getSequence())

        iconDtoRight1.setMatch(matches1)
        iconDtoRight2.setMatch(matches2)

        List<IconLeftDto> leftIcons = new ArrayList<>()
        List<IconRightDto> rightIcons = new ArrayList<>()

        leftIcons.add(iconDtoLeft1)
        leftIcons.add(iconDtoLeft2)
        rightIcons.add(iconDtoRight1)
        rightIcons.add(iconDtoRight2)

        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)
        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)

        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}

