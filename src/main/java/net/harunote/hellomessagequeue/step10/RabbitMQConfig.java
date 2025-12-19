package net.harunote.hellomessagequeue.step10;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "transactionQueue";
    public static final String EXCHANGE_NAME = "transactionExchange";
    public static final String ROUTING_KEY = "transactionRoutingKey";


    @Bean
    public Queue transactionQueue() {
        return QueueBuilder.durable(QUEUE_NAME)
                .withArgument("x-dead-letter-exchange", "")                     // 데드레터 익스체인지
                .withArgument("x-dead-letter-routing-key", "deadLetterQueue")   // 데드 레터 라우팅 키 (큐이름 패턴)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue("deadLetterQueue");
    }

    @Bean
    public DirectExchange transactionExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Binding transactionBinding(Queue transactionQueue, DirectExchange transactionExchange) {
        return BindingBuilder
                .bind(transactionQueue)
                .to(transactionExchange)
                .with(ROUTING_KEY);
    }


    /**
     *
     * ////////////////////////////////
     * 메시지 직렬화/역직렬화 담당
     * Java 객체 ↔ RabbitMQ 메시지 간 변환을 처리.
     * ////////////////////////////////
     *
     * Producer 측: Java 객체 → JSON/byte[] 로 변환하여 전송
     * Consumer 측: JSON/byte[] → Java 객체로 복원
     * 기본값은 SimpleMessageConverter (byte[], String만 지원)
     *
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }


    /**
     * ////////////////////////////////
     * 메시지 발송(Producing) 담당
     * ////////////////////////////////
     *
     * RabbitMQ로 메시지를 보내는 핵심 클래스. JdbcTemplate처럼 boilerplate 코드를 줄여주는 Template 패턴.
     *
     *
     * convertAndSend(): 객체를 변환하고 전송
     * send(): Message 객체 직접 전송
     * sendAndReceive(): RPC 패턴 (동기 응답 대기)
     *
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);   // JSON 변환기 등록
        rabbitTemplate.setMandatory(true);                      // ReturnCallback 활성화


        /// confirmCallBack 설정 (Exchange 도착 확인)
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if(ack) {
                System.out.println("### [Message confirmed]: " + (correlationData != null ? correlationData.getId() : "null"));


            } else {
                System.out.println("#### [Message not confirmed]: " + (correlationData != null ? correlationData.getId() : "null") + ", Reason: " + cause);


                // 실패 메시지에 대한 추가 처리 로직 (예: 로그 기록, DB 적재, 관리자 알림 등)
            }
        });


        /// ReturnCallBack 설정 (Queue 라우팅 실패 확인)
        rabbitTemplate.setReturnsCallback(returned -> {
            System.out.println("Return Message: " + returned.getMessage().getBody());
            System.out.println("Exchange : " + returned.getExchange());
            System.out.println("RoutingKey : " + returned.getRoutingKey());

            // 데드레터 설정 추가
        });


        return rabbitTemplate;
    }


    /**
     * ////////////////////////////////
     * 메시지 수신(Consuming) 컨테이너 생성 담당
     * ////////////////////////////////
     *
     * @RabbitListener가 붙은 메서드를 실행할 컨테이너를 만드는 팩토리.
     * @RabbitListener(queues = "order.queue") 이런식으로 사용
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL); // 수동 Ack 모드
        return factory;
    }

}
