// generated with ast extension for cup
// version 0.8
// 25/5/2023 21:12:28


package rs.ac.bg.etf.pp1.ast;

public class WhileStmt extends Matched {

    private WhileEntry WhileEntry;
    private Condition Condition;
    private Statement Statement;

    public WhileStmt (WhileEntry WhileEntry, Condition Condition, Statement Statement) {
        this.WhileEntry=WhileEntry;
        if(WhileEntry!=null) WhileEntry.setParent(this);
        this.Condition=Condition;
        if(Condition!=null) Condition.setParent(this);
        this.Statement=Statement;
        if(Statement!=null) Statement.setParent(this);
    }

    public WhileEntry getWhileEntry() {
        return WhileEntry;
    }

    public void setWhileEntry(WhileEntry WhileEntry) {
        this.WhileEntry=WhileEntry;
    }

    public Condition getCondition() {
        return Condition;
    }

    public void setCondition(Condition Condition) {
        this.Condition=Condition;
    }

    public Statement getStatement() {
        return Statement;
    }

    public void setStatement(Statement Statement) {
        this.Statement=Statement;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(WhileEntry!=null) WhileEntry.accept(visitor);
        if(Condition!=null) Condition.accept(visitor);
        if(Statement!=null) Statement.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(WhileEntry!=null) WhileEntry.traverseTopDown(visitor);
        if(Condition!=null) Condition.traverseTopDown(visitor);
        if(Statement!=null) Statement.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(WhileEntry!=null) WhileEntry.traverseBottomUp(visitor);
        if(Condition!=null) Condition.traverseBottomUp(visitor);
        if(Statement!=null) Statement.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("WhileStmt(\n");

        if(WhileEntry!=null)
            buffer.append(WhileEntry.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Condition!=null)
            buffer.append(Condition.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(Statement!=null)
            buffer.append(Statement.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [WhileStmt]");
        return buffer.toString();
    }
}
