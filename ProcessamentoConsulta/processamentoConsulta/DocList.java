package processamentoConsulta;

public class DocList {
	String docID;
	String termo;
	double frequencia;

	public DocList(String docID, String termo, double frequencia) {
		this.docID = docID;
		this.termo = termo;
		this.frequencia = frequencia;
	}

	public DocList() {
		// TODO Auto-generated constructor stub
	}

	DocList next;

	void insert(DocList l) {
		if (next == null) {
			next = l;
		}else {
			next.insert(l);
		}
	}

	double procurarTermo(String id, String busca) {
		if (id.equals(docID) && busca.equals(termo)) {
			return frequencia;
		}else {
			if (next != null) {
				return next.procurarTermo(id, busca);
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
