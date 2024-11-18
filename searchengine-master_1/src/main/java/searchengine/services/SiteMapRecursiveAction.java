package searchengine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import searchengine.model.Page;
import searchengine.model.Site;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.RecursiveAction;


public class SiteMapRecursiveAction extends RecursiveAction {
   private final SiteMap siteMap;
   public final Site site;
   private final static ConcurrentSkipListSet<String> linksPool = new ConcurrentSkipListSet<>();
   private static final Logger logger = LoggerFactory.getLogger(IndexingService.class);
   private final ConcurrentLinkedQueue<Page> pageQueue;
   private final IndexingService indexingService;


    public SiteMapRecursiveAction(SiteMap siteMap, Site site, ConcurrentLinkedQueue<Page> pageQueue, IndexingService indexingService) {
        this.siteMap = siteMap;
        this.site = site;
        this.pageQueue = pageQueue;
        this.indexingService = indexingService;
    }

    @Override
    protected void compute() {
        if(indexingService.isStopRequested())
        {  return;}

     String url = siteMap.getUrl();

        if(linksPool.add(url)){
            // получаем HTTP код и контент страницы
            try {
                int code = ParseHtml.getHttpCode(url);
                String content = ParseHtml.getContent(url);
                Page page = new Page();
                page.setSite(site);
                page.setPath(url);
                page.setCode(code);
                page.setContent(content);
                logger.info("Site ID for URL {}: {}", url, site.getId());
                pageQueue.add(page);
            } catch (Exception e) {
                logger.error("Ошибка при сохранении страницы {}: {}", url, e.getMessage());
            }

            //Получаем список ссылок на дочерние страницы
            ConcurrentSkipListSet<String> links = ParseHtml.getLinks(url);
            // для каждой новой ссылки добавляем ее в карту сайта
            for (String link : links) {
                if(indexingService.isStopRequested()){
                    return;
                }
             siteMap.addChildren(new SiteMap(link));
             }

             //Рекурсивный обход дочерних страниц
             List<SiteMapRecursiveAction> taskList = new ArrayList<>();
             for(SiteMap child : siteMap.getSiteMapChildrens()){

             if(indexingService.isStopRequested()){
             return;
               }
            SiteMapRecursiveAction task = new SiteMapRecursiveAction(child, site, pageQueue, indexingService);
            task.fork();
            taskList.add(task);
             }

             // Ожидаем завершения всех задач
              for(SiteMapRecursiveAction task : taskList){
              task.join();
              }
        }

    }
}
