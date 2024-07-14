package pt.ulisboa.tecnico.socialsoftware.tutor.question.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.TopicDto
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.domain.QuestionSubmission
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class RemoveQuestionTest extends SpockTest {
    def question
    def optionOK
    def optionKO
    def teacher

    def setup() {
        def image = new Image()
        image.setUrl(IMAGE_1_URL)
        image.setWidth(20)
        imageRepository.save(image)

        question = new Question()
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        question.setNumberOfAnswers(2)
        question.setNumberOfCorrect(1)
        question.setCourse(externalCourse)
        question.setImage(image)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

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

    def "remove an open answer question"() {
        given: "a question"
        def question1 = new Question()
        question1.setKey(2)
        question1.setTitle(QUESTION_1_TITLE)
        question1.setContent(QUESTION_1_CONTENT)
        question1.setStatus(Question.Status.AVAILABLE)
        question1.setCourse(externalCourse)

        def questionDetails = new OpenAnswerQuestion()
        question1.setQuestionDetails(questionDetails)
        question1.getQuestionDetails().setResponse(RESPONSE_1)

        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question1)

        when:
        questionService.removeQuestion(question1.getId())

        then: "the question is removeQuestion"
        questionRepository.count() == 1L
        imageRepository.count() == 1L
        optionRepository.count() == 2L

    }

    def "cannot remove an open answer question that was submitted"() {
        given: "a question"
        def question1 = new Question()
        question1.setKey(2)
        question1.setTitle(QUESTION_1_TITLE)
        question1.setContent(QUESTION_1_CONTENT)
        question1.setStatus(Question.Status.AVAILABLE)
        question1.setCourse(externalCourse)
        def questionDetails = new OpenAnswerQuestion()
        question1.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        question1.getQuestionDetails().setResponse(RESPONSE_1)
        questionRepository.save(question1)

        and: "a student"
        def student = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        userRepository.save(student)

        and: "a questionSubmission"
        def questionSubmission = new QuestionSubmission()
        questionSubmission.setQuestion(question1)
        questionSubmission.setSubmitter(student)
        questionSubmission.setCourseExecution(externalCourseExecution)
        questionSubmissionRepository.save(questionSubmission)

        when:
        questionService.removeQuestion(question1.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_DELETE_SUBMITTED_QUESTION

    }

    def "cannot remove an open answer question used in a quiz"() {
        given : "a question"
        def question1 = new Question()
        question1.setKey ( 2 )
        question1.setTitle (QUESTION_1_TITLE)
        question1.setContent (QUESTION_1_CONTENT)
        question1.setStatus ( Question.Status.AVAILABLE )
        question1.setCourse ( externalCourse )
        def questionDetails = new OpenAnswerQuestion()
        question1.setQuestionDetails ( questionDetails )
        questionDetailsRepository.save ( questionDetails )
        question1.getQuestionDetails().setResponse(RESPONSE_1)
        questionRepository.save (question1)

        and : "a quiz with answers"
        Quiz quiz = new Quiz()
        quiz.setKey ( 1 )
        quiz.setTitle ( QUIZ_TITLE )
        quiz.setType ( Quiz.QuizType.PROPOSED.toString ( ) )
        quiz.setAvailableDate ( LOCAL_DATE_BEFORE )
        quiz.setCourseExecution ( externalCourseExecution )
        quiz.setOneWay ( true )
        quizRepository.save ( quiz )

        QuizQuestion quizQuestion = new QuizQuestion()
        quizQuestion.setQuiz ( quiz )
        quizQuestion.setQuestion ( question1 )
        quizQuestionRepository.save ( quizQuestion )

        when :
        questionService.removeQuestion ( question1.getId ( ) )

        then : "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage ( ) == ErrorMessage.QUESTION_IS_USED_IN_QUIZ
    }


    def "remove an item combination question"() {
        given: "an item combination question"
        def questionItem = new Question()
        questionItem.setKey(2)
        questionItem.setTitle(QUESTION_1_TITLE)
        questionItem.setContent(QUESTION_1_CONTENT)
        questionItem.setStatus(Question.Status.AVAILABLE)
        questionItem.setNumberOfAnswers(2)
        questionItem.setNumberOfCorrect(1)
        questionItem.setCourse(externalCourse)
        def questionItemDetails = new ItemCombinationQuestion()
        questionItem.setQuestionDetails(questionItemDetails)
        questionDetailsRepository.save(questionItemDetails)
        questionRepository.save(questionItem)

        def iconLeft1 = new IconLeft()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        iconLeft1.setQuestionDetails(questionItemDetails)
        def iconLeft2 = new IconLeft()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        iconLeft2.setQuestionDetails(questionItemDetails)
        def iconRight1 = new IconRight()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        iconRight1.setQuestionDetails(questionItemDetails)
        def iconRight2 = new IconRight()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)
        iconRight2.setQuestionDetails(questionItemDetails)

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

        when:
        questionService.removeQuestion(questionItem.getId())

        then: "the question is removeQuestion"
        questionRepository.count() == 1L
        imageRepository.count() == 1L
        optionRepository.count() == 2L
        iconLeftRepository.count() == 0L
        iconRightRepository.count() == 0L

    }

    def "remove an item combination question in use by a quiz"() {
        given: "an item combination question"
        def questionItem = new Question()
        questionItem.setKey(2)
        questionItem.setTitle(QUESTION_1_TITLE)
        questionItem.setContent(QUESTION_1_CONTENT)
        questionItem.setStatus(Question.Status.AVAILABLE)
        questionItem.setNumberOfAnswers(2)
        questionItem.setNumberOfCorrect(1)
        questionItem.setCourse(externalCourse)
        def questionItemDetails = new ItemCombinationQuestion()
        questionItem.setQuestionDetails(questionItemDetails)
        questionDetailsRepository.save(questionItemDetails)
        questionRepository.save(questionItem)

        def iconLeft1 = new IconLeft()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        iconLeft1.setQuestionDetails(questionItemDetails)
        def iconLeft2 = new IconLeft()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        iconLeft2.setQuestionDetails(questionItemDetails)
        def iconRight1 = new IconRight()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        iconRight1.setQuestionDetails(questionItemDetails)
        def iconRight2 = new IconRight()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)
        iconRight2.setQuestionDetails(questionItemDetails)

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

        and: "a quiz"
        Quiz quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setOneWay(true)
        quizRepository.save(quiz)

        QuizQuestion quizQuestion= new QuizQuestion()
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(questionItem)
        quizQuestionRepository.save(quizQuestion)

        when:
        questionService.removeQuestion(questionItem.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_IS_USED_IN_QUIZ
    }

    def "remove an item combination question that has topics" () {
        given: "an item combination question"
        def questionItem = new Question()
        questionItem.setKey(2)
        questionItem.setTitle(QUESTION_1_TITLE)
        questionItem.setContent(QUESTION_1_CONTENT)
        questionItem.setStatus(Question.Status.AVAILABLE)
        questionItem.setNumberOfAnswers(2)
        questionItem.setNumberOfCorrect(1)
        questionItem.setCourse(externalCourse)
        def questionItemDetails = new ItemCombinationQuestion()
        questionItem.setQuestionDetails(questionItemDetails)
        questionDetailsRepository.save(questionItemDetails)
        questionRepository.save(questionItem)

        def iconLeft1 = new IconLeft()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        iconLeft1.setQuestionDetails(questionItemDetails)
        def iconLeft2 = new IconLeft()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        iconLeft2.setQuestionDetails(questionItemDetails)
        def iconRight1 = new IconRight()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        iconRight1.setQuestionDetails(questionItemDetails)
        def iconRight2 = new IconRight()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)
        iconRight2.setQuestionDetails(questionItemDetails)

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

        and: 'a question with topics'
        def topicDto = new TopicDto()
        topicDto.setName("name1")
        def topicOne = new Topic(externalCourse, topicDto)
        topicDto.setName("name2")
        def topicTwo = new Topic(externalCourse, topicDto)
        questionItem.getTopics().add(topicOne)
        topicOne.getQuestions().add(questionItem)
        questionItem.getTopics().add(topicTwo)
        topicTwo.getQuestions().add(questionItem)
        topicRepository.save(topicOne)
        topicRepository.save(topicTwo)

        when:
        questionService.removeQuestion(questionItem.getId())

        then:
        questionRepository.count() == 1L
        imageRepository.count() == 1L
        optionRepository.count() == 2L
        topicRepository.count() == 2L
        iconLeftRepository.count() == 0L
        iconRightRepository.count() == 0L
        topicOne.getQuestions().size() == 0
        topicTwo.getQuestions().size() == 0
    }

    def "remove an item combination question that was submitted" () {
        given: "an item combination question"
        def questionItem = new Question()
        questionItem.setKey(2)
        questionItem.setTitle(QUESTION_1_TITLE)
        questionItem.setContent(QUESTION_1_CONTENT)
        questionItem.setStatus(Question.Status.AVAILABLE)
        questionItem.setNumberOfAnswers(2)
        questionItem.setNumberOfCorrect(1)
        questionItem.setCourse(externalCourse)
        def questionItemDetails = new ItemCombinationQuestion()
        questionItem.setQuestionDetails(questionItemDetails)
        questionDetailsRepository.save(questionItemDetails)
        questionRepository.save(questionItem)

        def iconLeft1 = new IconLeft()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        iconLeft1.setQuestionDetails(questionItemDetails)
        def iconLeft2 = new IconLeft()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        iconLeft2.setQuestionDetails(questionItemDetails)
        def iconRight1 = new IconRight()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        iconRight1.setQuestionDetails(questionItemDetails)
        def iconRight2 = new IconRight()
        iconRight2.setContent(ICON_2_CONTENT)
        iconRight2.setSequence(1)
        iconRight2.setQuestionDetails(questionItemDetails)

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

        and: "a student"
        def student = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        userRepository.save(student)

        and: "a questionSubmission"
        def questionSubmission = new QuestionSubmission()
        questionSubmission.setQuestion(questionItem)
        questionSubmission.setSubmitter(student)
        questionSubmission.setCourseExecution(externalCourseExecution)
        questionSubmissionRepository.save(questionSubmission)

        when:
        questionService.removeQuestion(questionItem.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_DELETE_SUBMITTED_QUESTION

    }

    def "remove a multiple option order question used in a quiz"() {
        given: "a question"
        def questionOrder = new Question()
        questionOrder.setKey(1)
        questionOrder.setTitle(QUESTION_1_TITLE)
        questionOrder.setContent(QUESTION_1_CONTENT)
        questionOrder.setStatus(Question.Status.AVAILABLE)
        questionOrder.setNumberOfAnswers(2)
        questionOrder.setNumberOfCorrect(2)
        questionOrder.setCourse(externalCourse)

        def questionOrderDetails = new MultipleChoiceQuestion()
        questionOrder.setQuestionDetails(questionOrderDetails)
        questionOrder.getQuestionDetailsDto().setIsToOrder(true)
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

        and: "a quiz with answers"
        Quiz quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setOneWay(true)
        quizRepository.save(quiz)

        QuizQuestion quizQuestion= new QuizQuestion()
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(questionOrder)
        quizQuestionRepository.save(quizQuestion)

        and: "an image"
        def imageOrder = new Image()
        imageOrder.setUrl(IMAGE_1_URL)
        imageOrder.setWidth(20)
        imageRepository.save(imageOrder)

        when:
        questionService.removeQuestion(questionOrder.getId())

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_IS_USED_IN_QUIZ

    }

    def "remove a multiple option order question that was submitted"() {
        given: "a question"
        def questionOrder = new Question()
        questionOrder.setKey(1)
        questionOrder.setTitle(QUESTION_1_TITLE)
        questionOrder.setContent(QUESTION_1_CONTENT)
        questionOrder.setStatus(Question.Status.AVAILABLE)
        questionOrder.setNumberOfAnswers(2)
        questionOrder.setNumberOfCorrect(2)
        questionOrder.setCourse(externalCourse)

        def questionOrderDetails = new MultipleChoiceQuestion()
        questionOrder.setQuestionDetails(questionOrderDetails)
        questionOrder.getQuestionDetailsDto().setIsToOrder(true)
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

        and: "a student"
        def student = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        userRepository.save(student)

        and: "a questionSubmission"
        def questionSubmission = new QuestionSubmission()
        questionSubmission.setQuestion(questionOrder)
        questionSubmission.setSubmitter(student)
        questionSubmission.setCourseExecution(externalCourseExecution)
        questionSubmissionRepository.save(questionSubmission)

        and: "an image"
        def imageOrder = new Image()
        imageOrder.setUrl(IMAGE_1_URL)
        imageOrder.setWidth(20)
        imageRepository.save(imageOrder)

        when:
        questionService.removeQuestion(questionOrder.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_DELETE_SUBMITTED_QUESTION

    }

    def "remove a multiple option order question that has topics"() {
        given: "a question"
        def questionOrder = new Question()
        questionOrder.setKey(1)
        questionOrder.setTitle(QUESTION_1_TITLE)
        questionOrder.setContent(QUESTION_1_CONTENT)
        questionOrder.setStatus(Question.Status.AVAILABLE)
        questionOrder.setNumberOfAnswers(2)
        questionOrder.setNumberOfCorrect(2)
        questionOrder.setCourse(externalCourse)

        def questionOrderDetails = new MultipleChoiceQuestion()
        questionOrder.setQuestionDetails(questionOrderDetails)
        questionOrder.getQuestionDetailsDto().setIsToOrder(true)
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

        and: 'and topics'
        def topicDto = new TopicDto()
        topicDto.setName("name1")
        def topicOne = new Topic(externalCourse, topicDto)
        topicDto.setName("name2")
        def topicTwo = new Topic(externalCourse, topicDto)
        questionOrder.getTopics().add(topicOne)
        topicOne.getQuestions().add(questionOrder)
        questionOrder.getTopics().add(topicTwo)
        topicTwo.getQuestions().add(questionOrder)
        topicRepository.save(topicOne)
        topicRepository.save(topicTwo)

        and: "an image"
        def imageOrder = new Image()
        imageOrder.setUrl(IMAGE_1_URL)
        imageOrder.setWidth(20)
        imageRepository.save(imageOrder)
        questionOrder.setImage(imageOrder)

        when:
        questionService.removeQuestion(questionOrder.getId())

        then:
        questionRepository.count() == 1L
        imageRepository.count() == 1L
        optionRepository.count() == 2L
        topicRepository.count() == 2L
        topicOne.getQuestions().size() == 0
        topicTwo.getQuestions().size() == 0

    }

    def "remove a multiple option order question"() {
        given: "a question"
        def questionOrder = new Question()
        questionOrder.setKey(1)
        questionOrder.setTitle(QUESTION_1_TITLE)
        questionOrder.setContent(QUESTION_1_CONTENT)
        questionOrder.setStatus(Question.Status.AVAILABLE)
        questionOrder.setNumberOfAnswers(2)
        questionOrder.setNumberOfCorrect(2)
        questionOrder.setCourse(externalCourse)

        def questionOrderDetails = new MultipleChoiceQuestion()
        questionOrder.setQuestionDetails(questionOrderDetails)
        questionOrder.getQuestionDetailsDto().setIsToOrder(true)
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

        when:
        questionService.removeQuestion(questionOrder.getId())

        then: "the question is removeOrderQuestion"
        questionRepository.count() == 1L
        imageRepository.count() == 1L
        optionRepository.count() == 2L

    }

    def "remove a question"() {
        when:
        questionService.removeQuestion(question.getId())

        then: "the question is removeQuestion"
        questionRepository.count() == 0L
        imageRepository.count() == 0L
        optionRepository.count() == 0L
    }

    def "remove a question used in a quiz"() {
        given: "a question with answers"
        Quiz quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setOneWay(true)
        quizRepository.save(quiz)

        QuizQuestion quizQuestion= new QuizQuestion()
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        when:
        questionService.removeQuestion(question.getId())

        then: "the question an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.QUESTION_IS_USED_IN_QUIZ
    }

    def "remove a question that has topics"() {
        given: 'a question with topics'
        def topicDto = new TopicDto()
        topicDto.setName("name1")
        def topicOne = new Topic(externalCourse, topicDto)
        topicDto.setName("name2")
        def topicTwo = new Topic(externalCourse, topicDto)
        question.getTopics().add(topicOne)
        topicOne.getQuestions().add(question)
        question.getTopics().add(topicTwo)
        topicTwo.getQuestions().add(question)
        topicRepository.save(topicOne)
        topicRepository.save(topicTwo)

        when:
        questionService.removeQuestion(question.getId())

        then:
        questionRepository.count() == 0L
        imageRepository.count() == 0L
        optionRepository.count() == 0L
        topicRepository.count() == 2L
        topicOne.getQuestions().size() == 0
        topicTwo.getQuestions().size() == 0
    }

    def "remove a question that was submitted"() {
        given: "a student"
        def student = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        userRepository.save(student)

        and: "a questionSubmission"
        def questionSubmission = new QuestionSubmission()
        questionSubmission.setQuestion(question)
        questionSubmission.setSubmitter(student)
        questionSubmission.setCourseExecution(externalCourseExecution)
        questionSubmissionRepository.save(questionSubmission)

        when:
        questionService.removeQuestion(question.getId())

        then: "an exception is thrown"
        def exception = thrown(TutorException)
        exception.getErrorMessage() == ErrorMessage.CANNOT_DELETE_SUBMITTED_QUESTION
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}