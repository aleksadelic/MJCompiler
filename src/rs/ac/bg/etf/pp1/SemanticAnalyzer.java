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
		report_info("Deklarisan niz " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), new Struct(Struct.Array, varDecl.getType().struct));
		currentType = varDecl.getType().struct;
	}

	public void visit(VarDeclChainArr varDecl) {
		report_info("Deklarisan niz " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(), new Struct(Struct.Array, currentType));
	}

	public void visit(VarDeclHeadMatrix varDecl) {
		report_info("Deklarisana matrica " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(),
				new Struct(Struct.Array, new Struct(Struct.Array, varDecl.getType().struct)));
		currentType = varDecl.getType().struct;
	}

	public void visit(VarDeclChainMatrix varDecl) {
		report_info("Deklarisana matrica " + varDecl.getVarName(), varDecl);
		Obj varNode = Tab.insert(Obj.Var, varDecl.getVarName(),
				new Struct(Struct.Array, new Struct(Struct.Array, currentType)));
	}

	public void visit(VarDeclSemi varDeclSemi) {
		currentType = null;
	}

	public void visit(ConstDeclHead constDecl) {
		if (!constDecl.getType().struct.equals(constDecl.getConstant().struct)) {
			report_error("Greska na liniji " + constDecl.getLine() + " : "
					+ "nekompatibilni tipovi pri deklaraciji konstante! ", null);
		} else {
			report_info("Deklarisana konstanta " + constDecl.getConstName(), constDecl);
			Obj constNode = Tab.insert(Obj.Con, constDecl.getConstName(), constDecl.getType().struct);
			setConstValue(constDecl, constNode);
			currentType = constDecl.getType().struct;
		}
	}

	public void visit(ConstDeclChain constDecl) {
		if (!currentType.equals(constDecl.getConstant().struct)) {
			report_error("Greska na liniji " + constDecl.getLine() + " : "
					+ "nekompatibilni tipovi pri deklaraciji konstante! ", null);
		} else {
			report_info("Deklarisana konstanta " + constDecl.getConstName(), constDecl);
			Obj constNode = Tab.insert(Obj.Con, constDecl.getConstName(), currentType);
			setConstValue(constDecl, constNode);
		}
	}

	private void setConstValue(ConstDecl constDecl, Obj constNode) {
		Constant myConstant = null;
		if (constDecl instanceof ConstDeclHead)
			myConstant = ((ConstDeclHead) constDecl).getConstant();
		else if (constDecl instanceof ConstDeclChain)
			myConstant = ((ConstDeclChain) constDecl).getConstant();

		if (myConstant instanceof NumConst) {
			constNode.setAdr(((NumConst) myConstant).getNumber());
		} else if (myConstant instanceof CharConst) {
			constNode.setAdr(((CharConst) myConstant).getCharacter());
		} else if (myConstant instanceof BoolConst) {
			if (((BoolConst) myConstant).getBool()) {
				constNode.setAdr(1);
			} else {
				constNode.setAdr(0);
			}
		}
	}

	public void visit(ConstDeclSemi constDeclSemi) {
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
			report_error("Semanticka greska na liniji " + methodDecl.getLine() + ": funkcija " + currentMethod.getName()
					+ " nema return iskaz!", null);
		}
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();

		returnFound = false;
		currentMethod = null;
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

	public void visit(DesignatorStmtAssign assignment) {
		Obj designator = assignment.getDesignator().obj;
		checkDesignatorKind(designator, assignment);
		if (!assignment.getExpr().struct.assignableTo(assignment.getDesignator().obj.getType())) {
			report_error(
					"Greska na liniji " + assignment.getLine() + " : " + "nekompatibilni tipovi u dodeli vrednosti! ",
					null);
		}
	}

	public void visit(DesignatorStmtIncr designatorStmtIncr) {
		Obj designator = designatorStmtIncr.getDesignator().obj;
		checkDesignatorType(designator, designatorStmtIncr);
		checkDesignatorKind(designator, designatorStmtIncr);
	}

	public void visit(DesignatorStmtDecr designatorStmtDecr) {
		Obj designator = designatorStmtDecr.getDesignator().obj;
		checkDesignatorType(designator, designatorStmtDecr);
		checkDesignatorKind(designator, designatorStmtDecr);
	}

	public void checkDesignatorKind(Obj designator, DesignatorStatement designatorStmt) {
		if (designator.getKind() != Obj.Var) {
			report_error("Greska na liniji " + designatorStmt.getLine() + " : "
					+ "designator mora biti promenljiva ili element niza ili matrice! ", null);
		}
	}

	public void checkDesignatorType(Obj designator, DesignatorStatement designatorStmt) {
		if (designator.getType().getKind() != Struct.Int) {
			report_error(
					"Greska na liniji " + designatorStmt.getLine() + " : " + "designator mora biti celobrojnog tipa! ",
					null);
		}
	}

	public void visit(BreakStmt breakStmt) {
		checkIfStmtIsInWhile(breakStmt);
	}

	public void visit(ContinueStmt continueStmt) {
		checkIfStmtIsInWhile(continueStmt);
	}

	private void checkIfStmtIsInWhile(Matched statement) {
		SyntaxNode parent = statement.getParent();
		while (parent != null) {
			if (parent instanceof WhileStmt)
				return;
			parent = parent.getParent();
		}
		report_error("Greska na liniji " + statement.getLine() + " : "
				+ " break i continue naredbe se mogu naci samo u okviru while petlje! ", null);
	}

	public void visit(ReadStmt readStmt) {

	}

	public void visit(PrintStmt printStmt) {

	}

	public void visit(PrintStmtNum printStmt) {

	}

	public void visit(ReturnExpr returnExpr) {
		returnFound = true;
		if (currentMethod == null) {
			report_error("Greska na liniji " + returnExpr.getLine() + " : "
					+ "return naredba moze biti pozvana samo u telu funkcije!", null);
			return;
		}
		Struct currMethodType = currentMethod.getType();
		if (!currMethodType.compatibleWith(returnExpr.getExpr().struct)) {
			report_error("Greska na liniji " + returnExpr.getLine() + " : "
					+ "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije "
					+ currentMethod.getName(), null);
		}
	}

	public void visit(ReturnNoExpr returnNoExpr) {
		returnFound = true;
		if (currentMethod == null) {
			report_error("Greska na liniji " + returnNoExpr.getLine() + " : "
					+ "return naredba moze biti pozvana samo u telu funkcije!", null);
			return;
		}
		if (!currentMethod.getType().equals(Tab.noType)) {
			report_error("Greska na liniji " + returnNoExpr.getLine() + " : "
					+ "tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije "
					+ currentMethod.getName(), null);
		}
	}

	public void visit(UnmatchedIf unmatchedIf) {
		checkConditionType(unmatchedIf.getCondition());
	}

	public void visit(UnmatchedIfElse unmatchedIfElse) {
		checkConditionType(unmatchedIfElse.getCondition());
	}

	public void visit(MatchedIfElse matchedIfElse) {
		checkConditionType(matchedIfElse.getCondition());
	}

	public void visit(WhileStmt whileStmt) {
		checkConditionType(whileStmt.getCondition());
	}

	private void checkConditionType(Condition condition) {
		if (!condition.struct.equals(boolType)) {
			report_error("Greska na liniji " + condition.getLine() + " : " + "uslov mora biti boolean tipa! ", null);
		}
	}

	public void visit(ActualParams actualParams) {

	}

	public void visit(NoActualParams noActualParams) {

	}

	public void visit(MultCondition multCondition) {
		multCondition.struct = multCondition.getCondition().struct;
	}

	public void visit(SingleCondition singleCondition) {
		singleCondition.struct = singleCondition.getCondTerm().struct;
	}

	public void visit(MultCondTerm multCondTerm) {
		multCondTerm.struct = multCondTerm.getCondTerm().struct;
	}

	public void visit(SingleCondTerm singleCondTerm) {
		singleCondTerm.struct = singleCondTerm.getCondFact().struct;
	}

	public void visit(MultCondFact multCondFact) {
		Struct f1 = multCondFact.getExpr().struct;
		Struct f2 = multCondFact.getExpr().struct;

		if (!f1.compatibleWith(f2)) {
			report_error("Greska na liniji " + multCondFact.getLine() + " : nekompatibilni tipovi u logickom izrazu!",
					null);
		}
		multCondFact.struct = boolType;
	}

	public void visit(SingleCondFact singleCondFact) {
		singleCondFact.struct = boolType;
	}

	public void visit(TermExpr termExpr) {
		termExpr.struct = termExpr.getTerm().struct;
	}

	public void visit(TermExprMinus termExprMinus) {
		if (!termExprMinus.struct.equals(Tab.intType)) {
			report_error("Greska na liniji " + termExprMinus.getLine() + " : " + "izraz mora biti celobrojan! ", null);
		}
	}

	public void visit(AddOpTermExpr addOpTermExpr) {
		Struct te = addOpTermExpr.getExpr().struct;
		Struct t = addOpTermExpr.getTerm().struct;

		if (te.equals(t) && te == Tab.intType) {
			addOpTermExpr.struct = te;
		} else {
			report_error(
					"Greska na liniji " + addOpTermExpr.getLine() + " : nekompatibilni tipovi u izrazu za sabiranje.",
					null);
			addOpTermExpr.struct = Tab.noType;
		}
	}

	public void visit(FactorTerm factorTerm) {
		factorTerm.struct = factorTerm.getFactor().struct;
	}

	public void visit(MulOpFactorTerm mulOpFactorTerm) {
		Struct t1 = mulOpFactorTerm.getTerm().struct;
		Struct t2 = mulOpFactorTerm.getFactor().struct;

		if (!t1.equals(Tab.intType) || !t2.equals(Tab.intType)) {
			report_error("Greska na liniji " + mulOpFactorTerm.getLine() + " : cinioci moraju biti celobrojnog tipa!",
					null);
		}
		mulOpFactorTerm.struct = Tab.intType;
	}

	public void visit(FactorVar factorVar) {
		factorVar.struct = factorVar.getDesignator().obj.getType();
	}

	public void visit(FactorConst factorConst) {
		factorConst.struct = factorConst.getConstant().struct;
	}

	public void visit(FuncCall funcCall) {
		Obj func = funcCall.getDesignator().obj;
		if (func.getKind() == Obj.Meth) {
			report_info("Pronadjen poziv funkcije " + func.getName() + " na liniji " + funcCall.getLine(), null);
			funcCall.struct = func.getType();
		} else {
			report_error("Greska na liniji " + funcCall.getLine() + " : ime " + func.getName() + " nije funkcija!",
					null);
			funcCall.struct = Tab.noType;
		}
	}

	public void visit(ConstrFactorArr constrFactorArr) {
		if (!constrFactorArr.getExpr().struct.equals(Tab.intType)) {
			report_error(
					"Greska na liniji " + constrFactorArr.getLine() + " : " + "velicina niza mora biti celobrojna! ",
					null);
		}
		constrFactorArr.struct = new Struct(Struct.Array, constrFactorArr.getType().struct);
	}

	public void visit(ConstrFactorMatrix constrFactorMatrix) {
		if (!constrFactorMatrix.getExpr().struct.equals(Tab.intType)
				|| !constrFactorMatrix.getExpr1().struct.equals(Tab.intType)) {
			report_error(
					"Greska na liniji " + constrFactorMatrix.getLine() + " : " + "velicina niza mora biti celobrojna! ",
					null);
		}
		constrFactorMatrix.struct = new Struct(Struct.Array,
				new Struct(Struct.Array, constrFactorMatrix.getType().struct));
	}

	public void visit(FactorExpr factorExpr) {
		factorExpr.struct = factorExpr.getExpr().struct;
	}

	public void visit(DesignatorMatrix designatorMatrix) {
		Obj obj = Tab.find(designatorMatrix.getName());
		designatorMatrix.obj = obj;
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designatorMatrix.getLine() + " : ime " + designatorMatrix.getName()
					+ " nije deklarisano!", designatorMatrix);
		} else {
			if (designatorMatrix.obj.getType().getKind() != Struct.Array) {
				report_error("Greska na liniji " + designatorMatrix.getLine() + " : " + "designator mora biti niz! ",
						null);
			}
			if (!designatorMatrix.getExpr().struct.equals(Tab.intType)
					|| !designatorMatrix.getExpr1().struct.equals(Tab.intType)) {
				report_error("Greska na liniji " + designatorMatrix.getLine() + " : "
						+ "indeks niza mora biti celobrojnog tipa! ", null);
			}
			designatorMatrix.obj = new Obj(Obj.Var, designatorMatrix.obj.getName(), designatorMatrix.obj.getType().getElemType());
			if (designatorMatrix.obj.getType().getKind() == Struct.Array) {
				designatorMatrix.obj = new Obj(Obj.Var, designatorMatrix.obj.getName(), designatorMatrix.obj.getType().getElemType());
			}
		}
	}

	public void visit(DesignatorArr designatorArr) {
		Obj obj = Tab.find(designatorArr.getName());
		designatorArr.obj = obj;
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designatorArr.getLine() + " : ime " + designatorArr.getName()
					+ " nije deklarisano!", designatorArr);
		} else {
			if (designatorArr.obj.getType().getKind() != Struct.Array) {
				report_error("Greska na liniji " + designatorArr.getLine() + " : " + "designator mora biti niz! ",
						null);
			}
			if (!designatorArr.getExpr().struct.equals(Tab.intType)) {
				report_error("Greska na liniji " + designatorArr.getLine() + " : "
						+ "indeks niza mora biti celobrojnog tipa! ", null);
			}
			designatorArr.obj = new Obj(Obj.Var, designatorArr.obj.getName(), designatorArr.obj.getType().getElemType());
		}
	}

	public void visit(DesignatorIdent designator) {
		Obj obj = Tab.find(designator.getName());
		designator.obj = obj;
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName()
					+ " nije deklarisano!", designator);
		}
	}

	public boolean passed() {
		return !errorDetected;
	}

}
