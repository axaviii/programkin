package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.util.List;

@Repository
public interface SiteRepository  extends JpaRepository<SiteEntity, Integer> {

       SiteEntity findByUrl(String url);
       List<SiteEntity> findAllByStatus(Status status);
}
