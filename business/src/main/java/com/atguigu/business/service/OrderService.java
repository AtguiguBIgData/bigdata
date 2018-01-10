/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.service;

import com.atguigu.business.model.domain.Movie;
import com.atguigu.business.model.domain.Order;
import com.atguigu.commons.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MovieService movieService;

    public boolean payOrder(Order order){
        jdbcTemplate.update("insert into " + Constants.DB_ORDER_LIST + "(uid,mids,sum,time) values(?,?,?,?)",
                new Object[]{order.getUid(), order.getMids(), order.getSum(),order.getTime()});
        return true;
    }

    public List<Order> getOrdersByUid(int uid){
        List<Order> orders = null;
        orders = jdbcTemplate.query("select * from " + Constants.DB_ORDER_LIST + " where uid=" + uid, new RowMapper<Order>() {
            @Override
            public Order mapRow(ResultSet resultSet, int i) throws SQLException {
                Order movie = new Order();
                movie.setRid(resultSet.getInt("oid"));
                movie.setUid(resultSet.getInt("uid"));
                movie.setMids(resultSet.getString("mids"));
                movie.setSum(resultSet.getInt("sum"));
                movie.setTime(resultSet.getLong("time"));

                return movie;
            }
        });
        return orders;
    }

    public List<Order> getAllOrders(){
        List<Order> orders = null;
        orders = jdbcTemplate.query("select * from " + Constants.DB_ORDER_LIST, new RowMapper<Order>() {
            @Override
            public Order mapRow(ResultSet resultSet, int i) throws SQLException {
                Order movie = new Order();
                movie.setRid(resultSet.getInt("oid"));
                movie.setUid(resultSet.getInt("uid"));
                movie.setMids(resultSet.getString("mids"));
                movie.setSum(resultSet.getInt("sum"));
                movie.setTime(resultSet.getLong("time"));

                return movie;
            }
        });
        return orders;
    }

    public List<Movie> getOrderDetail(int oid){
        Order order = null;
        order = jdbcTemplate.query("select * from " + Constants.DB_ORDER_LIST + " where oid=" + oid, new ResultSetExtractor<Order>() {
            @Override
            public Order extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                Order movie = null;
                if (resultSet.next()) {
                    movie = new Order();
                    movie.setRid(resultSet.getInt("oid"));
                    movie.setUid(resultSet.getInt("uid"));
                    movie.setMids(resultSet.getString("mids"));
                    movie.setSum(resultSet.getInt("sum"));
                    movie.setTime(resultSet.getLong("time"));
                }
                return movie;
            }
        });

        List<Integer> ids = new ArrayList<>();
        for (String rec : order.getMids().split(",")) {
            ids.add(Integer.parseInt(rec));
        }
        return movieService.getMovies(ids);
    }

    public void removeOrder(int oid) {
        jdbcTemplate.execute("delete from " + Constants.DB_ORDER_LIST + " where oid=" + oid);
    }

}
