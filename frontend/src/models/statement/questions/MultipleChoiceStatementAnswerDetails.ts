import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import MultipleChoiceStatementCorrectAnswerDetails from '@/models/statement/questions/MultipleChoiceStatementCorrectAnswerDetails';

export default class MultipleChoiceStatementAnswerDetails extends StatementAnswerDetails {
  public optionsIdsHash: any = {};

  constructor(jsonObj?: MultipleChoiceStatementAnswerDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.optionsIdsHash = Object.assign({}, jsonObj.optionsIdsHash);
    }
  }

  isQuestionAnswered(): boolean {
    return this.optionsIdsHash != null && Object.keys(this.optionsIdsHash).length > 0;
  }

  isAnswerCorrect(
    correctAnswerDetails: MultipleChoiceStatementCorrectAnswerDetails
  ): boolean {
    if (Object.keys(correctAnswerDetails.correctOptions).length != Object.keys(this.optionsIdsHash).length) {
      return false;
    }

    for (let [key, value] of Object.entries(correctAnswerDetails.correctOptions)) {
      if (!this.optionsIdsHash.key != undefined || (this.optionsIdsHash.key != undefined && (this.optionsIdsHash.key != value))) {
        return false;
      }
    }

    return true;  
  }
}
