export default class StatementIconRight {
    rightIconId!: number;
    content!: string;
    match!: number[];
  
    constructor(jsonObj?: StatementIconRight) {
      if (jsonObj) {
        this.rightIconId = jsonObj.rightIconId;
        this.content = jsonObj.content;
        this.match = jsonObj.match;
      }
    }
  }
  