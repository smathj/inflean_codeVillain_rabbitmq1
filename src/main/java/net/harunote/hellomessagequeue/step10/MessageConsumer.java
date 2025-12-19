package net.harunote.hellomessagequeue.step10;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OptionalDataException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class MessageConsumer {
    private final StockRepository stockRepository;

    public MessageConsumer(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void receiveMessage(StockEntity stock,
                               @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag,
                               Channel channel) {
        try {
            System.out.println("[Consumer] " + stock);
            Thread.sleep(200);
            Optional<StockEntity> optionalStock = stockRepository.findById(stock.getId());
            if (optionalStock.isPresent()) {
                StockEntity stockEntity = optionalStock.get();
                stockEntity.setUpdatedAt(LocalDateTime.now());
                stockRepository.save(stockEntity); // 업데이트
                System.out.println("[Save Entity Consumer] " + stockEntity);
            } else {
                throw new RuntimeException("Stock not found");
            }

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            System.out.println("[Consumer Error] " + e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);

            } catch (IOException ex) {
                System.out.println("[Consumer send nack] " + ex.getMessage());
                //throw new RuntimeException(ex);
            }
        }
    }
}
