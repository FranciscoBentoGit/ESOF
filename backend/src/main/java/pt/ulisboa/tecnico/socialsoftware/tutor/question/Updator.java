package pt.ulisboa.tecnico.socialsoftware.tutor.question;

import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.*;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.OpenAnswerQuestion;

public interface Updator {
    default void update(MultipleChoiceQuestion question) {
    }

    default void update(CodeFillInQuestion question) {
    }

    default void update(CodeOrderQuestion codeOrderQuestion) {
    }

    default void update(ItemCombinationQuestion question) {
    }

    default void update(OpenAnswerQuestion openAnswerQuestion) {
    }
}

