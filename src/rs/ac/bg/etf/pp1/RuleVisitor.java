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

	public void visit(VarDeclSemi varDecl) {
		varDeclCount++;
	}

	public void visit(ConstDeclSemi constDecl) {
		constDeclCount++;
	}

	public void visit(VarDeclHead varDecl) {
		varCount++;
		log.info(varDecl.getLine() + " Prepoznata deklaracija promenljive " + varDecl.getVarName());
	}

	public void visit(VarDeclHeadArr varDecl) {
		varCount++;
		log.info(varDecl.getLine() + " Prepoznata deklaracija niza " + varDecl.getVarName());
	}

	public void visit(VarDeclHeadMatrix varDecl) {
		varCount++;
		log.info(varDecl.getLine() + " Prepoznata deklaracija matrice " + varDecl.getVarName());
	}

	public void visit(VarDeclChain varDecl) {
		varCount++;
		log.info(varDecl.getLine() + " Prepoznata deklaracija promenljive " + varDecl.getVarName());
	}

	public void visit(VarDeclChainArr varDecl) {
		varCount++;
		log.info(varDecl.getLine() + " Prepoznata deklaracija niza " + varDecl.getVarName());
	}

	public void visit(VarDeclChainMatrix varDecl) {
		varCount++;
		log.info(varDecl.getLine() + " Prepoznata deklaracija matrice " + varDecl.getVarName());
	}

	public void visit(ConstDeclHead varDecl) {
		constCount++;
	}

	public void visit(ConstDeclChain varDecl) {
		constCount++;
	}

	public void visit(ClassDecl classDecl) {
		classDeclCount++;
		log.info(classDecl.getLine() + " Prepoznata deklaracija klase");
	}

	public void visit(FormParsHead formPar) {
		log.info(formPar.getLine() + " Prepoznat formalni parametar " + formPar.getParName());
	}

	public void visit(FormParsChain formPar) {
		log.info(formPar.getLine() + " Prepoznat formalni parametar " + formPar.getParName());
	}

	public void visit(FormParsHeadArr formPar) {
		log.info(formPar.getLine() + " Prepoznat formalni parametar niz");
	}

	public void visit(FormParsChainArr formPar) {
		log.info(formPar.getLine() + " Prepoznat formalni parametar niz");
	}

	public void visit(FormParsHeadMatrix formPar) {
		log.info(formPar.getLine() + " Prepoznat formalni parametar matrica");
	}

	public void visit(FormParsChainMatrix formPar) {
		log.info(formPar.getLine() + " Prepoznat formalni parametar matrica");
	}

	public void visit(MatchedIfElse matchedIfElse) {
		log.info(matchedIfElse.getLine() + " Prepoznat MatchedIfElse");
	}

	public void visit(WhileStmt whileStmt) {
		log.info(whileStmt.getLine() + " Prepoznat while");
	}

	public void visit(BreakStmt breakStmt) {
		log.info(breakStmt.getLine() + " Prepoznat break");
	}

	public void visit(ContinueStmt continueStmt) {
		log.info(continueStmt.getLine() + " Prepoznat continue");
	}

	public void visit(SingleCondition condition) {
		log.info(condition.getLine() + " Prepoznat condition");
	}

	public void visit(ReturnExpr returnExpr) {
		log.info(returnExpr.getLine() + " Prepoznat returnExpr");
	}

	public void visit(DesignatorStmt designatorStmt) {
		log.info(designatorStmt.getLine() + " Prepoznat designatorStmt");
	}

	public void visit(ConstrFactorMatrix factor) {
		log.info(factor.getLine() + " Prepoznata alokacija matrice");
	}

	public void visit(ConstrFactorArr factor) {
		log.info(factor.getLine() + " Prepoznata alokacija niza");
	}

	public void visit(PrintStmt stmt) {
		log.info(stmt.getLine() + " Prepoznata print naredba");
	}

	public void visit(MapStmt stmt) {
		log.info(stmt.getLine() + " Prepoznata map funkcija");
	}
}
