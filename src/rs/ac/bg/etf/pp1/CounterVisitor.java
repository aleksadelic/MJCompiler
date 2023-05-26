package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.FormParsChain;
import rs.ac.bg.etf.pp1.ast.FormParsChainArr;
import rs.ac.bg.etf.pp1.ast.FormParsChainMatrix;
import rs.ac.bg.etf.pp1.ast.FormParsHead;
import rs.ac.bg.etf.pp1.ast.FormParsHeadArr;
import rs.ac.bg.etf.pp1.ast.FormParsHeadMatrix;
import rs.ac.bg.etf.pp1.ast.VarDeclChain;
import rs.ac.bg.etf.pp1.ast.VarDeclChainArr;
import rs.ac.bg.etf.pp1.ast.VarDeclChainMatrix;
import rs.ac.bg.etf.pp1.ast.VarDeclHead;
import rs.ac.bg.etf.pp1.ast.VarDeclHeadArr;
import rs.ac.bg.etf.pp1.ast.VarDeclHeadMatrix;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

public class CounterVisitor extends VisitorAdaptor {
	
	protected int count;
	
	public int getCount() {
		return count;
	}
	
	public static class FormParamCounter extends CounterVisitor {
		
		public void visit(FormParsHead formPar) {
			count++;
		}
		
		public void visit(FormParsHeadArr formPar) {
			count++;
		}
		
		public void visit(FormParsHeadMatrix formPar) {
			count++;
		}
		
		public void visit(FormParsChain formPar) {
			count++;
		}
		
		public void visit(FormParsChainArr formPar) {
			count++;
		}
		
		public void visit(FormParsChainMatrix formPar) {
			count++;
		}
	}
	
	public static class VarCounter extends CounterVisitor {
		
		public void visit(VarDeclHead varDecl) {
			count++;
		}
		
		public void visit(VarDeclHeadArr varDecl) {
			count++;
		}
		
		public void visit(VarDeclHeadMatrix varDecl) {
			count++;
		}
		
		public void visit(VarDeclChain varDecl) {
			count++;
		}
		
		public void visit(VarDeclChainArr varDecl) {
			count++;
		}
		
		public void visit(VarDeclChainMatrix varDecl) {
			count++;
		}
	}

}
