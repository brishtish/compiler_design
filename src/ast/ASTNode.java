package ast;

import java.util.List;

/**
 * Abstract Syntax Tree (AST) node definitions for BanglaScript.
 */
public abstract class ASTNode {

    public String type; // Type ("সংখ্যা", "বাক্য", "boolean")
    public int line;

    public static class ProgramNode extends ASTNode {
        public final List<ASTNode> statements;

        public ProgramNode(List<ASTNode> statements) {
            this.statements = statements;
        }
    }

    public static class NumberNode extends ASTNode {
        public final int value;
        public final String originalLexeme;

        public NumberNode(int value, String originalLexeme, int line) {
            this.value = value;
            this.originalLexeme = originalLexeme;
            this.line = line;
            this.type = "সংখ্যা";
        }
    }

    public static class StringNode extends ASTNode {
        public final String value;

        public StringNode(String value, int line) {
            this.value = value;
            this.line = line;
            this.type = "বাক্য";
        }
    }

    public static class VarNode extends ASTNode {
        public final String name;

        public VarNode(String name, int line) {
            this.name = name;
            this.line = line;
        }
    }

    public static class BinOpNode extends ASTNode {
        public final ASTNode left;
        public final String op;
        public final ASTNode right;

        public BinOpNode(ASTNode left, String op, ASTNode right, int line) {
            this.left = left;
            this.op = op;
            this.right = right;
            this.line = line;
        }
    }

    public static class DeclNode extends ASTNode {
        public final String declaredType; // e.g. "সংখ্যা", "বাক্য", or null
        public final String name;
        public final ASTNode expr;

        public DeclNode(String declaredType, String name, ASTNode expr, int line) {
            this.declaredType = declaredType;
            this.name = name;
            this.expr = expr;
            this.line = line;
        }
    }

    public static class AssignNode extends ASTNode {
        public final String name;
        public final ASTNode expr;

        public AssignNode(String name, ASTNode expr, int line) {
            this.name = name;
            this.expr = expr;
            this.line = line;
        }
    }

    public static class PrintNode extends ASTNode {
        public final ASTNode expr;

        public PrintNode(ASTNode expr, int line) {
            this.expr = expr;
            this.line = line;
        }
    }

    public static class IfNode extends ASTNode {
        public final ASTNode condition;
        public final List<ASTNode> thenBranch;
        public final List<ASTNode> elseBranch;

        public IfNode(ASTNode condition, List<ASTNode> thenBranch, List<ASTNode> elseBranch, int line) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
            this.line = line;
        }
    }

    public static class WhileNode extends ASTNode {
        public final ASTNode condition;
        public final List<ASTNode> body;

        public WhileNode(ASTNode condition, List<ASTNode> body, int line) {
            this.condition = condition;
            this.body = body;
            this.line = line;
        }
    }
}
