package dev.george.biolink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("dev.george.biolink.model")
public class BiolinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(BiolinkApplication.class, args);
    }

}
