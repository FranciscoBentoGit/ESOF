import Option from '@/models/management/Option';
import AnswerDetails from '@/models/management/questions/AnswerDetails';
import { QuestionTypes, convertToLetter } from '@/services/QuestionHelpers';
import MultipleChoiceQuestionDetails from '@/models/management/questions/MultipleChoiceQuestionDetails';


export default class MultipleChoiceAnswerType extends AnswerDetails {
  public optionsList: Array<Option> = new Array();

  constructor(jsonObj?: MultipleChoiceAnswerType) {
    super(QuestionTypes.MultipleChoice);
    if (jsonObj) {
      this.optionsList = jsonObj.optionsList;
    }
  }

  isCorrect(question: MultipleChoiceQuestionDetails): boolean {
    let questionOptionsCorrect = question.options.filter((element) => element.correct === true )
    let answerOptionsCorrect = this.optionsList.filter((element) => element.correct === true )
    if (questionOptionsCorrect.length != answerOptionsCorrect.length) {
      return false;
    } else {
      for (let entry of answerOptionsCorrect) {
        if (questionOptionsCorrect.filter((element) => element.id === entry.id).length === 0) {
          return false;
        }
      }
    }
    return true;
  }
  answerRepresentation(): string {
    let rep: string = '';
    if(this.optionsList.length !== 0) {
      for(let entry of this.optionsList) {
        rep.concat(convertToLetter(entry.sequence))
      }
      return rep;
    }
    return "-";
  }
}
