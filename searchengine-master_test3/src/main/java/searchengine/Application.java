package searchengine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import searchengine.services.IndexServiceTest;
import searchengine.services.IndexingService;

import java.util.concurrent.atomic.AtomicBoolean;


@SpringBootApplication
@EnableAsync
public class Application{

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }
}