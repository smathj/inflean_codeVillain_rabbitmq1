package net.harunote.hellomessagequeue.step8;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDeadLetterRetry {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.DLQ)
    public void processDlqMessage(String failedMessage) {

        try {

            System.out.println("[DLQ Received]: " + failedMessage);

            // 실패한 메시지를 성공 메시지로 변경
            String message = "success";

            // 수정된 메시지를 원래 큐로 재전송
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_TOPIC_EXCHANGE,
                    "order.completed.shipping",
                    message);

            System.out.println("Message successfully reprocessed : " + message);

        } catch (Exception e) {
            System.err.println("Error processing DLQ message : " + e);
        }
    }


}
