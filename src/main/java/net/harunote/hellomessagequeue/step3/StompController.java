package net.harunote.hellomessagequeue.step3;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StompController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    public void sendMessage(NotificationMessage notificationMessage) {

        // 수신된 메세지를 브로드 캐스팅
        String message = notificationMessage.getMessage();

        System.out.println("[#] message = " + message);

        // 클라이언트에 메세지 브로드캐스트
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }

}
