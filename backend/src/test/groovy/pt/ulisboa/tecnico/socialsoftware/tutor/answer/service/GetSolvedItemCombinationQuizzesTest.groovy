package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.dto.CourseExecutionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import spock.lang.Unroll

@DataJpaTest
class GetSolvedItemCombinationQuizzesTest extends SpockTest {
    def user
    def courseDto
    def question
    def iconLeft1
    def iconLeft2
    def iconRight1
    def iconRight2
    def quiz
    def quizQuestion

    def setup() {
        courseDto = new CourseExecutionDto(externalCourseExecution)

        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(externalCourse)
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        iconLeft1 = new IconLeft()
        iconLeft1.setContent(ICON_1_CONTENT)
        iconLeft1.setSequence(0)
        iconLeft1.setQuestionDetails(questionDetails)
        iconLeft2 = new IconLeft()
        iconLeft2.setContent(ICON_2_CONTENT)
        iconLeft2.setSequence(1)
        iconLeft2.setQuestionDetails(questionDetails)
        iconRight1 = new IconRight()
        iconRight1.setContent(ICON_1_CONTENT)
        iconRight1.setSequence(0)
        iconRight1.setQuestionDetails(questionDetails)
        iconRight2 = new IconRight()
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
    }

    @Unroll
    def "returns solved quiz with: quizType=#quizType | conclusionDate=#conclusionDate | resultsDate=#resultsDate"() {
        given: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quiz.setCourseExecution(externalCourseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)

        List<IconRight> rightIcons= new ArrayList<>()
        rightIcons.add(iconRight1)
        rightIcons.add(iconRight2)
        List<IconLeft> leftIcons = new ArrayList<>()
        leftIcons.add(iconLeft1)
        leftIcons.add(iconLeft2)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new ItemCombinationAnswer(questionAnswer, rightIcons, leftIcons)
        questionAnswer.setAnswerDetails(answerDetails);

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)

        when:
        def solvedQuizDtos = answerService.getSolvedQuizzes(user.getId(), courseDto.getCourseExecutionId())

        then: 'returns correct data'
        solvedQuizDtos.size() == 1
        def solvedQuizDto = solvedQuizDtos.get(0)
        def statementQuizDto = solvedQuizDto.getStatementQuiz()
        statementQuizDto.getQuestions().size() == 1
        solvedQuizDto.statementQuiz.getAnswers().size() == 1
        def answer = solvedQuizDto.statementQuiz.getAnswers().get(0)
        answer.getSequence() == 0
        answer.getAnswerDetails().getRightIconIds().containsKey(iconRight1.getId()) == true
        answer.getAnswerDetails().getRightIconIds().get(iconRight1.getId()) == iconRight1.getMatch()
        answer.getAnswerDetails().getRightIconIds().containsKey(iconRight2.getId()) == true
        answer.getAnswerDetails().getRightIconIds().get(iconRight2.getId()) == iconRight2.getMatch()
        answer.getAnswerDetails().getLeftIconIds().get(0) == iconLeft1.getId()
        answer.getAnswerDetails().getLeftIconIds().get(1) == iconLeft2.getId()
        solvedQuizDto.getCorrectAnswers().size() == 1
        def correct = solvedQuizDto.getCorrectAnswers().get(0)
        correct.getSequence() == 0
        correct.getCorrectAnswerDetails().getRightIconIds().containsKey(iconRight1.getId()) == true
        correct.getCorrectAnswerDetails().getRightIconIds().get(iconRight1.getId()) == iconRight1.getMatch()
        correct.getCorrectAnswerDetails().getRightIconIds().containsKey(iconRight2.getId()) == true
        correct.getCorrectAnswerDetails().getRightIconIds().get(iconRight2.getId()) == iconRight2.getMatch()
        correct.getCorrectAnswerDetails().getLeftIconIds().get(0) == iconLeft1.getId()
        correct.getCorrectAnswerDetails().getLeftIconIds().get(1) == iconLeft2.getId()

        where:
        quizType                 | conclusionDate    | resultsDate
        Quiz.QuizType.GENERATED  | null              | null
        Quiz.QuizType.PROPOSED   | null              | null
        Quiz.QuizType.IN_CLASS   | LOCAL_DATE_BEFORE | LOCAL_DATE_YESTERDAY
        Quiz.QuizType.IN_CLASS   | LOCAL_DATE_BEFORE | null
    }

    @Unroll
    def "does not return quiz with: quizType=#quizType | conclusionDate=#conclusionDate | resultsDate=#resultsDate"() {
        given: 'a quiz answered by the user'
        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(quizType.toString())
        quiz.setAvailableDate(LOCAL_DATE_BEFORE)
        quiz.setConclusionDate(conclusionDate)
        quiz.setResultsDate(resultsDate)
        quiz.setCourseExecution(externalCourseExecution)

        quizQuestion = new QuizQuestion()
        quizQuestion.setSequence(1)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)

        def quizAnswer = new QuizAnswer()
        quizAnswer.setAnswerDate(DateHandler.now())
        quizAnswer.setCompleted(true)
        quizAnswer.setUser(user)
        quizAnswer.setQuiz(quiz)

        List<IconRight> rightIcons= new ArrayList<>()
        rightIcons.add(iconRight1)
        rightIcons.add(iconRight2)
        List<IconLeft> leftIcons = new ArrayList<>()
        leftIcons.add(iconLeft1)
        leftIcons.add(iconLeft2)

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new ItemCombinationAnswer(questionAnswer, rightIcons, leftIcons)
        questionAnswer.setAnswerDetails(answerDetails);

        quizRepository.save(quiz)
        quizAnswerRepository.save(quizAnswer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answerDetails)


        when:
        def solvedQuizDtos = answerService.getSolvedQuizzes(user.getId(), courseDto.getCourseExecutionId())

        then: 'returns no quizzes'
        solvedQuizDtos.size() == 0

        where:
        quizType                | conclusionDate      | resultsDate
        Quiz.QuizType.IN_CLASS  | LOCAL_DATE_TOMORROW | LOCAL_DATE_LATER
        Quiz.QuizType.IN_CLASS  | LOCAL_DATE_TOMORROW | null
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
