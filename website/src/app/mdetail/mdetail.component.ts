import {Component, OnInit, ViewChild} from '@angular/core';
import {Movie} from "../model/movie";
import {HttpClient} from "@angular/common/http";
import {LoginService} from "../services/login.service";
import {Comment} from "../model/comment";
import {ActivatedRoute, ParamMap} from "@angular/router";
import 'rxjs/add/operator/switchMap';
import {constant} from "../model/constant";
import {AppComponent} from "../app.component";
import {StarComponent} from "../star/star.component";

@Component({
  selector: 'app-mdetail',
  templateUrl: './mdetail.component.html',
  styleUrls: ['./mdetail.component.css']
})
export class MdetailComponent implements OnInit {

  movie: Movie = new Movie;
  sameMovies: Movie[] = [];
  movieComments: Comment[] = [];

  imageServer = constant.IMAGE_SERVER_URL;

  @ViewChild(StarComponent) starScore: StarComponent;

  constructor(
    private route:ActivatedRoute,
    private httpService : HttpClient,
    private loginService: LoginService,
    public appCom:AppComponent
  ) {}

  ngOnInit(): void {

    this.route.params
      .subscribe(params => {
        var id = params['id'];
        this.getMovieInfo(id);
        this.getSameMovies(id);
        this.getMovieTags(id);
    });
  }

  addMyComment(id:number, name:string):void{

    var score = this.starScore.getCurrentValue();

    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'/rest/movie/comment/'+ id +'?uid='+this.loginService.user.uid+'&comment='+name+'&score='+score)
      .subscribe(
        data => {
          if(data['success'] == true){
            let comment = new Comment()
            comment.mid = id;
            comment.score = score;
            comment.tag = name;
            comment.uid = this.loginService.user.uid;
            comment.timestamp = new Date().getSeconds()
            this.movieComments.push(comment);
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }


  removeMyTag(id:number):void {
    /*for (let myTag of this.myTags) {
      if(myTag.id == id){
        this.myTags.unshift(myTag);
        break;
      }
    }*/
  }

  getMovieTags(id:number):void{
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'/rest/movie/tags/'+id)
      .subscribe(
        data => {
          if(data['success'] == true){
            this.movieComments = data['comments'];
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }


  getSameMovies(id:number):void{
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'/rest/movie/same/'+id+'?num=6')
      .subscribe(
        data => {
          if(data['success'] == true){
            this.sameMovies = data['movies']
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }

  getMovieInfo(id:number):void{
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'/rest/movie/info/'+id)
      .subscribe(
        data => {
          if(data['success'] == true){
            this.movie = data['movie'];
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }
}
