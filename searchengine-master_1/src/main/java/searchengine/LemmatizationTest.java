package searchengine;


import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import searchengine.services.LemmaFinder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LemmatizationTest {
    public static void main(String[] args) throws IOException {

        try {
            LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
            String world = "леса";
            List<String> wordBaseForm = luceneMorphology.getNormalForms(world);
            wordBaseForm.forEach(System.out::println);

            List<String> wordBaseForm2 = luceneMorphology.getMorphInfo("хитрый");
            wordBaseForm2.forEach(System.out::println);
        }catch (Exception e){

            System.err.println("Ошибка при инициализации лемматизатора: " + e.getMessage());
        }


       // LemmaFinder finder = LemmaFinder.getInstance();
        String text = "Повторное появление леопарда в Осетии позволяет предположить," +
                " что леопард постоянно обитает в некоторых районах Северного Кавказа.";
        // Map<String, Integer> lemmas =  finder.collectLemmas(text);
    //    lemmas.forEach((lemma, count) -> System.out.println(lemma + " - " + count));


    }

    public String extractTextFromHtml(String htmlContent){
        if (htmlContent==null || htmlContent.isEmpty()){
            return "";
        }
        return Jsoup.parse(htmlContent).text();
    }

}
