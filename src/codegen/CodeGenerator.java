package codegen;

import ast.ASTNode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CodeGenerator {

    public String generatePythonCode(ASTNode.ProgramNode program, String outputPath) {
        StringBuilder sb = new StringBuilder();

        if (program != null) {
            for (ASTNode stmt : program.statements) {
                sb.append(genStmt(stmt, 0));
            }
        }

        String pythonCode = sb.toString();

        try {
            File file = new File(outputPath);

            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            Files.write(
                    Paths.get(outputPath),
                    pythonCode.getBytes(StandardCharsets.UTF_8)
            );

        } catch (Exception e) {
            System.err.println("Failed to write target code file: " + e.getMessage());
        }

        return pythonCode;
    }

    private String genStmt(ASTNode node, int indentLevel) {
        String indent = "    ".repeat(indentLevel);
        StringBuilder sb = new StringBuilder();

        if (node instanceof ASTNode.DeclNode) {
            ASTNode.DeclNode decl = (ASTNode.DeclNode) node;

            sb.append(indent)
              .append(decl.name)
              .append(" = ")
              .append(genExpr(decl.expr))
              .append("\n");

        } else if (node instanceof ASTNode.AssignNode) {
            ASTNode.AssignNode assign = (ASTNode.AssignNode) node;

            sb.append(indent)
              .append(assign.name)
              .append(" = ")
              .append(genExpr(assign.expr))
              .append("\n");

        } else if (node instanceof ASTNode.PrintNode) {
            ASTNode.PrintNode printNode = (ASTNode.PrintNode) node;

            sb.append(indent)
              .append("print(")
              .append(genExpr(printNode.expr))
              .append(")\n");

        } else if (node instanceof ASTNode.IfNode) {
            ASTNode.IfNode ifNode = (ASTNode.IfNode) node;

            sb.append(indent)
              .append("if ")
              .append(genExpr(ifNode.condition))
              .append(":\n");

            for (ASTNode s : ifNode.thenBranch) {
                sb.append(genStmt(s, indentLevel + 1));
            }

            if (!ifNode.elseBranch.isEmpty()) {
                sb.append(indent)
                  .append("else:\n");

                for (ASTNode s : ifNode.elseBranch) {
                    sb.append(genStmt(s, indentLevel + 1));
                }
            }

        } else if (node instanceof ASTNode.WhileNode) {
            ASTNode.WhileNode whileNode = (ASTNode.WhileNode) node;

            sb.append(indent)
              .append("while ")
              .append(genExpr(whileNode.condition))
              .append(":\n");

            for (ASTNode s : whileNode.body) {
                sb.append(genStmt(s, indentLevel + 1));
            }
        }

        return sb.toString();
    }

    private String genExpr(ASTNode node) {
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

            String left = genExpr(bin.left);
            String right = genExpr(bin.right);

            return "(" + left + " " + bin.op + " " + right + ")";
        }

        return "None";
    }
}