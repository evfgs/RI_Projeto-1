package processamentoConsulta;

public class ListPosting {
	String docID;
	String termo;
	double frequencia;

	public ListPosting(String docID, String termo, double frequencia) {
		this.docID = docID;
		this.termo = termo;
		this.frequencia = frequencia;
	}

	public ListPosting() {
		// TODO Auto-generated constructor stub
	}

	ListPosting next;

	void insert(ListPosting l) {
		if (next == null) {
			next = l;
		}else {
			next.insert(l);
		}
	}

	double getFreqTerm(String id, String busca) {
		if (id.equals(docID) && busca.equals(termo)) {
			return frequencia;
		}else {
			if (next != null) {
				return next.getFreqTerm(id, busca);
			}else {
				return 0;
			}
		}
	}
	
	boolean docHasTerm(String id, String busca){
		if(id.equals(docID) && busca.equals(termo)){
			return true;
		} else{
			return false;
		}
	}
}
