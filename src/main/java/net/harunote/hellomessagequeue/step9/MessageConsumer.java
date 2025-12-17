package net.harunote.hellomessagequeue.step9;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MessageConsumer {

    private final StockRepository stockRepository;


    @RabbitListener(queues = "transactionQueue")
    public void receiveTransaction(StockEntity stockEntity) {

        System.out.println("# received message  = " + stockEntity);

        try {
            stockEntity.setProcessed(true);
            stockEntity.setUpdatedAt(LocalDateTime.now());
            stockRepository.save(stockEntity);
            System.out.println("# StockEntity 저장 완료 ");
        } catch(Exception e) {
            System.out.println("# Entity 수정 에러 " + e.getMessage());
            throw e; // todo 메시지를 데드레터 큐에 집어넣는다..
        }


    }
}
