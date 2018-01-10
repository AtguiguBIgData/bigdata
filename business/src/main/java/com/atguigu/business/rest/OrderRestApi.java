/*
 * Copyright (c) 2018. wuyufei
 */

package com.atguigu.business.rest;

import com.atguigu.business.model.domain.Order;
import com.atguigu.business.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@RequestMapping("rest/order")
@Controller
public class OrderRestApi {


    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/pay", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model pay(@RequestParam("uid") int uid, @RequestParam("mids") String mids, @RequestParam("sum") double sum, Model model) {
        Order order = new Order();
        order.setTime(System.currentTimeMillis() / 1000);
        order.setMids(mids);
        order.setSum(sum);
        order.setUid(uid);

        model.addAttribute("success",orderService.payOrder(order));
        return model;
    }

    @RequestMapping(value = "/", produces = "application/json", method = RequestMethod.GET )
    @ResponseBody
    public Model getall(Model model) {
        model.addAttribute("success",true);
        model.addAttribute("orders",orderService.getAllOrders());
        return model;
    }

    @RequestMapping(value = "/{uid}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getByUid(@PathVariable("uid") int uid, Model model) {
        model.addAttribute("success",true);
        model.addAttribute("orders",orderService.getOrdersByUid(uid));
        return model;
    }

    @RequestMapping(value = "/detail/{oid}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model getDetail(@PathVariable("oid") int oid,Model model) {
        model.addAttribute("success",true);
        model.addAttribute("movies",orderService.getOrderDetail(oid));
        return model;
    }

    @RequestMapping(value = "/remove/{oid}", produces = "application/json", method = RequestMethod.GET)
    @ResponseBody
    public Model removeOrder(@PathVariable("oid") int oid,Model model) {
        model.addAttribute("success",true);
        orderService.removeOrder(oid);
        return model;
    }
}
