package tac; 

import ast.ASTNode; 

import java.util.ArrayList; 
import java.util.List;      

public class TACGenerator {

    private final List<TACInstr> instructions = new ArrayList<>(); 
    private int tempCount = 0;  
    private int labelCount = 0; 

    
    public String newTemp() {
        return "t" + (tempCount++);
    }

    
    public String newLabel() {
        return "L" + (++labelCount); 
    }

    
    public void emit(TACInstr instr) {
        instructions.add(instr); 
    }

    
    public List<TACInstr> generate(ASTNode.ProgramNode program) {
        instructions.clear(); 
        tempCount = 0;        
        labelCount = 0;      

        if (program != null) {
            
            for (ASTNode stmt : program.statements) {
                genStmt(stmt); 
            }
        }
        return instructions;
    }

    
    public void genStmt(ASTNode node) {
        if (node instanceof ASTNode.DeclNode) {
            ASTNode.DeclNode decl = (ASTNode.DeclNode) node; 
            String result = genExpr(decl.expr); 
            emit(new TACInstr.TACCopy(decl.name, result)); 
        }
        else if (node instanceof ASTNode.AssignNode) {
            ASTNode.AssignNode assign = (ASTNode.AssignNode) node; 
            String result = genExpr(assign.expr); 
            emit(new TACInstr.TACCopy(assign.name, result)); 
        }
        else if (node instanceof ASTNode.PrintNode) {
            ASTNode.PrintNode printNode = (ASTNode.PrintNode) node; 
            String result = genExpr(printNode.expr); 
            emit(new TACInstr.TACPrint(result)); 
        }
        else if (node instanceof ASTNode.IfNode) {
            ASTNode.IfNode ifNode = (ASTNode.IfNode) node; 
            String condRes = genExpr(ifNode.condition); 
            String elseLabel = newLabel(); 
            String endLabel = newLabel();  

            emit(new TACInstr.TACJumpIfFalse(condRes, elseLabel)); 
           
            for (ASTNode s : ifNode.thenBranch) genStmt(s);
            emit(new TACInstr.TACJump(endLabel)); 

            emit(new TACInstr.TACLabel(elseLabel)); 
           
            if (!ifNode.elseBranch.isEmpty()) {
                for (ASTNode s : ifNode.elseBranch) genStmt(s);
            }

            emit(new TACInstr.TACLabel(endLabel));
        }
        else if (node instanceof ASTNode.WhileNode) {
            ASTNode.WhileNode whileNode = (ASTNode.WhileNode) node;
            String startLabel = newLabel(); 
            String endLabel = newLabel();  

            emit(new TACInstr.TACLabel(startLabel)); 
            String condRes = genExpr(whileNode.condition); 
            emit(new TACInstr.TACJumpIfFalse(condRes, endLabel)); 

            
            for (ASTNode s : whileNode.body) genStmt(s);
            emit(new TACInstr.TACJump(startLabel)); 

            emit(new TACInstr.TACLabel(endLabel)); 
        }
    }
    
    public String genExpr(ASTNode node) {
        if (node instanceof ASTNode.NumberNode) {
            return String.valueOf(((ASTNode.NumberNode) node).value); 
        }
        if (node instanceof ASTNode.StringNode) {
            return "\"" + ((ASTNode.StringNode) node).value + "\""; 
        }
        if (node instanceof ASTNode.VarNode) {
            return ((ASTNode.VarNode) node).name; 
        }
        if (node instanceof ASTNode.BinOpNode) {
            ASTNode.BinOpNode bin = (ASTNode.BinOpNode) node; 
            String leftName = genExpr(bin.left);  
            String rightName = genExpr(bin.right); 
            String dest = newTemp();               
            emit(new TACInstr.TACBinOp(dest, leftName, bin.op, rightName)); 
            return dest; 
        }
        return "null"; 
    }

    public void printTAC(List<TACInstr> instrs) {
        System.out.println("Three-Address Code (TAC):");
        System.out.printf("  %-4s  %-12s  %s%n", "#", "Kind", "Instruction");
        System.out.println("  ----+--------------+------------------------------");
        for (int i = 0; i < instrs.size(); i++) {
            TACInstr instr = instrs.get(i); 
            String kind = instr.getClass().getSimpleName().replace("TAC", ""); 
            System.out.printf("  %-4d  %-12s  %s%n", i, kind, instr.toString());
        }
        System.out.println();
    }
}
  

