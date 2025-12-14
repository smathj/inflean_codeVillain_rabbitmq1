package net.harunote.hellomessagequeue.step7;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ORDER_COMPLETED_QUEUE = "order_completed_queue";
    public static final String ORDER_EXCHANGE = "order_completed_exchange";
    public static final String DLQ = "deadLetterQueue";
    public static final String DLX = "deadLetterExchange";

    // 메인 Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(ORDER_EXCHANGE);
    }

    // Dead Letter Exchange - 실패한 메시지 라우팅용
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }

    // 메인 큐 - DLQ 연결됨
    @Bean
    public Queue orderQueue() {
        return QueueBuilder
                .durable(ORDER_COMPLETED_QUEUE)                   // "order_completed_queue" 이름으로 영구 큐 생성
                .withArgument("x-dead-letter-exchange", DLX)      // 실패시 이 Exchange로 보내라
                .withArgument("x-dead-letter-routing-key", DLQ)   // 그때 이 라우팅 키 써라
                .ttl(5000)                                        // 메시지 5초 지나면 자동 만료
                .build();
    }

    // Dead Letter Queue - 실패한 메시지 모이는 곳
    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DLQ);
    }

    // 메인 바인딩: Exchange → Queue
    @Bean
    public Binding orderCompletedBinding() {
        return BindingBuilder
                .bind(orderQueue())                             // 이 큐에
                .to(orderExchange())                            // 이 Exchange 를 연결 한다
                .with("order.completed.#");           // 조건은 저런 라우팅 키일때만
    }

    // DLQ 바인딩: DLX → DLQ
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())      // 이 큐에
                .to(deadLetterExchange())     // 이 Exchange를 연결하는데
                .with(DLQ);                   // 이 라우팅 키로 올 때만
    }
}
