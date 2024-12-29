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
        // получаем siteId для siteUrl
        Optional<SiteEntity> siteEntity = Optional.ofNullable(siteRepository.findByUrl(siteUrl));
        if (siteEntity.isEmpty()) {
            return new SearchResult(false, 0, Collections.emptyList(), "сайт не найден");
        }
        int siteEntityId = siteEntity.get().getId();
        List<String> sortedLemmas = sortLemmasByFrequency(lemmas, siteEntityId);
        List<Page> pages = findMatchingPages(sortedLemmas, siteEntityId);
        if (pages.isEmpty()) {
            return new SearchResult(false, 0, Collections.emptyList(), "нет нужных страниц");
        }

        Map<Page, Double> relevanceMap = calculateRelevance(pages, sortedLemmas, siteEntityId);
        List<DataSearchItem> dataItems = buildSearchResults(relevanceMap, query, siteEntity.get(), offset, limit);
        return new SearchResult(true, dataItems.size(),dataItems, null);
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
            Optional<Lemma> lemmaEntity = lemmaRepository.findByLemmaAndSiteEntityId(lemma, siteEntityId);
            if (lemmaEntity.isEmpty()) {
                return Collections.emptyList();
            }
            int lemmaId = lemmaEntity.get().getId();

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
                Optional<Lemma> lemmaEntity = lemmaRepository
                        .findByLemmaAndSiteEntityId(lemma, siteEntityId);
                if (lemmaEntity.isPresent()) {
                    int lemmaId = lemmaEntity.get().getId();
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

    private List<DataSearchItem> buildSearchResults(Map<Page, Double> relevanceMap, String query, SiteEntity siteEntity, int offset, int limit) {
        return relevanceMap
                .entrySet()
                .stream()
                //сортируем поток по значению релевантность в обратном порядке от большего к меньшему
                .sorted(Map.Entry.<Page, Double>comparingByValue().reversed())
                .skip(offset != null ? offset : 0)
                .limit(limit != null ? limit : Long.MAX_VALUE)
                .map(entry -> {
                    Page page = entry.getKey();
                    String snippet = generateSnippet(page.getContent(), extractLemmas(query));
                    String title = lemmasFinder.extractTitle(page.getContent());
                    return new DataSearchItem(
                            siteEntity.getUrl(),
                            siteEntity.getName(),
                            page.getPath(),
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
