package rs.ac.bg.etf.pp1;

import rs.etf.pp1.symboltable.concepts.Struct;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;

public class MyDumpSymbolTableVisitor extends DumpSymbolTableVisitor {
	
	@Override
	public void visitStructNode(Struct structToVisit) {
		int helperType;
		switch(structToVisit.getKind()) {
		case Struct.None:
			output.append("notype");
			break;
		case Struct.Int:
			output.append("int");
			break;
		case Struct.Char:
			output.append("char");
			break;
		case Struct.Bool:
			output.append("bool");
			break;
		case Struct.Array:
			if (structToVisit.getElemType().getKind() == Struct.Array) {
				output.append("Matrix of ");
				helperType = structToVisit.getElemType().getElemType().getKind();
			} else {
				output.append("Arr of ");
				helperType = structToVisit.getElemType().getKind();
			}
			
			switch (helperType) {
			case Struct.None:
				output.append("notype");
				break;
			case Struct.Int:
				output.append("int");
				break;
			case Struct.Char:
				output.append("char");
				break;
			case Struct.Bool:
				output.append("bool");
			}
			break;
		}
	}
}
