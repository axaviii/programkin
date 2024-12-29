package searchengine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import searchengine.services.IndexServiceTest;
import searchengine.services.IndexingService;

import java.util.concurrent.atomic.AtomicBoolean;

@SpringBootApplication
public class Application implements CommandLineRunner {
    private final IndexingService indexingService;

    public Application(IndexingService indexingService) {
        this.indexingService = indexingService;
    }


    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);

    }

    @Override
    public void run(String... args) {

       indexingService.startIndexing();
    }
}