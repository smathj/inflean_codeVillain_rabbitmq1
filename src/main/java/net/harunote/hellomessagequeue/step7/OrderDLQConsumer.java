package net.harunote.hellomessagequeue.step7;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderDLQConsumer {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.DLQ)
    public void process(String message) {

        System.out.println("DLQ Message Receive: " + message);


        try {
            String fixMessage = "success";

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    "order.completed.shipping",
                    fixMessage
            );

            System.out.println("DLQ Message Sent: " + fixMessage);

        } catch(Exception e) {
            System.err.println("### [DLQ Consumer Error] " + e.getMessage());
        }

    }


}
