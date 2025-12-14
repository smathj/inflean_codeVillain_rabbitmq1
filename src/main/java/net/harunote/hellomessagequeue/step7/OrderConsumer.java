package net.harunote.hellomessagequeue.step7;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class OrderConsumer {

    private static final int MAX_RETRIES = 3;   // 총 시도 제한 수
    private int retryCount = 0;

    @RabbitListener(queues = RabbitMQConfig.ORDER_COMPLETED_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void processOrder(String message, Channel channel, @Header("amqp_deliveryTag") long tag) {

        try {
            // 실패 유발
            if("fail".equalsIgnoreCase(message)) {
                if (retryCount < MAX_RETRIES) {
                    System.out.println("#### Fail & Retry = " + retryCount);
                    retryCount++;
                    throw new RuntimeException(message);
                } else {
                    System.err.println("#### 최대 횟수 초과, DLQ 이동 시킴");
                    retryCount = 0;

                    /**
                     * basicNack(deliveryTag, multiple, requeue)
                     * - deliveryTag: 메시지 고유 식별자
                     * - multiple: false = 이 메시지만, true = 이 태그 이하 모든 메시지
                     * - requeue: false = 큐에 다시 안 넣음 (DLQ로 이동), true = 큐에 다시 넣음 (재시도)
                     */
                    //! DLQ 로 이동
                    channel.basicNack(tag, false, false);
                    return;
                }
            }

            // 성공 처리
            System.out.println("# 성공 : " + message);

            /**
             * basicAck(deliveryTag, multiple)
             * - deliveryTag: 메시지 고유 식별자
             * - multiple: false = 이 메시지만 확인, true = 이 태그 이하 모든 메시지 확인
             */
            channel.basicAck(tag, false);
            retryCount = 0;

        } catch(Exception e) {
            System.err.println("# error 발생 : " + e.getMessage());
            try {
                /**
                 * basicReject(deliveryTag, requeue)
                 * - deliveryTag: 메시지 고유 식별자
                 * - requeue: true = 큐에 다시 넣음 (재시도), false = 버림 (DLQ로 이동)
                 * - basicNack와 차이점: reject는 단일 메시지만 처리 (multiple 옵션 없음)
                 */
                //? 큐에 다시 넣음
                channel.basicReject(tag, true);
            } catch(Exception e1) {
                System.err.println("# fail & reject message : " + e1.getMessage());
            }
        }
    }
}
