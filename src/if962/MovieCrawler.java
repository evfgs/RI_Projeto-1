package if962;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class MovieCrawler
{
  private static final int PAGES_TO_CRAWL = 100;
  private static String BASE_URL = "http://www.extra.com.br/";
  private Set<String> pagesVisited = new HashSet<String>();
  private List<String> pagesToVisit = new LinkedList<String>();
  private List<String> pagesReturned = new LinkedList<String>();
  private List<String> pathsBlocked = new LinkedList<String>();
 

  public void visit(String url)
  {
	  try{
  		URL conn = new URL(url +  "/robots.txt");
  		URLConnection connection = conn.openConnection();
  		BufferedReader br = new BufferedReader(
                  new InputStreamReader(connection.getInputStream()));
  		String line = null;
          while((line = br.readLine()) != null) {
              if(line.startsWith("Disallow: ")){
              	pathsBlocked.add(line.substring(10));
              }
          }
          System.out.println(pathsBlocked);
          
      } catch (IOException e) {
          e.printStackTrace();
      }
      while(this.pagesReturned.size() < PAGES_TO_CRAWL)
      {
          String currentUrl;
          Crawl crawling = new Crawl();
          if(this.pagesToVisit.isEmpty())
          {
              currentUrl = url;
              this.pagesVisited.add(url);
          }
          else
          {
              currentUrl = this.getNextLink();
              
              if(!currentUrl.startsWith(BASE_URL))
              
              while(!currentUrl.startsWith(BASE_URL)){
            	currentUrl = this.getNextLink();  
              }
             /* while(!currentUrl.startsWith(BASE_URL + "dvdsebluray/FilmeseSeriados/")){
            	  currentUrl = this.getNextLink();
              }*/
              while(parseRobots(currentUrl) == true){
            	  currentUrl = this.getNextLink();
              }
          }
          crawling.crawl(currentUrl);
          boolean success = crawling.searchForWord();
          if(success)
          {
              System.out.println(String.format("\n" + currentUrl + "\nContém palavras chave"));
              System.out.println("Foram encontrados: " + crawling.linksSize + " links!");
              pagesReturned.add(currentUrl);
              //metodo para pegar esses links que contem palavras chave e salvar em um txt
          }
          this.pagesToVisit.addAll(crawling.getLinks());
      }
  }

  
  private boolean parseRobots(String currentUrl){
	  for(String term: pathsBlocked){
		  if(currentUrl.contains(term)){
			  return true;
		  }
	  }
	return false;
  }
  
  private String getNextLink()
  {
      String nextLink;
      do
      {
          nextLink = this.pagesToVisit.remove(0);
      } while(this.pagesVisited.contains(nextLink));
      this.pagesVisited.add(nextLink);
      return nextLink;
  }
  
  public static void main(String[] args) throws URISyntaxException, IOException
  {
      MovieCrawler spider = new MovieCrawler();
      spider.visit(BASE_URL);
  }
}