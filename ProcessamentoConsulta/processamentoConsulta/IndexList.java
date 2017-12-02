package processamentoConsulta;

public class IndexList {
    public String term;
    public int count;
//    Postings list;
    public Postings head;
    public Postings last;
    
    public int getCount(){
        return count;
    }
    
    public String getTerm(){
        return term;
    }
}