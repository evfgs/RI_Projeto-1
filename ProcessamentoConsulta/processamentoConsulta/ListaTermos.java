package processamentoConsulta;

public class ListaTermos {
    public String term;
    public int count;
//    Postings list;
    public Postings head;
    public Postings last;
	public int frequency;
    
    public int getCount(){
        return count;
    }
    
    public String getTerm(){
        return term;
    }
    public int getFrequency(){
    	return frequency;
    }
}