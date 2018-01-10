import {Component, OnInit, TemplateRef} from '@angular/core';
import {LoginService} from "../services/login.service";
import {BsModalRef, BsModalService} from "ngx-bootstrap";
import {HttpClient} from "@angular/common/http";
import {Router} from "@angular/router";
import {State, Task} from "../model/model";

@Component({
  selector: 'app-analysis',
  templateUrl: './analysis.component.html',
  styleUrls: ['./analysis.component.css']
})
export class AnalysisComponent implements OnInit {

  modalRef: BsModalRef;
  openModal(template: TemplateRef<any>) {
    this.modalRef = this.modalService.show(template);
  }

  realtimeTasks:Task[] = []
  offlineTasks:Task[] = []

  constructor(private modalService: BsModalService,
              private httpService : HttpClient,
              private loginService:LoginService,
              private router:Router) {
  }

  ngOnInit(): void {
    this.getRealtimeTasks();
    this.getOfflineTasks();
  }

  getRealtimeTasks():void{

    this.realtimeTasks.push(new Task("1111","光爆","jar","sparkstreaming",State.COMPLETE))
    this.realtimeTasks.push(new Task("1111","光爆","jar","sparkstreaming",State.NEW))
    this.realtimeTasks.push(new Task("1111","光爆","jar","sparkstreaming",State.COMPLETE))
    this.realtimeTasks.push(new Task("1111","光爆","jar","sparkstreaming",State.COMPLETE))


  }

  getOfflineTasks():void{

    this.offlineTasks.push(new Task("1111","光爆","jar","sparkstreaming",State.COMPLETE))
    this.offlineTasks.push(new Task("1111","光爆","jar","sparkstreaming",State.COMPLETE))
    this.offlineTasks.push(new Task("1111","光爆","jar","sparkstreaming",State.COMPLETE))
    this.offlineTasks.push(new Task("1111","光爆","jar","sparkstreaming",State.COMPLETE))

  }

  scheduleTask():void{

  }

  stopTask():void{

  }

  removeTask():void{

  }

  addTask():void{

  }

  /*updateGenres():void {
    var prefGenres = "";
    this.genres.map(x=>{
      if(x.checked){
        prefGenres = prefGenres + x.value +','
      }
    })
    prefGenres = prefGenres.slice(0,prefGenres.length-1)
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/users/pref?username='+this.loginService.user.username+"&genres="+prefGenres)
      .subscribe(
        data => {
          if(data['success'] == true){
            this.loginService.user.first = false;
            this.router.navigate(['/home']);
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }

  isLoginFirst():boolean{
    return this.loginService.user.first
  }

  getGuessMovies():void{
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/movie/guess?num=6&username='+this.loginService.user.username)
      .subscribe(
        data => {
          if(data['success'] == true){
            this.guessMovies = data['movies'];
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }
  getHotMovies():void{
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/movie/hot?num=6&username='+this.loginService.user.username)
      .subscribe(
        data => {
          if(data['success'] == true){

            this.hotMovies = data['movies'];
            /!*
            angular.forEach(movies, function(data){
              var movie = new Movie;
              movie.id=data['mid'];
              movie.descri=data['descri'];
              movie.issue=data['issue'];
              movie.language=data['language'];
              movie.name=data['name'];
              movie.shoot=data['shoot'];
              movie.timelong=data['timelong'];
              this.hotMovies.push(movie);
            });*!/
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }
  getNewMovies():void{
    /!*var movie = new Movie;
    movie.mid=1;
    movie.descri="Set in the 22nd century, The Matrix tells the story of a computer hacker who joins a group of underground insurgents fighting the vast and powerful computers who now rule the earth.";
    movie.issue="November 20, 2001";
    movie.language="English";
    movie.name="The Matrix";
    movie.picture="./assets/1.jpg";
    movie.score=8;
    movie.shoot="1999";
    movie.timelong="136 minutes";

    this.newMovies.push(movie);
    this.newMovies.push(movie);
    this.newMovies.push(movie);
    this.newMovies.push(movie);
    this.newMovies.push(movie);
    this.newMovies.push(movie);*!/
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/movie/new?num=6&username='+this.loginService.user.username)
      .subscribe(
        data => {
          if(data['success'] == true){
            this.newMovies = data['movies'];
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }
  getRateMoreMovies():void{
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/movie/rate?num=6&username='+this.loginService.user.username)
      .subscribe(
        data => {
          if(data['success'] == true){
            this.rateMoreMovies = data['movies'];
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }
  getWishMovies():void{
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/movie/wish?num=6&username='+this.loginService.user.username)
      .subscribe(
        data => {
          if(data['success'] == true){
            this.wishMovies = data['movies'];
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }*/

}
