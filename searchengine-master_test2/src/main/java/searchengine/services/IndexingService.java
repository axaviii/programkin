package searchengine.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private ForkJoinPool forkJoinPool;

    public IndexingService(SiteRepository siteRepository, PageRepository pageRepository, SitesList sitesList, LemmaFinder lemmaFinder, ParseHtml parseHtml, LemmaAndIndexService lemmaAndIndexService) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.sitesList = sitesList;
        this.lemmaFinder = lemmaFinder;
        this.parseHtml = parseHtml;
        this.lemmaAndIndexService = lemmaAndIndexService;
    }



    public void startIndexing() {
        forkJoinPool = new ForkJoinPool();
        stopRequested.set(false);

        try {
            List<searchengine.config.Site> sites = sitesList.getSites();
            for (searchengine.config.Site siteUrl : sites) {
                if (stopRequested.get()) {
                    break;
                }
                logger.info("Начало индексации сайта: {}", siteUrl.getUrl());
                try {
                    Site existSite = siteRepository.findByUrl(siteUrl.getUrl());
                    if (existSite != null) {
                        logger.info("Удаление существующего сайта: {}", existSite.getUrl());
                        pageRepository.deleteBySite(existSite);
                        siteRepository.deleteById(existSite.getId());
                    }
                    //Создаем новую запись для сайта со статусом Indexing
                    Site site = new Site();
                    site.setUrl(siteUrl.getUrl());
                    site.setName(siteUrl.getName());
                    site.setStatus(Status.INDEXING);
                    site.setStatusTime(new Date());
                    logger.info("Создание новой записи для сайта: {}", siteUrl.getUrl());
                    siteRepository.save(site);
                    logger.info("Сохраненный сайт: ID={}, URL={}", site.getId(), site.getUrl());


                    //Запускаем индексацию сайта
                    SiteMap siteMap = new SiteMap(siteUrl.getUrl());
                    SiteMapRecursiveAction task = new SiteMapRecursiveAction(siteMap, site,
                            this, pageRepository, siteRepository, lemmaAndIndexService,
                            stopRequested);
                    logger.info("Запуск индексации для сайта: {}", siteUrl.getUrl());
                    forkJoinPool.invoke(task);
                    task.join();

                    if (!stopRequested.get()) {
                        //Обновляем статус на Indexed после завершения индексации
                       updateSiteStatus(site, Status.INDEXED, null);
                    }else {
                        updateSiteStatus(site, Status.FAILED,"Индексация остановлена пользователем");
                    }

                } catch (Exception e) {
                    handleSiteIndexingFailure(siteUrl, e);
                }
            }
        } finally {
            stopRequested.set(false);
            if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
                forkJoinPool.shutdown();
            }
        }
    }

    private void updateSiteStatus(Site site, Status status, String errorText) {
        site.setStatus(status);
        site.setStatusTime(new Date());
        site.setLastErrorText(errorText);
        siteRepository.save(site);
    }

    private void handleSiteIndexingFailure(searchengine.config.Site siteUrl, Exception e) {
        logger.error("Ошибка при индексации сайта: {} ", siteUrl.getUrl(), e);
        // если произошла ошибка меняем статус на Failed и сохраняем ошибку

        Site faildSite = siteRepository.findByUrl(siteUrl.getUrl());
        if (faildSite != null) {
            faildSite.setStatus(Status.FAILED);
            faildSite.setStatusTime(new Date());
            faildSite.setLastErrorText(e.getMessage());
            siteRepository.save(faildSite);
        }
    }


    @Transactional
    public void stopIndexing(AtomicBoolean stopRequested) {
        stopRequested.set(true);
        if (forkJoinPool != null && !forkJoinPool.isShutdown()) {
            forkJoinPool.shutdown();
        }
        updateFailedSites();
        logger.info("Индексация остановлена пользователем");
    }

    private void updateFailedSites() {
        List<Site> indexingSites = siteRepository.findAllByStatus(Status.INDEXED);
        for (Site site : indexingSites) {
            site.setStatus(Status.FAILED);
            site.setStatusTime(new Date());
            site.setLastErrorText("Индексация остановлена пользователем");
            siteRepository.save(site);
        }
    }
}
