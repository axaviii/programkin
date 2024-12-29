package searchengine.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Page;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.util.Date;

@Service
public class IndexServiceTest {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    public IndexServiceTest(SiteRepository siteRepository, PageRepository pageRepository) {
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
    }


    @Transactional
    public void insertTestData() {
        // Создаем тестовый сайт
        SiteEntity testSiteEntity = new SiteEntity();
        testSiteEntity.setUrl("https://example.com");
        testSiteEntity.setName("Example Test Site");
        testSiteEntity.setStatus(Status.INDEXED); // ENUM статус
        testSiteEntity.setStatusTime(new Date());
        testSiteEntity.setLastErrorText(null);

        // Сохраняем сайт
        testSiteEntity = siteRepository.save(testSiteEntity);
        System.out.println(testSiteEntity.getId());

        // Создаем несколько тестовых страниц
        Page page1 = new Page();
        page1.setSiteEntity(testSiteEntity);
        page1.setPath("/test-page-1");
        page1.setCode(200);
        page1.setContent("<html><body><h1>Test Page 1</h1></body></html>");

        Page page2 = new Page();
        page2.setSiteEntity(testSiteEntity);
        page2.setPath("/test-page-2");
        page2.setCode(404);
        page2.setContent("<html><body><h1>Test Page 2</h1></body></html>");

        // Сохраняем страницы
        pageRepository.save(page1);
        pageRepository.save(page2);
        System.out.println(page1.getId());
        System.out.println(page2.getId());

        System.out.println("Test data inserted successfully!");
    }
}
