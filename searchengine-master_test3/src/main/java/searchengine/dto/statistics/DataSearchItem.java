package searchengine.dto.statistics;

import lombok.Data;

@Data
public class DataSearchItem {
    private String site;
    private String siteName;
    private String url;
    private String title;
    private String snippet;
    private double relevance;

    public DataSearchItem(String site, String siteName, String url, String title, String snippet, double relevance) {
        this.site = site;
        this.siteName = siteName;
        this.url = url;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }
}
