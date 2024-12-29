package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.ConcurrentSkipListSet;

import static java.lang.Thread.sleep;

@Component
public class ParseHtml {
    private static final String USER_AGENT = "HeliontSearchBot";
    private static final String REFERRER = "http://www.google.com";
    private static ConcurrentSkipListSet<String> links = new ConcurrentSkipListSet<>();

    public static ConcurrentSkipListSet<String> getLinks(String url) {
        try {
            String domain = extractDomain(url);
            sleep(150);
            Connection connection = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .ignoreHttpErrors(true)
                    .timeout(50000)
                    .followRedirects(false);
            Document document = connection.get();
            Elements linkElements = document.select("body").select("a");
                   for (Element linkElement : linkElements) {
                       String link = linkElement.absUrl("href");
                       if (isLink(link, domain) && !isFile(link)) {
                           links.add(link);
                       }
                   }
        }catch (InterruptedException e){
            System.out.println(e + " - " + url);
        }catch (SocketTimeoutException e){
            System.out.println(e + " - " + url);
        }catch (IOException e){
            System.out.println("IOException - " + e.getMessage() + " for URL: " + url);
        }
        return links;
    }

    private static String extractDomain(String url) throws MalformedURLException {
        URL netUrl = new URL(url);
        String host = netUrl.getHost();
        if(host.startsWith("www.")) {
            host = host.substring(4);
        }
        String[] parts = host.split("\\.");
        if(parts.length > 1) {
            return parts[parts.length - 2];
        }else {
            return host;  // на случай если хост не содержит точек
        }
    }

    public static int getHttpCode(String url) {
        try {

            Connection.Response response = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .ignoreHttpErrors(true)
                    .timeout(50000)
                    .execute();

            return response.statusCode();
        }catch (IOException e){
            System.out.println("Error fething HTTP code for " + url + ": " + e.getMessage());
            return -1;
        }
    }

    public static String getContent(String url) {
        try {

            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .referrer(REFERRER)
                    .ignoreHttpErrors(true)
                    .timeout(50000)
                    .get();
            return document.html();
        }catch (IOException e){
            System.out.println("Error fething HTTP code for " + url + ": " + e.getMessage());
            return "";
        }
    }

    private static boolean isLink(String link, String domain) {
        String regex = "http[s]?://([a-zA-Z0-9\\-]+\\.)?" + domain + "\\.ru(/.*)?";
        return link.matches(regex);
    }

     private static boolean isFile(String link) {
        link.toLowerCase();
        return link.contains(".jpg")
                || link.contains(".jpeg") || link.contains(".png")|| link.contains(".gif") || link.contains(".webp")
                || link.contains(".pdf") || link.contains(".eps") || link.contains(".xlsx") || link.contains(".doc")
                || link.contains(".pptx") || link.contains(".docx") || link.contains("?_ga")
                || link.contains(".zip")|| link.contains(".sql");
     }
}
