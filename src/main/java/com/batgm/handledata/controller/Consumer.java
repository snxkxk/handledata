package com.batgm.handledata.controller;

import com.batgm.handledata.annotation.HandlingTime;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * activeMq 消费端测试
 */
@Component
public class Consumer {

    //接收queue类型消息
    //destination对应配置类中ActiveMQQueue("springboot.queue")设置的名字
    @JmsListener(destination="handle.queue")
    public void ListenQueue(String msg){
        if(msg.contains("@")){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("接收到handle.queue消息：" + msg);
    }


    //接收topic类型消息
    //destination对应配置类中ActiveMQTopic("springboot.topic")设置的名字
    //containerFactory对应配置类中注册JmsListenerContainerFactory的bean名称
    @JmsListener(destination="handle.topic", containerFactory = "jmsTopicListenerContainerFactory")
    public void ListenTopic(String msg){
        System.out.println("接收到handle.topic消息：" + msg);
    }

}
