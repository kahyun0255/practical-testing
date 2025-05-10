package sample.cafekiosk.spring.client.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MailSendClient {
    public boolean sendEmail(String fromEmail, String toEmail, String subject, String content) {
        log.info("메일 전송");
        throw new IllegalArgumentException("메일 전송");
    }

//    public void a(){ //기능이 많은 컴포넌트라고 가정(Spy)
//        log.info("a");
//    }
//
//    public void b(){
//        log.info("b");
//    }
//
//    public void c(){
//        log.info("c");
//    }
}
