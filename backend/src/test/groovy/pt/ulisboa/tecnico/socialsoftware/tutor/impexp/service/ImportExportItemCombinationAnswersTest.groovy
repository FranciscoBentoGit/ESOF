package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.MultipleChoiceAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ImportExportItemCombinationAnswersTest extends SpockTest {
    def quizAnswer
    def questionAnswer
    def iconLeft1
    def iconLeft2
    def iconRight1
    def iconRight2

    def setup() {
        Question question = new Question()
        question.setCourse(externalCourse)
        question.setKey(1)
        question.setTitle(QUESTION_1_TITLE)
        question.setContent(QUESTION_1_CONTENT)
        question.setStatus(Question.Status.AVAILABLE)
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

        List<IconRight> rightIcons= new ArrayList<>()
        rightIcons.add(iconRight1)
        rightIcons.add(iconRight2)
        List<IconLeft> leftIcons = new ArrayList<>()
        leftIcons.add(iconLeft1)
        leftIcons.add(iconLeft2)

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

        def answer = new ItemCombinationAnswer(questionAnswer, rightIcons, leftIcons)
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
        ((ItemCombinationAnswer) questionAnswerResult.getAnswerDetails()).getRightAnswer().get(0) == iconRight1
        ((ItemCombinationAnswer) questionAnswerResult.getAnswerDetails()).getRightAnswer().get(1) == iconRight2
        ((ItemCombinationAnswer) questionAnswerResult.getAnswerDetails()).getLeftAnswer().get(0) == iconLeft1
        ((ItemCombinationAnswer) questionAnswerResult.getAnswerDetails()).getLeftAnswer().get(1) == iconLeft2


        iconRight1.getQuestionAnswers().contains(questionAnswerResult.getAnswerDetails())
        iconRight2.getQuestionAnswers().contains(questionAnswerResult.getAnswerDetails())
        iconLeft1.getQuestionAnswers().contains(questionAnswerResult.getAnswerDetails())
        iconLeft2.getQuestionAnswers().contains(questionAnswerResult.getAnswerDetails())
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
