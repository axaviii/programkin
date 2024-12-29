package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Page;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Page, Integer> {
    void deleteBySiteEntity(SiteEntity siteEntity);

    Optional<Page> findByPathAndSiteEntity(String path, SiteEntity siteEntity);
    int countBySiteEntityId(Integer siteEntityId);


}
