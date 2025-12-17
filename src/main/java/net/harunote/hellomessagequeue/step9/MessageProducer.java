package net.harunote.hellomessagequeue.step9;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MessageProducer {

    private final StockRepository stockRepository;
    private final RabbitTemplate rabbitTemplate;


    public void sendMessage(StockEntity stockEntity, String testCase) {

        rabbitTemplate.execute(channel -> {

            try {
                channel.txSelect(); /// 트랜잭션 시작

                stockEntity.setProcessed(false);
                stockEntity.setCreatedAt(LocalDateTime.now());
                StockEntity stockEntitySaved = stockRepository.save(stockEntity);

                System.out.println("Stock Saved = " + stockEntitySaved);


                // 메세지 발행
                rabbitTemplate.convertAndSend("transactionQueue", stockEntitySaved);

                if("fail".equalsIgnoreCase(testCase)) {
                    throw new RuntimeException("트랜잭션 작업중에 에러 발생");
                }

                channel.txCommit();
                System.out.println("트랜잭션이 정상적으로 처리 되었음!~");


            } catch(Exception e) {
                System.out.println("트랜잭션 실패 : " + e.getMessage());
                channel.txRollback();
                throw new RuntimeException("트랜잭션 롤백 완료", e);
            } finally {
                if(channel != null) {
                    try {
                        channel.close();
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            return null;

        });

    }

}
