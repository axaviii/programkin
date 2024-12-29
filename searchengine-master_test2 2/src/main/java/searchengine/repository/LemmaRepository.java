package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.SiteEntity;

import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

//    @Query("SELECT l FROM Lemma l WHERE l.lemma = :lemma AND l.siteEntity = :siteEntity")
    Optional<Lemma> findByLemmaAndSiteEntityId(String lemma, Integer id);
}
