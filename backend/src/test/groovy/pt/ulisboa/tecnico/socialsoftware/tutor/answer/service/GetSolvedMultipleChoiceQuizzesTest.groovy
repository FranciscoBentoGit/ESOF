package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.dto.CourseExecutionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import spock.lang.Unroll

@DataJpaTest
class GetSolvedMultipleChoiceQuizzesTest extends SpockTest {
    def user
    def courseDto
    def question
    def optionOrder1
    def optionOrder2
    def optionOrder3
    def quiz
    def quizQuestion

    def setup() {
        courseDto = new CourseExecutionDto(externalCourseExecution)

        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        question = new Question()
        question.setKey(1)
        question.setCourse(externalCourse)
        question.setContent("Question Content")
        question.setTitle("Question Title")
        questionRepository.save(question)

        def questionDetails = new MultipleChoiceQuestion();
        question.setQuestionDetails(questionDetails);
        questionDetailsRepository.save(questionDetails)

        optionOrder1 = new Option()
        optionOrder1.setContent("Option 1 Content")
        optionOrder1.setCorrect(false)
        optionOrder1.setSequence(0)
        optionOrder1.setOrder(null)
        optionOrder1.setQuestionDetails(questionDetails)
        optionRepository.save(optionOrder1)

        optionOrder2 = new Option()
        optionOrder2.setContent("Option 2 Content")
        optionOrder2.setCorrect(true)
        optionOrder2.setSequence(1)
        optionOrder2.setOrder(2)
        optionOrder2.setQuestionDetails(questionDetails)
        optionRepository.save(optionOrder2)

        optionOrder3 = new Option()
        optionOrder3.setContent("Option 3 Content")
        optionOrder3.setCorrect(true)
        optionOrder3.setSequence(2)
        optionOrder3.setOrder(1)
        optionOrder3.setQuestionDetails(questionDetails)
        optionRepository.save(optionOrder3)
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

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, [optionOrder2,optionOrder3])
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
        answer.getAnswerDetails().getOptionsIds().containsKey(optionOrder1.getId()) == false
        answer.getAnswerDetails().getOptionsIds().containsKey(optionOrder2.getId()) == true
        answer.getAnswerDetails().getOptionsIds().get(optionOrder2.getId()) == optionOrder2.getOrder()
        answer.getAnswerDetails().getOptionsIds().containsKey(optionOrder3.getId()) == true
        answer.getAnswerDetails().getOptionsIds().get(optionOrder3.getId()) == optionOrder3.getOrder()
        solvedQuizDto.getCorrectAnswers().size() == 1
        def correct = solvedQuizDto.getCorrectAnswers().get(0)
        correct.getSequence() == 0
        correct.getCorrectAnswerDetails().getCorrectOptionId().keySet().getAt(0) == optionOrder2.getId()
        correct.getCorrectAnswerDetails().getCorrectOptionId().get(optionOrder2.getId()) == optionOrder2.getOrder()
        correct.getCorrectAnswerDetails().getCorrectOptionId().keySet().getAt(1) == optionOrder3.getId()
        correct.getCorrectAnswerDetails().getCorrectOptionId().get(optionOrder3.getId()) == optionOrder3.getOrder()

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

        def questionAnswer = new QuestionAnswer()
        questionAnswer.setSequence(0)
        questionAnswer.setQuizAnswer(quizAnswer)
        questionAnswer.setQuizQuestion(quizQuestion)
        def answerDetails = new MultipleChoiceAnswer(questionAnswer, [optionOrder2,optionOrder3]);
        questionAnswer.setAnswerDetails(answerDetails)

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
