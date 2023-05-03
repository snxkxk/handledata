package com.batgm.handledata.controller;

import com.batgm.handledata.constants.Constant;
import com.batgm.handledata.entity.StudyActionRecord;
import com.batgm.handledata.utils.CookieUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author yqq
 * @createdate 2020/7/23
 */
@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @RequestMapping(value = "getUserData", method = RequestMethod.GET)
    @ResponseBody
    public String getUserData(HttpServletRequest request,
                             HttpServletResponse response, @RequestParam(value = "value", required = false) String value) {
        String data = request.getSession().getId();
        StudyActionRecord record = (StudyActionRecord) request.getSession().getAttribute("record");
        if(record!=null){
            data += "\n record:" + record.toString();
        }
        return data;
    }

    @RequestMapping(value = "/login")
    @ResponseBody
    public String login(HttpServletRequest request,HttpServletResponse response, @RequestParam(value = "key", required = false) String key) {

        String msg = null;

        if("YQQ".equals(key)){
            StudyActionRecord record = new StudyActionRecord();
            record.setLoginPalce("YQQ");
            record.setAction("countdown:btn_login");
            record.setCreateTime(new Date());
            request.getSession().setAttribute("record", record);
            HashOperations<String, Object, Object> ops = stringRedisTemplate.opsForHash();
            ops.put("countdown:btn_login", "countdown:btn_login", "countdown:btn_login");
            stringRedisTemplate.expire("countdown:btn_login", 1, TimeUnit.HOURS);
     /*       try {
                response.sendRedirect("/swagger-ui.html");
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            msg = "success";
        }else {
            msg = "error";
        }

        return msg;
    }

    @RequestMapping(value = "/logout")
    @ResponseBody
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        if(session!=null){
            session.invalidate();
            stringRedisTemplate.delete("countdown:btn_login");
            Cookie cookie = CookieUtils.get(request, Constant.SESSION);
            if(cookie!=null){
                String sessionId = Constant.SPRING_SESSION_SESSIONS+cookie.getValue();
                String sessionIdEXPIRES = Constant.SPRING_SESSION_EXPIRES+cookie.getValue();
                if (stringRedisTemplate.hasKey(sessionId)) {
                    stringRedisTemplate.delete(sessionId);
                }
                if (stringRedisTemplate.hasKey(sessionIdEXPIRES)) {
                    stringRedisTemplate.delete(sessionIdEXPIRES);
                }
            }

        }

        return "success";
    }
}
