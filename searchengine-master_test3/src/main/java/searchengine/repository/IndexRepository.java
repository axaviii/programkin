package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Page;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexRepository extends JpaRepository<Index, Integer> {
    List<Index> findByPage(Page page);

    Optional<Index> findByPageIdAndLemmaId(Integer pageId, Integer lemmaId);

    List<Index> findPagesByLemmaId(Integer lemmaId);
}