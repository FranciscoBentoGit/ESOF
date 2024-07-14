export default class StatementIconLeft {
    leftIconId!: number;
    content!: string;
  
    constructor(jsonObj?: StatementIconLeft) {
      if (jsonObj) {
        this.leftIconId = jsonObj.leftIconId;
        this.content = jsonObj.content;
      }
    }
  }
  