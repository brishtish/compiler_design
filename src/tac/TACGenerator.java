package tac; // Java syntax: Declares this file belongs to the "tac" package folder.

import ast.ASTNode; // Java syntax: Imports all AST node definitions.

import java.util.ArrayList; // Java syntax: Imports ArrayList class to hold instructions.
import java.util.List;      // Java syntax: Imports List interface.

// Java syntax: Declares public class TACGenerator.
// Project context: Walks the AST and flattens nested expressions into Three-Address Code (TAC) instructions.
public class TACGenerator {

    private final List<TACInstr> instructions = new ArrayList<>(); // Java syntax: List storing generated TAC instructions.
    private int tempCount = 0;  // Project context: Counter for naming unique temporaries (t0, t1, t2...).
    private int labelCount = 0; // Project context: Counter for naming unique branch labels (L1, L2, L3...).

    // Project context: Generates a unique temporary variable name (e.g. "t0", "t1").
    public String newTemp() {
        return "t" + (tempCount++); // Java syntax: Returns "t" concatenated with incremented counter.
    }

    // Project context: Generates a unique branch label name (e.g. "L1", "L2").
    public String newLabel() {
        return "L" + (++labelCount); // Java syntax: Returns "L" concatenated with incremented counter.
    }

    // Project context: Emits a single TAC instruction into our list.
    public void emit(TACInstr instr) {
        instructions.add(instr); // Java syntax: Appends instruction to list.
    }

    // Project context: Main entry point. Generates TAC for an entire AST program.
    public List<TACInstr> generate(ASTNode.ProgramNode program) {
        instructions.clear(); // Java syntax: Clears previous instructions.
        tempCount = 0;        // Project context: Resets temp counter.
        labelCount = 0;       // Project context: Resets label counter.

        if (program != null) {
            // Java syntax: Enhanced for-loop iterating through all program statements.
            for (ASTNode stmt : program.statements) {
                genStmt(stmt); // Project context: Generates TAC for each statement.
            }
        }
        return instructions; // Java syntax: Returns the completed list of TAC instructions.
    }

    // Project context: Generates TAC for a single statement node.
    public void genStmt(ASTNode node) {
        if (node instanceof ASTNode.DeclNode) {
            ASTNode.DeclNode decl = (ASTNode.DeclNode) node; // Java syntax: Downcasts to DeclNode.
            String result = genExpr(decl.expr); // Project context: Generates code for initial value expression.
            emit(new TACInstr.TACCopy(decl.name, result)); // Project context: Emits copy instruction to variable.
        }
        else if (node instanceof ASTNode.AssignNode) {
            ASTNode.AssignNode assign = (ASTNode.AssignNode) node; // Java syntax: Downcasts to AssignNode.
            String result = genExpr(assign.expr); // Project context: Generates code for right-side expression.
            emit(new TACInstr.TACCopy(assign.name, result)); // Project context: Emits copy into variable.
        }
        else if (node instanceof ASTNode.PrintNode) {
            ASTNode.PrintNode printNode = (ASTNode.PrintNode) node; // Java syntax: Downcasts to PrintNode.
            String result = genExpr(printNode.expr); // Project context: Generates code for print expression.
            emit(new TACInstr.TACPrint(result)); // Project context: Emits print instruction.
        }
        else if (node instanceof ASTNode.IfNode) {
            ASTNode.IfNode ifNode = (ASTNode.IfNode) node; // Java syntax: Downcasts to IfNode.
            String condRes = genExpr(ifNode.condition); // Project context: Generates code for condition expression.
            String elseLabel = newLabel(); // Project context: Label for else branch (or end if no else).
            String endLabel = newLabel();  // Project context: Label for end of if statement.

            emit(new TACInstr.TACJumpIfFalse(condRes, elseLabel)); // Project context: Jump to else if condition is false.

            // Project context: Generates code for then branch statements.
            for (ASTNode s : ifNode.thenBranch) genStmt(s);
            emit(new TACInstr.TACJump(endLabel)); // Project context: Jump over else branch to end.

            emit(new TACInstr.TACLabel(elseLabel)); // Project context: Emits else label.
            // Project context: Generates code for else branch statements if present.
            if (!ifNode.elseBranch.isEmpty()) {
                for (ASTNode s : ifNode.elseBranch) genStmt(s);
            }

            emit(new TACInstr.TACLabel(endLabel)); // Project context: Emits end label.
        }
        else if (node instanceof ASTNode.WhileNode) {
            ASTNode.WhileNode whileNode = (ASTNode.WhileNode) node; // Java syntax: Downcasts to WhileNode.
            String startLabel = newLabel(); // Project context: Label for start of loop condition check.
            String endLabel = newLabel();   // Project context: Label for loop exit.

            emit(new TACInstr.TACLabel(startLabel)); // Project context: Emits start of loop label.
            String condRes = genExpr(whileNode.condition); // Project context: Generates code for loop condition.
            emit(new TACInstr.TACJumpIfFalse(condRes, endLabel)); // Project context: Exit loop if condition is false.

            // Project context: Generates code for loop body statements.
            for (ASTNode s : whileNode.body) genStmt(s);
            emit(new TACInstr.TACJump(startLabel)); // Project context: Jump back to start of loop condition.

            emit(new TACInstr.TACLabel(endLabel)); // Project context: Emits exit label.
        }
    }

    // Project context: Generates TAC for an expression node and returns the operand name holding the result.
    public String genExpr(ASTNode node) {
        if (node instanceof ASTNode.NumberNode) {
            return String.valueOf(((ASTNode.NumberNode) node).value); // Project context: Returns numeric literal string.
        }
        if (node instanceof ASTNode.StringNode) {
            return "\"" + ((ASTNode.StringNode) node).value + "\""; // Project context: Returns string literal value in quotes.
        }
        if (node instanceof ASTNode.VarNode) {
            return ((ASTNode.VarNode) node).name; // Project context: Returns variable name string.
        }
        if (node instanceof ASTNode.BinOpNode) {
            ASTNode.BinOpNode bin = (ASTNode.BinOpNode) node; // Java syntax: Downcasts to BinOpNode.
            String leftName = genExpr(bin.left);   // Project context: Recursively generates left operand TAC.
            String rightName = genExpr(bin.right); // Project context: Recursively generates right operand TAC.
            String dest = newTemp();               // Project context: Allocates a new temporary for operation result.
            emit(new TACInstr.TACBinOp(dest, leftName, bin.op, rightName)); // Project context: Emits binop TAC instruction.
            return dest; // Project context: Returns temporary name so caller can use it as an operand.
        }
        return "null"; // Java syntax: Fallback default string.
    }

    // Project context: Prints formatted TAC instruction table to the console.
    public void printTAC(List<TACInstr> instrs) {
        System.out.println("Three-Address Code (TAC):");
        System.out.printf("  %-4s  %-12s  %s%n", "#", "Kind", "Instruction");
        System.out.println("  ----+--------------+------------------------------");
        for (int i = 0; i < instrs.size(); i++) {
            TACInstr instr = instrs.get(i); // Java syntax: Gets instruction at index i.
            String kind = instr.getClass().getSimpleName().replace("TAC", ""); // Java syntax: Formats kind name.
            System.out.printf("  %-4d  %-12s  %s%n", i, kind, instr.toString());
        }
        System.out.println(); // Java syntax: Prints trailing blank line.
    }
}
  

