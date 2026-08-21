import ast.ASTNode;
import utils.BanglaUtil;

/**
 * Pretty-prints an Abstract Syntax Tree (AST) matching the standard clean format.
 */
public class ASTPrinter {

    public void print(ASTNode.ProgramNode program) {
        if (program == null || program.statements.isEmpty()) {
            System.out.println("  (Empty AST)");
            return;
        }
        System.out.println("PROGRAM");
        for (int i = 0; i < program.statements.size(); i++) {
            boolean isLast = (i == program.statements.size() - 1);
            printStatement(program.statements.get(i), "", isLast);
        }
    }

    private void printStatement(ASTNode node, String prefix, boolean isLast) {
        String branch = isLast ? "└── " : "├── ";
        String childPrefix = prefix + (isLast ? "    " : "│   ");

        if (node instanceof ASTNode.DeclNode) {
            ASTNode.DeclNode d = (ASTNode.DeclNode) node;
            System.out.println(prefix + branch + "DECLARATION");
            System.out.println(childPrefix + "├── TYPE  : " + (d.declaredType != null ? d.declaredType : "সংখ্যা"));
            System.out.println(childPrefix + "├── NAME  : " + d.name);
            System.out.println(childPrefix + "└── VALUE : " + exprToString(d.expr));

        } else if (node instanceof ASTNode.AssignNode) {
            ASTNode.AssignNode a = (ASTNode.AssignNode) node;
            System.out.println(prefix + branch + "ASSIGNMENT");
            System.out.println(childPrefix + "├── NAME  : " + a.name);
            System.out.println(childPrefix + "└── VALUE : " + exprToString(a.expr));

        } else if (node instanceof ASTNode.PrintNode) {
            ASTNode.PrintNode p = (ASTNode.PrintNode) node;
            System.out.println(prefix + branch + "PRINT");
            System.out.println(childPrefix + "└── EXPR  : " + exprToString(p.expr));

        } else if (node instanceof ASTNode.IfNode) {
            ASTNode.IfNode ifNode = (ASTNode.IfNode) node;
            System.out.println(prefix + branch + "IF");
            System.out.println(childPrefix + "├── CONDITION : " + exprToString(ifNode.condition));
            System.out.println(childPrefix + "├── THEN");
            for (int i = 0; i < ifNode.thenBranch.size(); i++) {
                boolean lastThen = (i == ifNode.thenBranch.size() - 1);
                printStatement(ifNode.thenBranch.get(i), childPrefix + "│   ", lastThen);
            }
            if (!ifNode.elseBranch.isEmpty()) {
                System.out.println(childPrefix + "└── ELSE");
                for (int i = 0; i < ifNode.elseBranch.size(); i++) {
                    boolean lastElse = (i == ifNode.elseBranch.size() - 1);
                    printStatement(ifNode.elseBranch.get(i), childPrefix + "    ", lastElse);
                }
            }

        } else if (node instanceof ASTNode.WhileNode) {
            ASTNode.WhileNode w = (ASTNode.WhileNode) node;
            System.out.println(prefix + branch + "WHILE");
            System.out.println(childPrefix + "├── CONDITION : " + exprToString(w.condition));
            System.out.println(childPrefix + "└── BODY");
            for (int i = 0; i < w.body.size(); i++) {
                boolean lastBody = (i == w.body.size() - 1);
                printStatement(w.body.get(i), childPrefix + "    ", lastBody);
            }
        }
    }

    private String exprToString(ASTNode node) {
        if (node instanceof ASTNode.NumberNode) {
            ASTNode.NumberNode num = (ASTNode.NumberNode) node;
            return BanglaUtil.toBanglaString(String.valueOf(num.value));
        }
        if (node instanceof ASTNode.StringNode) {
            return "\"" + ((ASTNode.StringNode) node).value + "\"";
        }
        if (node instanceof ASTNode.VarNode) {
            return ((ASTNode.VarNode) node).name;
        }
        if (node instanceof ASTNode.BinOpNode) {
            ASTNode.BinOpNode b = (ASTNode.BinOpNode) node;
            return "(" + exprToString(b.left) + " " + b.op + " " + exprToString(b.right) + ")";
        }
        return "";
    }
}
