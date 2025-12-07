package net.harunote.hellomessagequeue.step1;

import org.springframework.stereotype.Component;

/**
 * 컨슈머 역할, 메세지를 소모함!
 */
@Component
public class Receiver {

    public void receiveMessage(String message) {
        System.out.println("message = " + message);
    }

}
