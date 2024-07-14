import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import ItemCombinationStatementCorrectAnswerDetails from '@/models/statement/questions/ItemCombinationStatementCorrectAnswerDetails';

export default class ItemCombinationStatementAnswerDetails extends StatementAnswerDetails {
  public rightIconIds: any = {}

  constructor(jsonObj?:ItemCombinationStatementAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.rightIconIds = Object.assign({}, jsonObj.rightIconIds)
    }
  }

  isQuestionAnswered(): boolean {
    return this.rightIconIds != null;
  }

  isAnswerCorrect(
    correctAnswerDetails: ItemCombinationStatementCorrectAnswerDetails
  ): boolean {
    let areEqual = true;
    if (Object.keys(correctAnswerDetails.rightIconIds).length != Object.keys(this.rightIconIds).length) {
      return false;
    }

    for (let [key, value] of Object.entries(correctAnswerDetails.rightIconIds)) {
      if (!this.rightIconIds.has(key) || (this.rightIconIds.has(key) && (this.rightIconIds.get(key) != value))) {
        return false;
      }
    }

    return (
      !!correctAnswerDetails && areEqual
    );
  }
}
