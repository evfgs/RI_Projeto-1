package if962;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Crawl
{
    // We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links = new LinkedList<String>();
    private Document htmlDocument;
    private String[] keywords = {"filme", "dvd", "blu-ray", "produto", "titulo", "diretor", "elenco", "duração", "ação", "aventura", "drama", "animação","comédia",
    							"terror", "infantil", "policial", "guerra", "lançamento", "ficção"};
    public int linksSize;


    public boolean crawl(String url)
    {
        try
        {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            Document htmlDocument = connection.get();
            this.htmlDocument = htmlDocument;
            if(connection.response().statusCode() == 200)
            if(!connection.response().contentType().contains("text/html"))
            {
                return false;
            }
            Elements linksOnPage = htmlDocument.select("a[href]");
            linksSize = linksOnPage.size();
            //System.out.println("Found (" + linksOnPage.size() + ") links");
            for(Element link : linksOnPage)
            {
            	
                this.links.add(link.absUrl("href"));
            }
            return true;
        }
        catch(IOException ioe)
        {
            
            return false;
        }
    }



    public boolean searchForWord()
    {
    	int pageScore = 0;
        if(this.htmlDocument == null)
        {
            System.out.println("Corpo de html inexistente!");
            return false;
        }
        String bodyText = this.htmlDocument.body().text();
        for(String keyword: keywords){
        	if( bodyText.toLowerCase().contains(keyword.toLowerCase())){
        		pageScore++;
        	}
        }
        if(pageScore >=1 && bodyText.contains("R$")){
        	return true;
        }
		return false;
    }


    public List<String> getLinks()
    {
        return this.links;
    }
    

}
