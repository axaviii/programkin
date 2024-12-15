package searchengine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import searchengine.services.IndexServiceTest;
import searchengine.services.IndexingService;

import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
public class Application{

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }
}