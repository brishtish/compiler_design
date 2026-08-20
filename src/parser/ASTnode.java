
import java.util.List;

public abstract class ASTNode {

    
    public String type;


    /** Integer literal, e.g.  42  or  ৪২ */
    public static class NumberNode extends ASTNode {
        public final int value;

        public NumberNode(int value) {
            this.value = value;
            this.type  = "int";   // known at construction time
        }

        @Override public String toString() { return "Number(" + value + ")"; }
    }

    /** String literal, e.g.  "হ্যালো বিশ্ব" */
    public static class StringNode extends ASTNode {
        public final String value;

        public StringNode(String value) {
            this.value = value;
            this.type  = "String"; // known at construction time
        }

        @Override public String toString() { return "String(\"" + value + "\")"; }
    }

    /** Variable reference, e.g.  ক  or  myVar */
    public static class VarNode extends ASTNode {
        public final String name;

        public VarNode(String name) { this.name = name; }

        @Override public String toString() { return "Var(" + name + ")"; }
    }


    public static class BinOpNode extends ASTNode {
        public final ASTNode left;
        public final String  op;
        public final ASTNode right;

        public BinOpNode(ASTNode left, String op, ASTNode right) {
            this.left  = left;
            this.op    = op;
            this.right = right;
        }

        @Override public String toString() {
            return "BinOp(" + left + " " + op + " " + right + ")";
        }
    }



    public static class DeclNode extends ASTNode {
        public final String  name;
        public final ASTNode expr;

        public DeclNode(String name, ASTNode expr) {
            this.name = name;
            this.expr = expr;
        }

        @Override public String toString() { return "Decl(" + name + " = " + expr + ")"; }
    }

 
    public static class AssignNode extends ASTNode {
        public final String  name;
        public final ASTNode expr;

        public AssignNode(String name, ASTNode expr) {
            this.name = name;
            this.expr = expr;
        }

        @Override public String toString() { return "Assign(" + name + " = " + expr + ")"; }
    }


    public static class PrintNode extends ASTNode {
        public final ASTNode expr;

        public PrintNode(ASTNode expr) { this.expr = expr; }

        @Override public String toString() { return "Print(" + expr + ")"; }
    }

   
    public static class IfNode extends ASTNode {
        public final ASTNode       condition;
        public final List<ASTNode> thenBranch;
        public final List<ASTNode> elseBranch;   // empty list = no else

        public IfNode(ASTNode condition,
                      List<ASTNode> thenBranch,
                      List<ASTNode> elseBranch) {
            this.condition  = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }


    public static class WhileNode extends ASTNode {
        public final ASTNode       condition;
        public final List<ASTNode> body;

        public WhileNode(ASTNode condition, List<ASTNode> body) {
            this.condition = condition;
            this.body      = body;
        }
    }
}
