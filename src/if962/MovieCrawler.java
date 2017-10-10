package if962;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;



public class MovieCrawler
{
	private static final int PAGES_TO_CRAWL = 1000;
	private static String BASE_URL = "https://www.colecioneclassicos.com.br/filmes";
	private List<String> pagesVisited = new LinkedList<String>();
	private List<String> pagesToVisit = new LinkedList<String>();
	private int pagesSaved = 0;


	public void visit(String url) throws IOException{

		GetLinks crawling = new GetLinks();
		URL base = new URL(url);
		String protocol = base.getProtocol();
		String host = base.getHost();
		String hostname = protocol +  "://" + host;
		crawling.getPagesDisallowed(hostname);

		while(this.pagesSaved < PAGES_TO_CRAWL){

			String givenUrl;
			crawling.clearLinks();
			if(this.pagesToVisit.isEmpty()){
				givenUrl = url;
				this.pagesVisited.add(url);
			}
			else{
				givenUrl = this.getNextLink();

				while(!givenUrl.contains(hostname) || givenUrl.contains("twitter") || givenUrl.contains("facebook") || givenUrl.contains("plus.google")){
					givenUrl = this.getNextLink();  
				}
			}
			URL filename = new URL(givenUrl);
			if(crawling.checkpagesDisallowed(filename.getFile())){
				crawling.crawl(givenUrl, host);
				System.out.println(givenUrl);
				this.pagesToVisit.addAll(crawling.returnLinks());
				
				if(host.equals("www.pontofrio.com.br") || host.equals("www.casasbahia.com.br") || host.equals("www.saraiva.com.br") || host.equals("www.extra.com.br")){
					if(crawling.checkLastBar(givenUrl)){
						crawling.savePage(givenUrl);
						this.pagesSaved++;
					}
					
				}
				else if(host.equals("www.magazineluiza.com.br") || host.equals("www.livrariacultura.com.br") || host.equals("www.walmart.com.br")){
					if(crawling.checkProductAtUrl(givenUrl)){
						crawling.savePage(givenUrl);
						this.pagesSaved++;
					}
				}
				else if(host.equals("ewmix.com")){
					if(crawling.checkRelevance(givenUrl)){
						crawling.savePage(givenUrl);
						this.pagesSaved++;
					}
				}
				else if (host.equals("www.videoperola.com.br")){
					if(crawling.checkTitle(givenUrl)){
						crawling.savePage(givenUrl);
						this.pagesSaved++;
					}
				} 
				else{
					if(crawling.checkKeywordsBody(givenUrl)){
						crawling.savePage(givenUrl);
						this.pagesSaved++;
					}
				}
				//crawling.savePage(givenUrl);
				//loop pagesVisited and save in a file.txt for now
				//TO-DO create a method to download those urls
				//saveUrls(givenUrl, host.getHost());

			}
			/** boolean success = crawling.searchForWord();
          if(success)
          {
              System.out.println(String.format("\n" + givenUrl + "\nContém palavras chave"));
              //System.out.println("Foram encontrados: " + crawling.linksSize + " links!");
              pagesReturned.add(givenUrl);
              //metodo para pegar esses links que contem palavras chave e salvar em um txt
          }*/

		}

	}
	/** 
  private void saveUrls(String givenUrl, String hostname){
	  try{
		    PrintWriter writer = new PrintWriter("C:\\Users\\emanu\\workspace\\WebCrawler\\src\\outputs\\" + hostname + ".html", "UTF-8");
		    writer.println(givenUrl);
		    writer.close();
		} catch (IOException e) {
		   // do something
		}
  }
	 */


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
		//Connection connection = Jsoup.connect(BASE_URL).userAgent(USER_AGENT);
		//Document htmlDocument = connection.get();
		//System.out.println(htmlDocument.select("title"));
		MovieCrawler spider = new MovieCrawler();
		spider.visit(BASE_URL); 

	}
}