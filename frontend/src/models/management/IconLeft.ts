export default class IconLeft {
    id: number | null = null;
    sequence!: number | null;
    content: string = '';
  
    constructor(jsonObj?: IconLeft) {
      if (jsonObj) {
        this.id = jsonObj.id;
        this.sequence = jsonObj.sequence;
        this.content = jsonObj.content;
      }
    }
  }
  