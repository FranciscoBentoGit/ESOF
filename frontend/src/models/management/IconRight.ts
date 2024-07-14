export default class IconRight {
    id: number | null = null;
    sequence!: number | null;
    content: string = '';
    match: number | null = null;
  
    constructor(jsonObj?: IconRight) {
      if (jsonObj) {
        this.id = jsonObj.id;
        this.sequence = jsonObj.sequence;
        this.content = jsonObj.content;
        this.match = jsonObj.match;
      }
    }
  }
  