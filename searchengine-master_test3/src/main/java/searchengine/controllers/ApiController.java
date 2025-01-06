package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.SearchResult;
import searchengine.services.IndexingService;
import searchengine.services.PageIndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final PageIndexingService pageIndexingService;
    private final SearchService searchService;
    private AtomicBoolean stopRequested = new AtomicBoolean(false);

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, PageIndexingService pageIndexingService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.pageIndexingService = pageIndexingService;
        this.searchService = searchService;
    }


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        if (!stopRequested.get()) {
            indexingService.startIndexing();
            return ResponseEntity.ok(new IndexingResponse());
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new IndexingResponse("Ошибка при запуске индексации"));
        }
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        try {
            indexingService.stopIndexing();
            return ResponseEntity.ok(new IndexingResponse());
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new IndexingResponse("Индексация не запущена" +
                    e.getMessage()));
        }
    }
    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam("url") String url){
        if(!pageIndexingService.isUrlWithinConfiguredSites(url)){
            String errorMessage = "Данная страница находится за пределами сайтов, указанных в конф файле";
            return ResponseEntity.badRequest().body(new IndexingResponse(errorMessage));
        }
        try {
            pageIndexingService.indexPage(url);

            return ResponseEntity.ok(new IndexingResponse());
        } catch (Exception e){
            String errorMessage = "Ошибка индексации страницы" + e.getMessage();
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new IndexingResponse(errorMessage));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResult> search(
            @RequestParam("query") String query,
            @RequestParam(required = false) String site,
            @RequestParam(required = false, defaultValue = "0") Integer offset,
            @RequestParam(required = false, defaultValue = "20") Integer limit)
    {
        SearchResult result = searchService.search(query, site, offset, limit);
        if (result.isResult()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }
    }
}
