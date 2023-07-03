// generated with ast extension for cup
// version 0.8
// 25/5/2023 21:12:28


package rs.ac.bg.etf.pp1.ast;

public class MatchedIfElse extends Matched {

    private IfEntry IfEntry;
    private Condition Condition;
    private Matched Matched;
    private ElseEntry ElseEntry;
    private Matched Matched1;

    public MatchedIfElse (IfEntry IfEntry, Condition Condition, Matched Matched, ElseEntry ElseEntry, Matched Matched1) {
        this.IfEntry=IfEntry;
        if(IfEntry!=null) IfEntry.setParent(this);
        this.Condition=Condition;
        if(Condition!=null) Condition.setParent(this);
        this.Matched=Matched;
        if(Matched!=null) Matched.setParent(this);
        this.ElseEntry=ElseEntry;
        if(ElseEntry!=null) ElseEntry.setParent(this);
        this.Matched1=Matched1;
        if(Matched1!=null) Matched1.setParent(this);
    }

    public IfEntry getIfEntry() {
        return IfEntry;
    }

    public void setIfEntry(IfEntry IfEntry) {
        this.IfEntry=IfEntry;
    }

    public Condition getCondition() {
        return Condition;
    }

    public void setCondition(Condition Condition) {
        this.Condition=Condition;
    }

    public Matched getMatched() {
        return Matched;
    }

    public void setMatched(Matched Matched) {
        this.Matched=Matched;
    }

    public ElseEntry getElseEntry() {
        return ElseEntry;
    }

    public void setElseEntry(ElseEntry ElseEntry) {
        this.ElseEntry=ElseEntry;
    }

    public Matched getMatched1() {
        return Matched1;
    }

    public void setMatched1(Matched Matched1) {
        this.Matched1=Matched1;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(IfEntry!=null) IfEntry.accept(visitor);
        if(Condition!=null) Condition.accept(visitor);
        if(Matched!=null) Matched.accept(visitor);
        if(ElseEntry!=null) ElseEntry.accept(visitor);
        if(Matched1!=null) Matched1.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(IfEntry!=null) IfEntry.traverseTopDown(visitor);
        if(Condition!=null) Condition.traverseTopDown(visitor);
        if(Matched!=null) Matched.traverseTopDown(visitor);
        if(ElseEntry!=null) ElseEntry.traverseTopDown(visitor);
        if(Matched1!=null) Matched1.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(IfEntry!=null) IfEntry.traverseBottomUp(visitor);
        if(Condition!=null) Condition.traverseBottomUp(visitor);
        if(Matched!=null) Matched.traverseBottomUp(visitor);
        if(ElseEntry!=null) ElseEntry.traverseBottomUp(visitor);
        if(Matched1!=null) Matched1.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("MatchedIfElse(\n");

        if(IfEntry!=null)
            buffer.append(IfEntry.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Condition!=null)
            buffer.append(Condition.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Matched!=null)
            buffer.append(Matched.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ElseEntry!=null)
            buffer.append(ElseEntry.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Matched1!=null)
            buffer.append(Matched1.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [MatchedIfElse]");
        return buffer.toString();
    }
}
