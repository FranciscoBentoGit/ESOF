package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Image
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconLeftDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconRightDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import spock.lang.Unroll

@DataJpaTest
class UpdateQuestionTest extends SpockTest {
    def question
    def optionOK
    def optionKO
    def user

    def setup() {
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        and: 'an image'
        def image = new Image()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        imageRepository.save(image)

        given: "create a question"
        question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        question.setImage(image)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        and: 'two options'
        optionOK = new Option()
        optionOK.setContent(OPTION_1_CONTENT)
        optionOK.setCorrect(true)
        optionOK.setSequence(0)
        optionOK.setQuestionDetails(questionDetails)
        optionRepository.save(optionOK)

        optionKO = new Option()
        optionKO.setContent(OPTION_1_CONTENT)
        optionKO.setCorrect(false)
        optionKO.setSequence(1)
        optionKO.setQuestionDetails(questionDetails)
        optionRepository.save(optionKO)
    }

    def "update an open answer question" () {
        given: "a question"
        def questionOpen = new Question()
        questionOpen.setKey(1)
        questionOpen.setTitle(QUESTION_1_TITLE)
        questionOpen.setContent(QUESTION_1_CONTENT)
        questionOpen.setStatus(Question.Status.AVAILABLE)
        questionOpen.setNumberOfAnswers(2)
        questionOpen.setNumberOfCorrect(1)
        questionOpen.setCourse(externalCourse)

        def questionOpenAnswerDetails = new OpenAnswerQuestion()
        questionOpen.setQuestionDetails(questionOpenAnswerDetails)
        questionOpen.getQuestionDetails().setResponse(RESPONSE_1)

        questionDetailsRepository.save(questionOpenAnswerDetails)
        questionRepository.save(questionOpen)

        and: 'a changed question'
        def questionDto = new QuestionDto(questionOpen)
        questionDto.setTitle(NEW_TITLE)
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setResponse(NEW_RESPONSE)


        when:
        questionService.updateQuestion(questionOpen.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 2L
        def result = questionRepository.findAll().get(1)
        result.getId() == questionOpen.getId()
        result.getTitle() == NEW_TITLE
        result.getQuestionDetails().getResponse().equals(NEW_RESPONSE)


        and: 'these are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getContent() == QUESTION_1_CONTENT
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1

    }
    def "cannot update an open answer question with no title" () {
        given: "a question"
        def questionOpen = new Question()
        questionOpen.setKey(1)
        questionOpen.setTitle(QUESTION_1_TITLE)
        questionOpen.setContent(QUESTION_1_CONTENT)
        questionOpen.setStatus(Question.Status.AVAILABLE)
        questionOpen.setNumberOfAnswers(2)
        questionOpen.setNumberOfCorrect(1)
        questionOpen.setCourse(externalCourse)

        def questionOpenAnswerDetails = new OpenAnswerQuestion()
        questionOpen.setQuestionDetails(questionOpenAnswerDetails)
        questionOpen.getQuestionDetails().setResponse(RESPONSE_1)

        questionDetailsRepository.save(questionOpenAnswerDetails)
        questionRepository.save(questionOpen)

        and: "a changed question"
        def questionDto = new QuestionDto(questionOpen)
        questionDto.setTitle('')
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setResponse(NEW_RESPONSE)


        when:
        questionService.updateQuestion(questionOpen.getId(), questionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_TITLE_FOR_QUESTION
    }
    def "cannot update an open answer question with no content" () {
        given: "a question"
        def questionOpen = new Question()
        questionOpen.setKey(1)
        questionOpen.setTitle(QUESTION_1_TITLE)
        questionOpen.setContent(QUESTION_1_CONTENT)
        questionOpen.setStatus(Question.Status.AVAILABLE)
        questionOpen.setNumberOfAnswers(2)
        questionOpen.setNumberOfCorrect(1)
        questionOpen.setCourse(externalCourse)

        def questionOpenAnswerDetails = new OpenAnswerQuestion()
        questionOpen.setQuestionDetails(questionOpenAnswerDetails)
        questionOpen.getQuestionDetails().setResponse(RESPONSE_1)

        questionDetailsRepository.save(questionOpenAnswerDetails)
        questionRepository.save(questionOpen)

        and: 'a changed question'
        def questionDto = new QuestionDto(questionOpen)
        questionDto.setContent('')
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setResponse(NEW_RESPONSE)


        when:
        questionService.updateQuestion(questionOpen.getId(), questionDto)

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_CONTENT_FOR_QUESTION
    }

    def "cannot update a multiple option order question to have less than 2 options"() {
        given: "a question"
        def questionOrder = new Question()
        questionOrder.setKey(1)
        questionOrder.setTitle(QUESTION_1_TITLE)
        questionOrder.setContent(QUESTION_1_CONTENT)
        questionOrder.setStatus(Question.Status.AVAILABLE)
        questionOrder.setNumberOfAnswers(2)
        questionOrder.setNumberOfCorrect(1)
        questionOrder.setCourse(externalCourse)

        def questionOrderDetails = new MultipleChoiceQuestion()
        questionOrder.setQuestionDetails(questionOrderDetails)
        questionOrder.getQuestionDetails().setIsToOrder(true)
        questionDetailsRepository.save(questionOrderDetails)
        questionRepository.save(questionOrder)

        and: "options"
        def optionOrderOK = new Option()
        optionOrderOK.setContent(OPTION_1_CONTENT)
        optionOrderOK.setCorrect(true)
        optionOrderOK.setOrder(1)
        optionOrderOK.setSequence(0)
        optionOrderOK.setQuestionDetails(questionOrderDetails)
        optionRepository.save(optionOrderOK)

        def optionOrderOK2 = new Option()
        optionOrderOK2.setContent(OPTION_2_CONTENT)
        optionOrderOK2.setCorrect(true)
        optionOrderOK2.setOrder(2)
        optionOrderOK2.setSequence(0)
        optionOrderOK2.setQuestionDetails(questionOrderDetails)
        optionRepository.save(optionOrderOK2)

        and: 'changed options to 1 only'
        def questionDto = new QuestionDto(questionOrder)
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOrderOK)
        optionDto.setCorrect(true)
        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)
        when:
        questionService.updateQuestion(questionOrder.getId(), questionDto)

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.AT_LEAST_TWO_OPTION_NEEDED

    }

    def "cannot update a multiple option order question with student answers"() {
        given: "a question"
        def questionOrder = new Question()
        questionOrder.setKey(1)
        questionOrder.setTitle(QUESTION_1_TITLE)
        questionOrder.setContent(QUESTION_1_CONTENT)
        questionOrder.setStatus(Question.Status.AVAILABLE)
        questionOrder.setNumberOfAnswers(2)
        questionOrder.setNumberOfCorrect(1)
        questionOrder.setCourse(externalCourse)

        def questionOrderDetails = new MultipleChoiceQuestion()
        questionOrder.setQuestionDetails(questionOrderDetails)
        questionOrder.getQuestionDetails().setIsToOrder(true)
        questionDetailsRepository.save(questionOrderDetails)
        questionRepository.save(questionOrder)

        and: "options"
        def optionOrderOK = new Option()
        optionOrderOK.setContent(OPTION_1_CONTENT)
        optionOrderOK.setCorrect(true)
        optionOrderOK.setOrder(1)
        optionOrderOK.setSequence(0)
        optionOrderOK.setQuestionDetails(questionOrderDetails)
        optionRepository.save(optionOrderOK)

        def optionOrderOK2 = new Option()
        optionOrderOK2.setContent(OPTION_2_CONTENT)
        optionOrderOK2.setCorrect(true)
        optionOrderOK2.setOrder(2)
        optionOrderOK2.setSequence(0)
        optionOrderOK2.setQuestionDetails(questionOrderDetails)
        optionRepository.save(optionOrderOK2)

        and: "an image"
        def imageOrder = new Image()
        imageOrder .setUrl(IMAGE_1_URL)
        imageOrder .setWidth(20)
        imageRepository.save(imageOrder)

        and: "quiz with answers"
        Quiz quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quizRepository.save(quiz)

        QuizQuestion quizQuestion= new QuizQuestion()
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(questionOrder)
        quizQuestionRepository.save(quizQuestion)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)

        def questionAnswer = new QuestionAnswer()
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, [optionOrderOK,optionOrderOK2])
        questionAnswer.setAnswerDetails(answerDetails)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        and: "a changed question"
        def questionDto = new QuestionDto(questionOrder)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(true)

        and: '2 changed options'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOrderOK)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setOrder(2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOrderOK2)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setOrder(1)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(questionOrder.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_CHANGE_ANSWERED_QUESTION
    }

    @Unroll("invalid arguments: #title | #content | #isToOrder | #contentOption1 | #contentOption2 | #isCorrectOption1 | #isCorrectOption2 | #setOrderOption1 | #setOrderOption2 || errorMessage ")
    def "invalid input values to update a multiple option order question"(){
        given: "a question"
        def questionOrder = new Question()
        questionOrder.setKey(1)
        questionOrder.setTitle(QUESTION_1_TITLE)
        questionOrder.setContent(QUESTION_1_CONTENT)
        questionOrder.setStatus(Question.Status.AVAILABLE)
        questionOrder.setNumberOfAnswers(2)
        questionOrder.setNumberOfCorrect(1)
        questionOrder.setCourse(externalCourse)

        def questionOrderDetails = new MultipleChoiceQuestion()
        questionOrder.setQuestionDetails(questionOrderDetails)
        questionOrder.getQuestionDetails().setIsToOrder(true)
        questionDetailsRepository.save(questionOrderDetails)
        questionRepository.save(questionOrder)

        and: "options"
        def optionOrderOK = new Option()
        optionOrderOK.setContent(OPTION_1_CONTENT)
        optionOrderOK.setCorrect(true)
        optionOrderOK.setOrder(1)
        optionOrderOK.setSequence(0)
        optionOrderOK.setQuestionDetails(questionOrderDetails)
        optionRepository.save(optionOrderOK)

        def optionOrderOK2 = new Option()
        optionOrderOK2.setContent(OPTION_2_CONTENT)
        optionOrderOK2.setCorrect(true)
        optionOrderOK2.setOrder(2)
        optionOrderOK2.setSequence(0)
        optionOrderOK2.setQuestionDetails(questionOrderDetails)
        optionRepository.save(optionOrderOK2)


        and: "a changed question"
        def questionDto = new QuestionDto(questionOrder)
        questionDto.setTitle(title)
        questionDto.setContent(content)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(isToOrder)

        and: '2 changed options'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOrderOK)
        optionDto.setContent(contentOption1)
        optionDto.setCorrect(isCorrectOption1)
        optionDto.setOrder(setOrderOption1)
        options.add(optionDto)
        optionDto = new OptionDto(optionOrderOK2)
        optionDto.setContent(contentOption2)
        optionDto.setCorrect(isCorrectOption2)
        optionDto.setOrder(setOrderOption2)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(questionOrder.getId(), questionDto)

        then: "exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == errorMessage

        where:
        title            |content            |isToOrder |contentOption1     |contentOption2    |isCorrectOption1    |isCorrectOption2 |setOrderOption1 |setOrderOption2 ||errorMessage
        null             |QUESTION_1_CONTENT |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_TITLE_FOR_QUESTION
        "  "             |QUESTION_1_CONTENT |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_TITLE_FOR_QUESTION
        QUESTION_1_TITLE |null               |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_CONTENT_FOR_QUESTION
        QUESTION_1_TITLE |"    "             |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |true             |1               |2               ||ErrorMessage.INVALID_CONTENT_FOR_QUESTION
        QUESTION_1_TITLE |QUESTION_1_CONTENT |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |false               |false            |null            |null            ||ErrorMessage.NO_CORRECT_OPTION
        QUESTION_1_TITLE |QUESTION_1_CONTENT |true      |OPTION_1_CONTENT   |OPTION_2_CONTENT  |true                |false            |1               |null            ||ErrorMessage.AT_LEAST_TWO_CORRECT_OPTION_NEEDED

    }

    def "update a multiple option order question"() {
        given: "a question"
        def questionOrder = new Question()
        questionOrder.setKey(1)
        questionOrder.setTitle(QUESTION_1_TITLE)
        questionOrder.setContent(QUESTION_1_CONTENT)
        questionOrder.setStatus(Question.Status.AVAILABLE)
        questionOrder.setNumberOfAnswers(2)
        questionOrder.setNumberOfCorrect(1)
        questionOrder.setCourse(externalCourse)

        def questionOrderDetails = new MultipleChoiceQuestion()
        questionOrder.setQuestionDetails(questionOrderDetails)
        questionOrder.getQuestionDetails().setIsToOrder(true)
        questionDetailsRepository.save(questionOrderDetails)
        questionRepository.save(questionOrder)

        and: "options"
        def optionOrderOK = new Option()
        optionOrderOK.setContent(OPTION_1_CONTENT)
        optionOrderOK.setCorrect(true)
        optionOrderOK.setOrder(1)
        optionOrderOK.setSequence(0)
        optionOrderOK.setQuestionDetails(questionOrderDetails)
        optionRepository.save(optionOrderOK)

        def optionOrderOK2 = new Option()
        optionOrderOK2.setContent(OPTION_2_CONTENT)
        optionOrderOK2.setCorrect(true)
        optionOrderOK2.setOrder(2)
        optionOrderOK2.setSequence(0)
        optionOrderOK2.setQuestionDetails(questionOrderDetails)
        optionRepository.save(optionOrderOK2)

        and: "an image"
        def imageOrder = new Image()
        imageOrder.setUrl(IMAGE_1_URL)
        imageOrder.setWidth(20)
        imageRepository.save(imageOrder)
        questionOrder.setImage(imageOrder)

        and: "a changed question"
        def questionDto = new QuestionDto(questionOrder)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(true)

        and: '2 changed options'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOrderOK)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setOrder(2)
        options.add(optionDto)
        optionDto = new OptionDto(optionOrderOK2)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setOrder(1)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(questionOrder.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 2L
        def result = questionRepository.findAll().get(1)
        result.getId() == questionOrder.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1
        result.getImage() != null
        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 2
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getId() == optionOrderOK.getId()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_2_CONTENT
        resOptionOne.isCorrect()
        resOptionOne.getOrder() == 2
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getId() == optionOrderOK2.getId()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_1_CONTENT
        resOptionTwo.isCorrect()
        resOptionTwo.getOrder() == 1
    }


    def "update an item combination question"() {
        given: "an item combination question"
        def questionItem = new Question()
        questionItem.setKey(1)
        questionItem.setTitle(QUESTION_1_TITLE)
        questionItem.setContent(QUESTION_1_CONTENT)
        questionItem.setStatus(Question.Status.AVAILABLE)
        questionItem.setNumberOfAnswers(2)
        questionItem.setNumberOfCorrect(1)
        questionItem.setCourse(externalCourse)
        def questionDetails = new ItemCombinationQuestion()
        questionItem.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(questionItem)

        def iconLeft1 = new IconLeft()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        iconLeft1.setQuestionDetails(questionDetails)
        def iconLeft2 = new IconLeft()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        iconLeft2.setQuestionDetails(questionDetails)
        def iconRight1 = new IconRight()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        iconRight1.setQuestionDetails(questionDetails)
        def iconRight2 = new IconRight()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)
        iconRight2.setQuestionDetails(questionDetails)

        List<Integer> matches1 = new ArrayList<>()
        List<Integer> matches2 = new ArrayList<>()

        matches1.add(iconLeft1.getSequence())
        matches2.add(iconLeft2.getSequence())

        iconRight1.setMatch(matches1)
        iconRight2.setMatch(matches2)

        iconLeftRepository.save(iconLeft1)
        iconLeftRepository.save(iconLeft2)
        iconRightRepository.save(iconRight1)
        iconRightRepository.save(iconRight2)


        and: "a changed question"
        def questionDto = new QuestionDto(questionItem)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())
        and: "2 changed icons"
        def iconLeft1Dto = new IconLeftDto(iconLeft1)
        iconLeft1Dto.setContent(ICON_2_CONTENT)
        def iconLeft2Dto = new IconLeftDto(iconLeft2)
        iconLeft2Dto.setContent(ICON_1_CONTENT)
        def iconRight1Dto = new IconRightDto(iconRight1)
        iconRight1Dto.setMatch(matches2)
        def iconRight2Dto = new IconRightDto(iconRight2)
        iconRight2Dto.setMatch(matches1)

        def leftList = new ArrayList<IconLeftDto>()
        def rightList = new ArrayList<IconRightDto>()

        leftList.add(iconLeft1Dto)
        leftList.add(iconLeft2Dto)
        rightList.add(iconRight1Dto)
        rightList.add(iconRight2Dto)

        questionDto.getQuestionDetailsDto().setLeftIcons(leftList)
        questionDto.getQuestionDetailsDto().setRightIcons(rightList)

        when:
        questionService.updateQuestion(questionItem.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 2L
        def result = questionRepository.findAll().get(1)
        result.getId() == questionItem.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: "not changed"
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1
        and: "an icon is changed"
        def resIconOne = result.getQuestionDetails().getLeftIcons().stream().filter(op -> op.getId() == iconLeft1.getId()).findAny().orElse(null)
        resIconOne.getContent() == ICON_2_CONTENT
        def resIconTwo = result.getQuestionDetails().getLeftIcons().stream().filter(op -> op.getId() == iconLeft2.getId()).findAny().orElse(null)
        resIconTwo.getContent() == ICON_1_CONTENT
        def resIconThree = result.getQuestionDetails().getRightIcons().stream().filter(op -> op.getId() == iconRight1.getId()).findAny().orElse(null)
        resIconThree.getMatch().get(0) == iconLeft2.getSequence()
        def resIconFour = result.getQuestionDetails().getRightIcons().stream().filter(op -> op.getId() == iconRight2.getId()).findAny().orElse(null)
        resIconFour.getMatch().get(0) == iconLeft1.getSequence()
    }

    def "cannot update item combination question with invalid title"() {
        given: "an item combination question"
        def questionItem = new Question()
        questionItem.setKey(1)
        questionItem.setTitle(QUESTION_1_TITLE)
        questionItem.setContent(QUESTION_1_CONTENT)
        questionItem.setStatus(Question.Status.AVAILABLE)
        questionItem.setNumberOfAnswers(2)
        questionItem.setNumberOfCorrect(1)
        questionItem.setCourse(externalCourse)
        def questionDetails = new ItemCombinationQuestion()
        questionItem.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(questionItem)

        def iconLeft1 = new IconLeft()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        iconLeft1.setQuestionDetails(questionDetails)
        def iconLeft2 = new IconLeft()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        iconLeft2.setQuestionDetails(questionDetails)
        def iconRight1 = new IconRight()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        iconRight1.setQuestionDetails(questionDetails)
        def iconRight2 = new IconRight()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)
        iconRight2.setQuestionDetails(questionDetails)

        List<Integer> matches1 = new ArrayList<>()
        List<Integer> matches2 = new ArrayList<>()

        matches1.add(iconLeft1.getSequence())
        matches2.add(iconLeft2.getSequence())

        iconRight1.setMatch(matches1)
        iconRight2.setMatch(matches2)

        iconLeftRepository.save(iconLeft1)
        iconLeftRepository.save(iconLeft2)
        iconRightRepository.save(iconRight1)
        iconRightRepository.save(iconRight2)

        and: "a question"
        def questionDto = new QuestionDto(questionItem)
        questionDto.setTitle('     ')

        when:
        questionService.updateQuestion(questionItem.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_TITLE_FOR_QUESTION
    }

    def "cannot update item combination question with no content in one or both columns"() {
        given: "an item combination question"
        def questionItem = new Question()
        questionItem.setKey(1)
        questionItem.setTitle(QUESTION_1_TITLE)
        questionItem.setContent(QUESTION_1_CONTENT)
        questionItem.setStatus(Question.Status.AVAILABLE)
        questionItem.setNumberOfAnswers(2)
        questionItem.setNumberOfCorrect(1)
        questionItem.setCourse(externalCourse)
        def questionDetails = new ItemCombinationQuestion()
        questionItem.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(questionItem)

        def iconLeft1 = new IconLeft()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        iconLeft1.setQuestionDetails(questionDetails)
        def iconLeft2 = new IconLeft()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        iconLeft2.setQuestionDetails(questionDetails)
        def iconRight1 = new IconRight()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        iconRight1.setQuestionDetails(questionDetails)
        def iconRight2 = new IconRight()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)
        iconRight2.setQuestionDetails(questionDetails)

        List<Integer> matches1 = new ArrayList<>()
        List<Integer> matches2 = new ArrayList<>()

        matches1.add(iconLeft1.getSequence())
        matches2.add(iconLeft2.getSequence())

        iconRight1.setMatch(matches1)
        iconRight2.setMatch(matches2)

        iconLeftRepository.save(iconLeft1)
        iconLeftRepository.save(iconLeft2)
        iconRightRepository.save(iconRight1)
        iconRightRepository.save(iconRight2)

        and: "a questionDto"
        def questionDto = new QuestionDto(questionItem)
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "iconDtos"
        def iconLeft1Dto = new IconLeftDto(iconLeft1)
        iconLeft1Dto.setContent('     ')
        def iconLeft2Dto = new IconLeftDto(iconLeft2)
        def iconRight1Dto = new IconRightDto(iconRight1)
        def iconRight2Dto = new IconRightDto(iconRight2)

        def leftList = new ArrayList<IconLeftDto>()
        def rightList = new ArrayList<IconRightDto>()

        leftList.add(iconLeft1Dto)
        leftList.add(iconLeft2Dto)
        rightList.add(iconRight1Dto)
        rightList.add(iconRight2Dto)

        questionDto.getQuestionDetailsDto().setLeftIcons(leftList)
        questionDto.getQuestionDetailsDto().setRightIcons(rightList)

        when:
        questionService.updateQuestion(questionItem.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_CONTENT_FOR_OPTION
    }

    def "update a question"() {
        given: "a changed question"
        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        and: '2 changed options'
        def options = new ArrayList<OptionDto>()
        def optionDto = new OptionDto(optionOK)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_2_TITLE
        result.getContent() == QUESTION_2_CONTENT
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1
        result.getDifficulty() == 50
        result.getImage() != null
        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 2
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getId() == optionOK.getId()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_2_CONTENT
        !resOptionOne.isCorrect()
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getId() == optionKO.getId()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_1_CONTENT
        resOptionTwo.isCorrect()
    }

    def "update question with missing data"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setTitle('     ')

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.INVALID_TITLE_FOR_QUESTION
    }

    def "update question with two options true"() {
        given: 'a question'
        def questionDto = new QuestionDto(question)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        def optionDto = new OptionDto(optionOK)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question is changed"
        questionRepository.count() == 1L
        def result = questionRepository.findAll().get(0)
        result.getId() == question.getId()
        result.getTitle() == QUESTION_1_TITLE
        result.getContent() == QUESTION_1_CONTENT
        and: 'are not changed'
        result.getStatus() == Question.Status.AVAILABLE
        result.getNumberOfAnswers() == 2
        result.getNumberOfCorrect() == 1
        result.getDifficulty() == 50
        result.getImage() != null
        and: 'an option is changed'
        result.getQuestionDetails().getOptions().size() == 2
        def resOptionOne = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getId() == optionOK.getId()}).findAny().orElse(null)
        resOptionOne.getContent() == OPTION_2_CONTENT
        resOptionOne.isCorrect()
        def resOptionTwo = result.getQuestionDetails().getOptions().stream().filter({ option -> option.getId() == optionKO.getId()}).findAny().orElse(null)
        resOptionTwo.getContent() == OPTION_1_CONTENT
        resOptionTwo.isCorrect()

    }

    def "update correct option in a question with answers"() {
        given: "a question with answers"
        Quiz quiz = new Quiz()
        quiz.setKey(1)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quizRepository.save(quiz)

        QuizQuestion quizQuestion= new QuizQuestion()
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)
        quizAnswerRepository.save(quizAnswer)


        def questionAnswer = new QuestionAnswer()
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, [optionOK])
        questionAnswer.setAnswerDetails(answerDetails)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        questionAnswer = new QuestionAnswer()
        answerDetails = new MultipleChoiceAnswer(questionAnswer, [optionKO])
        questionAnswer.setAnswerDetails(answerDetails)
        questionAnswer.setQuizQuestion(quizQuestion)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)


        def questionDto = new QuestionDto(question)
        questionDto.setTitle(QUESTION_2_TITLE)
        questionDto.setContent(QUESTION_2_CONTENT)
        questionDto.setStatus(Question.Status.DISABLED.name())
        questionDto.setNumberOfAnswers(4)
        questionDto.setNumberOfCorrect(2)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())

        and: 'a optionId'
        def optionDto = new OptionDto(optionOK)
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        optionDto = new OptionDto(optionKO)
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        options.add(optionDto)
        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        questionService.updateQuestion(question.getId(), questionDto)

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_CHANGE_ANSWERED_QUESTION
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
