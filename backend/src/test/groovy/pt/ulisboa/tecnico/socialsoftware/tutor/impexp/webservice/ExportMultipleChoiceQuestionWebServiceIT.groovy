package pt.ulisboa.tecnico.socialsoftware.tutor.impexp.webservice

import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExportMultipleChoiceQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)
    }

    def "teacher exports questions"() {
        given: "a teacher"
        def teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(teacher)
        userRepository.save(teacher)

        and: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(true)

        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)

        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.createQuestion(externalCourse.getId(), questionDto)

        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
                path: "/courses/" + externalCourse.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is OK"
        assert map['response'].status == 200

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "demo teacher exports questions"() {
        given: "a demo teacher"
        demoTeacherLogin()

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(true)

        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)

        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)

        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
                path: "/courses/" + courseService.getDemoCourse().getCourseId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is OK"
        assert map['response'].status == 200
    }

    def "demo admin cannot export questions"() {
        given: "a demo admin"
        demoAdminLogin()

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(true)

        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)

        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)

        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
                path: "/courses/" + courseService.getDemoCourse().getCourseId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is OK"
        assert map['response'].status == 403
    }

    def "student cannot export questions"() {
        given: "a student"
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(student)
        userRepository.save(student)

        and: "a student logged in"
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setIsToOrder(true)

        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(2)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)
        optionDto.setOrder(1)

        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.createQuestion(externalCourse.getId(), questionDto)

        and: 'prepare request response'
        restClient.handler.failure = { resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = { resp, reader ->
            [response:resp, reader:reader]
        }

        when: "the web service is invoked"
        def map = restClient.get(
                path: "/courses/" + externalCourse.getId() + "/questions/export",
                requestContentType: "application/json"
        )

        then: "the response status is OK"
        assert map['response'].status == 403

        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def cleanup() {
        persistentCourseCleanup()
    }

}
