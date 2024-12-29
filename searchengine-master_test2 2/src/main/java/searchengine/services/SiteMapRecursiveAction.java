package searchengine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicBoolean;

public class SiteMapRecursiveAction extends RecursiveAction {
    private final SiteMap siteMap;
    private final SiteEntity siteEntity;
    private final IndexingService indexingService;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaAndIndexService lemmaAndIndexService;
    private final static ConcurrentSkipListSet<String> linksPool = new ConcurrentSkipListSet<>();
    private static final Logger logger = LoggerFactory.getLogger(SiteMapRecursiveAction.class);
    private final AtomicBoolean stopRequested;

    public SiteMapRecursiveAction(SiteMap siteMap, SiteEntity siteEntity, IndexingService indexingService,
                                  PageRepository pageRepository, SiteRepository siteRepository,
                                  LemmaAndIndexService lemmaAndIndexService, AtomicBoolean stopRequested) {
        this.siteMap = siteMap;
        this.siteEntity = siteEntity;
        this.indexingService = indexingService;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaAndIndexService = lemmaAndIndexService;
        this.stopRequested = stopRequested;
    }

    @Override
    protected void compute() {
        if (stopRequested.get()) {
            updateSiteStatusFailed("Индексация остановлена пользователем.");
            return;
        }
        String url = siteMap.getUrl();

        if (!linksPool.contains(url)) {
            linksPool.add(url);
        } else {
            return;

        }
        ConcurrentSkipListSet<String> links = ParseHtml.getLinks(url);
        for (String link : links) {
            if (!linksPool.contains(link)) {
                linksPool.add(link);

                // получаем HTTP код и контент страницы

                    int code = ParseHtml.getHttpCode(link);
                    String content = ParseHtml.getContent(link);

                    if (content == null || content.isBlank()) {
                        throw new IllegalStateException("Содержимое страницы пустое: " + url);
                    }
                    Page indexingPage = new Page();
                    indexingPage.setSiteEntity(siteEntity);
                    indexingPage.setPath(link);
                    indexingPage.setCode(code);
                    indexingPage.setContent(content);

                    SiteEntity siteEntityPage = siteRepository.findById(siteEntity.getId()).orElseThrow();
                    System.out.println(siteEntity.getId());
                    siteEntityPage.setStatusTime(new Date());
                    siteRepository.saveAndFlush(siteEntityPage);
                    pageRepository.save(indexingPage);
                    lemmaAndIndexService.processPageContent(indexingPage);

                    siteMap.addChildren(new SiteMap(link));

            }
        }
        //Рекурсивный обход дочерних страниц
        List<SiteMapRecursiveAction> taskList = new ArrayList<>();
        for (SiteMap child : siteMap.getSiteMapChildrens()) {
            if (!stopRequested.get()) {
                SiteMapRecursiveAction task = new SiteMapRecursiveAction(child, siteEntity,
                        indexingService, pageRepository,
                        siteRepository, lemmaAndIndexService,
                        stopRequested);
                task.fork();
                taskList.add(task);
            }
        }

        // Ожидаем завершения всех задач
        for (SiteMapRecursiveAction task : taskList) {
            task.join();
        }
        // Завершаем рекурсию только на верхнем уровне
        if (siteMap.getUrl().equals(siteEntity.getUrl())) {
            updateSiteStatus(); // Обновляем статус сайта после завершения обработки всех страниц
        }
    }
    private void updateSiteStatus() {
        siteEntity.setStatusTime(new Date());
        siteEntity.setStatus(Status.INDEXED); // Пример: установка статуса "INDEXED"
        siteRepository.saveAndFlush(siteEntity);  // Сохраняем изменения в базу данных
    }

    private void handlerError(String url, Exception ex) {
        logger.error("Ошибка при обработке страницы {}: {}", url, ex.getMessage());
        Page errorPage = new Page();
        errorPage.setSiteEntity(siteEntity);
        errorPage.setPath(url);
        errorPage.setCode(500);
        errorPage.setContent("Ошибка загрузки страницы: " + ex.getMessage());
        pageRepository.save(errorPage);
    }

    private void updateSiteStatusFailed(String errorMessage) {
        siteEntity.setStatusTime(new Date());
        siteEntity.setStatus(Status.FAILED); // Устанавливаем статус "FAILED"
        siteEntity.setLastErrorText(errorMessage); // Устанавливаем текст ошибки
        siteRepository.saveAndFlush(siteEntity); // Сохраняем изменения в базе данных
    }

}



