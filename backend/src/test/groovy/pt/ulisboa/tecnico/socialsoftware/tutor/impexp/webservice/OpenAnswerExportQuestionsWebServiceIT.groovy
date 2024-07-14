package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenAnswerExportQuestionsWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def admin
    def courseExecution
    def student



    def setup() {
        given: "a rest client"
        restClient = new RESTClient("http://localhost:" + port)

        and: 'a course execution'
        course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)
    }

    def "export questions by a teacher" () {
        given: 'a teacher logged in'
        demoTeacherLogin()

        and: 'an open answer question already in the repository'
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)

        and: 'a prepared request response'
        restClient.handler.failure = {resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = {resp, reader ->
            [response:resp, reader:reader]
        }

        when: 'the teacher invokes the service'
        def map = restClient.get(
                path: "/courses/" + courseService.getDemoCourse().getCourseId() + "/questions/export",
                requestContentType: "application/json")

        then: "the web service returns OK"
        assert map['response'].status == 200
    }

    def "Export questions by an admin" () {
        given: 'an admin logged in'
        demoAdminLogin()

        and: 'a prepared request response'
        restClient.handler.failure = {resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = {resp, reader ->
            [response:resp, reader:reader]
        }

        and: 'an open answer question already in the repository'
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)

        when: 'the web service is invoked'
        def map = restClient.get(
                path: "/courses/" + courseService.getDemoCourse().getCourseId() + "/questions/export",
                requestContentType: "application/json")

        then: "the request returns forbidden (403)"
        assert map['response'].status == 403

    }

    def "Export questions by a student" () {
        given: 'a student logged in'
        student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(externalCourseExecution)
        externalCourseExecution.addUser(student)
        userRepository.save(student)

        and: 'an open answer question already in the repository'
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        questionService.createQuestion(course.getId(), questionDto)

        and: "a student logged in"
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: 'a prepared request response'
        restClient.handler.failure = {resp, reader ->
            [response:resp, reader:reader]
        }
        restClient.handler.success = {resp, reader ->
            [response:resp, reader:reader]
        }

        when: 'the web service is invoked'
        def map = restClient.get(
                path: "/courses/" + courseService.getDemoCourse().getCourseId() + "/questions/export",
                requestContentType: "application/json")

        then: "the request returns forbidden (403)"
        assert map['response'].status == 403

        cleanup:
        userRepository.delete(student)
    }




    def cleanup() {
        persistentCourseCleanup()
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }
}