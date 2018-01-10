import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';

import { AppComponent }         from './app.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { LoginService } from "./services/login.service";
import { HttpClientModule } from "@angular/common/http";
import { RouterModule } from "@angular/router";
import { ChartComponent } from './chart/chart.component';
import {NgxEchartsModule} from "ngx-echarts";
import {AlertModule, BsDropdownModule, ButtonsModule, CarouselModule, ModalModule, PopoverModule} from "ngx-bootstrap";
import { AnalysisComponent } from './analysis/analysis.component';
import { RecommendComponent } from './recommend/recommend.component';
import {WebsocketEventService} from "./services/websocket-event.service";
import {HomeComponent} from "./home/home.component";
import {ThumbnailComponent} from "./thumbnail/thumbnail.component";
import {StarComponent} from "./star/star.component";
import {MdetailComponent} from "./mdetail/mdetail.component";
import {TagsComponent} from "./tags/tags.component";
import {ExploreComponent} from "./explore/explore.component";
import { OrderlistComponent } from './orderlist/orderlist.component';
import { OrderdetailComponent } from './orderdetail/orderdetail.component';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    RouterModule,
    NgxEchartsModule,
    BsDropdownModule.forRoot(),
    ButtonsModule.forRoot(),
    ModalModule.forRoot(),
    CarouselModule.forRoot(),
    PopoverModule.forRoot(),
    AlertModule.forRoot()
  ],
  declarations: [
    AppComponent,
    AnalysisComponent,
    RecommendComponent,
    LoginComponent,
    RegisterComponent,
    ChartComponent,
    HomeComponent,
    ThumbnailComponent,
    MdetailComponent,
    TagsComponent,
    StarComponent,
    ExploreComponent,
    OrderlistComponent,
    OrderdetailComponent,
  ],
  providers: [
    LoginService,
    WebsocketEventService
  ],
  bootstrap: [ AppComponent ]
})
export class AppModule {
}
