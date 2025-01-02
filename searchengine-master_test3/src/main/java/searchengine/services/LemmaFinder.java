package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.util.*;


public class LemmaFinder {
    private final LuceneMorphology russianMorphology;
    private final LuceneMorphology englishMorphology;
    private static final String RUSSIAN_WORLD_REGEX = "[а-яА-ЯёЁ]+$";
    private static final String ENGLISH_WORLD_REGEX = "[a-zA-Z]+$";
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String[] particlesNames = new String[] {"МЕЖД", "ПРЕДЛ", "СОЮЗ"};

    public LemmaFinder(LuceneMorphology russianMorphology, LuceneMorphology englishMorphology) {
        this.russianMorphology = russianMorphology;
        this.englishMorphology = englishMorphology;
    }

    public static LemmaFinder getInstance() throws IOException {
        LuceneMorphology russian = new RussianLuceneMorphology();
        LuceneMorphology english= new EnglishLuceneMorphology();
        return new LemmaFinder(russian, english);
    }

    private LemmaFinder() {
        throw new RuntimeException("Disallow construct");
    }


    //Определяем какой анализатор использовать
    private LuceneMorphology getMorphology(String word) {
        if(word.matches(RUSSIAN_WORLD_REGEX)) {
            return russianMorphology;
        }else if(word.matches(ENGLISH_WORLD_REGEX)) {
            return englishMorphology;
        }
        return null;  // для случаев чисел или символов
    }


    /**
     * Метод разделяет текст на слова, находит все леммы и считает их количество
     * @return ключ является леммой, а значение количеством найденных лемм
     * @Param text текст из которого будут выбираться леммы
     */
    public Map<String, Integer> collectLemmas(String text) {
        String[] words = arrayContainsRussianWordsAndEnglish(text);
        HashMap<String, Integer> lemmas = new HashMap<>();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            LuceneMorphology morphology = getMorphology(word);
            if (morphology != null) {
                continue;
            }

            List<String> wordBaseForms = morphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticle(wordBaseForms)) {
                continue;
            }
            List<String> normalForms = morphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }
            String normalWord = normalForms.get(0);
            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }
        return lemmas;
    }
    /**
     * @param text текст из которого собираем все леммы
     * @return набор уникальных лемм найденных в тексте
     */
    public Set<String> getLemmaSet(String text){
        String[] textArray = arrayContainsRussianWordsAndEnglish(text);
        Set<String> lemmaSet = new HashSet<>();
        for(String word : textArray){
            if(!word.isEmpty() /*&& isCorrectWordForm(word)*/){
                LuceneMorphology morphology = getMorphology(word);
                if (morphology != null) {
                    continue;
                }
                List<String> wordBaseForms = morphology.getMorphInfo(word);
                if(anyWordBaseBelongToParticle(wordBaseForms)){
                    continue;
                }
                lemmaSet.addAll(morphology.getNormalForms(word));
            }
        }
        return lemmaSet;
    }

    private boolean anyWordBaseBelongToParticle(List<String> wordBaseForms){
        return wordBaseForms.stream().anyMatch(this:: hasParticleProperty);

    }

    private boolean hasParticleProperty(String wordBase){
       for (String property : particlesNames){
           if(wordBase.toUpperCase().contains(property)){
               return true;
           }
       }
       return false;
    }

    private String[] arrayContainsRussianWordsAndEnglish(String text){
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-яёa-z\\s])", " ")
                .trim()
                .split("\\s+");
    }




   /* private boolean isCorrectWordForm(String word){
        List<String> wordInfo = luceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo){
            if(morphInfo.contains(WORD_TYPE_REGEX)){
                return false;
            }
        }
        return true;
    }*/

    /**
     * Метод очищает HTML-код и возвращает только текст.
     *
     * @param htmlContent HTML-код страницы.
     * @return Очищенный текст.
     */
    public String extractTextFromHtml(String htmlContent) {
        if (htmlContent == null || htmlContent.isEmpty()) {
            return "";
        }

        // Используем Jsoup для очистки текста
        return Jsoup.parse(htmlContent).text();
    }

    public String extractTitle(String htmlContent) {
      if (htmlContent == null || htmlContent.isEmpty()) {
          return "";
      }
        // Используем Jsoup для извлечения title
      return Jsoup.parse(htmlContent).title();
    }
}
