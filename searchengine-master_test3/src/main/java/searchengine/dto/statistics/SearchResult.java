package searchengine.dto.statistics;

import lombok.*;

import java.util.List;


@Data
public class SearchResult {
    private boolean result;
    private Integer count;
    private List<DataSearchItem> data;
    private String error;



}
