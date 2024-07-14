import Option from '@/models/management/Option';
import IconLeft from '@/models/management/IconLeft.ts';
import IconRight from '@/models/management/IconRight.ts';
import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class ItemCombinationQuestionDetails extends QuestionDetails {
  leftIcons: IconLeft[] = [new IconLeft(), new IconLeft()];
  rightIcons: IconRight[] = [new IconRight(), new IconRight()];

  constructor(jsonObj?: ItemCombinationQuestionDetails) {
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

  setAsNew(): void {
    this.leftIcons.forEach(leftIcon => {
      leftIcon.id = null;
    });
    this.rightIcons.forEach(rightIcon => {
      rightIcon.id = null;
    });
  }
}