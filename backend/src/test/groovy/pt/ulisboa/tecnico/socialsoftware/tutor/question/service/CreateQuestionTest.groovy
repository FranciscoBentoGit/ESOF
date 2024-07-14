package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CodeFillInQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.CodeOrderQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.*
import spock.lang.Unroll

import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage

@DataJpaTest
class CreateQuestionTest extends SpockTest {

    @Unroll("invalid arguments: #title | #content | #response || #errorMessage")
    def "invalid input values for open answer question" () {
        given: "a openAnswerQuestionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(title)
        questionDto.setContent(content)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setResponse(response)

        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        title             |content                  |response   ||errorMessage
        ''                |QUESTION_1_CONTENT       |RESPONSE_1 ||ErrorMessage.INVALID_TITLE_FOR_QUESTION
        null              |QUESTION_1_CONTENT       |RESPONSE_1 ||ErrorMessage.INVALID_TITLE_FOR_QUESTION
        QUESTION_1_TITLE  |''                       |RESPONSE_1 ||ErrorMessage.INVALID_CONTENT_FOR_QUESTION
        QUESTION_1_TITLE  |null                     |RESPONSE_1 ||ErrorMessage.INVALID_CONTENT_FOR_QUESTION
        QUESTION_1_TITLE  |QUESTION_1_CONTENT       |''         ||ErrorMessage.INVALID_RESPONSE_FOR_QUESTION
        QUESTION_1_TITLE  |QUESTION_1_CONTENT       |null       ||ErrorMessage.INVALID_RESPONSE_FOR_QUESTION
    }

    def "create an open question"(){
        given: "a openAnswerQuestionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)


        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetails().getResponse() == RESPONSE_1
        result.getImage() == null
        result.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(result)

    }

    def "create item combination question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        def iconLeft1 = new IconLeftDto()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        def iconLeft2 = new IconLeftDto()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        def iconLeft3 = new IconLeftDto()
        iconLeft3.setContent(ICON_3_CONTENT)
        iconLeft3.setSequence(2)
        def iconRight1 = new IconRightDto()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        def iconRight2 = new IconRightDto()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)
        def iconRight3 = new IconRightDto()
        iconRight3.setContent(ICON_3_CONTENT)
        iconRight3.setSequence(2)

        List<Integer> matches1 = new ArrayList<>()
        List<Integer> matches2 = new ArrayList<>()
        List<Integer> matches3 = new ArrayList<>()

        matches1.add(iconLeft1.getSequence())
        matches2.add(iconLeft1.getSequence())
        matches3.add(iconLeft3.getSequence())

        iconRight1.setMatch(matches1)
        iconRight2.setMatch(matches2)
        iconRight3.setMatch(matches3)

        List<IconLeftDto> leftIcons = new ArrayList<>()
        List<IconRightDto> rightIcons = new ArrayList<>()

        leftIcons.add(iconLeft1)
        leftIcons.add(iconLeft2)
        leftIcons.add(iconLeft3)
        rightIcons.add(iconRight1)
        rightIcons.add(iconRight2)
        rightIcons.add(iconRight3)

        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)
        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)

        when: "a question is created"

        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the question is in the repository"

        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
    }

    def "cannot create an item combination question with no title"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        def iconLeft1 = new IconLeftDto()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        def iconLeft2 = new IconLeftDto()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        def iconRight1 = new IconRightDto()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        def iconRight2 = new IconRightDto()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)

        List<Integer> matches1 = new ArrayList<>()
        List<Integer> matches2 = new ArrayList<>()

        matches1.add(iconLeft1.getSequence())
        matches2.add(iconLeft2.getSequence())

        iconRight1.setMatch(matches1)
        iconRight2.setMatch(matches2)

        List<IconLeftDto> leftIcons = new ArrayList<>()
        List<IconRightDto> rightIcons = new ArrayList<>()

        leftIcons.add(iconLeft1)
        leftIcons.add(iconLeft2)
        rightIcons.add(iconRight1)
        rightIcons.add(iconRight2)

        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)
        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_TITLE_FOR_QUESTION
    }

    def "cannot create an item combination question with no content"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        def iconLeft1 = new IconLeftDto()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        def iconLeft2 = new IconLeftDto()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        def iconRight1 = new IconRightDto()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        def iconRight2 = new IconRightDto()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)

        List<Integer> matches1 = new ArrayList<>()
        List<Integer> matches2 = new ArrayList<>()

        matches1.add(iconLeft1.getSequence())
        matches2.add(iconLeft2.getSequence())

        iconRight1.setMatch(matches1)
        iconRight2.setMatch(matches2)

        List<IconLeftDto> leftIcons = new ArrayList<>()
        List<IconRightDto> rightIcons = new ArrayList<>()

        leftIcons.add(iconLeft1)
        leftIcons.add(iconLeft2)
        rightIcons.add(iconRight1)
        rightIcons.add(iconRight2)

        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)
        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_CONTENT_FOR_QUESTION
    }

    def "cannot create a multiple option order question with less than 2 options"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(true)

        and: "a optionId"
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_TWO_OPTION_NEEDED

    }

    @Unroll("invalid arguments: #title | #content | #isToOrder | #contentOption1 | #contentOption2 | #isCorrectOption1 | #isCorrectOption2 | #setOrderOption1 | #setOrderOption2 || errorMessage ")
    def "invalid input values for multiple option order question"(){
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(title)
        questionDto.setContent(content)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(isToOrder)

        and: "two optionsIds"
        def optionOneDto = new OptionDto()
        optionOneDto.setContent(contentOption1)
        optionOneDto.setCorrect(isCorrectOption1)
        optionOneDto.setOrder(setOrderOption1)

        def optionTwoDto = new OptionDto()
        optionTwoDto.setContent(contentOption2)
        optionTwoDto.setCorrect(isCorrectOption2)
        optionTwoDto.setOrder(setOrderOption2)

        def options = new ArrayList<OptionDto>()
        options.add(optionOneDto)
        options.add(optionTwoDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        title            |content            |isToOrder |contentOption1     |contentOption2    |isCorrectOption1    |isCorrectOption2 |setOrderOption1 |setOrderOption2 ||errorMessage
        null             |QUESTION_1_CONTENT |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_TITLE_FOR_QUESTION
        "  "             |QUESTION_1_CONTENT |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_TITLE_FOR_QUESTION
        QUESTION_1_TITLE |null               |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_CONTENT_FOR_QUESTION
        QUESTION_1_TITLE |"    "             |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_CONTENT_FOR_QUESTION
        QUESTION_1_TITLE |QUESTION_1_CONTENT |true      |null               |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_CONTENT_FOR_OPTION
        QUESTION_1_TITLE |QUESTION_1_CONTENT |true      |"  "               |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_CONTENT_FOR_OPTION
        QUESTION_1_TITLE |QUESTION_1_CONTENT |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |false               |false            |null            |null            ||ErrorMessage.NO_CORRECT_OPTION
        QUESTION_1_TITLE |QUESTION_1_CONTENT |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |false            |1               |null            ||ErrorMessage.AT_LEAST_TWO_CORRECT_OPTION_NEEDED

    }

    def "create a multiple option order question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(true)

        and: 'two options'
        def optionOneDto = new OptionDto()
        optionOneDto.setContent(OPTION_1_CONTENT)
        optionOneDto.setCorrect(true)
        optionOneDto.setOrder(2)

        def optionTwoDto = new OptionDto()
        optionTwoDto.setContent(OPTION_2_CONTENT)
        optionTwoDto.setCorrect(true)
        optionTwoDto.setOrder(1)

        def options = new ArrayList<OptionDto>()
        options.add(optionOneDto)
        options.add(optionTwoDto)

        questionDto.getQuestionDetailsDto().setOptions(options) //order is included

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetails().getOptions().size() == 2
        result.getQuestionDetails().getOptions().get(0).getOrder() == 2
        result.getQuestionDetails().getOptions().get(1).getOrder() == 1

    }

    def "create a multiple choice question with two correct options"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: 'two options'
        def optionOneDto = new OptionDto()
        optionOneDto.setContent(OPTION_1_CONTENT)
        optionOneDto.setCorrect(true)

        def optionTwoDto = new OptionDto()
        optionTwoDto.setContent(OPTION_2_CONTENT)
        optionTwoDto.setCorrect(true)

        def options = new ArrayList<OptionDto>()
        options.add(optionOneDto)
        options.add(optionTwoDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getQuestionDetails().getOptions().size() == 2

    }

    def "create a multiple choice question with no image and one option"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getQuestionDetails().getOptions().size() == 1
        result.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(result)
        def resOption = result.getQuestionDetails().getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()
    }

    def "create a multiple choice question with image and two options"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: 'an image'
        def image = new ImageDto()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        questionDto.setImage(image)
        and: 'two options'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() != null
        result.getKey() == 1
        result.getStatus() == Question.Status.AVAILABLE
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage().getId() != null
        result.getImage().getUrl() == IMAGE_1_URL
        result.getImage().getWidth() == 20
        result.getQuestionDetails().getOptions().size() == 2
    }

    def "create two multiple choice questions"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when: 'are created two questions'
        questionService.createQuestion(externalCourse.getId(), questionDto)
        questionDto.setKey(null)
        questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the two questions are created with the correct numbers"
        questionRepository.count() == 2L
        def resultOne = questionRepository.findAll().get(0)
        def resultTwo = questionRepository.findAll().get(1)
        resultOne.getKey() + resultTwo.getKey() == 3
    }


    def "create a code fill in question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeFillInQuestionDto()
        codeQuestionDto.setCode(CODE_QUESTION_1_CODE)
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        CodeFillInSpotDto fillInSpotDto = new CodeFillInSpotDto()
        OptionDto optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        fillInSpotDto.getOptions().add(optionDto)
        fillInSpotDto.setSequence(1)

        codeQuestionDto.getFillInSpots().add(fillInSpotDto)

        questionDto.setQuestionDetailsDto(codeQuestionDto)

        when:
        def rawResult = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct data is sent back"
        rawResult instanceof QuestionDto
        def result = (QuestionDto) rawResult
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.toString()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getQuestionDetailsDto().getFillInSpots().size() == 1
        result.getQuestionDetailsDto().getFillInSpots().get(0).getOptions().size() == 1

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def repoResult = questionRepository.findAll().get(0)
        repoResult.getId() != null
        repoResult.getKey() == 1
        repoResult.getStatus() == Question.Status.AVAILABLE
        repoResult.getTitle() == QUESTION_1_TITLE
        repoResult.getContent() == QUESTION_1_CONTENT
        repoResult.getImage() == null
        repoResult.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(repoResult)

        def repoCode = (CodeFillInQuestion) repoResult.getQuestionDetails()
        repoCode.getFillInSpots().size() == 1
        repoCode.getCode() == CODE_QUESTION_1_CODE
        repoCode.getLanguage() == CODE_QUESTION_1_LANGUAGE
        def resOption = repoCode.getFillInSpots().get(0).getOptions().get(0)
        resOption.getContent() == OPTION_1_CONTENT
        resOption.isCorrect()

    }

    def "cannot create a code fill in question without fillin spots"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new CodeFillInQuestionDto())

        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_ONE_OPTION_NEEDED
    }

    def "cannot create a code fill in question with fillin spots without options"() {
        given: "a questionDto with 1 fill in spot without options"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new CodeFillInQuestionDto())

        CodeFillInSpotDto fillInSpotDto = new CodeFillInSpotDto()
        questionDto.getQuestionDetailsDto().getFillInSpots().add(fillInSpotDto)


        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CORRECT_OPTION
    }

    def "cannot create a code fill in question with fillin spots without correct options"() {
        given: "a questionDto with 1 fill in spot without options"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new CodeFillInQuestionDto())

        CodeFillInSpotDto fillInSpotDto = new CodeFillInSpotDto()
        OptionDto optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(false)
        questionDto.getQuestionDetailsDto().getFillInSpots().add(fillInSpotDto)


        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.NO_CORRECT_OPTION
    }


    def "create a code order question"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeOrderQuestionDto()
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        CodeOrderSlotDto slotDto1 = new CodeOrderSlotDto()
        slotDto1.content = OPTION_1_CONTENT;
        slotDto1.order = 1;

        CodeOrderSlotDto slotDto2 = new CodeOrderSlotDto()
        slotDto2.content = OPTION_1_CONTENT;
        slotDto2.order = 2;

        CodeOrderSlotDto slotDto3 = new CodeOrderSlotDto()
        slotDto3.content = OPTION_1_CONTENT;
        slotDto3.order = 3;

        codeQuestionDto.getCodeOrderSlots().add(slotDto1)
        codeQuestionDto.getCodeOrderSlots().add(slotDto2)
        codeQuestionDto.getCodeOrderSlots().add(slotDto3)

        questionDto.setQuestionDetailsDto(codeQuestionDto)

        when:
        def rawResult = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "the correct data is sent back"
        rawResult instanceof QuestionDto
        def result = (QuestionDto) rawResult
        result.getId() != null
        result.getStatus() == Question.Status.AVAILABLE.toString()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        result.getImage() == null
        result.getQuestionDetailsDto().getCodeOrderSlots().size() == 3
        result.getQuestionDetailsDto().getCodeOrderSlots().get(0).getContent() == OPTION_1_CONTENT

        then: "the correct question is inside the repository"
        questionRepository.count() == 1L
        def repoResult = questionRepository.findAll().get(0)
        repoResult.getId() != null
        repoResult.getKey() == 1
        repoResult.getStatus() == Question.Status.AVAILABLE
        repoResult.getTitle() == QUESTION_1_TITLE
        repoResult.getContent() == QUESTION_1_CONTENT
        repoResult.getImage() == null
        repoResult.getCourse().getName() == COURSE_1_NAME
        externalCourse.getQuestions().contains(repoResult)

        def repoCode = (CodeOrderQuestion) repoResult.getQuestionDetails()
        repoCode.getCodeOrderSlots().size() == 3
        repoCode.getLanguage() == CODE_QUESTION_1_LANGUAGE
        def resOption = repoCode.getCodeOrderSlots().get(0)
        resOption.getContent() == OPTION_1_CONTENT
    }

    def "cannot create a code order question without CodeOrderSlots"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeOrderQuestionDto()
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        questionDto.setQuestionDetailsDto(codeQuestionDto)

        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_THREE_SLOTS_NEEDED
    }

    def "cannot create a code order question without 3 CodeOrderSlots"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeOrderQuestionDto()
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        CodeOrderSlotDto slotDto1 = new CodeOrderSlotDto()
        slotDto1.content = OPTION_1_CONTENT;
        slotDto1.order = 1;

        CodeOrderSlotDto slotDto2 = new CodeOrderSlotDto()
        slotDto2.content = OPTION_1_CONTENT;
        slotDto2.order = 2;

        codeQuestionDto.getCodeOrderSlots().add(slotDto1)
        codeQuestionDto.getCodeOrderSlots().add(slotDto2)

        questionDto.setQuestionDetailsDto(codeQuestionDto)
        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_THREE_SLOTS_NEEDED
    }

    def "cannot create a code order question without 3 CodeOrderSlots with order"() {
        given: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())

        def codeQuestionDto = new CodeOrderQuestionDto()
        codeQuestionDto.setLanguage(CODE_QUESTION_1_LANGUAGE)

        CodeOrderSlotDto slotDto1 = new CodeOrderSlotDto()
        slotDto1.content = OPTION_1_CONTENT;
        slotDto1.order = 1;

        CodeOrderSlotDto slotDto2 = new CodeOrderSlotDto()
        slotDto2.content = OPTION_1_CONTENT;
        slotDto2.order = 2;

        CodeOrderSlotDto slotDto3 = new CodeOrderSlotDto()
        slotDto3.content = OPTION_1_CONTENT;
        slotDto3.order = null;

        codeQuestionDto.getCodeOrderSlots().add(slotDto1)
        codeQuestionDto.getCodeOrderSlots().add(slotDto2)
        codeQuestionDto.getCodeOrderSlots().add(slotDto3)

        questionDto.setQuestionDetailsDto(codeQuestionDto)
        when:
        def result = questionService.createQuestion(externalCourse.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_THREE_SLOTS_NEEDED
    }


    @Unroll
    def "fail to create any question for invalid/non-existent course (#nonExistentId)"(Integer nonExistentId) {
        given: "any multiple choice question dto"
        def questionDto = new QuestionDto()
        when:
        questionService.createQuestion(nonExistentId, questionDto)
        then:
        def exception = thrown(TutorException)
        exception.errorMessage == ErrorMessage.COURSE_NOT_FOUND
        where:
        nonExistentId << [-1, 0, 200]
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
