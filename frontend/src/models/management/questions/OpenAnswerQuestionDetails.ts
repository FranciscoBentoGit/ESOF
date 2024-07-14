import QuestionDetails from './QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class OpenAnswerQuestionDetails extends QuestionDetails {
  response: string = '';

  constructor(jsonObj?: OpenAnswerQuestionDetails) {
    super(QuestionTypes.OpenAnswer);
    if (jsonObj) {
      this.response = jsonObj.response || this.response;
    }
  }

  setAsNew(): void {}
}
