// generated with ast extension for cup
// version 0.8
// 25/5/2023 21:12:28


package rs.ac.bg.etf.pp1.ast;

public class DesignatorStmtFuncCall extends DesignatorStatement {

    private Designator Designator;
    private OptActParsInParens OptActParsInParens;

    public DesignatorStmtFuncCall (Designator Designator, OptActParsInParens OptActParsInParens) {
        this.Designator=Designator;
        if(Designator!=null) Designator.setParent(this);
        this.OptActParsInParens=OptActParsInParens;
        if(OptActParsInParens!=null) OptActParsInParens.setParent(this);
    }

    public Designator getDesignator() {
        return Designator;
    }

    public void setDesignator(Designator Designator) {
        this.Designator=Designator;
    }

    public OptActParsInParens getOptActParsInParens() {
        return OptActParsInParens;
    }

    public void setOptActParsInParens(OptActParsInParens OptActParsInParens) {
        this.OptActParsInParens=OptActParsInParens;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(Designator!=null) Designator.accept(visitor);
        if(OptActParsInParens!=null) OptActParsInParens.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(Designator!=null) Designator.traverseTopDown(visitor);
        if(OptActParsInParens!=null) OptActParsInParens.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(Designator!=null) Designator.traverseBottomUp(visitor);
        if(OptActParsInParens!=null) OptActParsInParens.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DesignatorStmtFuncCall(\n");

        if(Designator!=null)
            buffer.append(Designator.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(OptActParsInParens!=null)
            buffer.append(OptActParsInParens.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DesignatorStmtFuncCall]");
        return buffer.toString();
    }
}
