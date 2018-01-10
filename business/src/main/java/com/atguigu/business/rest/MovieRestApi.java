package com.atguigu.business.rest;

import com.atguigu.business.model.domain.Comment;
import com.atguigu.business.model.domain.User;
import com.atguigu.business.model.recom.Recommendation;
import com.atguigu.business.model.request.*;
import com.atguigu.business.service.CommentService;
import com.atguigu.business.service.MovieService;
import com.atguigu.business.service.RecommenderService;
import com.atguigu.business.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;


@RequestMapping("/rest/movie")
@Controller
public class MovieRestApi {

    private Logger logger = LoggerFactory.getLogger(MovieRestApi.class);

    @Autowired
    private RecommenderService recommenderService;
    @Autowired
    private MovieService movieService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;

    /**
     * 获取推荐的电影【实时推荐6 + 内容推荐4】
     * @param username
     * @param model
     * @return
     */
    // TODO: 2017/10/20  bug 混合推荐结果中，基于内容的推荐，基于MID，而非UID
    @RequestMapping(value = "/guess", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getGuessMovies(@RequestParam("username")String username,@RequestParam("num")int num, Model model) {
        User user = userService.findByUsername(username);
        List<Recommendation> recommendations = recommenderService.getHybridRecommendations(new MovieHybridRecommendationRequest(user.getUid(),num));
        if(recommendations.size()==0){
            String randomGenres = user.getPrefgenres().split(",")[new Random().nextInt(user.getPrefgenres().split(",").length)];
            recommendations = recommenderService.getTopGenresRecommendations(new TopGenresRecommendationRequest(randomGenres.split(" ")[0],num));
        }
        model.addAttribute("success",true);
        model.addAttribute("movies",movieService.getHybirdRecommendeMovies(recommendations));
        return model;
    }

    /**
     *
     * @param username
     * @param model
     * @return
     */
    @RequestMapping(value = "/wish", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getWishMovies(@RequestParam("username")String username,@RequestParam("num")int num, Model model) {
        User user = userService.findByUsername(username);
        List<Recommendation> recommendations = recommenderService.getCollaborativeFilteringRecommendations(new UserRecommendationRequest(user.getUid(),num));
        if(recommendations.size()==0){
            String randomGenres = user.getPrefgenres().split(",")[new Random().nextInt(user.getPrefgenres().split(",").length)];
            recommendations = recommenderService.getTopGenresRecommendations(new TopGenresRecommendationRequest(randomGenres.split(" ")[0],num));
        }
        model.addAttribute("success",true);
        model.addAttribute("movies",movieService.getRecommendeMovies(recommendations));
        return model;
    }

    /**
     * 获取热门推荐
     * @param model
     * @return
     */
    @RequestMapping(value = "/hot", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getHotMovies(@RequestParam("num")int num, Model model) {
        List<Recommendation> recommendations = recommenderService.getHotRecommendations(new HotRecommendationRequest(num));
        model.addAttribute("success",true);
        model.addAttribute("movies",movieService.getRecommendeMovies(recommendations));
        return model;
    }

    /**
     * 获取投票最多的电影
     * @param model
     * @return
     */
    @RequestMapping(value = "/rate", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getRateMoreMovies(@RequestParam("num")int num, Model model) {
        List<Recommendation> recommendations = recommenderService.getRateMoreRecommendations(new RateMoreRecommendationRequest(num));
        model.addAttribute("success",true);
        model.addAttribute("movies",movieService.getRecommendeMovies(recommendations));
        return model;
    }

    /**
     * 获取新添加的电影
     * @param model
     * @return
     */
    @RequestMapping(value = "/new", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getNewMovies(@RequestParam("num")int num, Model model) {
        model.addAttribute("success",true);
        model.addAttribute("movies",movieService.getNewMovies(new NewRecommendationRequest(num)));
        return model;
    }

    /**
     * 获取电影详细页面相似的电影集合
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/same/{id}", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getSameMovie(@PathVariable("id")int id,@RequestParam("num")int num, Model model) {
        List<Recommendation> recommendations = recommenderService.getCollaborativeFilteringRecommendations(new MovieRecommendationRequest(id,num));
        model.addAttribute("success",true);
        model.addAttribute("movies",movieService.getRecommendeMovies(recommendations));
        return model;
    }


    /**
     * 获取单个电影的信息
     * @param id
     * @param model
     * @return
     */
    @RequestMapping(value = "/info/{id}", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getMovieInfo(@PathVariable("id")int id, Model model) {
        model.addAttribute("success",true);
        model.addAttribute("movie",movieService.findByMID(id));
        return model;
    }

    /**
     * 模糊查询电影
     * @param query
     * @param model
     * @return
     */
    @RequestMapping(value = "/search", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getSearchMovies(@RequestParam("query")String query, Model model) {
        List<Recommendation> recommendations = recommenderService.getContentBasedSearchRecommendations(new SearchRecommendationRequest(query,100));
        model.addAttribute("success",true);
        model.addAttribute("movies",movieService.getRecommendeMovies(recommendations));
        return model;
    }

    /**
     * 查询类别电影
     * @param category
     * @param model
     * @return
     */
    @RequestMapping(value = "/genres", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getGenresMovies(@RequestParam("category")String category, Model model) {
        List<Recommendation> recommendations = recommenderService.getContentBasedGenresRecommendations(new SearchRecommendationRequest(category,100));
        model.addAttribute("success",true);
        model.addAttribute("movies",movieService.getRecommendeMovies(recommendations));
        return model;
    }

    @RequestMapping(value = "/comment/{mid}", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model rateToMovie(@PathVariable("mid")int mid,@RequestParam("score")Double score,@RequestParam("comment")String comment,@RequestParam("uid")int uid, Model model) {

        Comment comment1 = new Comment();
        comment1.setTimestamp(System.currentTimeMillis() / 1000);
        comment1.setTag(comment);
        comment1.setScore(score);
        comment1.setMid(mid);
        comment1.setUid(uid);

        commentService.addComment(comment1);

        /*if(complete)
            logger.info(Constants.USER_RATING_LOG_PREFIX + ":" + user.getUid() +"|"+ id +"|"+ request.getScore() +"|"+ System.currentTimeMillis()/1000);*/
        model.addAttribute("success",true);
        return model;
    }


    @RequestMapping(value = "/tags/{mid}", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getMovieComments(@PathVariable("mid")int mid, Model model) {
        model.addAttribute("success",true);
        model.addAttribute("comments",commentService.getMovieComments(mid));
        return model;
    }

}
