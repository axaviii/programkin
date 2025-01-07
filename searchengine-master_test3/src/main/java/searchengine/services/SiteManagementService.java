package searchengine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;
import searchengine.repository.IndexRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

@Component
public class SiteManagementService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);
    private final IndexRepository indexRepository;

    public SiteManagementService(SiteRepository siteRepository, PageRepository pageRepository, IndexRepository indexRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
    }

    @Transactional
    public void deleteSiteData(SiteEntity siteEntity) {
        indexRepository.deleteAll();
        pageRepository.deleteBySiteEntity(siteEntity);
        siteRepository.deleteById(siteEntity.getId());
        logger.info("Удаление существующего сайта: {}", siteEntity.getUrl());
    }

}
