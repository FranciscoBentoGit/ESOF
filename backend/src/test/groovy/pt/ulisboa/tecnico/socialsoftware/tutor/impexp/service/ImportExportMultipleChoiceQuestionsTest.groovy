package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ImageDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ImportExportMultipleChoiceQuestionsTest extends SpockTest {
    def questionId
    def teacher

    def setup() {
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)

        def optionDto = new OptionDto()
        optionDto.setSequence(0)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setSequence(1)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        questionId = questionService.createQuestion(externalCourse.getId(), questionDto).getId()
    }

    def 'export and import multiple order question to xml'() {
        given: "a question order"
        def questionOrderDto = new QuestionDto()
        questionOrderDto.setTitle(QUESTION_2_TITLE)
        questionOrderDto.setContent(QUESTION_2_CONTENT)
        questionOrderDto.setStatus(Question.Status.AVAILABLE.name())
        questionOrderDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionOrderDto.getQuestionDetailsDto().setIsToOrder(true)

        and: "options"
        def optionDto = new OptionDto()
        optionDto.setSequence(0)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setSequence(1)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)

        options.add(optionDto)

        questionOrderDto.getQuestionDetailsDto().setOptions(options)

        def questionOrderId = questionService.createQuestion(externalCourse.getId(), questionOrderDto).getId()

        and: 'a xml with questions'
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
        and: 'a clean database'
        questionService.removeQuestion(questionId)
        questionService.removeQuestion(questionOrderId)

        when:
        questionService.importQuestionsFromXml(questionsXml)

        then:
        questionRepository.findQuestions(externalCourse.getId()).size() == 2
        def questionResult = questionService.findQuestions(externalCourse.getId()).get(0)
        questionResult.getKey() == null
        questionResult.getTitle() == QUESTION_1_TITLE
        questionResult.getContent() == QUESTION_1_CONTENT
        questionResult.getStatus() == Question.Status.AVAILABLE.name()
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getOptions().size() == 2
        def optionOneResult = questionResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getOptions().get(1)
        optionOneResult.getSequence() + optionTwoResult.getSequence() == 1
        optionOneResult.getContent() == OPTION_1_CONTENT
        optionTwoResult.getContent() == OPTION_1_CONTENT
        !(optionOneResult.isCorrect() && optionTwoResult.isCorrect())
        optionOneResult.isCorrect() || optionTwoResult.isCorrect()

        def questionOrderResult = questionService.findQuestions(externalCourse.getId()).get(1)
        questionOrderResult.getKey() == null
        questionOrderResult.getTitle() == QUESTION_2_TITLE
        questionOrderResult.getContent() == QUESTION_2_CONTENT
        questionOrderResult.getStatus() == Question.Status.AVAILABLE.name()
        questionOrderResult.getQuestionDetailsDto().getOptions().size() == 2
        def optionOneOrderResult = questionOrderResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoOrderResult = questionOrderResult.getQuestionDetailsDto().getOptions().get(1)
        optionOneOrderResult.getSequence() + optionTwoOrderResult.getSequence() == 1
        optionOneOrderResult.getContent() == OPTION_1_CONTENT
        optionTwoOrderResult.getContent() == OPTION_2_CONTENT
        optionOneOrderResult.getOrder() == 1
        optionTwoOrderResult.getOrder() == 2

        (optionOneOrderResult.isCorrect() && optionTwoOrderResult.isCorrect())

    }

    def 'export a multiple order question to latex'() {
        given: "a question order"
        def questionOrderDto = new QuestionDto()
        questionOrderDto.setTitle(QUESTION_2_TITLE)
        questionOrderDto.setContent(QUESTION_2_CONTENT)
        questionOrderDto.setStatus(Question.Status.AVAILABLE.name())
        questionOrderDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionOrderDto.getQuestionDetailsDto().setIsToOrder(true)

        and: "options"
        def optionDto = new OptionDto()
        optionDto.setSequence(0)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setSequence(1)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)

        options.add(optionDto)

        questionOrderDto.getQuestionDetailsDto().setOptions(options)

        def questionOrderId = questionService.createQuestion(externalCourse.getId(), questionOrderDto).getId()

        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
    }

    def 'export and import questions to xml'() {
        given: 'a xml with questions'
        def questionsXml = questionService.exportQuestionsToXml()
        print questionsXml
        and: 'a clean database'
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
        def imageResult = questionResult.getImage()
        imageResult.getWidth() == 20
        imageResult.getUrl() == IMAGE_1_URL
        questionResult.getQuestionDetailsDto().getOptions().size() == 2
        def optionOneResult = questionResult.getQuestionDetailsDto().getOptions().get(0)
        def optionTwoResult = questionResult.getQuestionDetailsDto().getOptions().get(1)
        optionOneResult.getSequence() + optionTwoResult.getSequence() == 1
        optionOneResult.getContent() == OPTION_1_CONTENT
        optionTwoResult.getContent() == OPTION_1_CONTENT
        !(optionOneResult.isCorrect() && optionTwoResult.isCorrect())
        optionOneResult.isCorrect() || optionTwoResult.isCorrect()
    }

    def 'export to latex'() {
        when:
        def questionsLatex = questionService.exportQuestionsToLatex()

        then:
        questionsLatex != null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
