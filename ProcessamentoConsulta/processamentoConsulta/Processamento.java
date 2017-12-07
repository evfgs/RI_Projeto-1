package processamentoConsulta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Processamento {
	//TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
	//IDF(t) = log_e(Total number of documents / Number of documents with term t in it).
	// provavelmente o id minimo é 0009603 e o maximo é 0012901
	//totalizando 3298 docs
	//numero de termos num documento = ctrl + f
	//acho que vai precisar saber quais termos tem em determinado documento

	//TODO Refatoração, muito código repetido!
	
	private static String query = "hard hand"; //high hope
	private static ArrayList<String> postings;

	public static void main(String[] args) throws NumberFormatException, IOException {

		//Gambiarra master a seguir:
		Reader input = new FileReader("C:\\Users\\emanu\\Desktop\\RI\\FUNCIONAL\\taat-daat-master\\term.txt");
		BufferedReader indexReader = new BufferedReader(input);
		getPostings(query, indexReader);
		Reader input2 = new FileReader("C:\\Users\\emanu\\Desktop\\RI\\FUNCIONAL\\taat-daat-master\\term.txt");
		BufferedReader indexReader2 = new BufferedReader(input2);
		vectorsTf(postings, indexReader2);
		Reader input3 = new FileReader("C:\\Users\\emanu\\Desktop\\RI\\FUNCIONAL\\taat-daat-master\\term.txt");
		BufferedReader indexReader3 = new BufferedReader(input3);
		vectorsTfIdf(postings, indexReader3);
	}
	//Vai pegar os documentos e ver se eles possuem os termos da query(DocAtATime)
	public static String getPostingsAtTime(String[] terms, HashMap<String, Termo> indexMap){
		ArrayList<Termo> sortedList = new ArrayList<>();
		for(int i =0; i<terms.length;i++){
			Termo sortedDocs = sortDocFreq(indexMap.get(terms[i]));
			sortedList.add(sortedDocs);
		}
		ArrayList<String> postingsList;
		postingsList = documentAnd(sortedList);
		String printOutput = "Consulta: ";
		for(String term: terms){
			printOutput += term + ", ";
		}

		printOutput += "\n"+postingsList.size()+" documentos encontrados : \n";

		for(String posting : postingsList){
			postings = postingsList;
			printOutput += posting + ", ";
			//documentos retornados
		}
		return printOutput;
	}
	
	//Método principal para pegar os postings e retornar aqueles que possuem os termos da query
	public static void getPostings(String query, BufferedReader indexReader) throws NumberFormatException, IOException{

		String currentLine;
		ArrayList<Termo> index = new ArrayList<>();
		Postings post;
		//vai ler o indice invertido e separar os campos do mesmo(termo, docs, frequencia)
		while ((currentLine = indexReader.readLine()) != null) {
			Termo auxList = new Termo();
			String term[] = currentLine.split("\\\\");
			auxList.term = term[0];
			String out = currentLine.substring(currentLine.indexOf("[")+1,currentLine.indexOf("]"));//esse array seria a lista de postings e suas respectivas frequencias
			String[] fields = out.split(", ");
			auxList.count = fields.length;
			for (int k = 0; k<fields.length; k++) {
				post = new Postings();
				String field[] = fields[k].split("/");//os postings e respectivas frequencias sao divididos pela '/'
				post.docID = field[0];
				//times = frequencia
				post.freq = Integer.parseInt(field[1]);
				if(auxList.head == null){
					auxList.head = post;
					auxList.last = auxList.head;          
				}else {
					auxList.last.next = post;
					post.prev = auxList.last;
					post.next = null;
					auxList.last = post;
				}
			}
			index.add(auxList);
		}

		index = sortByCount(index);//faz o sort da Lista
		HashMap<String, Termo> IndexMap = new HashMap<>();
		for (Termo tIndex : index) {
			IndexMap.put(tIndex.getTerm(), tIndex);
		}

		String terms[] = query.split(" ");
		boolean termInQuery = true;

		for(String term :terms){//Se a lista de termos possui determinado elemento da query
			if(IndexMap.get(term)!= null){

			}else {
				termInQuery=false;
				System.out.println("Consulta " + term +  " não encontrada");
			}
		}
		if(termInQuery){
			String output = getPostingsAtTime(terms, IndexMap);
			System.out.println(output);
		}

	}
	
	//Avalia se os termos do documento são validos(not null), se são iguais(allEqual) 
	public static ArrayList<String> documentAnd(ArrayList<Termo> terms){
		ArrayList<String> toIncrement = new ArrayList<>();
		while(checkListNotNull(terms))
		{
			if(checkAllEqual(terms)){

				toIncrement.add(terms.get(0).head.docID);
				terms = increment(terms);

			}
			else
			{
				terms = increment(terms);
			}
		}

		return toIncrement;
	} 
	
	public static ArrayList<Termo> sortByCount(ArrayList<Termo> terms){//sorting
		Collections.sort(terms, new Comparator<Termo>() {
			public int compare(Termo a1, Termo a2) {
				if(a1.count > a2.count)
					return -1;
				else if (a1.count < a2.count)
					return +1;
				else
					return 0;
			}});
		return terms;
	}

	//Sorting by Document frequency
	public static Termo sortDocFreq(Termo newIndex){

		int size = 0;
		for(Postings p = newIndex.head; p != null; p = p.next)
			size++;

		boolean flag = true;
		String auxDocId;
		int auxFreq;
		while ( flag )
		{
			flag= false;
			for(int j=0;  j < size -1;  j++ )
			{
				Postings p1 = newIndex.head.getAt(newIndex.head, j);
				Postings p2 = newIndex.head.getAt(newIndex.head, j+1);

				if ( (newIndex.head.getAt(newIndex.head, j)).getDocID() > (newIndex.head.getAt(newIndex.head, j+1)).getDocID() )
				{
					auxDocId = p1.docID;
					auxFreq = p1.freq;
					p1.docID = p2.docID;
					p1.freq = p2.freq;
					p2.docID = auxDocId;
					p2.freq = auxFreq;
					flag = true;
				}
			}
		}
		return newIndex;
	}

	//check if list is not null
	public static boolean checkListNotNull(ArrayList<Termo> terms){
		boolean listNotNull = true;
		for(Termo term : terms){
			if(term.head == null)
				listNotNull=false;
			break;
		}
		return listNotNull;
	}

	//check if all terms of list are equal
	public static boolean checkAllEqual(ArrayList<Termo> terms){
		boolean allEqual = true;
		for(int i=0;i< terms.size()-1;i++){
			if(terms.get(i).head == null || terms.get(i+1).head== null){
				allEqual=false;
				break;
			}
			if(!(terms.get(i).head.docID.equals(terms.get(i+1).head.docID))){
				allEqual = false;
				break;
			}
		}

		return allEqual;
	}
	public static ArrayList<Termo> increment(ArrayList<Termo> terms){
		int lowest =0;
		for(int i=0; i<terms.size();i++){//take the lowest frequency
			if(terms.get(i).head == null)
				continue;
			int current = Integer.parseInt(terms.get(i).head.docID);
			if(current< lowest || lowest == 0)
			{
				lowest=current;
			}
		}
		for(int i=0; i<terms.size();i++){
			if(terms.get(i).head == null)
				continue;
			if(Integer.parseInt(terms.get(i).head.docID) == lowest){
				terms.get(i).head = terms.get(i).head.next;
			}
		}
		return terms;
	}
	
	//Vai tratar o tf, imprimindo os termos, vetores dos docs e query(todos com seus respectivos tf)
	//imprime tbm o valor da similaridade do cosseno, e os docs ordenados baseados nesse valor
	public static void vectorsTf(ArrayList<String> postings, BufferedReader indexReader) throws IOException {
		String currentLine;
		Postings post;
		ArrayList<Termo> newIndex = new ArrayList<>();
		ArrayList<String> termos = new ArrayList<>();
		ListPosting listDocs = new ListPosting();
		while ((currentLine = indexReader.readLine()) != null) { //Lê o indice invertido novamente, para poder avaliar os docs com o tf
			for(int i = 0; i < postings.size(); i++) {
				if (currentLine.contains(postings.get(i))) {
					Termo auxList = new Termo();
					String term[] = currentLine.split("\\\\");
					auxList.term = term[0];
					String out = currentLine.substring(currentLine.indexOf("[")+1,currentLine.indexOf("]")); 
					String[] fields = out.split(", ");
					auxList.count = fields.length;
					auxList.frequency = 0;
					if(!termos.contains(auxList.term)) {
						termos.add(auxList.term);
					}

					for (int k = 0; k<fields.length; k++) {
						post = new Postings();
						String field[] = fields[k].split("/");
						post.docID = field[0];
						if (post.docID.equals(postings.get(i))) {
							auxList.frequency += Integer.parseInt(field[1]);
							post.freq = Integer.parseInt(field[1]);
							listDocs.insert(new ListPosting(post.docID, auxList.term, Double.parseDouble(field[1])));
							//System.out.println(temp.term + " " + post.docID + " " + temp1[1]);
							if(auxList.head == null){
								auxList.head = post;
								auxList.last = auxList.head;           
							}else {
								auxList.last.next = post;
								post.prev = auxList.last;
								post.next = null;
								auxList.last = post;
							}
						}
					}
					newIndex.add(auxList); 
				}
			}
		}
		//cria vectors, que são todos os vetores dos documentos + o vetor da query
		System.out.println(termos);
		double vectors[][] = new double[postings.size() + 1][termos.size()]; //doc.length +1 pois adiciona o vetor da query
		for(int i = 0; i < postings.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				vectors[i][j] = listDocs.getFreqTerm(postings.get(i), termos.get(j));//pegar o valor dos vetores dos docs
			}
		}
		//Atualizar o vector com o valor do tf
		String[] queryArray;
		if (query.split(" ") == null) {
			queryArray = new String[0];
			queryArray[0] = query;
		}else {
			queryArray = query.split(" ");
		}
		for(int i = 0; i < queryArray.length; i++) {// criando o vetor da querry, onde
			for(int j = 0; j < termos.size(); j++) {//a celula referente ao termo vai ser: ((qtd de vezes que o termo aparece na query)/(qtd de docs que possui o termo da query)) * tf
				if (queryArray[i].equals(termos.get(j))) {
					double countDocTerm = 0;
					double termFrequency = 0;
					for(int p = 0; p < postings.size(); p++) {
						if (vectors[p][j] > 0) {
							termFrequency += vectors[p][j];
							countDocTerm++;
						}
					}
					vectors[postings.size()][j] += (1/countDocTerm) * termFrequency;//multifplicar pelo tf
				}
			}
		}
		//somente para printar algum resultado
		for(int i = 0; i < postings.size() + 1; i++) {
			for(int j = 0; j < termos.size(); j++) {
				System.out.print(vectors[i][j] + " ");
			}
			System.out.println();
		}
		for(int i = 0; i < postings.size(); i++) {
			System.out.println(postings.get(i) + "= " + cosSimilarity(postings, termos, vectors)[i]);
		}
		System.out.println(rank(postings, cosSimilarity(postings, termos, vectors)));
	}
	//Repetição de codigo desnecessaria
	//TODO: Refatoração
	//Vai tratar o tfidf, imprimindo os termos, vetores dos docs e query(todos com seus respectivos tfidf)
	//imprime tbm o valor da similaridade do cosseno, e os docs ordenados baseados nesse valor
	public static void vectorsTfIdf(ArrayList<String> postings2, BufferedReader indexReader) throws IOException {
		String currentLine;
		Postings post;
		ArrayList<Termo> newIndex = new ArrayList<>();
		ArrayList<String> termos = new ArrayList<>();
		ListPosting listDocs = new ListPosting();
		while ((currentLine = indexReader.readLine()) != null) {//Vai ler o indice invertido novamente 
			for(int i = 0; i < postings2.size(); i++) {        	//possivel refatoração aqui
				if (currentLine.contains(postings2.get(i))) {
					Termo auxList = new Termo();
					String term[] = currentLine.split("\\\\");
					auxList.term = term[0];
					String out = currentLine.substring(currentLine.indexOf("[")+1,currentLine.indexOf("]")); 
					String[] fields = out.split(", ");
					auxList.count = fields.length;
					auxList.frequency = 0;
					if(!termos.contains(auxList.term)) {
						termos.add(auxList.term);
					}

					for (int k = 0; k<fields.length; k++) {
						post = new Postings();
						String field[] = fields[k].split("/");
						post.docID = field[0];
						if (post.docID.equals(postings2.get(i))) {
							auxList.frequency += Integer.parseInt(field[1]);
							post.freq = Integer.parseInt(field[1]);
							listDocs.insert(new ListPosting(post.docID, auxList.term, Double.parseDouble(field[1])));
							if(auxList.head == null){
								auxList.head = post;
								auxList.last = auxList.head;           
							}else {
								auxList.last.next = post;
								post.prev = auxList.last;
								post.next = null;
								auxList.last = post;
							}
						}
					}
					newIndex.add(auxList); 
				}
			}
		}
		//imprime os termos
		System.out.println(termos);
		double vectors[][] = new double[postings2.size() + 1][termos.size()]; //doc.length +1 pois adiciona o vetor da query
		for(int i = 0; i < postings2.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				vectors[i][j] = listDocs.getFreqTerm(postings2.get(i), termos.get(j));//pegar o valor dos vetores dos docs
			}
		}
		//para calcular o numero de docs com o termo t
		double idfs[] = new double[termos.size()];
		for(int i = 0; i < termos.size(); i++){
			double docTermFreq = 0.0;
			for(int j = 0; j < postings2.size(); j++){
				if(vectors[j][i] > 0){
					docTermFreq++;
				}
			}
			//calcula idf
			double idf = Math.log(postings2.size()/docTermFreq);
			idfs[i] = idf;			
		}
		
		
		//atualiza valores do vector com o idf
		for(int i = 0; i < postings2.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				vectors[i][j] = listDocs.getFreqTerm(postings2.get(i), termos.get(j)) * idfs[j];//pegar o valor dos vetores dos docs
			}
		}

		String[] queryArray;
		if (query.split(" ") == null) {
			queryArray = new String[0];
			queryArray[0] = query;
		}else {
			queryArray = query.split(" ");
		}
		for(int i = 0; i < queryArray.length; i++) {// criando o vetor da querry, onde
			for(int j = 0; j < termos.size(); j++) {//a celula referente ao termo vai ser: ((qtd de vezes que o termo aparece na query)/(qtd de docs que possui o termo da query)) * tf
				if (queryArray[i].equals(termos.get(j))) {
					double countDocTerm = 0.0;
					double termFrequency = 0.0;
					for(int p = 0; p < postings2.size(); p++) {
						if (vectors[p][j] > 0) {
							termFrequency += vectors[p][j];
							countDocTerm++;
						}
					}
					double idf = Math.log(postings2.size()/countDocTerm);
					double weightTfIdf = termFrequency * idf;
					vectors[postings2.size()][j] += (1/countDocTerm) * weightTfIdf;//multifplicar pelo tf-idf
				}
			}
		}
		//somente para printar algum resultado
		for(int i = 0; i < postings2.size() + 1; i++) {
			for(int j = 0; j < termos.size(); j++) {
				System.out.print(vectors[i][j] + " ");
			}
			System.out.println();
		}
		for(int i = 0; i < postings2.size(); i++) {
			System.out.println(cosSimilarity(postings2, termos, vectors)[i]);
		}
		System.out.println(rank(postings2, cosSimilarity(postings2, termos, vectors)));
	}
	
	//rank descendente baseado na similaridade do cosseno
	public static Map<String, Double> rank(ArrayList<String> postings, double[] cos){
		Map<String,Double> map = new LinkedHashMap<String,Double>();
		for(int i = 0; i < cos.length; i++){
			map.put(postings.get(i), cos[i]);
		}
		return map;
		
	}	
	
	//calculo da similaridade do cosseno
	public static double[] cosSimilarity(ArrayList<String> postings2, ArrayList<String> termos, double vectors[][]){
		double cosSimilarity[] = new double[postings2.size()];
		for(int i = 0; i < postings2.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				cosSimilarity[i] = dotProduct(postings2, termos, vectors)[i]/vectorLength(postings2, termos, vectors)[i] * vectorLength(postings2, termos, vectors)[postings2.size()];
			}
		}
		return cosSimilarity;
	}

	//Calculo do dotProduct(entre cada document e a query)
	public static double[] dotProduct(ArrayList<String> postings2, ArrayList<String> termos, double vectors[][]){
		double dotProduct[] = new double[postings2.size()];
		for(int i = 0; i < postings2.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				dotProduct[i] += vectors[i][j] * vectors[postings2.size()][j];
			}

		}
		return dotProduct;
	}
	
	//Calculo do tamando do vetor(||v||) dos documentos e da query
	public static double[] vectorLength(ArrayList<String> postings2, ArrayList<String> termos, double vectors[][]){
		double vectorLength[] = new double[postings2.size() + 1];
		for(int i = 0; i < postings2.size() + 1; i++) {
			for(int j = 0; j < termos.size(); j++) {
				vectorLength[i] += vectors[i][j]*vectors[i][j];
			}
			vectorLength[i] = Math.sqrt(vectorLength[i]);
		}
		return vectorLength;
	}

}
