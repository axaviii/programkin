package searchengine.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class IndexingService {
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final SitesList sitesList;
    private final LemmaFinder lemmaFinder;
    private AtomicBoolean stopRequested = new AtomicBoolean(false);
    private final ParseHtml parseHtml;
    private final LemmaAndIndexService lemmaAndIndexService;
    private final SiteManagementService siteManagementService;
    private ForkJoinPool forkJoinPool;

    public IndexingService(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList, LemmaFinder lemmaFinder, ParseHtml parseHtml, LemmaAndIndexService lemmaAndIndexService, SiteManagementService siteManagementService) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;
        this.lemmaFinder = lemmaFinder;
        this.parseHtml = parseHtml;
        this.lemmaAndIndexService = lemmaAndIndexService;
        this.siteManagementService = siteManagementService;
    }

    @Async
    public void startIndexing() {
        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdown();
        }
        forkJoinPool = new ForkJoinPool();
        stopRequested.set(false);

        siteRepository.deleteAll();
        try {
            List<searchengine.config.Site> sites = sitesList.getSites();
            for (searchengine.config.Site siteUrl : sites) {
                if (stopRequested.get()) {
                    break;
                }
                logger.info("Начало индексации сайта: {}", siteUrl.getUrl());

                SiteEntity existSiteEntity = siteRepository.findByUrl(siteUrl.getUrl());

//                if (existSiteEntity != null) {
//                    siteManagementService.deleteSiteData(existSiteEntity);
//                }
                //Создаем новую запись для сайта со статусом Indexing
                SiteEntity siteEntity = new SiteEntity();
                siteEntity.setUrl(siteUrl.getUrl());
                siteEntity.setName(siteUrl.getName());
                siteEntity.setStatus(Status.INDEXING);
                siteEntity.setStatusTime(new Date());
                logger.info("Создание новой записи для сайта: {}", siteUrl.getUrl());
                siteRepository.save(siteEntity);
                logger.info("Сохраненный сайт: ID={}, URL={}", siteEntity.getId(), siteEntity.getUrl());


                //Запускаем индексацию сайта
                SiteMap siteMap = new SiteMap(siteUrl.getUrl());
                SiteMapRecursiveAction task = new SiteMapRecursiveAction(siteMap, siteEntity,
                        this, pageRepository, siteRepository, lemmaAndIndexService,
                        stopRequested,siteManagementService);
                logger.info("Запуск индексации для сайта: {}", siteUrl.getUrl());
                forkJoinPool.invoke(task);
                // task.join();

                if (!stopRequested.get()) {
                    //Обновляем статус на Indexed после завершения индексации
                    updateSiteStatus(siteEntity, Status.INDEXED, null);
                } else {
                    updateSiteStatus(siteEntity, Status.FAILED, "Индексация остановлена пользователем");
                }
            }
        } finally {
            stopRequested.set(false);
            if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
                forkJoinPool.shutdown();
            }
            SiteMapRecursiveAction.clearLinksPool();
            logger.info("Индексация завершена. Программа готова к дальнейшим действиям.");
        }
    }

    private void updateSiteStatus(SiteEntity siteEntity, Status status, String errorText) {
        siteEntity.setStatus(status);
        siteEntity.setStatusTime(new Date());
        siteEntity.setLastErrorText(errorText);
        siteRepository.save(siteEntity);
    }

    private void handleSiteIndexingFailure(searchengine.config.Site siteUrl, Exception e) {
        logger.error("Ошибка при индексации сайта: {} ", siteUrl.getUrl(), e);
        // если произошла ошибка меняем статус на Failed и сохраняем ошибку

        SiteEntity faildSiteEntity = siteRepository.findByUrl(siteUrl.getUrl());
        if (faildSiteEntity != null) {
            faildSiteEntity.setStatus(Status.FAILED);
            faildSiteEntity.setStatusTime(new Date());
            faildSiteEntity.setLastErrorText(e.getMessage());
            siteRepository.save(faildSiteEntity);
        }
    }

    @Transactional
    public void stopIndexing() {

        if(stopRequested.get()) {
         return;
        }
        stopRequested.set(true);
        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdown();
        }
        updateFailedSites();
        logger.info("Индексация остановлена пользователем");
    }

    private void updateFailedSites() {
        List<SiteEntity> indexingSitesEntities = siteRepository.findAllByStatus(Status.INDEXED);
        for (SiteEntity siteEntity : indexingSitesEntities) {
            siteEntity.setStatus(Status.FAILED);
            siteEntity.setStatusTime(new Date());
            siteEntity.setLastErrorText("Индексация остановлена пользователем");
            siteRepository.save(siteEntity);
        }
    }
}
