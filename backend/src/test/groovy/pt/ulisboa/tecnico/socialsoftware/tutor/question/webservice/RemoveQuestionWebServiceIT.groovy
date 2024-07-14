package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.apache.http.HttpStatus
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconLeftDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconRightDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RemoveQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)
    }

    def "Remove an open answer question by a teacher" () {
        given: 'a teacher and course'
        def course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        def courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        def teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)

        and: 'an open answer question already in the repository'
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        questionService.createQuestion(course.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        when: 'the question is deleted'
        def response = restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "the request returns OK"
        response != null
        response.status == 200

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())

    }

    def 'an admin tries to remove an open answer question' () {
        given: 'an admin and course'
        def course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        def courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        def admin = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.ADMIN, true, AuthUser.Type.TECNICO)
        admin.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        admin.addCourse(courseExecution)
        courseExecution.addUser(admin)
        userRepository.save(admin)

        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)

        and: 'an open answer question already in the repository'
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        questionService.createQuestion(course.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        when: 'the admin tries to delete the question'
        def response = restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: 'the request returns a forbidden (403) error'
        def error = thrown(HttpResponseException)
        error.response.getStatus() == HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(admin)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }

    def 'a student tries to remove an open answer question' () {
        given: 'a student and course'
        def course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        def courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        def student = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, true, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)

        and: 'an open answer question already in the repository'
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        questionService.createQuestion(course.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        when: 'the admin tries to delete the question'
        def response = restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: 'the request returns a forbidden (403) error'
        def error = thrown(HttpResponseException)
        error.response.getStatus() == HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())
    }

    def "teacher removes a multiple option order question with no image"() {
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
        def question = questionRepository.findAll().get(0)

        when: "question is deleted"
        def response = restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "check the response status"
        response != null
        response.status == 200

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "demo teacher removes a multiple option order question with no image"() {
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
        def question = questionRepository.findAll().get(0)

        when: "question is deleted"
        def response = restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "check the response status"
        response != null
        response.status == 200
    }

    def "student cannot remove a multiple option order question with no image"() {
        given: "a student"
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(externalCourseExecution)
        externalCourse.addCourseExecution(externalCourseExecution)
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
        def question = questionRepository.findAll().get(0)

        when: "question is deleted"
        restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "demo admin cannot remove a multiple option order question with no image"() {
        given: "an admin"
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
        def question = questionRepository.findAll().get(0)

        when: "question is deleted"
        restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN
    }
  
  def "remove a question with one left icon and one right icon and no image by a teacher"() {
        given: "a course execution and a course"
        def course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        def courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        and: "a teacher"
        def teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(courseExecution)
        courseExecution.addUser(teacher)
        userRepository.save(teacher)

        and: "a teacher logged in"
        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "iconDtos"
        def iconLeftDto = new IconLeftDto()
        iconLeftDto.setContent(ICON_1_CONTENT)
        iconLeftDto.setSequence(0)

        def iconRightDto = new IconRightDto()
        iconRightDto.setContent(ICON_1_CONTENT)
        iconRightDto.setSequence(0)

        def matches = new ArrayList<Integer>()
        matches.add(iconLeftDto.getSequence())

        iconRightDto.setMatch(matches)

        def rightIcons = new ArrayList<IconRightDto>()
        def leftIcons = new ArrayList<IconLeftDto>()

        rightIcons.add(iconRightDto)
        leftIcons.add(iconLeftDto)

        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)
        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)

        def question = questionService.createQuestion(course.getId(), questionDto)

        when: "question deleted"
        def response = restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "and request is OK"

        response != null
        response.status == 200
    }

    def "remove a question with one left icon and one right icon and no image by a demo teacher"() {
        given: "a demo teacher"
        demoTeacherLogin()


        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "iconDtos"
        def iconLeftDto = new IconLeftDto()
        iconLeftDto.setContent(ICON_1_CONTENT)
        iconLeftDto.setSequence(0)

        def iconRightDto = new IconRightDto()
        iconRightDto.setContent(ICON_1_CONTENT)
        iconRightDto.setSequence(0)

        def matches = new ArrayList<Integer>()
        matches.add(iconLeftDto.getSequence())

        iconRightDto.setMatch(matches)

        def rightIcons = new ArrayList<IconRightDto>()
        def leftIcons = new ArrayList<IconLeftDto>()

        rightIcons.add(iconRightDto)
        leftIcons.add(iconLeftDto)

        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)
        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)

        questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)
        def question = questionRepository.findAll().get(0)

        when: "question deleted"
        def response = restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "and request is OK"

        response != null
        response.status == 200
    }

    def "remove a question with one left icon and one right icon and no image by a student"() {
        given: "a course execution and a course"
        def course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        def courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        and: "a student"
        def student = new User(USER_2_NAME, USER_2_EMAIL, USER_2_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_2_PASSWORD))
        student.addCourse(externalCourseExecution)
        externalCourse.addCourseExecution(externalCourseExecution)
        userRepository.save(student)

        and: "a student logged in"
        createdUserLogin(USER_2_EMAIL, USER_2_PASSWORD)

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "iconDtos"
        def iconLeftDto = new IconLeftDto()
        iconLeftDto.setContent(ICON_1_CONTENT)
        iconLeftDto.setSequence(0)

        def iconRightDto = new IconRightDto()
        iconRightDto.setContent(ICON_1_CONTENT)
        iconRightDto.setSequence(0)

        def matches = new ArrayList<Integer>()
        matches.add(iconLeftDto.getSequence())

        iconRightDto.setMatch(matches)

        def rightIcons = new ArrayList<IconRightDto>()
        def leftIcons = new ArrayList<IconLeftDto>()

        rightIcons.add(iconRightDto)
        leftIcons.add(iconLeftDto)

        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)
        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)

        questionService.createQuestion(course.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        when: "question deleted"
        restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "check the response status"

        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "remove a question with one left icon and one right icon and no image by a demo admin"() {
        given: "a course execution and a course"
        def course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        def courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        and: "an admin"
        demoAdminLogin()

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "iconDtos"
        def iconLeftDto = new IconLeftDto()
        iconLeftDto.setContent(ICON_1_CONTENT)
        iconLeftDto.setSequence(0)

        def iconRightDto = new IconRightDto()
        iconRightDto.setContent(ICON_1_CONTENT)
        iconRightDto.setSequence(0)

        def matches = new ArrayList<Integer>()
        matches.add(iconLeftDto.getSequence())

        iconRightDto.setMatch(matches)

        def rightIcons = new ArrayList<IconRightDto>()
        def leftIcons = new ArrayList<IconLeftDto>()

        rightIcons.add(iconRightDto)
        leftIcons.add(iconLeftDto)

        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)
        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)

        questionService.createQuestion(course.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        when: "question deleted"
        restClient.delete(
                path: "/questions/" + question.getId(),
                requestContentType: 'application/json')

        then: "check the response status"

        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN
    }

    def cleanup() {
        persistentCourseCleanup()
    }

}