import javax.imageio.IIOException;
import java.io.IOException;
import java.net.URL;

public class ParsingHTML
{
    public static void main(String[] args)
    {
        try
        {
         Parser parser = new Parser();
         parser.parsingHTML(URL.RIA.getUrl());

        } catch (IOException ioException) { ioException.printStackTrace();}

    }
}
