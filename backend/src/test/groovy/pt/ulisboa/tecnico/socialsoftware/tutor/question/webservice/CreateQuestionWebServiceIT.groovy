package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpStatus
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import groovy.json.JsonOutput
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconLeftDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconRightDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)
    }

    def "Create an open answer question by a teacher" () {

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


        and: 'a question DTO'
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)


        when: 'the web service is invoked'
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/courses/' + course.getId() + "/questions/",
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json')

        then: "the request returns OK"
        response != null
        response.status == 200
        and: "if it responds with the correct question"
        def question = response.data

        question != null
        question.title == questionDto.getTitle()
        question.content == questionDto.getContent()

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())


    }

    def 'an admin cannot create an open answer question' () {
        given: 'an admin and a course'
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

        and: "a questionDto"
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        when: 'the admin tries to create a question'
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/courses/' + course.getId() + "/questions/",
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json')

        then: 'check that it returns an error'
        def error = thrown(HttpResponseException)
        error.response.getStatus() == HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(admin)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())

    }

    def "a student cannot create an open answer question" () {
        given: 'a student and a course'
        def course = new Course(COURSE_1_NAME, Course.Type.EXTERNAL)
        courseRepository.save(course)
        def courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, Course.Type.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        def student = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.STUDENT, false, AuthUser.Type.TECNICO)
        student.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        student.addCourse(courseExecution)
        courseExecution.addUser(student)
        userRepository.save(student)

        createdUserLogin(USER_1_EMAIL,USER_1_PASSWORD)


        and: 'a questionDto'
        def questionDto = new QuestionDto()
        questionDto.setKey(1)
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        when: 'the student tries to create a question'
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/courses/' + course.getId() + "/questions/",
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json')

        then: 'check that it returns an HTTP forbidden (403) error'
        def error = thrown(HttpResponseException)
        error.response.getStatus() == HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())

    }

    def "teacher create multiple choice question with only one correct option and no image"() {
        given: 'a teacher'
        def teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourse.addCourseExecution(externalCourseExecution)
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

        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(false)

        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        def response = restClient.post(
                path: '/courses/' + externalCourse.getId() + '/questions/',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct questionCreation"
        def questionCreation = response.data
        questionCreation.id != null
        questionCreation.title == questionDto.getTitle()
        questionCreation.content == questionDto.getContent()
        questionCreation.status == questionDto.getStatus()
        questionCreation.questionDetailsDto.isToOrder == false
        questionCreation.questionDetailsDto.options.get(0).content == OPTION_1_CONTENT
        questionCreation.questionDetailsDto.options.get(0).correct == true
        questionCreation.questionDetailsDto.options.get(1).content == OPTION_2_CONTENT
        questionCreation.questionDetailsDto.options.get(1).correct == false

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "teacher create multiple choice question with two correct options and no image"() {
        given: 'a teacher'
        def teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourse.addCourseExecution(externalCourseExecution)
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

        and: 'a optionId'
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)

        def options = new ArrayList<OptionDto>()
        options.add(optionDto)

        optionDto = new OptionDto()
        optionDto.setContent(OPTION_2_CONTENT)
        optionDto.setCorrect(true)

        options.add(optionDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        when:
        def response = restClient.post(
                path: '/courses/' + externalCourse.getId() + '/questions/',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct questionCreation"
        def questionCreation = response.data
        questionCreation.id != null
        questionCreation.title == questionDto.getTitle()
        questionCreation.content == questionDto.getContent()
        questionCreation.status == questionDto.getStatus()
        questionCreation.questionDetailsDto.isToOrder == false
        questionCreation.questionDetailsDto.options.get(0).content == OPTION_1_CONTENT
        questionCreation.questionDetailsDto.options.get(0).correct == true
        questionCreation.questionDetailsDto.options.get(1).content == OPTION_2_CONTENT
        questionCreation.questionDetailsDto.options.get(1).correct == true

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "teacher create multiple option order question with no image"() {
        given: 'a teacher'
        def teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourse.addCourseExecution(externalCourseExecution)
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

        when:
        def response = restClient.post(
                path: '/courses/' + externalCourse.getId() + '/questions/',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct questionCreation"
        def questionCreation = response.data
        questionCreation.id != null
        questionCreation.title == questionDto.getTitle()
        questionCreation.content == questionDto.getContent()
        questionCreation.status == questionDto.getStatus()
        questionCreation.questionDetailsDto.isToOrder == true
        questionCreation.questionDetailsDto.options.get(0).content == OPTION_1_CONTENT
        questionCreation.questionDetailsDto.options.get(0).correct == true
        questionCreation.questionDetailsDto.options.get(0).order == 2
        questionCreation.questionDetailsDto.options.get(1).content == OPTION_2_CONTENT
        questionCreation.questionDetailsDto.options.get(1).correct == true
        questionCreation.questionDetailsDto.options.get(1).order == 1

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "demo teacher create multiple option order question with no image"() {
        given: 'a demo teacher'
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

        when:
        def response = restClient.post(
                path: '/courses/' + courseService.getDemoCourse().getCourseId() + '/questions/',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct questionCreation"
        def questionCreation = response.data
        questionCreation.id != null
        questionCreation.title == questionDto.getTitle()
        questionCreation.content == questionDto.getContent()
        questionCreation.status == questionDto.getStatus()
        questionCreation.questionDetailsDto.isToOrder == true
        questionCreation.questionDetailsDto.options.get(0).content == OPTION_1_CONTENT
        questionCreation.questionDetailsDto.options.get(0).correct == true
        questionCreation.questionDetailsDto.options.get(0).order == 2
        questionCreation.questionDetailsDto.options.get(1).content == OPTION_2_CONTENT
        questionCreation.questionDetailsDto.options.get(1).correct == true
        questionCreation.questionDetailsDto.options.get(1).order == 1
    }

    def  "student cannot create multiple option order question with no image"() {
        given: 'a student'
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

        when:
        restClient.post(
                path: '/courses/' + externalCourse.getId() + '/questions/',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def  "demo admin cannot create multiple option order question with no image"() {
        given: 'a admin'
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

        when:
        restClient.post(
                path: '/courses/' + courseService.getDemoCourse().getCourseId() + '/questions/',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN
    }
  
  def "teacher create an item combination question with one left icon and one right icon and no image" () {
        given: 'a teacher'
        def teacher = new User(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL,
                User.Role.TEACHER, false, AuthUser.Type.TECNICO)
        teacher.authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        teacher.addCourse(externalCourseExecution)
        externalCourse.addCourseExecution(externalCourseExecution)
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

        when:
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/courses/' + externalCourse.getId() + '/questions/',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct question"
        def questionCreate = response.data
        questionCreate.id != null
        questionCreate.title == questionDto.getTitle()
        questionCreate.content == questionDto.getContent()
        questionCreate.status == questionDto.getStatus()
        questionCreate.questionDetailsDto.rightIcons.get(0).content == ICON_1_CONTENT
        questionCreate.questionDetailsDto.rightIcons.get(0).sequence == 0
        questionCreate.questionDetailsDto.rightIcons.get(0).match.get(0) == 0
        questionCreate.questionDetailsDto.leftIcons.get(0).content == ICON_1_CONTENT
        questionCreate.questionDetailsDto.leftIcons.get(0).sequence == 0

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())

    }

    def "demo teacher create an item combination question with one left icon and one right icon and no image" () {
        given: 'a demo teacher'
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

        when:
        def mapper = new ObjectMapper()
        def response = restClient.post(
                path: '/courses/' + courseService.getDemoCourse().getCourseId() + '/questions/',
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct question"
        def questionCreate = response.data
        questionCreate.id != null
        questionCreate.title == questionDto.getTitle()
        questionCreate.content == questionDto.getContent()
        questionCreate.status == questionDto.getStatus()
        questionCreate.questionDetailsDto.rightIcons.get(0).content == ICON_1_CONTENT
        questionCreate.questionDetailsDto.rightIcons.get(0).sequence == 0
        questionCreate.questionDetailsDto.rightIcons.get(0).match.get(0) == 0
        questionCreate.questionDetailsDto.leftIcons.get(0).content == ICON_1_CONTENT
        questionCreate.questionDetailsDto.leftIcons.get(0).sequence == 0
    }

    def "student cannot create item combination question with one let icon and one right icon and no image"() {

        given: 'a student'
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

        when:
        restClient.post(
                path: '/courses/' + externalCourse.getId() + '/questions/',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "demo admin cannot create item combination question with one let icon and one right icon and no image"() {

        given: 'a admin'
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

        when:
        restClient.post(
                path: '/courses/' + courseService.getDemoCourse().getCourseId() + '/questions/',
                body: JsonOutput.toJson(questionDto),
                requestContentType: 'application/json'
        )

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN

    }

    def cleanup() {
        persistentCourseCleanup()
    }
}