package searchengine.config;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import searchengine.services.LemmaFinder;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class AppConfig{
    @Bean
    public LemmaFinder lemmaFinder() throws IOException {
        return LemmaFinder.getInstance();
    }

    @Bean
    public LuceneMorphology luceneMorphology() throws IOException {
    return new RussianLuceneMorphology();
    }
    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

}
