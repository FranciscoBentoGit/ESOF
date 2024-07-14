import StatementAnswerDetails from '@/models/statement/questions/StatementAnswerDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import OpenAnswerStatementCorrectAnswerDetails from '@/models/statement/questions/OpenAnswerStatementCorrectAnswerDetails';

export default class OpenAnswerStatementAnswerDetails extends StatementAnswerDetails {
  public response: string | null = null;

  constructor(jsonObj?: OpenAnswerStatementAnswerDetails) {
    super(QuestionTypes.OpenAnswer);
    if (jsonObj) {
      this.response = jsonObj.response;
    }
  }
  isQuestionAnswered(): boolean {
    return this.response != null;
  }

  isAnswerCorrect(
    correctAnswerDetails: OpenAnswerStatementCorrectAnswerDetails
  ): boolean {
    return (
      !!correctAnswerDetails &&
      this.response === correctAnswerDetails.correctResponse
    );
  }
}
