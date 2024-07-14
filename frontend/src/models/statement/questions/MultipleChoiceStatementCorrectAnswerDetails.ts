import StatementCorrectAnswerDetails from '@/models/statement/questions/StatementCorrectAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import Option from '@/models/management/Option';

export default class MultipleChoiceStatementCorrectAnswerDetails extends StatementCorrectAnswerDetails {
  public correctOptions: any = {};

  constructor(jsonObj?: MultipleChoiceStatementCorrectAnswerDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.correctOptions = Object.assign({}, jsonObj.correctOptions);
    }
  }

}
