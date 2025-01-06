package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.SiteEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    Optional<Lemma> findByLemmaAndSiteEntityId(String lemma, Integer id);

    List<Lemma> findByLemmaInAndSiteEntityId(List<String> lemmas, Integer siteEntityId);

    int countBySiteEntity(Integer siteEntityId);
}
