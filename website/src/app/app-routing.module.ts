import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ChartComponent} from "./chart/chart.component";
import {AnalysisComponent} from "./analysis/analysis.component";
import {RecommendComponent} from "./recommend/recommend.component";
import {MdetailComponent} from "./mdetail/mdetail.component";
import {HomeComponent} from "./home/home.component";
import {LoginComponent} from "./login/login.component";
import {RegisterComponent} from "./register/register.component";
import {TagsComponent} from "./tags/tags.component";
import {ExploreComponent} from "./explore/explore.component";
import {OrderdetailComponent} from "./orderdetail/orderdetail.component";
import {OrderlistComponent} from "./orderlist/orderlist.component";

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'movies/:id', component: MdetailComponent },
  { path: 'home',     component: HomeComponent },
  { path: 'login',     component: LoginComponent },
  { path: 'register',     component: RegisterComponent },
  { path: 'tags', component: TagsComponent},
  { path: 'explore',     component: ExploreComponent },
  { path: 'chart',     component: ChartComponent },
  {path: 'analysis', component: AnalysisComponent},
  {path: 'recommend', component: RecommendComponent},
  {path: 'pay', component: OrderdetailComponent},
  {path: 'order', component: OrderlistComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
