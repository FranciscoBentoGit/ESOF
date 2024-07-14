package pt.ulisboa.tecnico.socialsoftware.tutor.question.webservice

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonOutput
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
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OpenAnswerQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.ItemCombinationQuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconLeftDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.IconRightDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.OptionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UpdateQuestionWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def response

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)
    }

    def "update an open answer question by a teacher" () {
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

        and: 'an alteration to the previously created question'
        def questionUpdateDto = new QuestionDto()
        questionUpdateDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setStatus(Question.Status.AVAILABLE.name())
        questionUpdateDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        when: 'the web service is invoked'
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: "/questions/" + question.getId(),
                body: mapper.writeValueAsString(questionUpdateDto),
                requestContentType: 'application/json')

        then: "the request returns OK"
        response != null
        response.status == 200
        and: "if it responds with the correct question"
        def questionResponse = response.data

        questionResponse != null
        questionResponse.title == QUESTION_2_TITLE
        questionResponse.content == QUESTION_2_CONTENT

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())


    }

    def "admin tries to update an open answer question" () {
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

        and: 'an alteration to the previously created question'
        def questionUpdateDto = new QuestionDto()
        questionUpdateDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setStatus(Question.Status.AVAILABLE.name())
        questionUpdateDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        when: 'the admin tries to update the existing question'
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: "/questions/" + question.getId(),
                body: mapper.writeValueAsString(questionUpdateDto),
                requestContentType: 'application/json')

        then: 'the request returns a forbidden (403) error'
        def error = thrown(HttpResponseException)
        error.response.getStatus() == HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(admin)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())

    }

    def "student tries to update an open answer question" () {
        given: 'a student logged in'
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

        and: 'an open answer question already in the repository'
        def questionDto = new QuestionDto()
        questionDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.AVAILABLE.name())
        questionDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        questionService.createQuestion(course.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        and: 'an alteration to the previously created question'
        def questionUpdateDto = new QuestionDto()
        questionUpdateDto.setQuestionDetailsDto(new OpenAnswerQuestionDto())
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setStatus(Question.Status.AVAILABLE.name())
        questionUpdateDto.getQuestionDetailsDto().setResponse(RESPONSE_1)

        when: 'the student tries to update the existing question'
        def mapper = new ObjectMapper()
        response = restClient.put(
                path: "/questions/" + question.getId(),
                body: mapper.writeValueAsString(questionUpdateDto),
                requestContentType: 'application/json')

        then: 'the request returns a forbidden (403) error'
        def error = thrown(HttpResponseException)
        error.response.getStatus() == HttpStatus.SC_FORBIDDEN


        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(courseExecution.getId())
        courseRepository.deleteById(course.getId())

    }

    def "teacher update a multiple option order question with no image"() {
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
        def optionOneDto = new OptionDto()
        optionOneDto.setContent(OPTION_1_CONTENT)
        optionOneDto.setCorrect(true)
        optionOneDto.setOrder(2)

        def options = new ArrayList<OptionDto>()
        options.add(optionOneDto)

        def optionTwoDto = new OptionDto()
        optionTwoDto.setContent(OPTION_2_CONTENT)
        optionTwoDto.setCorrect(true)
        optionTwoDto.setOrder(1)

        options.add(optionTwoDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.createQuestion(externalCourse.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        def optionOne = question.getQuestionDetails().getOptions().get(0)
        def optionTwo = question.getQuestionDetails().getOptions().get(1)

        and: "a changed question"
        def questionUpdateDto = questionDto
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionUpdateDto.getQuestionDetailsDto().setIsToOrder(true)

        and: '2 changed options'
        def optionsUpdate = new ArrayList<OptionDto>()
        def optionOneUpdateDto = new OptionDto(optionOne)
        optionOneUpdateDto.setContent(OPTION_2_CONTENT)
        optionOneUpdateDto.setOrder(2)
        optionsUpdate.add(optionOneUpdateDto)
        def optionTwoUpdateDto = new OptionDto(optionTwo)
        optionTwoUpdateDto.setContent(OPTION_1_CONTENT)
        optionTwoUpdateDto.setOrder(1)
        optionsUpdate.add(optionTwoUpdateDto)

        questionUpdateDto.getQuestionDetailsDto().setOptions(optionsUpdate)

        when: "question is updated"
        def response = restClient.put(
                path: "/questions/" + question.getId(),
                body: JsonOutput.toJson(questionUpdateDto),
                requestContentType: 'application/json')

        then: "check the response status"
        response != null
        response.status == 200

        def result = response.data
        result.id == question.getId()
        result.title == QUESTION_2_TITLE
        result.content == QUESTION_2_CONTENT
        result.status == Question.Status.AVAILABLE.name()
        result.questionDetailsDto.isToOrder == true
        result.questionDetailsDto.options.get(0).content == OPTION_2_CONTENT
        result.questionDetailsDto.options.get(0).correct == true
        result.questionDetailsDto.options.get(0).order == 2
        result.questionDetailsDto.options.get(1).content == OPTION_1_CONTENT
        result.questionDetailsDto.options.get(1).correct == true
        result.questionDetailsDto.options.get(1).order == 1

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "demo teacher update a multiple option order question with no image"() {
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
        def optionOneDto = new OptionDto()
        optionOneDto.setContent(OPTION_1_CONTENT)
        optionOneDto.setCorrect(true)
        optionOneDto.setOrder(2)

        def options = new ArrayList<OptionDto>()
        options.add(optionOneDto)

        def optionTwoDto = new OptionDto()
        optionTwoDto.setContent(OPTION_2_CONTENT)
        optionTwoDto.setCorrect(true)
        optionTwoDto.setOrder(1)

        options.add(optionTwoDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)
        def question = questionRepository.findAll().get(0)

        def optionOne = question.getQuestionDetails().getOptions().get(0)
        def optionTwo = question.getQuestionDetails().getOptions().get(1)

        and: "a changed question"
        def questionUpdateDto = questionDto
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionUpdateDto.getQuestionDetailsDto().setIsToOrder(true)

        and: '2 changed options'
        def optionsUpdate = new ArrayList<OptionDto>()
        def optionOneUpdateDto = new OptionDto(optionOne)
        optionOneUpdateDto.setContent(OPTION_2_CONTENT)
        optionOneUpdateDto.setOrder(2)
        optionsUpdate.add(optionOneUpdateDto)
        def optionTwoUpdateDto = new OptionDto(optionTwo)
        optionTwoUpdateDto.setContent(OPTION_1_CONTENT)
        optionTwoUpdateDto.setOrder(1)
        optionsUpdate.add(optionTwoUpdateDto)

        questionUpdateDto.getQuestionDetailsDto().setOptions(optionsUpdate)

        when: "question is updated"
        def response = restClient.put(
                path: "/questions/" + question.getId(),
                body: JsonOutput.toJson(questionUpdateDto),
                requestContentType: 'application/json')

        then: "check the response status"
        response != null
        response.status == 200

        def result = response.data
        result.id == question.getId()
        result.title == QUESTION_2_TITLE
        result.content == QUESTION_2_CONTENT
        result.status == Question.Status.AVAILABLE.name()
        result.questionDetailsDto.isToOrder == true
        result.questionDetailsDto.options.get(0).content == OPTION_2_CONTENT
        result.questionDetailsDto.options.get(0).correct == true
        result.questionDetailsDto.options.get(0).order == 2
        result.questionDetailsDto.options.get(1).content == OPTION_1_CONTENT
        result.questionDetailsDto.options.get(1).correct == true
        result.questionDetailsDto.options.get(1).order == 1
    }

    def "student cannot update a multiple option order question with no image"() {
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
        def optionOneDto = new OptionDto()
        optionOneDto.setContent(OPTION_1_CONTENT)
        optionOneDto.setCorrect(true)
        optionOneDto.setOrder(2)

        def options = new ArrayList<OptionDto>()
        options.add(optionOneDto)

        def optionTwoDto = new OptionDto()
        optionTwoDto.setContent(OPTION_2_CONTENT)
        optionTwoDto.setCorrect(true)
        optionTwoDto.setOrder(1)

        options.add(optionTwoDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.createQuestion(externalCourse.getId(), questionDto)
        def question = questionRepository.findAll().get(0)

        def optionOne = question.getQuestionDetails().getOptions().get(0)
        def optionTwo = question.getQuestionDetails().getOptions().get(1)

        and: "a changed question"
        def questionUpdateDto = questionDto
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionUpdateDto.getQuestionDetailsDto().setIsToOrder(true)

        and: '2 changed options'
        def optionsUpdate = new ArrayList<OptionDto>()
        def optionOneUpdateDto = new OptionDto(optionOne)
        optionOneUpdateDto.setContent(OPTION_2_CONTENT)
        optionOneUpdateDto.setOrder(2)
        optionsUpdate.add(optionOneUpdateDto)
        def optionTwoUpdateDto = new OptionDto(optionTwo)
        optionTwoUpdateDto.setContent(OPTION_1_CONTENT)
        optionTwoUpdateDto.setOrder(1)
        optionsUpdate.add(optionTwoUpdateDto)

        questionUpdateDto.getQuestionDetailsDto().setOptions(optionsUpdate)

        when: "question is updated"
        response = restClient.put(
                path: "/questions/" + question.getId(),
                body: JsonOutput.toJson(questionUpdateDto),
                requestContentType: 'application/json')

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN

        cleanup:
        userRepository.delete(student)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "demo admin cannot update a multiple option order question with no image"() {
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
        def optionOneDto = new OptionDto()
        optionOneDto.setContent(OPTION_1_CONTENT)
        optionOneDto.setCorrect(true)
        optionOneDto.setOrder(2)

        def options = new ArrayList<OptionDto>()
        options.add(optionOneDto)

        def optionTwoDto = new OptionDto()
        optionTwoDto.setContent(OPTION_2_CONTENT)
        optionTwoDto.setCorrect(true)
        optionTwoDto.setOrder(1)

        options.add(optionTwoDto)

        questionDto.getQuestionDetailsDto().setOptions(options)

        questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)
        def question = questionRepository.findAll().get(0)

        def optionOne = question.getQuestionDetails().getOptions().get(0)
        def optionTwo = question.getQuestionDetails().getOptions().get(1)

        and: "a changed question"
        def questionUpdateDto = questionDto
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionUpdateDto.getQuestionDetailsDto().setIsToOrder(true)

        and: '2 changed options'
        def optionsUpdate = new ArrayList<OptionDto>()
        def optionOneUpdateDto = new OptionDto(optionOne)
        optionOneUpdateDto.setContent(OPTION_2_CONTENT)
        optionOneUpdateDto.setOrder(2)
        optionsUpdate.add(optionOneUpdateDto)
        def optionTwoUpdateDto = new OptionDto(optionTwo)
        optionTwoUpdateDto.setContent(OPTION_1_CONTENT)
        optionTwoUpdateDto.setOrder(1)
        optionsUpdate.add(optionTwoUpdateDto)

        questionUpdateDto.getQuestionDetailsDto().setOptions(optionsUpdate)

        when: "question is updated"
        response = restClient.put(
                path: "/questions/" + question.getId(),
                body: JsonOutput.toJson(questionUpdateDto),
                requestContentType: 'application/json')

        then: "check the response status"
        def error = thrown(HttpResponseException)
        error.response.getStatus() == org.apache.http.HttpStatus.SC_FORBIDDEN
    }

    def "update a question with one left icon and one right icon and no image by a teacher"() {
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

        def iconUpdateRight = question.getQuestionDetailsDto().getRightIcons().get(0)
        def iconUpdateLeft = question.getQuestionDetailsDto().getLeftIcons().get(0)

        and: "a changed question"
        def questionUpdateDto = questionDto
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "two changed icons"
        def iconLeftUpdateDto = iconUpdateLeft
        iconLeftUpdateDto.setContent(ICON_2_CONTENT)
        def iconRightUpdateDto = iconUpdateRight
        iconRightUpdateDto.setContent(ICON_2_CONTENT)

        rightIcons.clear()
        leftIcons.clear()

        rightIcons.add(iconRightUpdateDto)
        leftIcons.add(iconLeftUpdateDto)

        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)
        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)

        when: "question updated"
        def mapper = new ObjectMapper()
        def response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "and request is OK"

        response != null
        response.status == 200
        and: "it updates correctly"
        def questionUpdate = response.data
        questionUpdate.id != null
        questionUpdate.title == questionDto.getTitle()
        questionUpdate.content == questionDto.getContent()
        questionUpdate.status == questionDto.getStatus()
        questionUpdate.questionDetailsDto.rightIcons.get(0).content == ICON_2_CONTENT
        questionUpdate.questionDetailsDto.rightIcons.get(0).sequence == 0
        questionUpdate.questionDetailsDto.rightIcons.get(0).match.get(0) == 0
        questionUpdate.questionDetailsDto.leftIcons.get(0).content == ICON_2_CONTENT
        questionUpdate.questionDetailsDto.leftIcons.get(0).sequence == 0

        cleanup:
        userRepository.delete(teacher)
        courseExecutionRepository.deleteById(externalCourseExecution.getId())
        courseRepository.deleteById(externalCourse.getId())
    }

    def "update a question with one left icon and one right icon and no image by a demo teacher"() {
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

        def question = questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)

        def iconUpdateRight = question.getQuestionDetailsDto().getRightIcons().get(0)
        def iconUpdateLeft = question.getQuestionDetailsDto().getLeftIcons().get(0)

        and: "a changed question"
        def questionUpdateDto = questionDto
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "two changed icons"
        def iconLeftUpdateDto = iconUpdateLeft
        iconLeftUpdateDto.setContent(ICON_2_CONTENT)
        def iconRightUpdateDto = iconUpdateRight
        iconRightUpdateDto.setContent(ICON_2_CONTENT)

        rightIcons.clear()
        leftIcons.clear()

        rightIcons.add(iconRightUpdateDto)
        leftIcons.add(iconLeftUpdateDto)

        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)
        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)

        when: "question updated"
        def mapper = new ObjectMapper()
        def response = restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
                requestContentType: 'application/json'
        )

        then: "and request is OK"

        response != null
        response.status == 200
        and: "it updates correctly"
        def questionUpdate = response.data
        questionUpdate.id != null
        questionUpdate.title == questionDto.getTitle()
        questionUpdate.content == questionDto.getContent()
        questionUpdate.status == questionDto.getStatus()
        questionUpdate.questionDetailsDto.rightIcons.get(0).content == ICON_2_CONTENT
        questionUpdate.questionDetailsDto.rightIcons.get(0).sequence == 0
        questionUpdate.questionDetailsDto.rightIcons.get(0).match.get(0) == 0
        questionUpdate.questionDetailsDto.leftIcons.get(0).content == ICON_2_CONTENT
        questionUpdate.questionDetailsDto.leftIcons.get(0).sequence == 0
    }

    def "update a question with one left icon and one right icon and no image by a student"() {
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

        def question = questionService.createQuestion(course.getId(), questionDto)

        def iconUpdateRight = question.getQuestionDetailsDto().getRightIcons().get(0)
        def iconUpdateLeft = question.getQuestionDetailsDto().getLeftIcons().get(0)

        and: "a changed question"
        def questionUpdateDto = questionDto
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "two changed icons"
        def iconLeftUpdateDto = iconUpdateLeft
        iconLeftUpdateDto.setContent(ICON_2_CONTENT)
        def iconRightUpdateDto = iconUpdateRight
        iconRightUpdateDto.setContent(ICON_2_CONTENT)

        rightIcons.clear()
        leftIcons.clear()

        rightIcons.add(iconRightUpdateDto)
        leftIcons.add(iconLeftUpdateDto)

        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)
        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)

        when: "question updated"
        def mapper = new ObjectMapper()
        restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
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

    def "update a question with one left icon and one right icon and no image by a demo admin"() {

        given: "an admin"
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

        def question = questionService.createQuestion(courseService.getDemoCourse().getCourseId(), questionDto)

        def iconUpdateRight = question.getQuestionDetailsDto().getRightIcons().get(0)
        def iconUpdateLeft = question.getQuestionDetailsDto().getLeftIcons().get(0)

        and: "a changed question"
        def questionUpdateDto = questionDto
        questionUpdateDto.setTitle(QUESTION_2_TITLE)
        questionUpdateDto.setContent(QUESTION_2_CONTENT)
        questionUpdateDto.setQuestionDetailsDto(new ItemCombinationQuestionDto())

        and: "two changed icons"
        def iconLeftUpdateDto = iconUpdateLeft
        iconLeftUpdateDto.setContent(ICON_2_CONTENT)
        def iconRightUpdateDto = iconUpdateRight
        iconRightUpdateDto.setContent(ICON_2_CONTENT)

        rightIcons.clear()
        leftIcons.clear()

        rightIcons.add(iconRightUpdateDto)
        leftIcons.add(iconLeftUpdateDto)

        questionDto.getQuestionDetailsDto().setRightIcons(rightIcons)
        questionDto.getQuestionDetailsDto().setLeftIcons(leftIcons)

        when: "question updated"
        def mapper = new ObjectMapper()
        restClient.put(
                path: '/questions/' + question.getId(),
                body: mapper.writeValueAsString(questionDto),
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