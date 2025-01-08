package searchengine.services;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class SiteMap {
    private final String url;
    private final CopyOnWriteArraySet<SiteMap> siteMapChildrens;

    public SiteMap(String url) {
        this.url = url;
        siteMapChildrens = new CopyOnWriteArraySet<>();
    }

    public void addChildren(SiteMap children) {
        siteMapChildrens.add(children);
    }

    public String getUrl() {
        return url;
    }

    public CopyOnWriteArraySet<SiteMap> getSiteMapChildrens() {
        return siteMapChildrens;
    }
}
