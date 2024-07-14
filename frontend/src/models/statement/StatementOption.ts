export default class StatementOption {
  optionId!: number;
  content!: string;
  order!: number | null;

  constructor(jsonObj?: StatementOption) {
    if (jsonObj) {
      this.optionId = jsonObj.optionId;
      this.content = jsonObj.content;
      this.order = jsonObj.order;
    }
  }
}
