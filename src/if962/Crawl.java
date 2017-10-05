package if962;

import java.io.File;
import java.io.FileOutputStream;
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
	private String[] keywordsAtUrl = {"filme", "filmes", "dvd", "blu-ray", "discos", "trilogia", "blu", "ray", "colecao", "3d", "box"};
	private List<String> exceptions = new LinkedList<String>();
	private List<String> regexExceptions = new LinkedList<String>();
	private int countFile = 0;
	private String hostname;
	private String htmlFile;
	//public int linksSize;


	public boolean crawl(String url, String hostname){
		this.hostname = hostname;
		try
		{
			Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
			Document htmlDocument = connection.get();
			this.htmlDocument = htmlDocument;
			this.htmlFile = htmlDocument.toString();
			if(connection.response().statusCode() == 200)
				if(!connection.response().contentType().contains("text/html"))
				{
					return false;
				}
			Elements linksOnPage = htmlDocument.select("a[href]");
			//linksSize = linksOnPage.size();
			//System.out.println("Found (" + linksOnPage.size() + ") links");
			for(Element link : linksOnPage)
			{
				String pagina = link.absUrl("href").toLowerCase();
				if(pagina.contains("filme") || pagina.contains("dvd") || pagina.contains("blu-ray")){
					this.links.add(link.absUrl("href"));
				}
				//this.links.add(link.absUrl("href"));
				//System.out.println(link.absUrl("href"));
			}
			//System.out.println("cabo");
			return true;
		}
		catch(IOException ioe)
		{
			return false;
		}
	}



	/**
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
		if(pageScore >=5 && bodyText.contains("R$")){
			return true;
		}
		return false;
	}*/


	public List<String> getLinks()
	{
		return this.links;
	}
	
	public void clearLinks(){
		this.links = new LinkedList<String>();;
	}

	public void createExceptions(String url) {
		try{
			Connection connection = Jsoup.connect(url + "/robots.txt").userAgent(USER_AGENT);
			Document htmlDocument = connection.get();
			this.htmlDocument = htmlDocument;

			System.out.println(url + "/robots.txt");

			String body = htmlDocument.body().text();

			while (body.contains("Disallow: ")) {
				body = body.substring(body.indexOf("Disallow: ") + 10);
				if (body.contains(" ")) {
					String exception = body.substring(0, body.indexOf(" "));
					body = body.substring(body.indexOf(" "), body.length());
					this.exceptions.add(exception);
					//System.out.println(exception);
				}else {
					String exception = body;
					this.exceptions.add(exception);
				}
			}
			createRegex(url);

			System.out.println(regexExceptions);
		}
		catch(IOException ioe)
		{
			System.out.println(ioe);
		}
	}

	public void createRegex(String urlPrefix){

		for(String exception: exceptions) {
			String regex = "";

			if(exception.charAt(0) == '*') {
				exception = exception.substring(1, exception.length());
				if(!exception.contains("*")) {
					regex = ".*(";
				}
			}else {
				regex = "^(";
			}
			if(exception.contains("*")) {
				while(exception.contains("*")) {
					regex = regex + ".*(" + exception.substring(0, exception.indexOf("*")) + ")";
					exception = exception.substring(exception.indexOf("*") + 1, exception.length());
					if(!exception.contains("*")) {
						regex = regex + ".*(" + exception + ")";
						exception = exception.substring(exception.indexOf("*") + 1, exception.length());
						if(regex.charAt(0) == '^') {
							regex = regex + ")";
						}
					}
				}
			}else {
				regex = regex + exception + ")";
			}
			regex = fixRegex(regex);
			this.regexExceptions.add(regex);
		}
	}

	public String fixRegex(String regex) {
		String fixedRegex = "";
		for(int i = 0; i < regex.length(); i++) {
			if((regex.charAt(i) + "").matches("[?]")) {
				fixedRegex = fixedRegex + "\\" + regex.charAt(i);
			}else {
				fixedRegex = fixedRegex + regex.charAt(i);
			}
		}
		return fixedRegex;
	}

	public boolean checkExceptions(String prefix) {
		boolean valReturn = true;
		for(String exception: regexExceptions) {
			if(prefix.matches(exception)) {
				valReturn = false;
			}
		}
		return valReturn;
	}

	public void savePage(String givenUrl) throws IOException {
		String urlAtHtml = "<!--" + givenUrl + "-->\n"; 
		
		try 
		{
			File f = new File("C:\\Users\\emanu\\Desktop\\outputs\\heuristica\\saraiva\\" + this.hostname + countFile + ".html");
			FileOutputStream fop = new FileOutputStream(f);
			fop.write(urlAtHtml.getBytes());
			fop.write(this.htmlFile.getBytes());
			fop.flush();
			fop.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		countFile++;
	}
	
	public boolean checkLastBar(String givenUrl){
		String aux;
		List<Integer> indices = new LinkedList<Integer>();
		for(int i = 0; i < givenUrl.length(); i++){
			if(givenUrl.charAt(i) == '/'){
				indices.add(i);
			}
		}
		aux = givenUrl.substring(indices.get(indices.size()-1)+1).toLowerCase();
		return checkRelevance(aux);
	}
	
	public boolean checkRelevance(String text){
		for(int i = 0; i < keywordsAtUrl.length; i++){
			if(text.contains("temporada") || text.contains("season")){
				return false;
			}
			if(text.contains(keywordsAtUrl[i].toLowerCase())){
				return true;
			}
		}
		return false;
	}
	
	public boolean checkProductAtUrl(String text){
		if(text.contains("/p/") || text.contains("/pr")){
			return true;
		}
		return false;
	}


}
