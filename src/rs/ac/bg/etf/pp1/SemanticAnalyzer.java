package rs.ac.bg.etf.pp1;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {

	Obj currentMethod = null;
	boolean returnFound = false;
	boolean errorDetected = false;
	int nVars;

	Logger log = Logger.getLogger(getClass());
	
	private Struct boolType = Tab.find("bool").getType();
	private Struct currentType = null;

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.info(msg.toString());
	}

	public void visit(ProgName progName) {
		progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
		Tab.openScope();
	}

	public void visit(Program program) {
		nVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}

	public void visit(VarDeclHead varDecl) {
		report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), varDecl.getType().struct);
		currentType = varDecl.getType().struct;
	}
	
	public void visit(VarDeclChain varDecl) {
		report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), currentType);
	}
	
	public void visit(VarDeclHeadArr varDecl) {	
		report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), varDecl.getType().struct);
		currentType = varDecl.getType().struct;
	}
	
	public void visit(VarDeclChainArr varDecl) {
		report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), currentType);
	}
	
	public void visit(VarDeclHeadMatrix varDecl) {
		report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), varDecl.getType().struct);
		currentType = varDecl.getType().struct;
	}
	
	public void visit(VarDeclChainMatrix varDecl) {
		report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), currentType);
	}
	
	public void visit(VarDeclSemi varDeclSemi) {
		currentType = null;
	}
	
	public void visit(ConstDeclHead constDecl) {
		if (!constDecl.getType().struct.equals(constDecl.getConstant().struct)) {
			report_error("Greska na liniji " + constDecl.getLine() + " : " + "nekompatibilni tipovi pri deklaraciji konstante! ", null);
		} else {
			report_info("Deklarisana konstanta " + constDecl.getConstName(), constDecl);
			Obj constNode = Tab.insert(Obj.Con, constDecl.getConstName(), constDecl.getType().struct);
			Constant myConstant = constDecl.getConstant();
			
			if (myConstant instanceof NumConst) {
				constNode.setAdr(((NumConst)myConstant).getNumber());
			} else if (myConstant instanceof CharConst) {
				constNode.setAdr(((CharConst)myConstant).getCharacter());
			} else if (myConstant instanceof BoolConst) {
				if (((BoolConst)myConstant).getBool()) {
					constNode.setAdr(1);	
				} else {
					constNode.setAdr(0);
				}
			}
			
			currentType = constDecl.getType().struct;
		}
	}
	
	public void visit(ConstDeclChain constDecl) {
		if (!currentType.equals(constDecl.getConstant().struct)) {
			report_error("Greska na liniji " + constDecl.getLine() + " : " + "nekompatibilni tipovi pri deklaraciji konstante! ", null);
		} else {
			report_info("Deklarisana konstanta " + constDecl.getConstName(), constDecl);
			Obj constNode = Tab.insert(Obj.Con, constDecl.getConstName(), currentType);
			Constant myConstant = constDecl.getConstant();
			
			if (myConstant instanceof NumConst) {
				constNode.setAdr(((NumConst)myConstant).getNumber());
			} else if (myConstant instanceof CharConst) {
				constNode.setAdr(((CharConst)myConstant).getCharacter());
			} else if (myConstant instanceof BoolConst) {
				if (((BoolConst)myConstant).getBool()) {
					constNode.setAdr(1);	
				} else {
					constNode.setAdr(0);
				}
			}
			
		}
	}
	
	public void visit(ConstDeclSemi constDeclSemi) {
		currentType = null;
	}
	
	public void visit(ConstDecl constDecl) {
		currentType = null;
	}

	public void visit(Type type) {
		Obj typeNode = Tab.find(type.getTypeName());
		if (typeNode == Tab.noObj) {
			report_error("Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola!", null);
			type.struct = Tab.noType;
		} else {
			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			} else {
				report_error("Greska: Ime " + type.getTypeName() + " ne predstavlja tip!", type);
				type.struct = Tab.noType;
			}
		}
	}
	
	public void visit(MethodTypeVoid methodTypeVoid) {
		methodTypeVoid.struct = Tab.noType;
	}
	
	public void visit(MethodTypeNotVoid methodTypeNotVoid) {
		methodTypeNotVoid.struct = methodTypeNotVoid.getType().struct;
	}
	
	public void visit(MethodTypeName methodTypeName) {
		currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMethodName(), methodTypeName.getMethodType().struct);
		methodTypeName.obj = currentMethod;
		Tab.openScope();
		report_info("Obradjuje se funkcija " + methodTypeName.getMethodName(), methodTypeName);
	}
	
	public void visit(MethodDecl methodDecl) {
		if (returnFound == false && currentMethod.getType() != Tab.noType) {
			report_error("Semanticka greska na liniji " + methodDecl.getLine() + ": funkcija " + currentMethod.getName() + " nema return iskaz!", null);
		}
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();
		
		returnFound = false;
		currentMethod = null;
	}
	
	public void visit(DesignatorIdent designator) {
		Obj obj = Tab.find(designator.getName());
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName() + " nije deklarisano!", designator);
		}
		designator.obj = obj;
	}
	
	public void visit(FuncCall funcCall) {
		Obj func = funcCall.getDesignator().obj;
		if (func.getKind() == Obj.Meth) {
			report_info("Pronadjen poziv funkcije " + func.getName() + " na liniji " + funcCall.getLine(), null);
			funcCall.struct = func.getType();
		} else {
			report_error("Greska na liniji " + funcCall.getLine()+" : ime " + func.getName() + " nije funkcija!", null);
			funcCall.struct = Tab.noType;
		}
	}
	
	public void visit(FactorTerm factorTerm) {
		factorTerm.struct = factorTerm.getFactor().struct;
	}
	
	public void visit(TermExpr termExpr) {
		termExpr.struct = termExpr.getTerm().struct;
	}
	
	public void visit(AddOpTermExpr addOpTermExpr) {
		Struct te = addOpTermExpr.getExpr().struct;
		Struct t = addOpTermExpr.getTerm().struct;
		
		if (te.equals(t) && te == Tab.intType) {
			addOpTermExpr.struct = te;
		} else {
			report_error("Greska na liniji "+ addOpTermExpr.getLine()+" : nekompatibilni tipovi u izrazu za sabiranje.", null);
			addOpTermExpr.struct = Tab.noType;
		}
	}
	
	public void visit(NumConst numConst) {
		numConst.struct = Tab.intType;
	}
	
	public void visit(CharConst charConst) {
		charConst.struct = Tab.charType;
	}
	
	public void visit(BoolConst boolConst) {
		boolConst.struct = boolType;
	}
	
	public void visit(FactorConst factorConst) {
		factorConst.struct = factorConst.getConstant().struct;
	}
	
	public void visit(FactorVar factorVar) {
		factorVar.struct = factorVar.getDesignator().obj.getType();
	}
	
	public void visit(ReturnExpr returnExpr) {
		returnFound = true;
		Struct currMethodType = currentMethod.getType();
		if (!currMethodType.compatibleWith(returnExpr.getExpr().struct)) {
			report_error("Greska na liniji " + returnExpr.getLine() + " : " + "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije " + currentMethod.getName(), null);
		}
	}
	
	public void visit(DesignatorStmtAssign assignment) {
		if (!assignment.getExpr().struct.assignableTo(assignment.getDesignator().obj.getType())) {
    		report_error("Greska na liniji " + assignment.getLine() + " : " + "nekompatibilni tipovi u dodeli vrednosti! ", null);
		}
	}
	
	public boolean passed() {
		return !errorDetected;
	}

}
