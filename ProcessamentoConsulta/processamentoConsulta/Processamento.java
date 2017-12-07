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
	
	private static String query = "hard hand";
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


	public static ArrayList<ListaTermos> sortByCount(ArrayList<ListaTermos> a){//sorting
		Collections.sort(a, new Comparator<ListaTermos>() {
			public int compare(ListaTermos a1, ListaTermos a2) {
				if(a1.count > a2.count)
					return -1;
				else if (a1.count < a2.count)
					return +1;
				else
					return 0;
			}});
		return a;
	}

	public static String documentAtATimeAnd(String[] terms, HashMap<String, ListaTermos> indexMap){
		long time = System.currentTimeMillis();
		ArrayList<ListaTermos> listOfLL = new ArrayList<>();
		for(int i =0; i<terms.length;i++)
		{
			ListaTermos addToTemp = sortedByDF_list_desc(indexMap.get(terms[i]));
			listOfLL.add(addToTemp);
		}
		ArrayList<String> s;
		s = documentAnd(listOfLL);
		//        System.out.println(s.toString());
		time = System.currentTimeMillis() - time;
		String DAATAnd = "Consulta: ";
		for(String term: terms){
			DAATAnd += term + ", ";
		}

		DAATAnd += "\n"+s.size()+" documentos encontrados : \n";

		for(String term : s){
			postings = s;
			DAATAnd += term + ", ";
			//documentos retornados
		}
		return DAATAnd;
	}

	public static ListaTermos sortedByDF_list_desc(ListaTermos newIndex){

		int size = 0;
		for(Postings p = newIndex.head; p != null; p = p.next)
			size++;

		boolean flag = true;
		String tempDocID;
		int tempTimes;
		while ( flag )
		{
			flag= false;
			for(int j=0;  j < size -1;  j++ )
			{
				Postings p1 = newIndex.head.getAt(newIndex.head, j);
				Postings p2 = newIndex.head.getAt(newIndex.head, j+1);

				if ( (newIndex.head.getAt(newIndex.head, j)).getDocID() > (newIndex.head.getAt(newIndex.head, j+1)).getDocID() )
				{
					tempDocID = p1.docID;
					tempTimes = p1.times;
					p1.docID = p2.docID;
					p1.times = p2.times;
					p2.docID = tempDocID;
					p2.times = tempTimes;
					flag = true;
				}
			}
		}
		return newIndex;
	}
	public static ArrayList<String> documentAnd(ArrayList<ListaTermos> al){
		ArrayList<String> res = new ArrayList<>();
		while(allNotNull(al))
		{
			if(allAreEqual(al)){

				res.add(al.get(0).head.docID);
				al = incrementLowest(al);

			}
			else
			{
				al = incrementLowest(al);
			}
		}

		return res;
	} 

	public static boolean allNotNull(ArrayList<ListaTermos> al){
		boolean flag = true;
		for(ListaTermos a : al){
			if(a.head == null)
				flag=false;
			break;
		}
		return flag;
	}

	public static boolean allAreEqual(ArrayList<ListaTermos> al){
		//       DAATComp=0;
		boolean flag = true;
		for(int i=0;i< al.size()-1;i++){
			if(al.get(i).head == null || al.get(i+1).head== null){
				flag=false;
				break;
			}
			if(!(al.get(i).head.docID.equals(al.get(i+1).head.docID))){
				flag = false;
				break;
			}
			//           DAATComp++;
		}

		return flag;
	}

	public static ArrayList<ListaTermos> incrementLowest(ArrayList<ListaTermos> al){
		int low =0;
		for(int i=0; i<al.size();i++){
			if(al.get(i).head == null)
				continue;
			int current = Integer.parseInt(al.get(i).head.docID);
			if(current< low || low == 0)
			{
				low=current;
			}
		}
		for(int i=0; i<al.size();i++){
			if(al.get(i).head == null)
				continue;
			if(Integer.parseInt(al.get(i).head.docID) == low){
				al.get(i).head = al.get(i).head.next;
			}
		}
		return al;
	}
	public static void vectorsTf(ArrayList<String> postings2, BufferedReader indexReader) throws IOException {
		String sCurrentLine;
		Postings post;
		ArrayList<ListaTermos> newIndex = new ArrayList<>();
		ArrayList<String> termos = new ArrayList<>();
		DocList l1 = new DocList();
		while ((sCurrentLine = indexReader.readLine()) != null) {
			for(int i = 0; i < postings2.size(); i++) {
				if (sCurrentLine.contains(postings2.get(i))) {
					ListaTermos temp = new ListaTermos();
					String term[] = sCurrentLine.split("\\\\");
					temp.term = term[0];
					String out = sCurrentLine.substring(sCurrentLine.indexOf("[")+1,sCurrentLine.indexOf("]")); 
					String[] parts = out.split(", ");
					temp.count = parts.length;
					temp.frequency = 0;
					if(!termos.contains(temp.term)) {
						termos.add(temp.term);
					}

					for (int k = 0; k<parts.length; k++) {
						post = new Postings();
						String temp1[] = parts[k].split("/");
						post.docID = temp1[0];
						if (post.docID.equals(postings2.get(i))) {
							temp.frequency += Integer.parseInt(temp1[1]);
							post.times = Integer.parseInt(temp1[1]);
							l1.insert(new DocList(post.docID, temp.term, Double.parseDouble(temp1[1])));
							//System.out.println(temp.term + " " + post.docID + " " + temp1[1]);
							if(temp.head == null){
								temp.head = post;
								temp.last = temp.head;           
							}else {
								temp.last.next = post;
								post.prev = temp.last;
								post.next = null;
								temp.last = post;
							}
						}
					}
					//System.out.println(temp.term + " " + temp.frequency);
					newIndex.add(temp); 
				}
			}
		}
		System.out.println(termos);
		double vectors[][] = new double[postings2.size() + 1][termos.size()]; //doc.length +1 pois adiciona o vetor da query
		for(int i = 0; i < postings2.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				vectors[i][j] = l1.procurarTermo(postings2.get(i), termos.get(j));//pegar o valor dos vetores dos docs
			}
		} //fim do metodo buildVectors

		String[] queryArr;
		if (query.split(" ") == null) {
			queryArr = new String[0];
			queryArr[0] = query;
		}else {
			queryArr = query.split(" ");
		}
		for(int i = 0; i < queryArr.length; i++) {// criando o vetor da querry, onde
			for(int j = 0; j < termos.size(); j++) {//a celula referente ao termo vai ser: ((qtd de vezes que o termo aparece na query)/(qtd de docs que possui o termo da query)) * tf
				if (queryArr[i].equals(termos.get(j))) {
					double countDocTerm = 0;
					double termFrequency = 0;
					for(int p = 0; p < postings2.size(); p++) {
						if (vectors[p][j] > 0) {
							termFrequency += vectors[p][j];
							countDocTerm++;
						}
					}
					vectors[postings2.size()][j] += (1/countDocTerm) * termFrequency;//multifplicar pelo tf
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
		//sortByComparator(rank(postings2, cosSimilarity(postings2, termos, vectors)));
		System.out.println(rank(postings2, cosSimilarity(postings2, termos, vectors)));
	}
	//Repetição de codigo desnecessaria
	//TODO: Refatoração
	public static void vectorsTfIdf(ArrayList<String> postings2, BufferedReader indexReader) throws IOException {
		String sCurrentLine;
		Postings post;
		ArrayList<ListaTermos> newIndex = new ArrayList<>();
		ArrayList<String> termos = new ArrayList<>();
		DocList l1 = new DocList();
		while ((sCurrentLine = indexReader.readLine()) != null) {
			for(int i = 0; i < postings2.size(); i++) {
				if (sCurrentLine.contains(postings2.get(i))) {
					ListaTermos temp = new ListaTermos();
					String term[] = sCurrentLine.split("\\\\");
					temp.term = term[0];
					String out = sCurrentLine.substring(sCurrentLine.indexOf("[")+1,sCurrentLine.indexOf("]")); 
					String[] parts = out.split(", ");
					temp.count = parts.length;
					temp.frequency = 0;
					if(!termos.contains(temp.term)) {
						termos.add(temp.term);
					}

					for (int k = 0; k<parts.length; k++) {
						post = new Postings();
						String temp1[] = parts[k].split("/");
						post.docID = temp1[0];
						if (post.docID.equals(postings2.get(i))) {
							temp.frequency += Integer.parseInt(temp1[1]);
							post.times = Integer.parseInt(temp1[1]);
							l1.insert(new DocList(post.docID, temp.term, Double.parseDouble(temp1[1])));
							//System.out.println(temp.term + " " + post.docID + " " + temp1[1]);
							if(temp.head == null){
								temp.head = post;
								temp.last = temp.head;           
							}else {
								temp.last.next = post;
								post.prev = temp.last;
								post.next = null;
								temp.last = post;
							}
						}
					}
					//System.out.println(temp.term + " " + temp.frequency);
					newIndex.add(temp); 
				}
			}
		}
		System.out.println(termos);
		double vectors[][] = new double[postings2.size() + 1][termos.size()]; //doc.length +1 pois adiciona o vetor da query
		for(int i = 0; i < postings2.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				vectors[i][j] = l1.procurarTermo(postings2.get(i), termos.get(j));//pegar o valor dos vetores dos docs
			}
		}
		//para calcular o numero de docs com o termo
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
				vectors[i][j] = l1.procurarTermo(postings2.get(i), termos.get(j)) * idfs[j];//pegar o valor dos vetores dos docs
			}
		}

		String[] queryArr;
		if (query.split(" ") == null) {
			queryArr = new String[0];
			queryArr[0] = query;
		}else {
			queryArr = query.split(" ");
		}
		for(int i = 0; i < queryArr.length; i++) {// criando o vetor da querry, onde
			for(int j = 0; j < termos.size(); j++) {//a celula referente ao termo vai ser: ((qtd de vezes que o termo aparece na query)/(qtd de docs que possui o termo da query)) * tf
				if (queryArr[i].equals(termos.get(j))) {
					double countDocTerm = 0.0;
					double termFrequency = 0.0;
					for(int p = 0; p < postings2.size(); p++) {
						if (vectors[p][j] > 0) {
							termFrequency += vectors[p][j];
							countDocTerm++;
						}
					}
					//tem que pegar o tf antigo
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
		//sortByComparator(rank(postings2, cosSimilarity(postings2, termos, vectors)));
		System.out.println(rank(postings2, cosSimilarity(postings2, termos, vectors)));
	}
	
	public static void getPostings(String query, BufferedReader indexReader) throws NumberFormatException, IOException{

		String currentLine;
		ArrayList<ListaTermos> index = new ArrayList<>();
		Postings post;
		//vai ler o indice invertido e separar os campos do mesmo(termo, docs, frequencia)
		while ((currentLine = indexReader.readLine()) != null) {
			ListaTermos auxList = new ListaTermos();
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
				post.times = Integer.parseInt(field[1]);
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
		HashMap<String, ListaTermos> IndexMap = new HashMap<>();
		for (ListaTermos tIndex : index) {
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
			String output = documentAtATimeAnd(terms, IndexMap);
			System.out.println(output);
		}

	}
	
	public static Map<String, Double> rank(ArrayList<String> postings, double[] cos){
		Map<String,Double> map = new LinkedHashMap<String,Double>();
		for(int i = 0; i < cos.length; i++){
			map.put(postings.get(i), cos[i]);
		}
		return map;
		
	}
	
	/*public static void sortByComparator(Map<String, Double> unsortMap){
		Map<Object, Object> sortedMap = 
			     unsortMap.entrySet().stream()
			    .sorted(Entry.comparingByValue())
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue,
			                              (e1, e2) -> e1, LinkedHashMap::new));		
		
		ArrayList<Object> keys = new ArrayList<Object>(sortedMap.keySet());
        for(int i=keys.size()-1; i>=0;i--){
            System.out.println(sortedMap.get(keys.get(i)));
        }
	}*/
	
	public static double[] cosSimilarity(ArrayList<String> postings2, ArrayList<String> termos, double vectors[][]){
		double cosSimilarity[] = new double[postings2.size()];
		for(int i = 0; i < postings2.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				cosSimilarity[i] = dotProduct(postings2, termos, vectors)[i]/vectorLength(postings2, termos, vectors)[i] * vectorLength(postings2, termos, vectors)[postings2.size()];
			}
		}
		return cosSimilarity;
	}

	public static double[] dotProduct(ArrayList<String> postings2, ArrayList<String> termos, double vectors[][]){
		double dotProduct[] = new double[postings2.size()];
		for(int i = 0; i < postings2.size(); i++) {
			for(int j = 0; j < termos.size(); j++) {
				dotProduct[i] += vectors[i][j] * vectors[postings2.size()][j];
			}

		}
		return dotProduct;
	}
	
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
