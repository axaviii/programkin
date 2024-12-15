package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.PageIndexingService;
import searchengine.services.StatisticsService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final PageIndexingService pageIndexingService;
    private AtomicBoolean stopRequested = new AtomicBoolean(false);

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, PageIndexingService pageIndexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.pageIndexingService = pageIndexingService;
    }


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<String> startIndexing() {
        if (!stopRequested.get()) {
            indexingService.startIndexing();
            return ResponseEntity.ok("Indexing started");
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при" +
                    "запуске индексации");
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<String> stopIndexing() {
        try {
            indexingService.stopIndexing();
            return ResponseEntity.ok("Indexing end");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Индексация не запущена" +
                    e.getMessage());
        }
    }
    @PostMapping("/indexPage")
    public ResponseEntity<Map<String, Object>> indexPage(@RequestParam("url") String url){
        Map<String, Object> response = new HashMap<>();
        if(!pageIndexingService.isUrlWithinConfiguredSites(url)){
            response.put("result", false);
            response.put("error", "Данная страница находится за пределами сайтов, указанных в конф файле");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            pageIndexingService.indexPage(url);
            response.put("result", true);
            return ResponseEntity.ok(response);
        } catch (Exception e){
            response.put("result", false);
            response.put("error", "Ошибка индексации" + e.getMessage());
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
