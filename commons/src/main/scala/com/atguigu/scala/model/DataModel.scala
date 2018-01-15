/*
 * Copyright (c) 2017. Atguigu Inc. All Rights Reserved.
 * Date: 10/31/17 5:07 PM.
 * Author: wuyufei.
 */

package com.atguigu.scala.model

/**
  * Movie Class 电影类
  *
  * @param mid       电影的ID
  * @param name      电影的名称
  * @param descri    电影的描述
  * @param timelong  电影的时长
  * @param issue     电影的发行时间
  * @param shoot     电影的拍摄时间
  * @param language  电影的语言
  * @param genres    电影的类别
  * @param actors    电影的演员
  * @param directors 电影的导演
  */
case class Movie(val mid: Int, val name: String, val descri: String, val timelong: String, val issue: String,
                 val shoot: String, val language: String, val genres: String, val actors: String, val directors: String)

/**
  * MovieRating Class 电影的评分类
  *
  * @param uid       用户的ID
  * @param mid       电影的ID
  * @param score     用户为该电影的评分
  * @param timestamp 用户为该电影评分的时间
  */
case class MovieRating(val uid: Int, val mid: Int, val score: Double, val timestamp: Int)

/**
  * Tag Class  电影标签类
  *
  * @param uid       用户的ID
  * @param mid       电影的ID
  * @param tag       用户为该电影打的标签
  * @param timestamp 用户为该电影打标签的时间
  */
case class Tag(val uid: Int, val mid: Int, val tag: String, val timestamp: Int)

/**
  * MySQL的连接配置
  * @param url        URL
  * @param user       用户名
  * @param password   密码
  */
case class MySqlConfig(val url: String, val user:String, val password:String)

/**
  * ElasticSearch的连接配置
  *
  * @param httpHosts      Http的主机列表，以，分割
  * @param transportHosts Transport主机列表， 以，分割
  * @param index          需要操作的索引
  * @param clustername    ES集群的名称，
  */
case class ESConfig(val httpHosts: String, val transportHosts: String, val index: String, val clustername: String)

//推荐
case class Recommendation(rid: Int, r: Double){
  override def toString :String = {rid.toString + ":" + r.toString}
}

// 用户的推荐
case class UserRecs(uid: Int, recs: String)

//电影的相似度
case class MovieRecs(mid: Int, recs: String)















//***************** 输入表 *********************

/**
  * 用户访问动作表
  *
  * @param date               用户点击行为的日期
  * @param user_id            用户的ID
  * @param session_id         Session的ID
  * @param page_id            某个页面的ID
  * @param action_time        点击行为的时间点
  * @param search_keyword     用户搜索的关键词
  * @param click_category_id  某一个商品品类的ID
  * @param click_product_id   某一个商品的ID
  * @param order_category_ids 一次订单中所有品类的ID集合
  * @param order_product_ids  一次订单中所有商品的ID集合
  * @param pay_category_ids   一次支付中所有品类的ID集合
  * @param pay_product_ids    一次支付中所有商品的ID集合
  * @param city_id            城市ID
  */
case class UserVisitAction(date: String,
                           user_id: Long,
                           session_id: String,
                           page_id: Long,
                           action_time: String,
                           search_keyword: String,
                           click_category_id: Long,
                           click_product_id: Long,
                           order_category_ids: String,
                           order_product_ids: String,
                           pay_category_ids: String,
                           pay_product_ids: String,
                           city_id: Long
                          )

/**
  * 用户信息表
  *
  * @param user_id      用户的ID
  * @param username     用户的名称
  * @param name         用户的名字
  * @param age          用户的年龄
  * @param professional 用户的职业
  * @param city         用户所在的城市
  * @param sex          用户的性别
  */
case class UserInfo(user_id: Long,
                    username: String,
                    name: String,
                    age: Int,
                    professional: String,
                    city: String,
                    sex: String
                   )

/**
  * 产品表
  *
  * @param product_id   商品的ID
  * @param product_name 商品的名称
  * @param extend_info  商品额外的信息
  */
case class ProductInfo(product_id: Long,
                       product_name: String,
                       extend_info: String
                      )