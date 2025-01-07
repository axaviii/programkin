package searchengine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.SiteEntity;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

@Component
public class SiteManagementService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);

    public SiteManagementService(SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }

    @Transactional
    public void deleteSiteData(SiteEntity siteEntity) {
        pageRepository.deleteBySiteEntity(siteEntity);
        siteRepository.deleteById(siteEntity.getId());
        logger.info("Удаление существующего сайта: {}", siteEntity.getUrl());
    }

}
