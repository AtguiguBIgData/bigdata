import {Component, OnInit, TemplateRef} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {State, Task} from "../model/model";
import {BsModalRef, BsModalService} from "ngx-bootstrap";
import {Router} from "@angular/router";
import {LoginService} from "../services/login.service";
import {constant} from "../model/constant";
import {Order} from "../model/order";

@Component({
  selector: 'app-orderlist',
  templateUrl: './orderlist.component.html',
  styleUrls: ['./orderlist.component.css']
})
export class OrderlistComponent implements OnInit {

  orders:Order[] = []

  constructor(private modalService: BsModalService,
              private httpService : HttpClient,
              private loginService:LoginService,
              private router:Router) {
  }

  ngOnInit(): void {
    if(this.loginService.user.role == "admin")
      this.getAllOrders()
    else
      this.getOrdersByUid()
  }

  getAllOrders():void{
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/order/')
      .subscribe(
        data => {
          if(data['success'] == true){
            this.orders = data['orders'];
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }

  getOrdersByUid():void{

    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/order/'+this.loginService.user.uid)
      .subscribe(
        data => {
          if(data['success'] == true){
            this.orders = data['orders'];
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }

  delete(oid):void{
    var self = this;
    this.httpService
      .get(constant.BUSSINESS_SERVER_URL+'rest/order/remove/'+oid)
      .subscribe(
        data => {
          if(data['success'] == true){

            var tmpOrders = []
            for(let order of self.orders){
              if(order.rid != oid){
                tmpOrders.push(order)
              }
            }

            self.orders = tmpOrders;
          }
        },
        err => {
          console.log('Somethi,g went wrong!');
        }
      );
  }

}
