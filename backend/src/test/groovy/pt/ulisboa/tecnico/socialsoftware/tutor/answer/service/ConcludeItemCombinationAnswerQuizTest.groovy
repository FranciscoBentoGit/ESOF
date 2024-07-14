package pt.ulisboa.tecnico.socialsoftware.tutor.answer.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.ItemCombinationAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuizAnswer
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconLeft
import pt.ulisboa.tecnico.socialsoftware.tutor.utils.DateHandler
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.ItemCombinationQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.IconRight
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.QuizQuestion
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.ItemCombinationStatementAnswerDetailsDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementAnswerDto
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.dto.StatementQuizDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NOT_YET_AVAILABLE
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.QUIZ_NO_LONGER_AVAILABLE

@DataJpaTest
class ConcludeItemCombinationAnswerQuizTest extends SpockTest {

    def user
    def quizQuestion
    def iconLeft1
    def iconLeft2
    def iconRight1
    def iconRight2
    def quizAnswer
    def date
    def quiz

    def setup() {
        user = new User(USER_1_NAME, USER_1_USERNAME, USER_1_EMAIL, User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        user.addCourse(externalCourseExecution)
        userRepository.save(user)

        quiz = new Quiz()
        quiz.setKey(1)
        quiz.setTitle("Quiz Title")
        quiz.setType(Quiz.QuizType.PROPOSED.toString())
        quiz.setCourseExecution(externalCourseExecution)
        quiz.setAvailableDate(DateHandler.now())
        quizRepository.save(quiz)

        def question = new Question()
        question.setKey(1)
        question.setTitle("Question Title")
        question.setCourse(externalCourse)
        def questionDetails = new ItemCombinationQuestion()
        question.setQuestionDetails(questionDetails)
        questionDetailsRepository.save(questionDetails)
        questionRepository.save(question)

        quizQuestion = new QuizQuestion(quiz, question, 0)
        quizQuestionRepository.save(quizQuestion)

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

        date = DateHandler.now()

        quizAnswer = new QuizAnswer(user, quiz)
        quizAnswerRepository.save(quizAnswer)
    }

    def 'conclude quiz without conclusionDate, without answering'() {
        given: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        quizAnswer.getAnswerDate() != null
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getAnswerDetails() == null
        and: 'the return value is OK'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0
        correctAnswerDto.getCorrectAnswerDetails().getRightIconIds().keySet().getAt(0) == iconRight1.getId()
        correctAnswerDto.getCorrectAnswerDetails().getRightIconIds().keySet().getAt(1) == iconRight2.getId()
    }

    def 'conclude quiz IN_CLASS without answering, before conclusionDate'() {
        given: 'an IN_CLASS quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))
        quiz.setType(Quiz.QuizType.IN_CLASS.toString())
        and: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        quizAnswer.getAnswerDate() == null
        quizAnswerItemRepository.findAll().size() == 1
        def quizAnswerItem = quizAnswerItemRepository.findAll().get(0)
        quizAnswerItem.getQuizId() == quiz.getId()
        quizAnswerItem.getQuizAnswerId() == quizAnswer.getId()
        quizAnswerItem.getAnswerDate() != null
        quizAnswerItem.getAnswersList().size() == 1
        def resStatementAnswerDto = quizAnswerItem.getAnswersList().get(0)
        resStatementAnswerDto.getAnswerDetails() == null
        resStatementAnswerDto.getSequence() == 0
        resStatementAnswerDto.getTimeTaken() == 100
        and: 'does not return answers'
        correctAnswers == []
    }

    def 'conclude quiz with answer, before conclusionDate'() {
        given: 'a quiz with future conclusionDate'
        quiz.setConclusionDate(DateHandler.now().plusDays(2))
        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()
        List<Integer> leftIds = new ArrayList<>()
        leftIds.add(iconLeft1.getId())
        leftIds.add(iconLeft2.getId())
        Map<Integer, List<Integer>> rightIds = new HashMap<>()
        rightIds.put(iconRight1.getId(), iconRight1.getMatch())
        rightIds.put(iconRight2.getId(), iconRight2.getMatch())
        itemCombinationAnswerDto.setLeftIconIds(leftIds)
        itemCombinationAnswerDto.setRightIconIds(rightIds)

        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        ((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getRightAnswer().get(0) == iconRight1
        ((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getRightAnswer().get(1) == iconRight2
        ((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getLeftAnswer().get(0) == iconLeft1
        ((ItemCombinationAnswer) questionAnswer.getAnswerDetails()).getLeftAnswer().get(1) == iconLeft2


        iconRight1.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        iconRight2.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        iconLeft1.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        iconLeft2.getQuestionAnswers().contains(questionAnswer.getAnswerDetails())
        and: 'the return value is OK'
        correctAnswers.size() == 1
        def correctAnswerDto = correctAnswers.get(0)
        correctAnswerDto.getSequence() == 0
        correctAnswerDto.getCorrectAnswerDetails().getRightIconIds().keySet().getAt(0) == iconRight1.getId()
        correctAnswerDto.getCorrectAnswerDetails().getRightIconIds().keySet().getAt(1) == iconRight2.getId()
    }

    def 'conclude quiz without answering, before availableDate'() {
        given: 'a quiz with future availableDate'
        quiz.setAvailableDate(DateHandler.now().plusDays(2))
        and: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        when:
        answerService.concludeQuiz(statementQuizDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == QUIZ_NOT_YET_AVAILABLE
    }

    def 'conclude quiz without answering, after conclusionDate'() {
        given: 'an IN_CLASS quiz with conclusionDate before now in days'
        quiz.setType(Quiz.QuizType.IN_CLASS.toString())
        quiz.setAvailableDate(DateHandler.now().minusDays(2))
        quiz.setConclusionDate(DateHandler.now().minusDays(1))
        and: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        when:
        answerService.concludeQuiz(statementQuizDto)

        then:
        TutorException exception = thrown()
        exception.getErrorMessage() == QUIZ_NO_LONGER_AVAILABLE
    }

    def 'conclude quiz without answering, 9 minutes after conclusionDate'() {
        given: 'an IN_CLASS quiz with conclusionDate before now in days'
        quiz.setType(Quiz.QuizType.IN_CLASS.toString())
        quiz.setAvailableDate(DateHandler.now().minusDays(2))
        quiz.setConclusionDate(DateHandler.now().minusMinutes(9))
        and: 'an empty answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()

        when:
        answerService.concludeQuiz(statementQuizDto)

        then: 'the value is createQuestion and persistent'
        quizAnswer.isCompleted()
        quizAnswer.getAnswerDate() == null
        quizAnswerItemRepository.findAll().size() == 1
    }

    def 'conclude completed quiz'() {
        given:  'a completed quiz'
        quizAnswer.completed = true
        and: 'an answer'
        def statementQuizDto = new StatementQuizDto()
        statementQuizDto.id = quiz.getId()
        statementQuizDto.quizAnswerId = quizAnswer.getId()
        def statementAnswerDto = new StatementAnswerDto()
        def itemCombinationAnswerDto = new ItemCombinationStatementAnswerDetailsDto()
        List<Integer> leftIds = new ArrayList<>()
        leftIds.add(iconLeft1.getId())
        leftIds.add(iconLeft2.getId())
        Map<Integer, List<Integer>> rightIds = new HashMap<>()
        rightIds.put(iconRight1.getId(), iconRight1.getMatch())
        rightIds.put(iconRight2.getId(), iconRight2.getMatch())
        itemCombinationAnswerDto.setLeftIconIds(leftIds)
        itemCombinationAnswerDto.setRightIconIds(rightIds)
        statementAnswerDto.setAnswerDetails(itemCombinationAnswerDto)
        statementAnswerDto.setSequence(0)
        statementAnswerDto.setTimeTaken(100)
        statementAnswerDto.setQuestionAnswerId(quizAnswer.getQuestionAnswers().get(0).getId())
        statementQuizDto.getAnswers().add(statementAnswerDto)

        when:
        def correctAnswers = answerService.concludeQuiz(statementQuizDto)

        then: 'nothing occurs'
        quizAnswer.getAnswerDate() == null
        questionAnswerRepository.findAll().size() == 1
        def questionAnswer = questionAnswerRepository.findAll().get(0)
        questionAnswer.getQuizAnswer() == quizAnswer
        quizAnswer.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getQuizQuestion() == quizQuestion
        quizQuestion.getQuestionAnswers().contains(questionAnswer)
        questionAnswer.getAnswerDetails() == null
        and: 'the return value is OK'
        correctAnswers.size() == 0
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}