package searchengine.dto.statistics;

import lombok.*;

import java.util.List;


@Data
public class SearchResult {
    private boolean result;
    private Integer count;
    private List<DataSearchItem> data;
    private String error;

    public SearchResult(boolean result, Integer count, List<DataSearchItem> data, String error) {
        this.result = result;
        this.count = count;
        this.data = data;
        this.error = error;
    }
}
