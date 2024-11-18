package searchengine.services;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import searchengine.model.*;

import searchengine.config.SitesList;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
public class IndexingService {
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);
    private static final int BATCH_SIZE = 3;

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SitesList sitesList;
    private final LemmaFinder lemmaFinder;
    private final DataSourceTransactionManager transactionManager;
    private volatile boolean stopRequested = false;
    private ForkJoinPool forkJoinPool;
    private final ParseHtml parseHtml;

    public IndexingService(SiteRepository siteRepository, PageRepository pageRepository,
                           LemmaRepository lemmaRepository, IndexRepository indexRepository,
                           SitesList sitesList, LemmaFinder lemmaFinder, ParseHtml parseHtml, DataSourceTransactionManager transactionManager) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.sitesList = sitesList;
        this.lemmaFinder = lemmaFinder;
        this.parseHtml = parseHtml;
        this.transactionManager = transactionManager;
    }

    @Transactional
    public void startIndexing() {
        stopRequested = false;
        List<searchengine.config.Site> sites = sitesList.getSites();
        // Логируем загруженные сайты
        logger.info("Загруженные сайты для индексации: {}", sites);

        for (searchengine.config.Site siteUrl : sites) {
            if (stopRequested) {
                logger.info("Индексация остановлена пользователем перед началом сайта");
                break;
            }
            logger.info("Начало индексации сайта: {}", siteUrl.getUrl());
            try {
                startSiteIndexing(siteUrl);
            } catch (Exception e) {
                handleSiteIndexingFailure(siteUrl, e);
            }
        }
    }
    @Transactional
            public void startSiteIndexing(searchengine.config.Site siteUrl){
                // Удаляем все записи по сайту
                Site existSite = siteRepository.findByUrl(siteUrl.getUrl());
                if (existSite != null) {
                    logger.info("Удаление существующего сайта: {}", existSite.getUrl());
                    pageRepository.deleteBySite(existSite);
                    siteRepository.delete(existSite);
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

                //запускаем асинхронную обработку pageQueue
                ConcurrentLinkedQueue<Page> pageQueue = new ConcurrentLinkedQueue<>();
                Thread pageProcessorThread = new Thread(() -> {
                    while (!stopRequested || !pageQueue.isEmpty()) {
                        processPages(pageQueue);
                    }
                });
                pageProcessorThread.start();

                //Запускаем индексацию сайта
                SiteMap siteMap = new SiteMap(siteUrl.getUrl());
                SiteMapRecursiveAction task = new SiteMapRecursiveAction(siteMap, site, pageQueue, this);
                logger.info("Запуск индексации для сайта: {}", siteUrl.getUrl());
                ForkJoinPool forkJoinPool = new ForkJoinPool(4);
        forkJoinPool.submit(() -> {
            // Обрабатываем транзакцию в отдельном потоке
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {
                forkJoinPool.invoke(task);
                return null;
            });
        });
                try {
                    //Ждем завершения обработки очереди после завершения индексации
                    pageProcessorThread.join();
                }catch (InterruptedException e){
                   throw new RuntimeException("Ошибка при ожидании завершения потока обработки страниц", e);

                }
                    //Обновляем статус на Indexed после завершения индексации
                    site.setStatus(Status.INDEXED);
                    site.setStatusTime(new Date());

                    siteRepository.save(site);
                    logger.info("Индексация завершена для сайта: {}", siteUrl.getUrl());
    }
    @Transactional
    public void processPages(ConcurrentLinkedQueue<Page> pageQueue) {
        List<Page> batch = new ArrayList<>(BATCH_SIZE);
        while (!pageQueue.isEmpty()) {
            Page page = pageQueue.poll();
            if (page != null) {
                batch.add(page);
            }
            if (batch.size() == BATCH_SIZE || pageQueue.isEmpty()) {
                try {
                    logger.info("Пытаемся сохранить страницы: {}", batch.stream().map(Page::getPath).collect(Collectors.joining(", ")));
                    savePagesInTransaction(batch);
                    logger.info("Страницы успешно сохранены в БД");
                    for (Page savedPage : batch) {
                        processPageContent(savedPage);
                    }
                    batch.clear();
                } catch (DataAccessException e) {
                    logger.error("Ошибка при пакетном сохранении", e.getMessage());
                }
            }
        }
    }
    @Transactional
    public void savePagesInTransaction(List<Page> batch) {
        for (Page page : batch) {
            Site site = page.getSite();
            if(site.getId() ==0){
                logger.warn("Сайт не сохранен! Сохраняем сейчас: {}", site.getUrl());
                siteRepository.save(site);
            }
        }
        pageRepository.saveAll(batch); // Сохраняем страницы в транзакции
    }

    private void processPageContent(Page page) {
        try {
            String content = page.getContent();
            String text = lemmaFinder.extractTextFromHtml(content);
            Map<String, Integer> lemmas = lemmaFinder.collectLemmas(text);
            List<Lemma> lemmaToSave = new ArrayList<>();
            List<Index> indexesToSave = new ArrayList<>();

            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                String lemmaText = entry.getKey();
                int count = entry.getValue();

                Lemma lemma = lemmaRepository.findByLemmaAndSite(lemmaText, page.getSite()).orElseGet(() -> {

                    Lemma newlemma = new Lemma();
                    newlemma.setLemma(lemmaText);
                    newlemma.setFrequency(0);
                    newlemma.setSite(page.getSite());
                    return newlemma;
                });
                //увеличиваем частоту
                lemma.setFrequency(lemma.getFrequency() + 1);
                lemmaToSave.add(lemma);
                // создаем запись в таблице index
                Index index = new Index();
                index.setPage(page);
                index.setLemma(lemma);
                index.setRank(count);
                indexesToSave.add(index);
            }
            lemmaRepository.saveAll(lemmaToSave);
            indexRepository.saveAll(indexesToSave);
        }catch (Exception e){
            logger.error("Ошибка при обработке контента страницы", page.getPath(), e);
        }
    }

    private void handleSiteIndexingFailure(searchengine.config.Site siteUrl, Exception e) {
        logger.error("Ошибка при индексации сайта: {}, ошибка: {}", siteUrl.getUrl(), e.getMessage());
        // если произошла ошибка меняем статус на Failed и сохраняем ошибку
        e.printStackTrace();
        Site faildSite = siteRepository.findByUrl(siteUrl.getUrl());
        if (faildSite != null) {
            faildSite.setStatus(Status.FAILED);
            faildSite.setStatusTime(new Date());
            faildSite.setLastErrorText(e.getMessage());
            siteRepository.save(faildSite);
        }
    }

    public boolean isStopRequested() {
        return stopRequested;
    }

    @Transactional
    public void stopIndexing() {
        stopRequested = true;
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

    @Transactional
    public void insertTestData() {
        // Создаем тестовый сайт
        Site testSite = new Site();
        testSite.setUrl("https://example.com");
        testSite.setName("Example Test Site");
        testSite.setStatus(Status.INDEXED); // ENUM статус
        testSite.setStatusTime(new Date());
        testSite.setLastErrorText(null);

        // Сохраняем сайт
        testSite = siteRepository.save(testSite);

        // Создаем несколько тестовых страниц
        Page page1 = new Page();
        page1.setSite(testSite);
        page1.setPath("/test-page-1");
        page1.setCode(200);
        page1.setContent("<html><body><h1>Test Page 1</h1></body></html>");

        Page page2 = new Page();
        page2.setSite(testSite);
        page2.setPath("/test-page-2");
        page2.setCode(404);
        page2.setContent("<html><body><h1>Test Page 2</h1></body></html>");

        // Сохраняем страницы
        pageRepository.save(page1);
        pageRepository.save(page2);


        System.out.println("Test data inserted successfully!");
    }


}
