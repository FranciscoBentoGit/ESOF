import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';
import OpenAnswerQuestionDetails from './OpenAnswerQuestionDetails';

export default class OpenAnswerAnswerType extends AnswerDetails {
  response!: string;

  constructor(jsonObj?: OpenAnswerAnswerType) {
    super(QuestionTypes.OpenAnswer);
    if (jsonObj) {
      this.response = jsonObj.response;
    }
  }
  isCorrect(questionDetails: OpenAnswerQuestionDetails): boolean {
    return questionDetails.response == this.response;
  }

  answerRepresentation(): string {
    return this.response;
  }
}
