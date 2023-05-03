package com.batgm.handledata.controller;

import com.batgm.handledata.constants.Constant;
import com.batgm.handledata.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yqq
 * @createdate 2020/7/23
 */
@Controller
@RequestMapping("/page")
public class PageController {
    @RequestMapping(value = "/test5")
    @ResponseBody
    public String test5(HttpServletRequest request) {
        int x = 1/0;
        return "test5";
    }

    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request) {
        return "index";
    }

    @RequestMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request, @RequestParam(value = "test", required = false) String test, @RequestBody(required = false) Map<String, Object> map) {
        String a = request.getQueryString();
        if (a != null) {
            a = a.substring(1);
        }else {
            a ="success..";
        }
        return a;
    }

    @RequestMapping("JVMtest")
    @ResponseBody
    public String JVMtest(@RequestParam(value = "type",required = false) Character type,@RequestParam(value = "c",required = false) Long c ) {
        String str = null;
        long startTime = System.currentTimeMillis();   //获取开始时间
        Map map = null;
        List mapList = new ArrayList<Map>();
        if(type == null){
            type = 'a';
        }
        if(c == null){
            c = 100L;
        }
        if('a'==type){
            for(int i= 0 ;i<c;i++){
                map = new HashMap();
                mapList.add(map);
            }
        }
        if('b'==type) {
            for (int i = 0; i < c; i++) {
                Map map1 = new HashMap();
                mapList.add(map1);
                map1 = null;
            }
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        long time = (endTime - startTime);

        str = String.format("执行成功,总耗时 %d 秒[%d毫秒]", time/1000,time);

        return str;

    }

    private void test() {
        System.out.println("yes!");
    }


}
