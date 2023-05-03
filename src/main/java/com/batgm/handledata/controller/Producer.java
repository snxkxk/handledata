package com.batgm.handledata.controller;

import com.batgm.handledata.annotation.HandlingTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Queue;
import javax.jms.Topic;

/**
 * activeMq 生产端测试
 */
@RestController
@RequestMapping("mq")
public class Producer {
    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue queue;

    @Autowired
    private Topic topic;

    //发送queue类型消息
    @GetMapping("/queue")
    @HandlingTime("Producer.sendQueueMsg")
    public String sendQueueMsg(@RequestParam(value = "msg",required = true) String msg,Long ct){
        long count = 1000;
        if(ct!=null){
            count = ct;
        }
        for (int i=0;i<count;i++){
            jmsTemplate.convertAndSend(queue, msg+i);
        }
        return String.format("you put %d messages, your message is : %s",ct,msg);

    }

    //发送topic类型消息
    @GetMapping("/topic")
    public String sendTopicMsg(@RequestParam(value = "msg",required = true) String msg){
        jmsTemplate.convertAndSend(topic, msg);
        return String.format("this is handle topic msg, your message is : %s",msg);
    }

}
