package net.harunote.hellomessagequeue.step10;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/message")
public class TransactionController {

    private final MessageProducer messageProducer;

    public TransactionController(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    @PostMapping
    public ResponseEntity<String> publishMessage(@RequestBody StockEntity stockEntity, @RequestParam boolean testCase) {
        System.out.println("Publisher Confirms Send message : " + stockEntity);

        try {
            messageProducer.sendMessage(stockEntity, testCase);
            return ResponseEntity.ok("Publisher Confirms sent successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Publisher Confirms 트랜잭션 실패 : " + e.getMessage());
        }
    }
}
