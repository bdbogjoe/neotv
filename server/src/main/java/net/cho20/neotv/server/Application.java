package net.cho20.neotv.server;

import net.cho20.neotv.core.service.Storage;
import net.cho20.neotv.server.service.ProcessorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public ProcessorService getProcessorService(Storage storage, @Value("${app.code}") String code, @Value("${app.api}") String api, @Value("${app.groups}") String groups) {
        return new ProcessorService(storage, code, api, groups);
    }


}
