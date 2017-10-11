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

public class GetLinks {

	private static final String USER_AGENT =
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
	private List<String> links = new LinkedList<String>();
	private Document html;
	private String[] keywordsAtUrl = {"filme", "filmes", "dvd", "blu-ray", "discos", "trilogia", "blu", "ray", "colecao", "3d", "box"};
	private String[] keywordsAtBody = {"diretor", "título", "idioma", "legenda", "origem", "distribuidora", "produção", "discos", "colorido", "formato", "duração", "classificação indicativa","estúdio", "elenco", "sinopse", "gênero"};
	private List<String> pagesDisallowed = new LinkedList<String>();
	private List<String> listRegex = new LinkedList<String>();
	private int countFile = 0;
	private String hostname;
	private String htmlFile;


	public boolean crawl(String url, String hostname){
		this.hostname = hostname;
		try
		{
			Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
			Document htmlDocument = connection.get();
			this.html = htmlDocument;
			this.htmlFile = htmlDocument.toString();
			if(connection.response().statusCode() == 200)
				if(!connection.response().contentType().contains("text/html")){

					return false;
				}
			Elements linksOnPage = htmlDocument.select("a[href]");
			for(Element link : linksOnPage){

				String pagina = link.absUrl("href").toLowerCase();
				if(pagina.contains("filme") || pagina.contains("dvd") || pagina.contains("blu-ray")){
					this.links.add(link.absUrl("href"));
				}

			}
			return true;
		}
		catch(IOException ioe)
		{
			return false;
		}
	}




	public boolean checkKeywordsBody(String givenUrl){
		int pageScore = 0;

		if(this.html == null){
			return false;
		}
		String bodyText = this.html.body().text();
		if(bodyText.toLowerCase().contains("temporada")){
			return false;
		}
		for(String keyword: keywordsAtBody){
			if( bodyText.toLowerCase().contains(keyword.toLowerCase())){
				pageScore++;
			}
		}
		if(pageScore >=6){
			return true;
		}
		return false;

	}


	public List<String> returnLinks()
	{
		return this.links;
	}

	public void clearLinks(){
		this.links = new LinkedList<String>();;
	}

	public void getPagesDisallowed(String url) {
		try{
			Connection connection = Jsoup.connect(url + "/robots.txt").userAgent(USER_AGENT);
			Document htmlDocument = connection.get();
			this.html = htmlDocument;
			String body = htmlDocument.body().text();

			while (body.contains("Disallow: ")) {
				body = body.substring(body.indexOf("Disallow: ") + 10);
				if (body.contains(" ")) {
					String exception = body.substring(0, body.indexOf(" "));
					body = body.substring(body.indexOf(" "), body.length());
					this.pagesDisallowed.add(exception);
				}else {
					String exception = body;
					//guarda os caminhos desabilitados na lista pagesDisallowed
					this.pagesDisallowed.add(exception);
				}
			}
			setRegex(url);

			System.out.println(listRegex);
		}
		catch(IOException ioe)
		{
			System.out.println(ioe);
		}
	}

	public void setRegex(String urlPrefix){

		for(String exception: pagesDisallowed) {
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
			regex = matchRegex(regex);
			this.listRegex.add(regex);
		}
	}

	public String matchRegex(String regex) {
		String newRegex = "";
		for(int i = 0; i < regex.length(); i++) {
			if((regex.charAt(i) + "").matches("[?]")) {
				newRegex = newRegex + "\\" + regex.charAt(i);
			}else {
				newRegex = newRegex + regex.charAt(i);
			}
		}
		return newRegex;
	}

	public boolean checkpagesDisallowed(String prefix) {
		boolean valReturn = true;
		for(String exception: listRegex) {
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
			File f = new File("C://Users//emanu//Desktop//outputs//heuristica//Nova Pasta//" + this.hostname + countFile + ".html");
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
		if(text.contains("temporada") || text.contains("season")|| text.contains("secao") || text.contains("temp")){
			return false;
		}
		for(int i = 0; i < keywordsAtUrl.length; i++){
			if(text.contains(keywordsAtUrl[i].toLowerCase())){
				return true;
			}
		}
		return false;
	}

	public boolean checkProductAtUrl(String text){
		if(text.contains("/p/") || text.contains("/pr")){
			return checkRelevance(text);
		}
		return false;
	}

	public boolean checkTitle(String givenUrl){
		try{
			Connection connection = Jsoup.connect(givenUrl).userAgent(USER_AGENT);
			Document htmlDocument = connection.get();
			Elements tagTitle = htmlDocument.select("title");
			String title = tagTitle.toString().toLowerCase();
			if(title.contains("temporada") || title.contains("season") || title.contains("serie") || title.contains("secao") || title.contains("temp")){
				return false;
			} else if(title.contains("dvd") || title.contains("blu-ray")){
				return true;
			}
		}
		catch(IOException ioe)
		{
			System.out.println(ioe);
		}
		return false;
	}



}
