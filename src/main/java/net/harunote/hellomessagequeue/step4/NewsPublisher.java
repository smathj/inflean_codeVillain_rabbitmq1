package net.harunote.hellomessagequeue.step4;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class NewsPublisher {
    private final RabbitTemplate rabbitTemplate;

    public NewsPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }


    /**
     * 공통 호출 함수
     */
    private String publishMessage(String news, String messageSuffix) {

        String message = news + messageSuffix;

        // 하나의 팬아웃에게 전달하면 세개의 모든 큐에 전송됨
        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE_FOR_NEWS, news, message);

        System.out.println("News Published: " + message);
        return message;
    }

    public String publish(String news) {
        return publishMessage(news, " 관련 새 소식이 있어요!");
    }

    public String publishAPI(String news) {
        return publishMessage(news, " - 관련 새 소식이 나왔습니다. (API)");
    }
}
