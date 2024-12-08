package searchengine.services;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.util.Map;
import java.util.Optional;

@Service
public class LemmaAndIndexService {
    private static final Logger logger = LoggerFactory.getLogger(LemmaAndIndexService.class);
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaFinder lemmaFinder;


    public LemmaAndIndexService(LemmaRepository lemmasRepository, IndexRepository indexRepository, LemmaFinder lemmaFinder) {
        this.lemmaRepository = lemmasRepository;
        this.indexRepository = indexRepository;
        this.lemmaFinder = lemmaFinder;
    }


    public void processPageContent(Page page) {
        try {
            String content = page.getContent();
            String text = lemmaFinder.extractTextFromHtml(content);
            Map<String, Integer> lemmas = lemmaFinder.collectLemmas(text);
            for (Map.Entry<String, Integer> entry : lemmas.entrySet()) {
                String lemma = entry.getKey();
                Integer frequency = entry.getValue();
                saveLemma(lemma, frequency, page);
            }
            logger.debug("Индексация страницы, lemmas: " + lemmas.size());
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void saveLemma(String lemmaText, Integer frequency, Page page) {
//        Site managedSite = page.getSite();
//        if (!managedSite.isManaged()) {
//            managedSite = siteRepository.findById(managedSite.getId())
//                    .orElseThrow(() -> new RuntimeException("Сайт не найден"));
        Optional<Lemma> lemmaOptional = lemmaRepository.findByLemmaAndSiteEntityId(lemmaText, page.getSiteEntity().getId());
        if (lemmaOptional.isEmpty()) {
            Lemma newlemma = new Lemma();
            newlemma.setLemma(lemmaText);
            newlemma.setFrequency(0);
            newlemma.setSiteEntity(page.getSiteEntity());
            lemmaRepository.saveAndFlush(newlemma);
        } else {
            Lemma lemma = lemmaOptional.get();
            //увеличиваем частоту и обновляем лемму
            lemma.setFrequency(lemma.getFrequency() + frequency);
            lemmaRepository.saveAndFlush(lemma);
            // создаем или обновляем индекс
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

}
