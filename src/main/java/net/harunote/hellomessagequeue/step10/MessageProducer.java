package net.harunote.hellomessagequeue.step10;

import jakarta.transaction.Transactional;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class MessageProducer {
    private final RabbitTemplate rabbitTemplate;
    private final StockRepository stockRepository;

    public MessageProducer(RabbitTemplate rabbitTemplate, StockRepository stockRepository) {
        this.rabbitTemplate = rabbitTemplate;
        this.stockRepository = stockRepository;
    }


    @Transactional
    public void sendMessage(StockEntity stockEntity, boolean testCase) {
        stockEntity.setProcessed(false);
        stockEntity.setCreatedAt(LocalDateTime.now());
        StockEntity entity = stockRepository.save(stockEntity);

        System.out.println("[producer entity] : " + entity);

        if (stockEntity.getUserId() == null || stockEntity.getUserId().isEmpty()) {
            throw new RuntimeException("User id is required");
        }

        try {
            // 메시지를 rabbitmq 에 전송
            CorrelationData correlationData = new CorrelationData(entity.getId().toString());
            rabbitTemplate.convertAndSend(
                    testCase ? "nonExistentExchange" : RabbitMQConfig.EXCHANGE_NAME,
                    testCase ? "invalidRoutingKey" : RabbitMQConfig.ROUTING_KEY,
                    entity,
                    correlationData
            );

            if (correlationData.getFuture().get(5, TimeUnit.SECONDS).isAck()) {
                System.out.println("[producer correlationData] 성공" + entity);
                entity.setProcessed(true);
                stockRepository.save(entity);
            } else {
                throw new RuntimeException("# confirm 실패 - 롤백");
            }

        } catch (Exception e) {
            System.out.println("[producer exception fail] : " + e);
            throw new RuntimeException(e);
        }
    }
}
