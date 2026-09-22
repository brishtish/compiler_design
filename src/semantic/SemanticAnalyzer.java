package semantic;

import ast.ASTNode;
import utils.ErrorReporter;

public class SemanticAnalyzer {

    private final SymbolTable symbolTable;
    private final ErrorReporter errors;

    public SemanticAnalyzer(ErrorReporter errors) {
        this.symbolTable = new SymbolTable();
        this.errors = errors;
    }

    public SemanticAnalyzer() {
        this(new ErrorReporter());
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public boolean analyze(ASTNode.ProgramNode program) {
        if (program == null)
            return false;
        for (ASTNode stmt : program.statements) {
            checkStmt(stmt);
        }
        return !errors.hasErrors();
    }

    private void checkStmt(ASTNode node) {
        if (node instanceof ASTNode.DeclNode) {
            checkDecl((ASTNode.DeclNode) node);
        } else if (node instanceof ASTNode.AssignNode) {
            checkAssign((ASTNode.AssignNode) node);
        } else if (node instanceof ASTNode.PrintNode) {
            checkPrint((ASTNode.PrintNode) node);
        } else if (node instanceof ASTNode.IfNode) {
            checkIf((ASTNode.IfNode) node);
        } else if (node instanceof ASTNode.WhileNode) {
            checkWhile((ASTNode.WhileNode) node);
        }
    }

    private Object evalConst(ASTNode node) {
        if (node == null)
            return null;
        if (node instanceof ASTNode.NumberNode) {
            return ((ASTNode.NumberNode) node).value;
        }
        if (node instanceof ASTNode.StringNode) {
            return ((ASTNode.StringNode) node).value;
        }
        if (node instanceof ASTNode.VarNode) {
            Symbol sym = symbolTable.lookup(((ASTNode.VarNode) node).name);
            return (sym != null) ? sym.constValue : null;
        }
        if (node instanceof ASTNode.BinOpNode) {
            ASTNode.BinOpNode bin = (ASTNode.BinOpNode) node;
            Object l = evalConst(bin.left);
            Object r = evalConst(bin.right);
            if (l instanceof Integer && r instanceof Integer) {
                int leftVal = (Integer) l;
                int rightVal = (Integer) r;
                switch (bin.op) {
                    case "+":
                        return leftVal + rightVal;
                    case "-":
                        return leftVal - rightVal;
                    case "*":
                        return leftVal * rightVal;
                    case "/":
                        return (rightVal != 0) ? (leftVal / rightVal) : null;
                }
            }
            if (bin.op.equals("+") && (l != null || r != null)) {
                if (l instanceof String || r instanceof String) {
                    return (l != null ? l.toString() : "") + (r != null ? r.toString() : "");
                }
            }
        }
        return null;
    }

    private void checkDecl(ASTNode.DeclNode node) {
        int errorsBefore = errors.count();
        if (node.expr == null) {
            boolean success = symbolTable.declare(node.name, node.declaredType, false, node.line, null);

            if (!success) {
                errors.add( "Semantic", node.line, "Variable '" + node.name + "' is already declared in this scope.");
            }

            return;
        }
        String exprType = checkExpr(node.expr);
        String targetType = (node.declaredType != null) ? node.declaredType : exprType;
        node.type = targetType;

        boolean hasTypeMismatch = false;
        if (!targetType.equals(exprType) && !"unknown".equals(exprType)) {
            errors.add("Semantic", node.line, "Type mismatch: Cannot assign '" + exprType + "' to '" + node.name
                    + "' of declared type '" + targetType + "'.");
            hasTypeMismatch = true;
        }

        boolean exprHadError = (errors.count() > errorsBefore) || hasTypeMismatch || "unknown".equals(exprType);
        if (!exprHadError) {
            Object constVal = evalConst(node.expr);
            boolean success = symbolTable.declare(node.name, targetType, true, node.line, constVal);
            if (!success) {
                errors.add("Semantic", node.line, "Variable '" + node.name + "' is already declared in this scope.");
            }
        }
    }

    private void checkAssign(ASTNode.AssignNode node) {
        Symbol sym = symbolTable.lookup(node.name);
        if (sym == null) {
            errors.add("Semantic", node.line,
                    "Variable '" + node.name + "' is assigned before declaration (declare with 'ধরো').");
            return;
        }

        String exprType = checkExpr(node.expr);
        node.type = exprType;

        if (!sym.type.equals(exprType) && !"unknown".equals(exprType)) {
            errors.add("Semantic", node.line, "Type mismatch: Cannot assign '" + exprType + "' to variable '"
                    + node.name + "' of type '" + sym.type + "'.");
        }

        sym.constValue = evalConst(node.expr);
    }

    private void checkPrint(ASTNode.PrintNode node) {
        String exprType = checkExpr(node.expr);
        node.type = exprType;
    }

    private void checkIf(ASTNode.IfNode node) {
        checkExpr(node.condition);

        symbolTable.enterScope();
        for (ASTNode s : node.thenBranch)
            checkStmt(s);
        symbolTable.exitScope();

        if (!node.elseBranch.isEmpty()) {
            symbolTable.enterScope();
            for (ASTNode s : node.elseBranch)
                checkStmt(s);
            symbolTable.exitScope();
        }
    }

    private void checkWhile(ASTNode.WhileNode node) {
        checkExpr(node.condition);

        symbolTable.enterScope();
        for (ASTNode s : node.body)
            checkStmt(s);
        symbolTable.exitScope();
    }

    private String checkExpr(ASTNode node) {
        if (node == null)
            return "unknown";
        if (node instanceof ASTNode.NumberNode) {
            node.type = "সংখ্যা";
            return "সংখ্যা";
        }

        if (node instanceof ASTNode.StringNode) {
            node.type = "বাক্য";
            return "বাক্য";
        }

        if (node instanceof ASTNode.VarNode) {
            ASTNode.VarNode v = (ASTNode.VarNode) node;
            Symbol sym = symbolTable.lookup(v.name);
            if (sym == null) {
                errors.add("Semantic", v.line, "Variable '" + v.name + "' is used before it was declared.");
                node.type = "unknown";
                return "unknown";
            }
            node.type = sym.type;
            return sym.type;
        }

        if (node instanceof ASTNode.BinOpNode) {
            ASTNode.BinOpNode bin = (ASTNode.BinOpNode) node;
            String leftType = checkExpr(bin.left);
            String rightType = checkExpr(bin.right);
            String op = bin.op;

            // Division-by-zero check (both literals and variables/expressions)
            if (op.equals("/")) {
                Object rightConst = evalConst(bin.right);
                if (rightConst instanceof Integer && ((Integer) rightConst) == 0) {
                    errors.add("Semantic", bin.line, "Division by zero is not allowed.");
                }
            }

            // Comparison operators: == != < > <= >=
            if (op.equals("==") || op.equals("!=") || op.equals("<") ||
                    op.equals(">") || op.equals("<=") || op.equals(">=")) {

                if (op.equals("==") || op.equals("!=")) {
                    if (!leftType.equals(rightType) && !leftType.equals("unknown") && !rightType.equals("unknown")) {
                        errors.add("Semantic", bin.line, "Cannot compare incompatible types with '" + op + "': '"
                                + leftType + "' and '" + rightType + "'.");
                    }
                }
                
                else {
                    if ((!"সংখ্যা".equals(leftType) || !"সংখ্যা".equals(rightType)) && !"unknown".equals(leftType)
                            && !"unknown".equals(rightType)) {
                        errors.add("Semantic", bin.line,
                                "Relational operator '" + op + "' requires 'সংখ্যা' (int) operands, got: '" + leftType
                                        + "' and '" + rightType + "'.");
                    }
                }
                bin.type = "boolean";
                return "boolean";
            }

            // String concatenation
            if (op.equals("+") && ("বাক্য".equals(leftType) || "বাক্য".equals(rightType))) {
                bin.type = "বাক্য";
                return "বাক্য";
            }

            // Arithmetic: + - * /
            if (op.equals("+") || op.equals("-") || op.equals("*") || op.equals("/")) {
                if ((!"সংখ্যা".equals(leftType) || !"সংখ্যা".equals(rightType)) && !"unknown".equals(leftType)
                        && !"unknown".equals(rightType)) {
                    errors.add("Semantic", bin.line, "Operator '" + op + "' requires 'সংখ্যা' (int) operands, got: '"
                            + leftType + "' and '" + rightType + "'.");
                }
                bin.type = "সংখ্যা";
                return "সংখ্যা";
            }
        }

        node.type = "unknown";
        return "unknown";
    }
}
