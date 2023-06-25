package rs.ac.bg.etf.pp1;

import java.util.HashMap;
import java.util.Stack;

import org.apache.log4j.Logger;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.AddOp;
import rs.ac.bg.etf.pp1.ast.AddOpMinus;
import rs.ac.bg.etf.pp1.ast.AddOpPlus;
import rs.ac.bg.etf.pp1.ast.AddOpTermExpr;
import rs.ac.bg.etf.pp1.ast.BoolConst;
import rs.ac.bg.etf.pp1.ast.BreakStmt;
import rs.ac.bg.etf.pp1.ast.CharConst;
import rs.ac.bg.etf.pp1.ast.ConstrFactorArr;
import rs.ac.bg.etf.pp1.ast.ConstrFactorMatrix;
import rs.ac.bg.etf.pp1.ast.ContinueStmt;
import rs.ac.bg.etf.pp1.ast.Designator;
import rs.ac.bg.etf.pp1.ast.DesignatorArr;
import rs.ac.bg.etf.pp1.ast.DesignatorIdent;
import rs.ac.bg.etf.pp1.ast.DesignatorMatrix;
import rs.ac.bg.etf.pp1.ast.DesignatorStmtAssign;
import rs.ac.bg.etf.pp1.ast.DesignatorStmtDecr;
import rs.ac.bg.etf.pp1.ast.DesignatorStmtFuncCall;
import rs.ac.bg.etf.pp1.ast.DesignatorStmtIncr;
import rs.ac.bg.etf.pp1.ast.ElseEntry;
import rs.ac.bg.etf.pp1.ast.FactorFuncCall;
import rs.ac.bg.etf.pp1.ast.FactorVar;
import rs.ac.bg.etf.pp1.ast.IfEntry;
import rs.ac.bg.etf.pp1.ast.MapEntry;
import rs.ac.bg.etf.pp1.ast.MapStmt;
import rs.ac.bg.etf.pp1.ast.MatchedIfElse;
import rs.ac.bg.etf.pp1.ast.MethodDecl;
import rs.ac.bg.etf.pp1.ast.MethodTypeName;
import rs.ac.bg.etf.pp1.ast.MulOp;
import rs.ac.bg.etf.pp1.ast.MulOpDiv;
import rs.ac.bg.etf.pp1.ast.MulOpFactorTerm;
import rs.ac.bg.etf.pp1.ast.MulOpMod;
import rs.ac.bg.etf.pp1.ast.MulOpMul;
import rs.ac.bg.etf.pp1.ast.MultCondFact;
import rs.ac.bg.etf.pp1.ast.MultCondition;
import rs.ac.bg.etf.pp1.ast.NumConst;
import rs.ac.bg.etf.pp1.ast.Or;
import rs.ac.bg.etf.pp1.ast.PrintStmt;
import rs.ac.bg.etf.pp1.ast.PrintStmtNum;
import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.ast.ReadStmt;
import rs.ac.bg.etf.pp1.ast.RelOp;
import rs.ac.bg.etf.pp1.ast.RelOpEq;
import rs.ac.bg.etf.pp1.ast.RelOpGe;
import rs.ac.bg.etf.pp1.ast.RelOpGt;
import rs.ac.bg.etf.pp1.ast.RelOpLe;
import rs.ac.bg.etf.pp1.ast.RelOpLt;
import rs.ac.bg.etf.pp1.ast.RelOpNeq;
import rs.ac.bg.etf.pp1.ast.ReturnExpr;
import rs.ac.bg.etf.pp1.ast.ReturnNoExpr;
import rs.ac.bg.etf.pp1.ast.SingleCondFact;
import rs.ac.bg.etf.pp1.ast.SingleCondition;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.ast.TermExprMinus;
import rs.ac.bg.etf.pp1.ast.UnmatchedIf;
import rs.ac.bg.etf.pp1.ast.UnmatchedIfElse;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;
import rs.ac.bg.etf.pp1.ast.WhileEntry;
import rs.ac.bg.etf.pp1.ast.WhileStmt;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {
	
	Logger log = Logger.getLogger(getClass());

	private int mainPc;

	private HashMap<String, Obj> arrMap = null;
	
	private Obj arrLen = null;
	private Obj iterator = null;
	private Obj arrSrc = null;

	private Stack<BranchContext> contextStack = new Stack<>();
	
	private boolean inMap = false;
	private int mapPc;
	private Obj identInMap = null;
	private boolean firstInMap = false;
	
	public int getMainPc() {
		return mainPc;
	}
 	
	public CodeGenerator(HashMap<String, Obj> arrMap) {
		this.arrMap = arrMap;
		
		arrLen = new Obj(Obj.Var, "arrLen", Tab.intType);
		iterator = new Obj(Obj.Var, "iterator", Tab.intType);
		Tab.currentScope.addToLocals(arrLen);
		Tab.currentScope.addToLocals(iterator);
	}
	
	public void visit(Program program) {
		Tab.dump(new MyDumpSymbolTableVisitor());
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
		processIncrDecrStmt(designatorStmtIncr.getDesignator().obj, 1);
	}

	public void visit(DesignatorStmtDecr designatorStmtDecr) {
		processIncrDecrStmt(designatorStmtDecr.getDesignator().obj, -1);
	}
	
	private void processIncrDecrStmt(Obj desObj, int num) {
		if (desObj.getKind() == Obj.Var) {
			Obj obj = desObj;
			Code.load(obj);
			Code.loadConst(num);
			Code.put(Code.add);
			Code.store(obj);
		} else {
			Obj obj = arrMap.get(desObj.getName());
			Code.put(Code.dup_x1);
			Code.put(Code.aload);
			Code.loadConst(num);
			Code.put(Code.add);
			Code.load(obj);
			Code.put(Code.dup_x2);
			Code.put(Code.pop);
			Code.put(Code.astore);
		}
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
		processPrintStmt(printStmt, 5);
	}
	
	public void visit(PrintStmtNum printStmt) {
		processPrintStmt(printStmt, printStmt.getN2());
	}
	
	private void processPrintStmt(SyntaxNode printStmt, int width) {
		Struct struct = null;
		if (printStmt instanceof PrintStmt) {
			struct = ((PrintStmt) printStmt).getExpr().struct;
		} else {
			struct = ((PrintStmtNum) printStmt).getExpr().struct;
		}
		if (struct.equals(Tab.intType)) {
			Code.loadConst(width);
			Code.put(Code.print);
		} else if (struct.equals(Tab.charType)) {
			Code.loadConst(width);
			Code.put(Code.bprint);
		} else {
			// Print boolean
			Code.put(Code.dup);
			Code.loadConst(0);
			Code.putFalseJump(Code.eq, 0);
			int patchAdr = Code.pc - 2;
			String toWrite = "false";
			for (char c: toWrite.toCharArray()) {
				Code.loadConst(c);
				Code.loadConst(1);
				Code.put(Code.bprint);
			}
			Code.putJump(0);
			int patchAdr2 = Code.pc - 2;
			Code.fixup(patchAdr);
			toWrite = "true";
			for (char c: toWrite.toCharArray()) {
				Code.loadConst(c);
				Code.loadConst(1);
				Code.put(Code.bprint);
			}
			Code.fixup(patchAdr2);
		}
	}
	
	public void visit(ReadStmt readStmt) {
		Obj obj = readStmt.getDesignator().obj;
		if (obj.getType().equals(Tab.charType)) {
			Code.put(Code.bread);
		} else {
			Code.put(Code.read);
		}
		Code.store(obj);
	}
	
	public void visit(MapStmt mapStmt) {
		Obj arrDst = arrMap.get(mapStmt.getDesignator().obj.getName());
		
		int[] tempBuff = new int[Code.pc - mapPc];
		
		for (int i = 0; i < tempBuff.length; i++) {
			tempBuff[i] = Code.buf[mapPc + i];
		}
		
		Code.pc = mapPc;
		
		Code.load(arrSrc);
		Code.put(Code.arraylength);
		Code.store(arrLen);
		
		Code.loadConst(0);
		Code.store(iterator);
		
		Code.load(arrLen);
		Code.put(Code.newarray);
		Code.put(1);
		Code.store(arrDst);
		
		int loopStartAdr = Code.pc;
		Code.load(iterator);
		Code.load(arrLen);
		Code.putFalseJump(Code.lt, 0);
		int patchAdr = Code.pc - 2;
		
		///////////
		for (int i = 0; i < tempBuff.length; i++) {
			Code.put(tempBuff[i]);
		}
		
		Code.load(arrDst);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.load(iterator);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.put(Code.astore);
		
		Code.load(iterator);
		Code.loadConst(1);
		Code.put(Code.add);
		Code.store(iterator);
		
		Code.putJump(loopStartAdr);
		Code.fixup(patchAdr);
		
		inMap = false;
	}
	
	public void visit(MapEntry mapEntry) {
		inMap = true;
		firstInMap = true;
		mapPc = Code.pc;
		arrSrc = arrMap.get(mapEntry.getDesignator().obj.getName());
	}
	
	public void visit(UnmatchedIf unmatchedIf) {
		Code.fixup(((IfBranchContext) contextStack.pop()).patchAdrs.getLast());
	}
	
	public void visit(UnmatchedIfElse unmatchedIfElse) {
		Code.fixup(((IfBranchContext) contextStack.pop()).patchAdrThenEnd);
	}
	
	public void visit(MatchedIfElse matchedIfElse) {
		Code.fixup(((IfBranchContext) contextStack.pop()).patchAdrThenEnd);
	}
		
	public void visit(IfEntry ifEntry) {
		contextStack.push(new IfBranchContext());
	}
	
	public void visit(ElseEntry elseEntry) {
		Code.putJump(0);
		IfBranchContext context = (IfBranchContext) contextStack.peek();
		context.patchAdrThenEnd = Code.pc - 2;
		
		if (context.patchAdrs.size() != 0) {
			for (int i = context.fixed; i < context.patchAdrs.size(); i++) {
				Code.fixup(context.patchAdrs.get(i));
				context.fixed++;
			}
		}
	}
	
	public void visit(WhileStmt whileStmt) {
		WhileBranchContext context = (WhileBranchContext) contextStack.pop();
		Code.putJump(context.adrWhileStart);
		
		if (context.patchAdrs.size() != 0) {
			for (int i = context.fixed; i < context.patchAdrs.size(); i++) {
				Code.fixup(context.patchAdrs.get(i));
				context.fixed++;
			}
		}
	}
	
	public void visit(WhileEntry whileEntry) {
		WhileBranchContext context = new WhileBranchContext();
		context.adrWhileStart = Code.pc;
		contextStack.push(context);
		
	}
	
	public void visit(BreakStmt breakStmt) {
		Code.putJump(0);
		BranchContext context = contextStack.peek();
		Stack<BranchContext> tempStack = new Stack<>();
		while (!(context instanceof WhileBranchContext)) {
			context = contextStack.pop();
			tempStack.push(context);
			context = contextStack.peek();
		}
		context.patchAdrs.add(Code.pc - 2);
		while (!tempStack.isEmpty()) {
			contextStack.push(tempStack.pop());
		}
	}
	
	public void visit(ContinueStmt continueStmt) {
		BranchContext context = contextStack.peek();
		Stack<BranchContext> tempStack = new Stack<>();
		while (!(context instanceof WhileBranchContext)) {
			context = contextStack.pop();
			tempStack.push(context);
			context = contextStack.peek();
		}
		Code.putJump(((WhileBranchContext) context).adrWhileStart);
		
		while (!tempStack.isEmpty()) {
			contextStack.push(tempStack.pop());
		}
	}
	
	public void visit(SingleCondition singleCondition) {
		BranchContext context = contextStack.peek();
		for (int i = 0; i < context.patchAdrsThenStart.size(); i++) {
			Code.fixup(context.patchAdrsThenStart.get(i));
		}
	}
	
	public void visit(MultCondition multCondition) {
		BranchContext context = contextStack.peek();
		for (int i = 0; i < context.patchAdrsThenStart.size(); i++) {
			Code.fixup(context.patchAdrsThenStart.get(i));
		}
	}
	
	public void visit(Or or) {
		BranchContext context = contextStack.peek();
		Code.putJump(0);
		context.patchAdrsThenStart.add(Code.pc - 2);
		
		if (context.patchAdrs.size() != 0) {
			for (int i = context.fixed; i < context.patchAdrs.size(); i++) {
				Code.fixup(context.patchAdrs.get(i));
				context.fixed++;
			}
		}
	}
	
	public void visit(SingleCondFact condFact) {
		Code.loadConst(0);
		Code.putFalseJump(Code.ne, 0);
		
		BranchContext context = contextStack.peek();
		context.patchAdrs.add(Code.pc - 2);
	}
		
	public void visit(MultCondFact condFact) {
		RelOp op = condFact.getRelOp();
		if (op instanceof RelOpEq) {
			Code.putFalseJump(Code.eq, 0);
		} else if (op instanceof RelOpNeq) {
			Code.putFalseJump(Code.ne, 0);
		} else if (op instanceof RelOpLt) {
			Code.putFalseJump(Code.lt, 0);
		} else if (op instanceof RelOpLe) {
			Code.putFalseJump(Code.le, 0);
		} else if (op instanceof RelOpGt) {
			Code.putFalseJump(Code.gt, 0);
		} else if (op instanceof RelOpGe) {
			Code.putFalseJump(Code.ge, 0);
		}
		
		BranchContext context = contextStack.peek();
		context.patchAdrs.add(Code.pc - 2);
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
			if (factorVar.getDesignator().obj.getKind() == Obj.Var || factorVar.getDesignator().obj.getKind() == Obj.Con) {
				if (inMap && factorVar.getDesignator().obj == identInMap) {
					Code.load(arrSrc);
					Code.load(iterator);
					Code.put(Code.aload);
				} else {
					Code.load(factorVar.getDesignator().obj);
				}
			} else if (factorVar.getDesignator().obj.getKind() == Obj.Elem) {
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
		Code.put(Code.dup_x1);
		Code.put(Code.mul);
		Code.put(Code.newarray);
		if (constrFactorMatrix.getType().struct.equals(Tab.intType)) {
			Code.put(1);
		} else {
			Code.put(0);
		}
		
		Code.put(Code.new_);
		Code.put2(8);
		
		Code.put(Code.dup_x2);
		Code.put(Code.dup_x2);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		
		Code.put(Code.putfield);
		Code.put2(0);
		Code.put(Code.putfield);
		Code.put2(1);
	}
	
	public void visit(DesignatorIdent designator) {
		Obj obj = designator.obj;
		
		if (inMap && firstInMap) {
			firstInMap = false;
			identInMap = obj;
		}
	}
	
	public void visit(DesignatorArr designatorArr) {
		Obj obj = arrMap.get(designatorArr.obj.getName());
		Code.load(obj);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
	}

	public void visit(DesignatorMatrix designatorMatrix) {
		Obj obj = arrMap.get(designatorMatrix.obj.getName());
		
		Code.load(obj);
		Code.put(Code.getfield);
		Code.put2(0);
		Code.put(Code.dup_x2);
		Code.put(Code.pop);
		Code.put(Code.dup_x1);
		Code.put(Code.pop);
		Code.load(obj);
		Code.put(Code.getfield);
		Code.put2(1);
		Code.put(Code.mul);
		Code.put(Code.add);
	}

}
