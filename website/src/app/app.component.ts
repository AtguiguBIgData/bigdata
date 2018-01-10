import { Component } from '@angular/core';
import {Router} from "@angular/router";
import {LoginService} from "./services/login.service";
import {User} from "./model/user";
import {constant} from "./model/constant";
import {Movie} from "./model/movie";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  login: LoginService;
  query:String = "";
  imageServer = constant.IMAGE_SERVER_URL;

  movies:Movie[] = [];

  constructor(public loginService:LoginService, public router:Router
  ) {
    this.login = loginService;
  }

  isLogin(): boolean {
    return this.loginService.isLogin();
  }

  isAdmin(): boolean {
    if(!this.isLogin())
      return false;
    return this.loginService.user.role=="admin";
  }

  logout(): void {
    this.loginService.logout();
  }

  enterPress(event:any): void {
    if(event.keyCode == 13){
      this.router.navigate(['/explore', { "type": "search", "query": this.query}]);
    }
  }

  search():void{
    this.router.navigate(['/explore', { "type": "search", "query": this.query}]);
  }

  alerts: any = [];

  add(movie): void {

    this.movies.push(movie);

    this.alerts.push({
      type: 'info',
      msg: `添加【`+ movie.name+`】到购物车成功`,
      timeout: 3000
    });
  }

  remove(mid):void{
    var tmpMovies:Movie[] = [];
    for(let movie of this.movies){
      if(movie.mid != mid){
        tmpMovies.push(movie)
      }
    }
    this.movies = tmpMovies;
  }

  clear():void{
    this.movies = [];
  }

  pay():void{
    this.router.navigate(['/pay',{"type":"pay"}]);
  }

}
