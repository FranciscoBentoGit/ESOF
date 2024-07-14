import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class ItemCombinationStatementCorrectAnswerDetails extends StatementCorrectAnswerDetails {
  public rightIconIds: any = {};

  constructor(jsonObj?:ItemCombinationStatementCorrectAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.rightIconIds = Object.assign({}, jsonObj.rightIconIds)
    }
  }
}
