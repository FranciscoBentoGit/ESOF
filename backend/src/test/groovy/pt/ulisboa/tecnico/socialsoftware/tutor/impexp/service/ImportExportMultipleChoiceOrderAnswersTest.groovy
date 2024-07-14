package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.MultipleChoiceQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Option
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ImportExportMultipleChoiceOrderAnswersTest extends SpockTest {
    def quizAnswer
    def questionAnswer
    def optionOrder1
    def optionOrder2
    def optionOrder3

    def setup() {
        Question question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
        def questionDetails = new MultipleChoiceQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

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

        Quiz quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle(QUIZ_TITLE)
        quiz.setType(Quiz.QuizType.GENERATED.toString())
        quiz.setCourseExecution(externalCourseExecution)

        quiz.setCreationDate(DateHandler.now())
        quiz.setAvailableDate(DateHandler.now())
        quiz.setConclusionDate(DateHandler.now())
        quiz.setType(Quiz.QuizType.EXAM.toString())
        quizRepository.save(quiz)

        QuizQuestion quizQuestion= new QuizQuestion()
        quizQuestion.setSequence(0)
        quizQuestion.setQuiz(quiz)
        quizQuestion.setQuestion(question)
        quizQuestionRepository.save(quizQuestion)

        User user = userService.createUserWithAuth(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, AuthUser.Type.EXTERNAL).getUser()

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswer.setAnswerDate(LOCAL_DATE_TODAY)
        quizAnswer.setCompleted(true)
        quizAnswerRepository.save(quizAnswer)

        questionAnswer = new QuestionAnswer(quizAnswer, quizQuestion, 1, 0)
        def answer = new MultipleChoiceAnswer(questionAnswer, [optionOrder2,optionOrder3])
        questionAnswer.setAnswerDetails(answer)
        questionAnswerRepository.save(questionAnswer)
        answerDetailsRepository.save(answer)
    }

    def 'export and import answers'() {
        given: 'a xml with a quiz'
        def answersXml = answerService.exportAnswers()
        print(answersXml)
        and: 'delete answers'
        answerService.deleteQuizAnswer(quizAnswer)

        when:
        answerService.importAnswers(answersXml)

        then:
        quizAnswerRepository.findAll().size() == 1
        def quizAnswerResult = quizAnswerRepository.findAll().get(0)
        quizAnswerResult.getAnswerDate() == LOCAL_DATE_TODAY
        quizAnswerResult.isCompleted()
        quizAnswerResult.getUser().getName() == USER_1_NAME
        quizAnswerResult.getUser().getUsername() == USER_1_USERNAME
        quizAnswerResult.getQuiz().getKey() == 1
        questionAnswerRepository.findAll().size() == 1
        def questionAnswerResult = questionAnswerRepository.findAll().get(0)
        questionAnswerResult.getTimeTaken() == 1
        ((MultipleChoiceAnswer) questionAnswerResult.getAnswerDetails()).getOptions().get(0) == optionOrder2
        ((MultipleChoiceAnswer) questionAnswerResult.getAnswerDetails()).getOptions().get(1) == optionOrder3
        optionOrder2.getQuestionAnswers().contains(questionAnswerResult.getAnswerDetails())
        optionOrder3.getQuestionAnswers().contains(questionAnswerResult.getAnswerDetails())
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
