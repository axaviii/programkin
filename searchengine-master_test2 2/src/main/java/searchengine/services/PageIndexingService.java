package searchengine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

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

        if(!isUrlWithinConfiguredSites(url)){
            throw new IllegalAccessException("Данная страница находится за пределами сайтов");
        }
        try {
            //находим сайт к которому относится Url
            SiteEntity siteEntity = siteRepository.findByUrl(getSiteBaseUrl(url));
            if (siteEntity == null) {
                throw new IllegalStateException(" Сайт для url не найден в базе данных");
            }
            // Удаляем старую запись, если страница уже существует

            Page existingPage = pageRepository.findByPathAndSiteEntity(url, siteEntity).orElse(null);
            if (existingPage != null) {
                deleteExistingPageData(existingPage);
                logger.info("Старая запись для страницы {} удалена",url);
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

            // сохраняем леммы и их связь с текущей страницей
            saveLemmasAndIndexes(lemmas,page);


        }catch (Exception e){
            logger.error("Ошибка при индексации страницы {}: {}", url, e.getMessage());
            throw new RuntimeException("Индексация не удалась: " +e.getMessage());
        }
    }

    private void deleteExistingPageData(Page page) {
        // удаляем записи из таблицы index
        List<Index> indexes = indexRepository.findByPage(page);
        for(Index index : indexes){
            Lemma lemma = index.getLemma();

            //уменьшаем частоту леммы
            lemma.setFrequency(lemma.getFrequency()-1);
            if(lemma.getFrequency() <= 0){
                lemmaRepository.delete(lemma);
            }else {
                lemmaRepository.save(lemma);
            }
            //удаляем запись из index
            indexRepository.delete(index);
        }
        pageRepository.delete(page);
    }

    private void saveLemmasAndIndexes(Map<String, Integer> lemmas, Page page) {
        for(Map.Entry<String, Integer> entry : lemmas.entrySet()){
            String lemmatext = entry.getKey();
            int lemmaFrequencyOnPage = entry.getValue();

            // Ищем существующую лемму или создаем новую
            Lemma lemma = lemmaRepository.findByLemmaAndSiteEntityId(lemmatext, page.getSiteEntity().getId()).orElseGet(()->{
                Lemma newLemma = new Lemma();
                newLemma.setLemma(lemmatext);
                newLemma.setFrequency(0);
                return newLemma;
            });

            //увеличиваем частоту использования леммы
            lemma.setFrequency(lemma.getFrequency() +1);
            lemmaRepository.save(lemma);

            //Создаем связь между леммой и страницей
            Index index = new Index();
            index.setLemma(lemma);
            index.setPage(page);
            index.setRank(lemmaFrequencyOnPage);
            indexRepository.save(index);
        }
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
