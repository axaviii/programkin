package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;
import searchengine.model.Status;

import java.util.List;

@Repository
public interface SiteRepository  extends JpaRepository<Site, Integer> {
       Site findByUrl(String url);
       List<Site> findAllByStatus(Status status);
}
