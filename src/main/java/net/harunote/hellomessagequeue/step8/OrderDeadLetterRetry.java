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
    public void processDeadLetter(String message) {
        System.out.println("[DLQ Received]: " + message);

        try {
            // "fail" 메시지를 수정하여 성공적으로 처리되도록 변경
            if ("fail".equalsIgnoreCase(message)) {
                message = "success";
                System.out.println("[DLQ] Message fixed: " + message);
            } else {
                // 이미 수정된 메시지는 다시 처리하지 않음
                System.err.println("[DLQ] Message already fixed. Ignoring: " + message);
                return;
            }

            // 수정된 메시지를 원래 큐로 다시 전송
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_TOPIC_EXCHANGE, "order.completed", message );

            System.out.println("[DLQ] Message requeued to original queue: " + message);
        } catch (Exception e) {
            System.err.println("[DLQ] Failed to reprocess message: " + e.getMessage());
        }
    }


}
