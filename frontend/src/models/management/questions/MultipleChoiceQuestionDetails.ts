import Option from '@/models/management/Option';
import QuestionDetails from '@/models/management/questions/QuestionDetails';
import { QuestionTypes } from '@/services/QuestionHelpers';

export default class MultipleChoiceQuestionDetails extends QuestionDetails {
  isToOrder: boolean = false;
  options: Option[] = [new Option(), new Option(), new Option(), new Option()];

  constructor(jsonObj?: MultipleChoiceQuestionDetails) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.options = jsonObj.options.map(
        (option: Option) => new Option(option)
      );
      this.isToOrder = jsonObj.isToOrder;
      console.log(this.isToOrder);
    }
  }

  setAsNew(): void {
    this.options.forEach(option => {
      option.id = null;
    });
  }
}
