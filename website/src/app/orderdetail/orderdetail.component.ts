import {Component, OnInit, TemplateRef} from '@angular/core';
import {AppComponent} from "../app.component";
import {HttpClient} from "@angular/common/http";
import {State, Task} from "../model/model";
import {BsModalRef, BsModalService} from "ngx-bootstrap";
import {ActivatedRoute, Router} from "@angular/router";
import {LoginService} from "../services/login.service";
import {constant} from "../model/constant";
import {Movie} from "../model/movie";

@Component({
  selector: 'app-orderdetail',
  templateUrl: './orderdetail.component.html',
  styleUrls: ['./orderdetail.component.css']
})
export class OrderdetailComponent implements OnInit {

  imageServer = constant.IMAGE_SERVER_URL;

  constructor(public appCom: AppComponent,
              private router: Router,
              private httpService : HttpClient,
              private loginService: LoginService,
              public route:ActivatedRoute) {
  }

  // rid = -1 means for pay
  rid = -1
  movies:Movie[] = []

  ngOnInit(): void {
    var self = this;
    this.route.params.subscribe(params => {
      if(params['type']=="pay"){
        self.movies = this.appCom.movies

      }else if(params['type']== "detail"){
        self.rid = params['rid']
        console.log(self.rid)
        self.httpService
          .get(constant.BUSSINESS_SERVER_URL+'rest/order/detail/'+self.rid)
          .subscribe(
            data => {
              self.movies = data['movies']
            },
            err => {
              console.log('Somethi,g went wrong!');
            }
          );
      }

    });
  }

  total(): number {
    var sum = 0
    for(let movie of this.movies){
      sum += movie.score
    }
    return sum;
  }

  pay():void{
    var mids = "";
    for(let movie of this.appCom.movies){
      mids += ","
      mids += movie.mid
    }

    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/order/pay?uid='+this.loginService.user.uid+'&mids='+mids.slice(1,mids.length-1)+'&sum='+this.total())
      .subscribe(
        data => {
          this.router.navigate(['/order']);
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );


  }
}
