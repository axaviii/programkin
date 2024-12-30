package searchengine.dto.statistics;

import lombok.Data;

@Data
public class DataSearchItem {
    private String site;
    private String siteName;
    private String uri;
    private String title;
    private String snippet;
    private double relevance;

    public DataSearchItem(String site, String siteName, String uri, String title, String snippet, double relevance) {
        this.site = site;
        this.siteName = siteName;
        this.uri = uri;
        this.title = title;
        this.snippet = snippet;
        this.relevance = relevance;
    }
}
