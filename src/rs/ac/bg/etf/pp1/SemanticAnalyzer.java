package rs.ac.bg.etf.pp1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;

public class SemanticAnalyzer extends VisitorAdaptor {

	Obj currentMethod = null;
	boolean returnFound = false;
	boolean mainFound = false;
	boolean errorDetected = false;
	int nVars;

	Logger log = Logger.getLogger(getClass());

	private Struct boolType = null;
	private Struct currentType = null;

	private Map<Obj, List<Struct>> funcPars = null;
	private List<Struct> currFormPars = null;
	private List<Struct> currActPars = null;

	private Map<Integer, String> objKindMap = null;
	private Map<Integer, String> typeKindMap = null;
	
	private Set<String> currFormParsSet = null;

	public SemanticAnalyzer() {
		boolType = Tab.find("bool").getType();

		funcPars = new HashMap<>();
		List<Struct> chrPars = new ArrayList<>();
		List<Struct> ordPars = new ArrayList<>();
		List<Struct> lenPars = new ArrayList<>();
		chrPars.add(Tab.intType);
		ordPars.add(Tab.charType);
		lenPars.add(new Struct(Struct.Array));

		funcPars.put(Tab.chrObj, chrPars);
		funcPars.put(Tab.ordObj, ordPars);
		funcPars.put(Tab.lenObj, lenPars);
		
		currFormParsSet = new HashSet<>();

		objKindMap = new HashMap<>();
		objKindMap.put(0, "Con");
		objKindMap.put(1, "Var");
		objKindMap.put(2, "Type");
		objKindMap.put(3, "Meth");
		objKindMap.put(4, "Fld");
		objKindMap.put(5, "Elem");
		objKindMap.put(6, "Prog");

		typeKindMap = new HashMap<>();
		typeKindMap.put(0, "none");
		typeKindMap.put(1, "int");
		typeKindMap.put(2, "char");
		typeKindMap.put(3, "array");
		typeKindMap.put(4, "class");
		typeKindMap.put(5, "bool");
		typeKindMap.put(6, "enum");
		typeKindMap.put(7, "interface");
	}

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
		if (!mainFound) {
			report_error("Greska: Program mora sadrzati main funkciju!", null);
		}
	}

	public void visit(VarDeclHead varDecl) {
		checkIfNameIsDeclared(varDecl.getVarName(), varDecl);
		report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
		Tab.insert(Obj.Var, varDecl.getVarName(), varDecl.getType().struct);
		currentType = varDecl.getType().struct;
	}

	public void visit(VarDeclChain varDecl) {
		checkIfNameIsDeclared(varDecl.getVarName(), varDecl);
		report_info("Deklarisana promenljiva " + varDecl.getVarName(), varDecl);
		Tab.insert(Obj.Var, varDecl.getVarName(), currentType);
	}

	public void visit(VarDeclHeadArr varDecl) {
		checkIfNameIsDeclared(varDecl.getVarName(), varDecl);
		report_info("Deklarisan niz " + varDecl.getVarName(), varDecl);
		Tab.insert(Obj.Var, varDecl.getVarName(), new Struct(Struct.Array, varDecl.getType().struct));
		currentType = varDecl.getType().struct;
	}

	public void visit(VarDeclChainArr varDecl) {
		checkIfNameIsDeclared(varDecl.getVarName(), varDecl);
		report_info("Deklarisan niz " + varDecl.getVarName(), varDecl);
		Tab.insert(Obj.Var, varDecl.getVarName(), new Struct(Struct.Array, currentType));
	}

	public void visit(VarDeclHeadMatrix varDecl) {
		checkIfNameIsDeclared(varDecl.getVarName(), varDecl);
		report_info("Deklarisana matrica " + varDecl.getVarName(), varDecl);
		Tab.insert(Obj.Var, varDecl.getVarName(),
				new Struct(Struct.Array, new Struct(Struct.Array, varDecl.getType().struct)));
		currentType = varDecl.getType().struct;
	}

	public void visit(VarDeclChainMatrix varDecl) {
		checkIfNameIsDeclared(varDecl.getVarName(), varDecl);
		report_info("Deklarisana matrica " + varDecl.getVarName(), varDecl);
		Tab.insert(Obj.Var, varDecl.getVarName(),
				new Struct(Struct.Array, new Struct(Struct.Array, currentType)));
	}

	public void visit(VarDeclSemi varDeclSemi) {
		currentType = null;
	}

	private void checkIfNameIsDeclared(String name, SyntaxNode node) {
		if (Tab.find(name) != Tab.noObj) {
			report_error("Greska na liniji " + node.getLine() + " : " + "ime " + name + " je vec deklarisano! ", null);
		}
	}

	public void visit(ConstDeclHead constDecl) {
		checkIfNameIsDeclared(constDecl.getConstName(), constDecl);
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
		checkIfNameIsDeclared(constDecl.getConstName(), constDecl);
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
		checkIfNameIsDeclared(methodTypeName.getMethodName(), methodTypeName);
		currentMethod = Tab.insert(Obj.Meth, methodTypeName.getMethodName(), methodTypeName.getMethodType().struct);
		methodTypeName.obj = currentMethod;
		Tab.openScope();
		currFormPars = new ArrayList<>();
		report_info("Obradjuje se funkcija " + methodTypeName.getMethodName(), methodTypeName);
		
		if (methodTypeName.getMethodName().equals("main")) {
			mainFound = true;
			if (!methodTypeName.getMethodType().struct.equals(Tab.noType))
				report_error("Greska na liniji " + methodTypeName.getLine() + ": main funkcija mora biti void tipa!", null);
		}
	}

	public void visit(MethodDecl methodDecl) {
		if (returnFound == false && currentMethod.getType() != Tab.noType) {
			report_error("Greska na liniji " + methodDecl.getLine() + ": funkcija " + currentMethod.getName()
					+ " nema return iskaz!", null);
		}
		Tab.chainLocalSymbols(currentMethod);
		Tab.closeScope();

		returnFound = false;
		currentMethod = null;
		currFormPars = null;
		currFormParsSet.clear();
	}

	public void visit(FormParamsList formParamsList) {
		StringBuilder sb = new StringBuilder();
		sb.append("Pronadjeni formalni parametri funkcije " + currentMethod.getName() + " tipa: ");
		funcPars.put(currentMethod, currFormPars);
		boolean isFirst = true;
		for (Struct type : currFormPars) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(", ");
			}
			if (type.getKind() == Struct.Array) {
				Struct elemType = type.getElemType();
				if (elemType.getKind() == Struct.Array) {
					sb.append("Matrix of " + typeKindMap.get(elemType.getElemType().getKind()));
				} else {
					sb.append("Array of " + typeKindMap.get(elemType.getKind()));
				}
			} else {
				sb.append(typeKindMap.get(type.getKind()));
			}
		}
		report_info(sb.toString(), formParamsList);
		
		if (currentMethod.getName().equals("main") && currFormPars.size() != 0) {
			report_error("Greska na liniji " + formParamsList.getLine() + ": main funkcija ne sme imati argumente!", null);
		}
	}

	public void visit(FormParsChainMatrix formParsChainMatrix) {
		Struct type = new Struct(Struct.Array, new Struct(Struct.Array, formParsChainMatrix.getType().struct));
		Tab.insert(Obj.Var, formParsChainMatrix.getParName(), type);
		currFormPars.add(type);
		currFormParsSet.add(formParsChainMatrix.getParName());
	}

	public void visit(FormParsChainArr formParsChainArr) {
		Struct type = new Struct(Struct.Array, formParsChainArr.getType().struct);
		Tab.insert(Obj.Var, formParsChainArr.getParName(), type);
		currFormPars.add(type);
		currFormParsSet.add(formParsChainArr.getParName());
	}

	public void visit(FormParsChain formParsChain) {
		Struct type = formParsChain.getType().struct;
		Tab.insert(Obj.Var, formParsChain.getParName(), type);
		currFormPars.add(type);
		currFormParsSet.add(formParsChain.getParName());
	}

	public void visit(FormParsHeadMatrix formParsHeadMatrix) {
		Struct type = new Struct(Struct.Array, new Struct(Struct.Array, formParsHeadMatrix.getType().struct));
		Tab.insert(Obj.Var, formParsHeadMatrix.getParName(), type);
		currFormPars.add(type);
		currFormParsSet.add(formParsHeadMatrix.getParName());
	}

	public void visit(FormParsHeadArr formParsHeadArr) {
		Struct type = new Struct(Struct.Array, formParsHeadArr.getType().struct);
		Tab.insert(Obj.Var, formParsHeadArr.getParName(), type);
		currFormPars.add(type);
		currFormParsSet.add(formParsHeadArr.getParName());
	}

	public void visit(FormParsHead formParsHead) {
		Struct type = formParsHead.getType().struct;
		Tab.insert(Obj.Var, formParsHead.getParName(), type);
		currFormPars.add(type);
		currFormParsSet.add(formParsHead.getParName());
	}

	public void visit(MultActPars multActPars) {
		currActPars.add(multActPars.getExpr().struct);
	}

	public void visit(SingleActPar singleActPar) {
		currActPars = new ArrayList<>();
		currActPars.add(singleActPar.getExpr().struct);
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

	public void visit(DesignatorStmtFuncCall funcCall) {
		Obj func = funcCall.getDesignator().obj;
		if (func.getKind() == Obj.Meth) {
			report_info("\tDetektovan poziv funkcije " + func.getName() + " na liniji " + funcCall.getLine(), null);

			checkActualParams(func, funcCall);
		} else {
			report_error("Greska na liniji " + funcCall.getLine() + " : ime " + func.getName() + " nije funkcija!",
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

	public void checkDesignatorKind(Obj designator, SyntaxNode node) {
		if (designator.getKind() != Obj.Var) {
			report_error("Greska na liniji " + node.getLine() + " : "
					+ "designator mora biti promenljiva ili element niza ili matrice! ", null);
		}
	}

	public void checkDesignatorType(Obj designator, SyntaxNode node) {
		if (!designator.getType().equals(Tab.intType)) {
			report_error("Greska na liniji " + node.getLine() + " : " + "designator mora biti celobrojnog tipa! ",
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
		Obj designator = readStmt.getDesignator().obj;
		checkDesignatorKind(designator, readStmt);
		int kind = designator.getType().getKind();
		if (kind != Struct.Int && kind != Struct.Char && kind != Struct.Bool) {
			report_error(
					"Greska na liniji " + readStmt.getLine() + " : " + "designator mora biti tipa int, char ili bool! ",
					null);
		}
	}

	public void visit(PrintStmt printStmt) {
		int kind = printStmt.getExpr().struct.getKind();

		if (kind != Struct.Int && kind != Struct.Char && kind != Struct.Bool) {
			report_error("Greska na liniji " + printStmt.getLine() + " : " + "expr mora biti tipa int, char ili bool! ",
					null);
		}
	}

	public void visit(PrintStmtNum printStmt) {
		int kind = printStmt.getExpr().struct.getKind();

		if (kind != Struct.Int && kind != Struct.Char && kind != Struct.Bool) {
			report_error("Greska na liniji " + printStmt.getLine() + " : " + "expr mora biti tipa int, char ili bool! ",
					null);
		}
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

	public void visit(MapStmt mapStmt) {
		report_info("Prepoznat poziv map funkcije", mapStmt);
		Struct desType = mapStmt.getDesignator().obj.getType();
		Struct srcType = mapStmt.getDesignator1().obj.getType();
		if (desType.getKind() != Struct.Array || desType.getElemType().getKind() == Struct.Array
				|| srcType.getKind() != Struct.Array || srcType.getElemType().getKind() == Struct.Array) {
			report_error(
					"Greska na liniji " + mapStmt.getLine() + " : " + "designator mora biti jednodimenzionalni niz! ",
					null);
		} else {
			String ident = mapStmt.getVarName();
			Obj var = Tab.find(ident);

			if (var.getKind() != Obj.Var) {
				report_error("Greska na liniji " + mapStmt.getLine() + " : "
						+ "ident mora biti lokalna ili globalna promeljiva! ", null);
			}

			if (var.getType().getKind() != srcType.getElemType().getKind()) {
				report_error("Greska na liniji " + mapStmt.getLine() + " : "
						+ "ident mora biti istog tipa kao i element niza! ", null);
			}
		}
	}

	private void checkConditionType(Condition condition) {
		if (!condition.struct.equals(boolType)) {
			report_error("Greska na liniji " + condition.getLine() + " : " + "uslov mora biti boolean tipa! ", null);
		}
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
		} else {
			if (f1.getKind() == Struct.Array && !(multCondFact.getRelOp() instanceof RelOpEq) && !(multCondFact.getRelOp() instanceof RelOpNeq)) {
				report_error("Greska na liniji " + multCondFact.getLine() + " : uz promenljive tipa niza mogu se koristiti samo == i != !",
						null);
			}
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
		termExprMinus.struct = termExprMinus.getTerm().struct;
		if (!termExprMinus.struct.equals(Tab.intType)) {
			report_error("Greska na liniji " + termExprMinus.getLine() + " : " + "izraz mora biti celobrojan! ", null);
		}
	}

	public void visit(AddOpTermExpr addOpTermExpr) {
		Struct te = addOpTermExpr.getExpr().struct;
		Struct t = addOpTermExpr.getTerm().struct;

		if (te.equals(t) && te.equals(Tab.intType)) {
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

	public void visit(FactorFuncCall funcCall) {
		Obj func = funcCall.getDesignator().obj;
		if (func.getKind() == Obj.Meth) {
			report_info("\tDetektovan poziv funkcije " + func.getName() + " na liniji " + funcCall.getLine(), null);
			funcCall.struct = func.getType();

			checkActualParams(func, funcCall);
		} else {
			report_error("Greska na liniji " + funcCall.getLine() + " : ime " + func.getName() + " nije funkcija!",
					null);
			funcCall.struct = Tab.noType;
		}
	}

	private void checkActualParams(Obj func, SyntaxNode funcCall) {
		List<Struct> formPars = funcPars.get(func);
		if (formPars == null) {
			if (currActPars != null && currActPars.size() != 0) {
				report_error("Greska na liniji " + funcCall.getLine()
						+ " : broj stvarnih parametara nije jednak broju formalnih parametara!", null);
			}
			currActPars = null;
			return;
		}

		if (formPars.size() != currActPars.size()) {
			report_error("Greska na liniji " + funcCall.getLine()
					+ " : broj stvarnih parametara nije jednak broju formalnih parametara!", null);
		}

		for (int i = 0; i < currActPars.size(); i++) {
			Struct act = currActPars.get(i);
			Struct form = formPars.get(i);

			if (!act.assignableTo(form)) {
				report_error("Greska na liniji " + funcCall.getLine()
						+ " : tip stvarnog parametra ne odgovara tipu formalnog parametra!", null);
			}
		}
		currActPars = null;
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
			designatorMatrix.obj = new Obj(Obj.Var, designatorMatrix.obj.getName(),
					designatorMatrix.obj.getType().getElemType());
			if (designatorMatrix.obj.getType().getKind() == Struct.Array) {
				designatorMatrix.obj = new Obj(Obj.Var, designatorMatrix.obj.getName(),
						designatorMatrix.obj.getType().getElemType());
			}

			reportMatrixElemAcces(obj, designatorMatrix);
			reportDetectedSymbol(obj, designatorMatrix);
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
			designatorArr.obj = new Obj(Obj.Var, designatorArr.obj.getName(),
					designatorArr.obj.getType().getElemType());

			reportArrayElemAcces(obj, designatorArr);
			reportDetectedSymbol(obj, designatorArr);
		}
	}

	public void visit(DesignatorIdent designator) {
		Obj obj = Tab.find(designator.getName());
		designator.obj = obj;
		if (obj == Tab.noObj) {
			report_error("Greska na liniji " + designator.getLine() + " : ime " + designator.getName()
					+ " nije deklarisano!", designator);
		} else {
			reportDetectedSymbol(obj, designator);
		}

	}

	private void reportDetectedSymbol(Obj obj, SyntaxNode node) {
		StringBuilder sb = new StringBuilder();

		sb.append("Pretraga na " + node.getLine());
		sb.append("(" + obj.getName() + "), nadjeno ");
		sb.append(objKindMap.get(obj.getKind()) + " " + obj.getName() + ": ");

		Struct type = obj.getType();
		if (type.getKind() == Struct.Array) {
			Struct elemType = type.getElemType();
			if (elemType.getKind() == Struct.Array) {
				sb.append("Matrix of " + typeKindMap.get(elemType.getElemType().getKind()));
			} else {
				sb.append("Array of " + typeKindMap.get(elemType.getKind()));
			}
		} else {
			sb.append(typeKindMap.get(type.getKind()));
		}
		sb.append(", " + obj.getAdr() + ", " + obj.getLevel());

		report_info(sb.toString(), null);
		
		if (obj.getKind() == Obj.Var) {
			if (obj.getLevel() == 0) {
				report_info("\tDetektovana globalna promenljiva " + obj.getName(), node);
			} else if (currFormParsSet.contains(obj.getName())) {
				report_info("\tDetektovana formalni parametar " + obj.getName(), node);
			} else {
				report_info("\tDetektovana lokalna promenljiva " + obj.getName(), node);
			}
		} else if (obj.getKind() == Obj.Con) {
			report_info("\tDetektovana simbolicka konstanta" + obj.getName(), node);
		}
	}

	private void reportArrayElemAcces(Obj obj, SyntaxNode node) {
		report_info("\tDetektovan pristup elementu niza " + obj.getName(), node);
	}

	private void reportMatrixElemAcces(Obj obj, SyntaxNode node) {
		report_info("\tDetektovan pristup elementu matrice " + obj.getName(), node);
	}

	public boolean passed() {
		return !errorDetected;
	}

}
