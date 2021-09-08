// 'use strict';

//startContext();
//startPageContext();

/* global bp, random */

const url = 'https://sandbox.moodledemo.net/login/index.php';
bp.log.info("URL:" + url);
//const url = 'http://localhost/login/index.php';
const NUM_COURSES = 2;
const NUM_QUIZES_PER_COURSE = 1;
const NUM_QUESTIONS_PER_QUIZ = 1;
const COURSE_NAMES = [];

for (let i = 0; i < NUM_COURSES; i++) {
    COURSE_NAMES.push({
        short: random.string(4, "short_name_"),
        full: random.string(10, "course_name_")
    });
}

bthread('Admin adds courses', function(){
    startSession('A1', url);

    login({ s: 'A1', username: 'admin', password: 'sandbox' });

    turnSiteEditingOn({ s: 'A1' });

    for (let i = 0; i < NUM_COURSES; i++)
        addCourse({ s: 'A1', fullname: COURSE_NAMES[i].full, shortname: COURSE_NAMES[i].short });

    endSession('A1');
});

bthread("Admin enrols teachers", function () {
    when(Any("AddCourse"), function (e) {
        block(EndSession(e.s), function () {
            enrolUserToCourse({ s: e.s, course: e.fullname, user: 'Terri Teacher', role: 'Teacher' });
        });
    });
});

bthread("Admin enrols students", function () {
    when(Any("AddCourse"), function (e) {
        block(EndSession(e.s), function () {
            enrolUserToCourse({ s: e.s, course: e.fullname, user: 'Sam Student', role: 'Student' });
        });
    });
});


bthread('Teacher adds quizzes with questions', function () {
    let on = false;
    let s = "teacher_session_" + 1;

    startSession(s, url);

    login({ s: s, username: 'teacher', password: 'sandbox' });

    when(Any("EnrolUserToCourse"), function (e) {
        if (e.user == "Terri Teacher") {
            if (!on) {
                on = true;
                turnCourseEditingOn({ s: s, course: e.course });
            }

            for (let i = 0; i < NUM_QUIZES_PER_COURSE; i++) {
                addQuiz({
                    s: s, course: e.course,
                    quizname: "quiz_name" + "-" + i,
                    description: "quiz_description_" + e.course + "-" + i + " long description goes here"
                });
            }
        }
    });
});

bthread("Teacher adds true false questions", function () {
    when(Any("AddQuiz"), function (e) {

        block(EndSession(e.s), function () {
            for (let i = 0; i < NUM_QUESTIONS_PER_QUIZ; i++) {
                var question_text = 'Text for question ' + i;
                bthread("interrupt 'Add true false questions'", function () {
                    let attempt = waitFor(Any("TrueFalseQuestionAttempt")).data;
                    if (attempt.course == e.course && attempt.quiz == e.quizname && attempt.question_text == question_text) {
                        waitFor(EndOfActionInSession(attempt.s));
                    }
                });

                addTrueFalseQuestionToQuiz({
                    s: e.s, questionname: 'My question ' + i, question_text: question_text, answer: 'True',
                    course: e.course, quiz: e.quizname, interrupt: "code_red"
                });
            }
        });
    });
});

bthread('A student answers questions', function () {
    when(Any("AddTrueFalseQuestionToQuiz"), function (e) {
        let s = "student_session_" + 1;

        startSession(s, url);

        login({ s: s, username: 'student', password: 'sandbox' });

        gotoMainMenu({ s: s }); // Refreshes the list of courses
        trueFalseQuestionAttempt({ s: s, course: e.course, quiz: e.quiz, question_text: e.question_text, answer: 'True' });

        // gotoMainMenu({ s: s }); // Refreshes the list of courses
        // trueFalseQuestionAttempt({ s: s, course: e.course, quiz: e.quiz, question_text: e.question_text, answer: 'False' })

        endSession(s);
    });
});


/*bthread('Delete questions', function () {
  whenAddTrueFalseQuestionToQuiz(function (e) {

    let s = "teacher_session_" + e.hashCode();

    startSession(s, url)
    bp.log.info("started session for delete quiz. " + s)
    login({ s: s, username: 'teacher', password: 'sandbox' })

    gotoCourse({ s: s, course: e.course })

    turnEditingOn({ s: s })

    deleteTrueFalseQuestionFromQuiz({ s: s, course: e.course, quiz: e.quiz, questionname: e.questionname })

    endSession(s)
    bp.log.info("ended session for delete quiz. " + s)
  })
})*/