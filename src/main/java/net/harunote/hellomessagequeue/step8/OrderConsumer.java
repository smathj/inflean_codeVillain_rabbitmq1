package net.harunote.hellomessagequeue.step8;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final RabbitTemplate rabbitTemplate;
    private final RetryTemplate retryTemplate;


    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE)
    public void consume(String message) {
        retryTemplate.execute(context -> {

            // 이 람다식 블록 전체가 재시도 단위

            try {

                System.out.println("# 리시브 메시지 : " + message + " # retry : " + context.getRetryCount());

                // 실패 조건
                if ("fail".equalsIgnoreCase(message)) {
                    throw new RuntimeException(message);
                }
                System.out.println("# 메시지 처리 성공 " + message);


            } catch (Exception e) {

                if (context.getRetryCount() >= 2) {

                    rabbitTemplate.convertAndSend(
                        RabbitMQConfig.ORDER_TOPIC_DLX,
                        RabbitMQConfig.DEAD_LETTER_ROUTING_KEY,
                        message
                    );


                } else {
                    throw e;
                }
            }
            return null;
        });
    }
}
