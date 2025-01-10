package searchengine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PageIndexingService {
    private static final Logger logger = LoggerFactory.getLogger(PageIndexingService.class);
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final SiteRepository siteRepository;
    private final ParseHtml parseHtml;
    private final LemmaFinder lemmaFinder;
    private final SitesList sitesList;

    public PageIndexingService(PageRepository pageRepository, LemmaRepository lemmaRepository,
                               IndexRepository indexRepository, SiteRepository siteRepository,
                               ParseHtml parseHtml, LemmaFinder lemmaFinder, SitesList sitesList) {
        this.pageRepository = pageRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
        this.siteRepository = siteRepository;
        this.parseHtml = parseHtml;
        this.lemmaFinder = lemmaFinder;
        this.sitesList = sitesList;
    }

    @Transactional
    public void indexPage(String url) throws IllegalAccessException {

        if (!isUrlWithinConfiguredSites(url)) {
            throw new IllegalAccessException("Данная страница находится за пределами сайтов");
        }
        try {
            if(url.endsWith("/")){
            url =  url.substring(0, url.length()-1);
            }
            //находим сайт к которому относится Url
            SiteEntity siteEntity = siteRepository.findByUrl(getSiteBaseUrl(url));
            if (siteEntity == null) {
                siteEntity = new SiteEntity();
                siteEntity.setUrl(getSiteBaseUrl(url));
                siteEntity.setName("New");
                siteEntity.setStatusTime(new Date());
                siteEntity.setStatus(Status.INDEXING);
                siteRepository.save(siteEntity);

            }
            // Удаляем старую запись, если страница уже существует

            Page existingPage = pageRepository.findByPathAndSiteEntity(url, siteEntity).orElse(null);
            if (existingPage != null) {
                pageRepository.delete(existingPage);
                logger.info("Старая запись для страницы {} удалена", url);
            }
            // Получаем данные страницы
            int httpCode = parseHtml.getHttpCode(url);
            String content = parseHtml.getContent(url);

            // Создаем и сохраняем новую страницу
            Page page = new Page();
            page.setSiteEntity(siteEntity);
            page.setPath(url);
            page.setCode(httpCode);
            page.setContent(content);
            pageRepository.save(page);

            // извлекаем текст и лемматизируем его
            String text = lemmaFinder.extractTextFromHtml(content);
            Map<String, Integer> lemmas = lemmaFinder.collectLemmas(text);
            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                String lemmaText = entry.getKey();
                int lemmaFrequencyOnPage = entry.getValue();

                // сохраняем леммы и их связь с текущей страницей
                saveLemmasAndIndexes(lemmaText,lemmaFrequencyOnPage, page);
            }
            logger.info("Индексация завершена. Программа готова к дальнейшим действиям.");

        } catch (Exception e) {
            logger.error("Ошибка при индексации страницы {}: {}", url, e.getMessage());
            throw new RuntimeException("Индексация не удалась: " + e.getMessage());
        }
    }

    private void saveLemmasAndIndexes(String lemmaText, Integer frequency, Page page) {
            // Ищем существующую лемму или создаем новую
            List<Lemma> lemmaOptional = lemmaRepository.findByLemmaAndSiteEntityId(lemmaText, page.getSiteEntity().getId());
            if (lemmaOptional.isEmpty()) {
                Lemma newLemma = new Lemma();
                newLemma.setLemma(lemmaText);
                newLemma.setFrequency(0);
                newLemma.setSiteEntity(page.getSiteEntity());
                lemmaRepository.saveAndFlush(newLemma);
                ;
            } else {
                Lemma lemma = lemmaOptional.get(0);
                //увеличиваем частоту использования леммы
                lemma.setFrequency(lemma.getFrequency() + frequency);
                lemmaRepository.saveAndFlush(lemma);
                createIndex(page, lemma, frequency);

            }
        }

    private void createIndex(Page page, Lemma lemma, Integer count) {
        Index index = indexRepository
                .findByPageIdAndLemmaId(page.getId(), lemma.getId())
                .orElseGet(() -> {
                    // создаем запись в таблице index
                    Index newindex = new Index();
                    newindex.setPage(page);
                    newindex.setLemma(lemma);
                    newindex.setRank(count);
                    return indexRepository.save(newindex);
                });
        //обновить ранг индекса
        index.setRank(index.getRank() + count);
        indexRepository.save(index);

    }

    public boolean isUrlWithinConfiguredSites(String url) {
        for(searchengine.config.Site site : sitesList.getSites()){
            if(url.startsWith(site.getUrl())){
                return true; // нашли совпадение
            }
        }
        return  false;// нет совпадений
    }

    private String getSiteBaseUrl(String url) {

        for(searchengine.config.Site site : sitesList.getSites()){
            if(url.startsWith(site.getUrl())){
                return site.getUrl();// найден базовый url сайта
            }

        }
        throw  new IllegalArgumentException("Сайт для данного url не найден");
    }
}
