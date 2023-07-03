// generated with ast extension for cup
// version 0.8
// 25/5/2023 21:12:28


package rs.ac.bg.etf.pp1.ast;

public class DeclListConst extends DeclList {

    private DeclList DeclList;
    private ConstDeclSemi ConstDeclSemi;

    public DeclListConst (DeclList DeclList, ConstDeclSemi ConstDeclSemi) {
        this.DeclList=DeclList;
        if(DeclList!=null) DeclList.setParent(this);
        this.ConstDeclSemi=ConstDeclSemi;
        if(ConstDeclSemi!=null) ConstDeclSemi.setParent(this);
    }

    public DeclList getDeclList() {
        return DeclList;
    }

    public void setDeclList(DeclList DeclList) {
        this.DeclList=DeclList;
    }

    public ConstDeclSemi getConstDeclSemi() {
        return ConstDeclSemi;
    }

    public void setConstDeclSemi(ConstDeclSemi ConstDeclSemi) {
        this.ConstDeclSemi=ConstDeclSemi;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(DeclList!=null) DeclList.accept(visitor);
        if(ConstDeclSemi!=null) ConstDeclSemi.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(DeclList!=null) DeclList.traverseTopDown(visitor);
        if(ConstDeclSemi!=null) ConstDeclSemi.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(DeclList!=null) DeclList.traverseBottomUp(visitor);
        if(ConstDeclSemi!=null) ConstDeclSemi.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("DeclListConst(\n");

        if(DeclList!=null)
            buffer.append(DeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(ConstDeclSemi!=null)
            buffer.append(ConstDeclSemi.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [DeclListConst]");
        return buffer.toString();
    }
}
