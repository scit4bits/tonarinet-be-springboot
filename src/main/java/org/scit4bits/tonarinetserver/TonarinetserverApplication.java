package org.scit4bits.tonarinetserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 토나리넷 서버 애플리케이션의 메인 클래스
 */
@EnableJpaAuditing
@SpringBootApplication
public class TonarinetserverApplication {

    /**
     * 애플리케이션의 메인 진입점
     * @param args 커맨드 라인 인수
     */
    public static void main(String[] args) {
        SpringApplication.run(TonarinetserverApplication.class, args);
    }

}
