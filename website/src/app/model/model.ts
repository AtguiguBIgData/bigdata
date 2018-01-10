export enum State{
  RUNNING = 0, //显示停止按钮
  FAIL = 1,
  COMPLETE = 2,
  NEW = 3
}

export class Task {
  id: string;
  title:string;
  jarname: string;
  type:string;
  state:State;


  constructor(id: string, title: string, jarname: string, type: string, state: State) {
    this.id = id;
    this.title = title;
    this.jarname = jarname;
    this.type = type;
    this.state = state;
  }
}
