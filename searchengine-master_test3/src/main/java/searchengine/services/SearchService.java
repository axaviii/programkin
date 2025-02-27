package searchengine.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.statistics.DataSearchItem;
import searchengine.dto.statistics.SearchResult;
import searchengine.model.*;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    private final LemmaAndIndexService lemmasAndIndexService;
    private final LemmaFinder lemmasFinder;
    private final PageRepository pageRepository;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;

    private static final double MAX_ALLOWED_FREQUENCY_PERCENT = 0.7;// мак допустимый % страниц
    private final IndexRepository indexRepository;


    public SearchService(LemmaAndIndexService lemmasAndIndexService, LemmaFinder lemmasFinder, PageRepository pageRepository, SiteRepository siteRepository, LemmaRepository lemmaRepository, IndexRepository indexRepository) {
        this.lemmasAndIndexService = lemmasAndIndexService;
        this.lemmasFinder = lemmasFinder;
        this.pageRepository = pageRepository;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.indexRepository = indexRepository;
    }

    @Transactional
    public SearchResult search(String query, String siteUrl, Integer offset, Integer limit) {
        List<String> lemmas = extractLemmas(query);
        if (lemmas.isEmpty()) {
            return new SearchResult(false, 0, Collections.emptyList(), "не найдены леммы в запросе");
        }

        if (siteUrl != null) {
            // получаем siteId для siteUrl
            SiteEntity siteEntity = siteRepository.findByUrl(siteUrl);
            if (siteEntity == null) {
                return new SearchResult(false, 0, Collections.emptyList(), "сайт не найден");
            }
            return oneSiteSearch(query, lemmas, siteEntity, offset, limit);
        } else {
            // поиск по всем сайтам
            List<SiteEntity> siteEntities = siteRepository.findAll();
            if (siteEntities.isEmpty()) {
                return new SearchResult(false, 0, Collections.emptyList(), "Нет доступных сайтов для поиска");
            }

            List<DataSearchItem> allSiteResults = new ArrayList<>();
            int totalPagesCount = 0;
            for (SiteEntity siteEntity : siteEntities) {
                SearchResult searchResult = oneSiteSearch(query, lemmas, siteEntity, offset, limit);
                if(searchResult.isResult()){
                    totalPagesCount += searchResult.getCount();
                    allSiteResults.addAll(searchResult.getData());
                }
            }
            return new SearchResult(true, totalPagesCount, allSiteResults, null);
        }
    }

    private SearchResult oneSiteSearch(String query, List<String> lemmas, SiteEntity siteEntity, Integer offset, Integer limit) {
        int siteEntityId = siteEntity.getId();
        List<String> sortedLemmas = sortLemmasByFrequency(lemmas, siteEntityId);
        List<Page> pages = findMatchingPages(sortedLemmas, siteEntityId);
        if (pages.isEmpty()) {
            return new SearchResult(false, 0, Collections.emptyList(), "нет нужных страниц для сайта");
        }
        int totalPagesCount = pages.size();

        Map<Page, Double> relevanceMap = calculateRelevance(pages, sortedLemmas, siteEntityId);
        List<DataSearchItem> dataItems = buildSearchResults(relevanceMap, query, siteEntity, offset, limit);
        return new SearchResult(true, totalPagesCount, dataItems, null);
    }


    //метод принимает поисковый запрос и возвращает список уникальных лемм
    private List<String> extractLemmas(String query) {
        return new ArrayList<>(lemmasFinder.getLemmaSet(query));
    }

    private List<String> sortLemmasByFrequency(List<String> lemmas, Integer siteEntityId) {
        Map<String, Integer> lemmaFrequencies = new HashMap<>();
        int totalPages = pageRepository.countBySiteEntityId(siteEntityId);
        for (String lemma : lemmas) {
            int frequency = lemmasAndIndexService.getLemmaFrequency(lemma, siteEntityId);
            if (frequency <= totalPages * MAX_ALLOWED_FREQUENCY_PERCENT) {
                lemmaFrequencies.put(lemma, frequency);
            }

        }
        return lemmaFrequencies.keySet().stream()
                .sorted(Comparator.comparingInt(lemmaFrequencies::get))
                .collect(Collectors.toList());
    }

    private List<Page> findMatchingPages(List<String> lemmas, int siteEntityId) {
        if (lemmas.isEmpty()) {
            return Collections.emptyList();
        }
        System.out.println("lemmas: " + lemmas);
        System.out.println("siteEntityId: " + siteEntityId);
        List<Lemma> initialLemmas = lemmaRepository.findByLemmaInAndSiteEntityId(lemmas,siteEntityId);
        if (initialLemmas.isEmpty()) {
            return Collections.emptyList();
        }
        List<Index> matchingIndexes = indexRepository.findPagesByLemmaId(initialLemmas.get(0).getId());


        for (int i = 1; i < lemmas.size(); i++) {
            String lemma = lemmas.get(i);
            List<Lemma> lemmaEntity = lemmaRepository.findByLemmaAndSiteEntityId(lemma, siteEntityId);
            if (lemmaEntity.isEmpty()) {
                return Collections.emptyList();
            }
            int lemmaId = lemmaEntity.get(0).getId();

            List<Index> currentIndexes = indexRepository.findPagesByLemmaId(lemmaId);
                    matchingIndexes.retainAll(currentIndexes);// остаются только пересечения
            if (matchingIndexes.isEmpty()) {
                break;
            }
        }
        return matchingIndexes.stream()
                .map(Index:: getPage)
                .distinct()
                .toList();
    }

    //для каждой найденной страницы рассчитать абсолютную релевантность
    private Map<Page, Double> calculateRelevance(List<Page> pages, List<String> lemmas, int siteEntityId) {
        Map<Page, Double> relevanceMap = new HashMap<>();
        for (Page page : pages) {
            double relevance = 0.0;
            for (String lemma : lemmas) {
                List<Lemma> lemmaEntity = lemmaRepository
                        .findByLemmaAndSiteEntityId(lemma, siteEntityId);
                if (!lemmaEntity.isEmpty()) {
                    int lemmaId = lemmaEntity.get(0).getId();
                    relevance += lemmasAndIndexService.getLemmaRank(page.getId(), lemmaId);
                }
            }
            relevanceMap.put(page, relevance);
        }
        //рассчитываем относительную релевантность
        double maxRelevance = relevanceMap.values().stream().max(Double::compareTo).orElse(1.0);
        for(Map.Entry<Page, Double> entry : relevanceMap.entrySet()) {
             entry.setValue(entry.getValue() / maxRelevance);
        }
        return relevanceMap;
    }

    private List<DataSearchItem> buildSearchResults(Map<Page, Double> relevanceMap, String query, SiteEntity siteEntity, Integer offset, Integer limit) {

        String siteRootUrl = siteEntity.getUrl().endsWith("/") ? siteEntity.getUrl() : siteEntity.getUrl() + "/";
        return relevanceMap
                .entrySet()
                .stream()
                //сортируем поток по значению релевантность в обратном порядке от большего к меньшему
                .sorted(Map.Entry.<Page, Double>comparingByValue().reversed())
                .skip(offset != null ? offset : 0)
                .limit(limit != null ? limit : 20)
                .map(entry -> {
                    Page page = entry.getKey();
                    String fullPath = page.getPath();
                    String relativePath = fullPath.substring(siteRootUrl.length());
                    String snippet = generateSnippet(page.getContent(), extractLemmas(query));
                    String title = lemmasFinder.extractTitle(page.getContent());
                    return new DataSearchItem(
                            siteEntity.getUrl(),
                            siteEntity.getName(),
                            relativePath,
                            title,
                            snippet,
                            entry.getValue());
                })
                .collect(Collectors.toList());
    }

    private String generateSnippet(String content, List<String> lemmas) {
        String text = lemmasFinder.extractTextFromHtml(content);
        for (String lemma : lemmas) {
            text = text.replaceAll("(?i)\\b" + lemma + "\\b", "<b>" + lemma + "</b>");
        }
        return text.length() > 200 ? text.substring(0, 200) + "..." : text;
    }

}
