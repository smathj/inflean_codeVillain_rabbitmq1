package net.harunote.hellomessagequeue.step4;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {

    // step4 예제 추가
    public static final String FANOUT_EXCHANGE_FOR_NEWS = "newsExchange";

    public static final String JAVA_QUEUE = "javaQueue";
    public static final String SPRING_QUEUE = "springQueue";
    public static final String VUE_QUEUE = "vueQueue";

    @Bean
    public Queue javaQueue() {
        return new Queue(JAVA_QUEUE, false);    // 메세지는 volatile 로 설정 (비영속)
    }

    @Bean
    public Queue springQueue() {
        return new Queue(SPRING_QUEUE, false);    // 메세지는 volatile 로 설정 (비영속)
    }

    @Bean
    public Queue vueQueue() {
        return new Queue(VUE_QUEUE, false);    // 메세지는 volatile 로 설정 (비영속)
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        // 메세지를 수신하면 연결된 모든 큐로 브로드캐스트
        return new FanoutExchange(FANOUT_EXCHANGE_FOR_NEWS);
    }


    @Bean
    public Binding javaBinding(Queue javaQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(javaQueue).to(fanoutExchange);
    }

    @Bean
    public Binding springBinding(Queue springQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(springQueue).to(fanoutExchange);
    }

    @Bean
    public Binding vueBinding(Queue vueQueue, FanoutExchange fanoutExchange) {
        return BindingBuilder.bind(vueQueue).to(fanoutExchange);
    }

}
