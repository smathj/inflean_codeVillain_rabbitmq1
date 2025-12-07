package net.harunote.hellomessagequeue.step2;


import org.springframework.stereotype.Component;

@Component
public class WorkQueueConsumer {

    public void workQueueTask(String message) {

        String[] messageParts = message.split("\\|");

        String originMessage = messageParts[0];

        int duration = Integer.parseInt(messageParts[1].trim());

        System.out.println("# Consumer Received: " + originMessage + " (duration: " + duration + " ms)");


        try {
            // duration을 초 단위로 변환
            int seconds = duration / 1000;

            for (int i = 0; i <= seconds; i++) {

                // 1초 대기
                Thread.sleep(1000);

                // 진행 상태 출력
                System.out.print(".");

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n[X] " + originMessage + " Completed!!");

    }
}
