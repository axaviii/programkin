package searchengine.services;

import java.util.concurrent.CopyOnWriteArrayList;

public class SiteMap {
    private final String url;
    private final CopyOnWriteArrayList<SiteMap> siteMapChildrens;

    public SiteMap(String url) {
        this.url = url;
        siteMapChildrens = new CopyOnWriteArrayList<>();
    }

    public void addChildren(SiteMap children) {
        siteMapChildrens.add(children);
    }

    public String getUrl() {
        return url;
    }

    public CopyOnWriteArrayList<SiteMap> getSiteMapChildrens() {
        return siteMapChildrens;
    }
}
