package net.harunote.hellomessagequeue.step5;

import org.springframework.stereotype.Component;

@Component
public class CustomExceptionHandler {

    private final LogPublisher logPublisher;

    public CustomExceptionHandler(LogPublisher logPublisher) {
        this.logPublisher = logPublisher;
    }

    // 에러나 로그 처리
    public void handleException(Exception e) {
        String message = e.getMessage();

        String routingKey;

        if (e instanceof NullPointerException) {
            routingKey = "error";
        } else if (e instanceof IllegalArgumentException) {
            routingKey = "warn";
        } else {
            routingKey = "error";
        }

        logPublisher.publish(routingKey, "Exception이 발생했음 : " + message);
    }

    // 메시지 처리
    public void handleMessage(String message) {
        String routingKey = "info";
        logPublisher.publish(routingKey, "Info Log : "  + message);
    }
}
