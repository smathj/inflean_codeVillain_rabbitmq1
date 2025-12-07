package net.harunote.hellomessagequeue.step5;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/logs")
public class LogController {

    private final CustomExceptionHandler exceptionHandler;

    public LogController(CustomExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    @GetMapping("/error")
    public ResponseEntity<String> errorAPI() {
        try {
            String value = null;
            value.getBytes(); // null pointer
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        return ResponseEntity.ok("Controller Nullpointer Exception 처리 ");
    }


    @GetMapping("/warn")
    public ResponseEntity<String> warnAPI() {
        try {
            throw new IllegalArgumentException("invalid argument입니다.");
        } catch (Exception e) {
            exceptionHandler.handleException(e);
        }
        return ResponseEntity.ok("Controller IllegalArugument Exception 처리 ");
    }

    @PostMapping("/info")
    public ResponseEntity<String> infoAPI(@RequestBody String message) {
        exceptionHandler.handleMessage(message);
        return ResponseEntity.ok("Controller Info log 발송 처리 ");
    }

}
