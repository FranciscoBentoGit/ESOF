import StatementQuestionDetails from '@/models/statement/questions/StatementQuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';
import StatementIconRight from '@/models/statement/StatementIconRight';
import StatementIconLeft from '@/models/statement/StatementIconLeft';
import { _ } from 'vue-underscore';

export default class ItemCombinationStatementQuestionDetails extends StatementQuestionDetails {
  rightIcons: StatementIconRight[] = [];
  leftIcons: StatementIconLeft[] = [];

  constructor(jsonObj?: ItemCombinationStatementQuestionDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      if (jsonObj.rightIcons) {
        this.rightIcons = _.shuffle(
          jsonObj.rightIcons.map(
            (option: StatementIconRight) => new StatementIconRight(option)
          )
        );
      }
      if (jsonObj.leftIcons) {
        this.leftIcons = _.shuffle(
          jsonObj.leftIcons.map(
            (option: StatementIconLeft) => new StatementIconLeft(option)
          )
        );
      }
    }
  }
}
