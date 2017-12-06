package processamentoConsulta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class Processamento {
	//TF(t) = (Number of times term t appears in a document) / (Total number of terms in the document).
	//IDF(t) = log_e(Total number of documents / Number of documents with term t in it).
	// provavelmente o id minimo � 0009603 e o maximo � 0012901
	//totalizando 3298 docs
	//numero de termos num documento = ctrl + f
	//acho que vai precisar saber quais termos tem em determinado documento

	private static int DAATAndCount = 0;
	private static String query = "discuss gain";
	static String[] docs = {"0009797", "0010073", "0011037", "0011465"};

	public static void main(String[] args) throws NumberFormatException, IOException {
		
		//Gambiarra master a seguir:
		Reader input = new FileReader("C:\\Users\\emanu\\Desktop\\RI\\FUNCIONAL\\taat-daat-master\\term.txt");
		BufferedReader indexReader = new BufferedReader(input);
		readIndexAndSearch(query, indexReader);
		Reader input2 = new FileReader("C:\\Users\\emanu\\Desktop\\RI\\FUNCIONAL\\taat-daat-master\\term.txt");
		BufferedReader indexReader2 = new BufferedReader(input2);
		tf(docs, indexReader2);
	}

	public static void readIndexAndSearch(String query, BufferedReader indexReader) throws NumberFormatException, IOException{

		String sCurrentLine;
		ArrayList<IndexList> newIndex = new ArrayList<>();
		Postings post;

		while ((sCurrentLine = indexReader.readLine()) != null) {
			IndexList temp = new IndexList();
			String term[] = sCurrentLine.split("\\\\");
			temp.term = term[0];
			String out = sCurrentLine.substring(sCurrentLine.indexOf("[")+1,sCurrentLine.indexOf("]"));
			String[] parts = out.split(", ");
			temp.count = parts.length;
			for (int k = 0; k<parts.length; k++) {
				post = new Postings();
				String temp1[] = parts[k].split("/");
				post.docID = temp1[0];
				//times = frequencia
				post.times = Integer.parseInt(temp1[1]);
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
			newIndex.add(temp);
		}

		newIndex = sortByCount(newIndex);
		HashMap<String, IndexList> IndexMap = new HashMap<>();
		for (IndexList tIndex : newIndex) {
			IndexMap.put(tIndex.getTerm(), tIndex);
		}

		String terms[] = query.split(" ");
		boolean abc = true;

		for(String term :terms){
			if(IndexMap.get(term)!= null){

			}else {
				abc=false;
				System.out.println("Consulta " + term +  " n�o encontrada");
			}
		}
		if(abc){
			String DAATAnd = documentAtATimeAnd(terms, IndexMap);
			System.out.println(DAATAnd);
		}



	}

	public static ArrayList<IndexList> sortByCount(ArrayList<IndexList> a){
		Collections.sort(a, new Comparator<IndexList>() {
			public int compare(IndexList a1, IndexList a2) {
				if(a1.count > a2.count)
					return -1;
				else if (a1.count < a2.count)
					return +1;
				else
					return 0;
			}});
		return a;
	}

	public static String documentAtATimeAnd(String[] terms, HashMap<String, IndexList> indexMap){
		long time = System.currentTimeMillis();
		DAATAndCount =0;
		ArrayList<IndexList> listOfLL = new ArrayList<>();
		for(int i =0; i<terms.length;i++)
		{
			IndexList addToTemp = sortedByDF_list_desc(indexMap.get(terms[i]));
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
			DAATAnd += term + ", ";
			//documentos retornados
		}
		return DAATAnd;
	}

	public static IndexList sortedByDF_list_desc(IndexList newIndex){

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
	public static ArrayList<String> documentAnd(ArrayList<IndexList> al){
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

	public static boolean allNotNull(ArrayList<IndexList> al){
		boolean flag = true;
		for(IndexList a : al){
			if(a.head == null)
				flag=false;
			break;
		}
		return flag;
	}

	public static boolean allAreEqual(ArrayList<IndexList> al){
		//       DAATComp=0;
		boolean flag = true;
		for(int i=0;i< al.size()-1;i++){
			if(al.get(i).head == null || al.get(i+1).head== null){
				flag=false;
				DAATAndCount++;
				break;
			}
			if(!(al.get(i).head.docID.equals(al.get(i+1).head.docID))){
				flag = false;
				DAATAndCount++;
				break;
			}
			//           DAATComp++;
		}

		return flag;
	}

	public static ArrayList<IndexList> incrementLowest(ArrayList<IndexList> al){
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
	public static void tf(String[] doc, BufferedReader indexReader) throws IOException {
		String sCurrentLine;
		Postings post;
		ArrayList<IndexList> newIndex = new ArrayList<>();
		ArrayList<String> termos = new ArrayList<>();
		DocList l1 = new DocList();
		while ((sCurrentLine = indexReader.readLine()) != null) {
			for(int i = 0; i < doc.length; i++) {
				if (sCurrentLine.contains(doc[i])) {
					IndexList temp = new IndexList();
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
						if (post.docID.equals(doc[i])) {
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
		double vectors[][] = new double[doc.length + 1][termos.size()]; //doc.length +1 pois adiciona o vetor da query
		for(int i = 0; i < doc.length; i++) {
			for(int j = 0; j < termos.size(); j++) {
				vectors[i][j] = l1.procurarTermo(doc[i], termos.get(j));//pegar o valor dos vetores dos docs
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
					for(int p = 0; p < doc.length; p++) {
						if (vectors[p][j] > 0) {
							//termFrequency += vectors[p][j];
							countDocTerm++;
						}
					}
					vectors[doc.length][j] += (1/countDocTerm);//multifplicar pelo tf-idf
				}
			}
		}
		
		double dotProduct[] = new double[doc.length];
		for(int i = 0; i < doc.length; i++) {
			for(int j = 0; j < termos.size(); j++) {
				dotProduct[i] += vectors[i][j] * vectors[doc.length][j];
			}
		}
		
		double vectorLength[] = new double[doc.length + 1];
		for(int i = 0; i < doc.length + 1; i++) {
			for(int j = 0; j < termos.size(); j++) {
				vectorLength[i] += vectors[i][j]*vectors[i][j];
			}
			vectorLength[i] = Math.sqrt(vectorLength[i]);
		}
		
		double cosSimilarity[] = new double[doc.length];
		for(int i = 0; i < doc.length; i++) {
			for(int j = 0; j < termos.size(); j++) {
				cosSimilarity[i] = dotProduct[i]/vectorLength[i]*vectorLength[doc.length];
			}
		}

		for(int i = 0; i < doc.length + 1; i++) {
			for(int j = 0; j < termos.size(); j++) {
				System.out.print(vectors[i][j] + " ");
			}
			System.out.println();
		}
		for(int i = 0; i < doc.length; i++) {
			System.out.println(cosSimilarity[i]);
		}

	}

}
