package com.batgm.handledata.config;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Queue;
import javax.jms.Topic;

@Configuration
@EnableJms
public class ActiveMQConfig {


    //springboot默认只配置queue类型消息，如果要使用topic类型的消息，则需要配置该bean
    @Bean
    public JmsListenerContainerFactory jmsTopicListenerContainerFactory(ConnectionFactory connectionFactory){
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        //这里必须设置为true，false则表示是queue类型
        factory.setPubSubDomain(true);
        return factory;
    }

    /**
     * 配置用于异步发送的非持久化JmsTemplate
     */
    @Bean
    @Primary
    public JmsTemplate asynJmsTemplate(PooledConnectionFactory pooledConnectionFactory) {
        JmsTemplate template = new JmsTemplate(pooledConnectionFactory);
        template.setExplicitQosEnabled(true);
        template.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        return template;
    }
    @Bean
    public Queue queue() {
        return new ActiveMQQueue("handle.queue") ;
    }
    @Bean
    public Topic topic() {
        return new ActiveMQTopic("handle.topic") ;
    }
}
