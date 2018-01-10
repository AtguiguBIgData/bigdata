import {Component, Input, OnInit} from '@angular/core';
import {Movie} from "../model/movie";
import {constant} from "../model/constant";
import {AppComponent} from "../app.component";

@Component({
  selector: 'app-thumbnail',
  templateUrl: './thumbnail.component.html',
  styleUrls: ['./thumbnail.component.css']
})
export class ThumbnailComponent implements OnInit {

  detail:boolean = false;

  imageServer = constant.IMAGE_SERVER_URL;

  @Input() movie: Movie = new Movie;

  constructor(public appCom:AppComponent) {

  }

  ngOnInit() {

  }

  hover():void{
    this.detail = true;
  }

  leave():void{
    this.detail = false;
  }

  addCat():void{

  }

}
