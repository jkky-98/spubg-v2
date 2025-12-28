package com.junki.demo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
@Configuration
@Slf4j
public class RabbitMQConfig {

    public static final String MATCH_PROCESS_QUEUE = "match.process.queue";

    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitmqHost;

    @Value("${spring.rabbitmq.port:5672}")
    private int rabbitmqPort;

    @Bean
    public Queue matchProcessQueue() {
        log.info("[RabbitMQConfig] Queue 생성: {}", MATCH_PROCESS_QUEUE);
        return new Queue(MATCH_PROCESS_QUEUE, true); // durable
    }

    @Bean
    public JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .build();
    }

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {

        log.info("[RabbitMQConfig] JacksonJsonMessageConverter 설정 완료");
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        log.info("[RabbitMQConfig] RabbitTemplate 초기화. RabbitMQ Host: {}:{}", rabbitmqHost, rabbitmqPort);
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter(jsonMapper()));
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        log.info("[RabbitMQConfig] RabbitListenerContainerFactory 초기화");
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter(jsonMapper()));
        factory.setConcurrentConsumers(3); // 동시에 3개까지 처리
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
}

