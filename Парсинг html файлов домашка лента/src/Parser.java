import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Parser
{

            private static final String PATH_FOR_IMG = "src/main/resources/"; // путь для сохранения картинок - создаем папку
            public void parsingHTML(String URL) throws IOException
            {

                   Document doc = Jsoup.connect(URL).get(); //Создаем doc типа Document в котором у нас будет хранится HTML код страницы. Метод .connect принимает ссылочку на сайт и метод .get получает его.

                for (Element element : doc.select("img")) // цикл перебирает весь код страницы и находит строки где есть тег img
                {

                  URL imageUrl = null;
                  String imageUrlString = element.absUrl("src"); //Далее создаем строку и присваеваем ей абсолютный путь к картинке

                  String[] fragmentsImage = imageUrlString.split("\\/"); //Создали массив из String, разделили с помощью /.

                  String fileName = fragmentsImage[fragmentsImage.length -1].replace(":", "").replace("?", ""); // fragments[fragments.length - 1] — это последний элемент массива.

                  if(fragmentsImage[fragmentsImage.length-1].contains("jpg"))
                  {
                      imageUrl  = new URL(imageUrlString);
                      InputStream in = imageUrl.openStream();
                      OutputStream out = new BufferedOutputStream(new FileOutputStream(PATH_FOR_IMG + fileName));

                      for (int i; (i= in.read()) != -1;) {out.write(i);}
                      out.close();
                      in.close();
                  }
                }

    }
}
