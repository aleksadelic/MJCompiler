package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;
import rs.ac.bg.etf.pp1.ast.*;

public class RuleVisitor extends VisitorAdaptor {

	int varDeclCount = 0;
	int constDeclCount = 0;
	int classDeclCount = 0;
	
	int varCount = 0;
	int constCount = 0;
	
	Logger log = Logger.getLogger(getClass());

	public void visit(VarDeclSemi varDecl){
		varDeclCount++;
	}
	
    public void visit(ConstDeclSemi constDecl) {
		constDeclCount++;
	}
    
    public void visit(VarDeclHead varDecl) {
    	varCount++;
    }
    
    public void visit(VarDeclHeadArr varDecl) {
    	varCount++;
    	log.info("Prepoznata deklaracija niza");
    }
    
    public void visit(VarDeclHeadMatrix varDecl) {
    	varCount++;
    	log.info("Prepoznata deklaracija matrice");
    }
    
    public void visit(VarDeclChain varDecl) {
    	varCount++;
    }
    
    public void visit(VarDeclChainArr varDecl) {
    	varCount++;
    	log.info("Prepoznata deklaracija niza");
    }
    
    public void visit(VarDeclChainMatrix varDecl) {
    	varCount++;
    	log.info("Prepoznata deklaracija matrice");
    }
    
    public void visit(ConstDeclHead varDecl) {
    	constCount++;
    }
    
    public void visit(ConstDeclChain varDecl) {
    	constCount++;
    }
    
    public void visit(ClassDecl classDecl) {
    	classDeclCount++;
    	log.info("Prepoznata deklaracija klase");
    }

}
