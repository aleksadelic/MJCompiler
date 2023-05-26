package rs.ac.bg.etf.pp1;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.AddOp;
import rs.ac.bg.etf.pp1.ast.AddOpMinus;
import rs.ac.bg.etf.pp1.ast.AddOpPlus;
import rs.ac.bg.etf.pp1.ast.AddOpTermExpr;
import rs.ac.bg.etf.pp1.ast.BoolConst;
import rs.ac.bg.etf.pp1.ast.CharConst;
import rs.ac.bg.etf.pp1.ast.ConstrFactorArr;
import rs.ac.bg.etf.pp1.ast.ConstrFactorMatrix;
import rs.ac.bg.etf.pp1.ast.Designator;
import rs.ac.bg.etf.pp1.ast.DesignatorArr;
import rs.ac.bg.etf.pp1.ast.DesignatorIdent;
import rs.ac.bg.etf.pp1.ast.DesignatorMatrix;
import rs.ac.bg.etf.pp1.ast.DesignatorStmtAssign;
import rs.ac.bg.etf.pp1.ast.DesignatorStmtDecr;
import rs.ac.bg.etf.pp1.ast.DesignatorStmtFuncCall;
import rs.ac.bg.etf.pp1.ast.DesignatorStmtIncr;
import rs.ac.bg.etf.pp1.ast.Expr;
import rs.ac.bg.etf.pp1.ast.FactorFuncCall;
import rs.ac.bg.etf.pp1.ast.FactorVar;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodTypeName;
import rs.ac.bg.etf.pp1.ast.MulOp;
import rs.ac.bg.etf.pp1.ast.MulOpDiv;
import rs.ac.bg.etf.pp1.ast.MulOpFactorTerm;
import rs.ac.bg.etf.pp1.ast.MulOpMod;
import rs.ac.bg.etf.pp1.ast.MulOpMul;
import rs.ac.bg.etf.pp1.ast.NumConst;
import rs.ac.bg.etf.pp1.ast.PrintStmt;
import rs.ac.bg.etf.pp1.ast.ProgName;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.ast.ReturnExpr;
import rs.ac.bg.etf.pp1.ast.ReturnNoExpr;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.ast.TermExprMinus;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;

public class CodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(getClass());

	private int mainPc;

	private HashMap<String, Obj> arrMap = null;
	
	public int getMainPc() {
		return mainPc;
	}
	
	public CodeGenerator(HashMap<String, Obj> arrMap) {
		this.arrMap = arrMap;
	}

	public void visit(MethodTypeName methodTypeName) {
		if (methodTypeName.getMethodName().equalsIgnoreCase("main")) {
			mainPc = Code.pc;
		}
		methodTypeName.obj.setAdr(Code.pc);

		// Collect arguments and local variables
		SyntaxNode methodNode = methodTypeName.getParent();

		VarCounter varCounter = new VarCounter();
		methodNode.traverseTopDown(varCounter);

		FormParamCounter formParamCounter = new FormParamCounter();
		methodNode.traverseTopDown(formParamCounter);

		// Generate the entry
		Code.put(Code.enter);
		Code.put(formParamCounter.getCount());
		Code.put(formParamCounter.getCount() + varCounter.getCount());
	}

	public void visit(MethodDecl methodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	public void visit(DesignatorStmtAssign designatorStmtAssign) {
		Designator des = designatorStmtAssign.getDesignator();
		if (des.obj.getKind() == Obj.Var) {
			Code.store(designatorStmtAssign.getDesignator().obj);
		} else {
			Code.put(Code.astore);
		}
	}

	public void visit(DesignatorStmtFuncCall designatorStmtFuncCall) {
		Obj funcObj = designatorStmtFuncCall.getDesignator().obj;
		int offset = funcObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
		if (!designatorStmtFuncCall.getDesignator().obj.getType().equals(Tab.noType)) {
			Code.put(Code.pop);
		}
	}

	public void visit(DesignatorStmtIncr designatorStmtIncr) {
		Obj obj = designatorStmtIncr.getDesignator().obj;
		Code.load(obj);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(obj);
	}

	public void visit(DesignatorStmtDecr designatorStmtDecr) {
		Obj obj = designatorStmtDecr.getDesignator().obj;
		Code.load(obj);
		Code.loadConst(-1);
		Code.put(Code.add);
		Code.store(obj);
	}

	public void visit(ReturnExpr returnExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	public void visit(ReturnNoExpr returnNoExpr) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	public void visit(PrintStmt printStmt) {
		if (printStmt.getExpr().struct.equals(Tab.intType)) {
			Code.loadConst(5);
			Code.put(Code.print);
		} else if (printStmt.getExpr().struct.equals(Tab.charType)) {
			Code.loadConst(1);
			Code.put(Code.bprint);
		}
	}

	public void visit(AddOpTermExpr addOpTermExpr) {
		AddOp op = addOpTermExpr.getAddOp();
		if (op.getClass() == AddOpPlus.class) {
			Code.put(Code.add);
		} else if (op.getClass() == AddOpMinus.class) {
			Code.put(Code.sub);
		}
	}

	public void visit(TermExprMinus termExprMinus) {
		Code.loadConst(-1);
		Code.put(Code.mul);
	}

	public void visit(MulOpFactorTerm mulOpFactorTerm) {
		MulOp op = mulOpFactorTerm.getMulOp();
		if (op.getClass() == MulOpMul.class) {
			Code.put(Code.mul);
		} else if (op.getClass() == MulOpDiv.class) {
			Code.put(Code.div);
		} else if (op.getClass() == MulOpMod.class) {
			Code.put(Code.rem);
		}
	}

	public void visit(FactorFuncCall factorFuncCall) {
		Obj funcObj = factorFuncCall.getDesignator().obj;
		int offset = funcObj.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
	}

	public void visit(FactorVar factorVar) {
		SyntaxNode parent = factorVar.getParent();
		
		if (parent.getClass() != DesignatorStmtAssign.class && parent.getClass() != DesignatorStmtFuncCall.class) {
			if (factorVar.getDesignator().obj.getKind() == Obj.Var) {
				Code.load(factorVar.getDesignator().obj);
			} else {
				Code.put(Code.aload);
			}
		}
	}

	public void visit(NumConst numConst) {
		Obj con = Tab.insert(Obj.Con, "$", numConst.struct);
		con.setLevel(0);
		con.setAdr(numConst.getNumber());

		Code.load(con);
	}

	public void visit(CharConst charConst) {
		Obj con = Tab.insert(Obj.Con, "$", charConst.struct);
		con.setLevel(0);
		con.setAdr(charConst.getCharacter());

		Code.load(con);
	}

	public void visit(BoolConst boolConst) {
		Obj con = Tab.insert(Obj.Con, "$", boolConst.struct);
		con.setLevel(0);
		con.setAdr(boolConst.getBool() ? 1 : 0);

		Code.load(con);
	}

	public void visit(ConstrFactorArr constrFactorArr) {
		Code.put(Code.newarray);
		if (constrFactorArr.getType().struct.equals(Tab.intType)) {
			Code.put(1);
		} else {
			Code.put(0);
		}
	}

	public void visit(ConstrFactorMatrix constrFactorMatrix) {

	}

	public void visit(DesignatorArr designatorArr) {
		Obj obj = arrMap.get(designatorArr.obj.getName());
		Code.load(obj);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
	}

	public void visit(DesignatorMatrix designatorMatrix) {
		
	}

}
