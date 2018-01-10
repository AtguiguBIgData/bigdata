package com.atguigu.business.service;

import com.atguigu.business.model.domain.Comment;
import com.atguigu.business.model.domain.Order;
import com.atguigu.business.model.domain.User;
import com.atguigu.business.model.request.MovieRatingRequest;
import com.atguigu.commons.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Jedis jedis;

    public void addComment(Comment comment){
        jdbcTemplate.update("insert into " + Constants.DB_COMMENT + "(uid,mid,score,tag,timestamp) values(?,?,?,?,?)",
                new Object[]{comment.getUid(), comment.getMid(), comment.getScore(),comment.getTag(),comment.getTimestamp()});
    }

    public List<Comment> getMovieComments(int mid){
        List<Comment> comments = null;
        comments = jdbcTemplate.query("select * from " + Constants.DB_COMMENT + " where mid=" + mid, new RowMapper<Comment>() {
            @Override
            public Comment mapRow(ResultSet resultSet, int i) throws SQLException {
                Comment movie = new Comment();
                movie.setMid(mid);
                movie.setUid(resultSet.getInt("uid"));
                movie.setScore(resultSet.getDouble("score"));
                movie.setTag(resultSet.getString("tag"));
                movie.setTimestamp(resultSet.getLong("timestamp"));

                return movie;
            }
        });
        return comments;
    }

}
