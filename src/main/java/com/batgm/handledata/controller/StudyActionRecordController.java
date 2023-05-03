package com.batgm.handledata.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.batgm.handledata.elasticsearch.BulkProcessorUtils;
import com.batgm.handledata.entity.StudyActionRecord;
import com.batgm.handledata.service.StudyActionRecordService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

/**
 * @author yqq
 * @createdate 2020/3/27
 */
@RequestMapping("studyActionRecord")
@Controller
public class StudyActionRecordController {

    private static   final Logger logger = LoggerFactory.getLogger(StudyActionRecordController.class);


    @Autowired
    private StudyActionRecordService studyActionRecordService;

    @RequestMapping("getStudyActionRecordById")
    public String getStudyActionRecordById(@RequestParam("id") BigInteger id, Model model){
        StudyActionRecord bean= studyActionRecordService.findById(id);
        if(bean!=null){
            logger.info("json_result:"+JSON.toJSONString(bean));
            model.addAttribute("obj",JSON.toJSON(bean));
        }
        return "studyActionRecord";
    }


    @RequestMapping("grid")
    public String grid( Model model){
        return "grid";
    }


    @RequestMapping("update")
    @ResponseBody
    public String update( Model model,String info){
        StudyActionRecord bean =   new StudyActionRecord();
          bean.setId(new BigInteger("66666"));
          bean.setUpdateTime(new Date());
          bean.setLoginPalce(info);
        studyActionRecordService.updateByPrimaryKeySelective(bean);
        return "update:"+bean.toString();
    }

    @GetMapping("list")
    @ResponseBody
    public String list( Model model,@RequestParam(value = "pageNum",required = false) Integer pageNum,@RequestParam(value = "pageSize",required = false) Integer pageSize){
        JSONObject json = new JSONObject();

        if(pageNum == null){
            pageNum = 0;
        }
        if(pageSize == null){
            pageSize = 5;
        }
        PageHelper.startPage(pageNum, pageSize);
        List<StudyActionRecord> list = studyActionRecordService.listAll();
        PageInfo<StudyActionRecord> page = new PageInfo<>(list);
        json.put("data",page);
        json.put("msg","查询成功");
        json.put("success",true);
        return json.toString();
    }


    @PostMapping("gridData")
    @ResponseBody
    public String gridData( Model model,@RequestParam(value = "pageNum",required = false) Integer pageNum,@RequestParam(value = "pageSize",required = false) Integer pageSize){
        JSONObject json = new JSONObject();

        if(pageNum == null){
            pageNum = 0;
        }
        if(pageSize == null){
            pageSize = 5;
        }
        PageHelper.startPage(pageNum, pageSize);
        List<StudyActionRecord> list = studyActionRecordService.listAll();
        PageInfo<StudyActionRecord> page = new PageInfo<>(list);
        json.put("current",page.getPageNum());
        json.put("rowCount",page.getPageSize());
        json.put("rows",page.getList());
        json.put("total",page.getTotal());
        return json.toString();
    }




}
