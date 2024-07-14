import Option from '@/models/management/Option';
import AnswerDetails from '@/models/management/questions/AnswerDetails';
import IconLeft from '@/models/management/IconLeft.ts';
import IconRight from '@/models/management/IconRight.ts';
import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';
import ItemCombinationQuestionDetails from './ItemCombinationQuestionDetails';
import QuestionDetails from './QuestionDetails';

export default class ItemCombinationAnswerDetails extends AnswerDetails {
  leftIcons: IconLeft[] = [];
  rightIcons: IconRight[] = [];

  constructor(jsonObj?: ItemCombinationAnswerDetails) {
    super(QuestionTypes.ItemCombination);
    if (jsonObj) {
      this.leftIcons = jsonObj.leftIcons.map(
        (leftIcon: IconLeft) => new IconLeft(leftIcon)
      );
    }
    if (jsonObj) {
      this.rightIcons = jsonObj.rightIcons.map(
        (rightIcon: IconRight) => new IconRight(rightIcon)
      );
    }
  }
  
  isCorrect(questionDetails: ItemCombinationQuestionDetails): boolean {
    return this.rightIcons.length == questionDetails.rightIcons.length;
  }
  answerRepresentation(): string {
    
    return "answer";
  }
  
}
