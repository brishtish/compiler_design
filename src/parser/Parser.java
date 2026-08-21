package parser;

import ast.ASTNode;
import lexer.Token;
import lexer.TokenType;
import utils.ErrorReporter;

import java.util.*;

/**
 * Recursive-descent parser for BanglaScript.
 */
public class Parser {

    private final List<Token> tokens;
    private final ErrorReporter errors;
    private int pos;

    private static final Set<TokenType> COMP_OPS = new HashSet<>(Arrays.asList(
            TokenType.EQUAL_EQUAL, TokenType.NOT_EQUAL,
            TokenType.LESS_THAN,   TokenType.GREATER_THAN,
            TokenType.LESS_EQUAL,  TokenType.GREATER_EQUAL));

    public Parser(List<Token> tokens, ErrorReporter errors) {
        this.tokens = tokens;
        this.errors = errors;
        this.pos = 0;
    }

    public Parser(List<Token> tokens) {
        this(tokens, new ErrorReporter());
    }

    private Token current() {
        return tokens.get(pos);
    }

    private Token advance() {
        Token t = tokens.get(pos);
        if (pos < tokens.size() - 1) pos++;
        return t;
    }

    private boolean check(TokenType type) {
        return current().type == type;
    }

    private Token expect(TokenType type) {
        if (check(type)) return advance();
        Token t = current();
        errors.add("Parser", t.line, "Expected " + type + " but found '" + t.lexeme + "' (" + t.type + ")");
        return t;
    }

    private void synchronize() {
        while (!check(TokenType.EOF) && !check(TokenType.SEMICOLON) && !check(TokenType.RIGHT_BRACE)) {
            advance();
        }
        if (check(TokenType.SEMICOLON)) advance();
    }

    public ASTNode.ProgramNode parseProgram() {
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.EOF)) {
            try {
                ASTNode stmt = parseStmt();
                if (stmt != null) stmts.add(stmt);
            } catch (Exception e) {
                errors.add("Parser", current().line, e.getMessage());
                synchronize();
            }
        }
        return new ASTNode.ProgramNode(stmts);
    }

    private List<ASTNode> parseBlock() {
        expect(TokenType.LEFT_BRACE);
        List<ASTNode> stmts = new ArrayList<>();
        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            try {
                ASTNode stmt = parseStmt();
                if (stmt != null) stmts.add(stmt);
            } catch (Exception e) {
                errors.add("Parser", current().line, e.getMessage());
                synchronize();
            }
        }
        expect(TokenType.RIGHT_BRACE);
        return stmts;
    }

    private ASTNode parseStmt() {
        Token t = current();
        switch (t.type) {
            case DHORO:       return parseDeclStmt();
            case IDENTIFIER:  return parseAssignStmt();
            case DEKHAAO:     return parsePrintStmt();
            case JODI:        return parseIfStmt();
            case JOKHON:      return parseWhileStmt();
            default:
                errors.add("Parser", t.line, "Unexpected statement starting with '" + t.lexeme + "' (" + t.type + ")");
                advance();
                return null;
        }
    }

    private ASTNode parseDeclStmt() {
        Token start = advance(); // consume ধরো / ধরি
        String declaredType = null;

        // Optional type annotation: সংখ্যা or বাক্য
        if (check(TokenType.SONGKHA)) {
            declaredType = "সংখ্যা";
            advance();
        } else if (check(TokenType.BAKKO)) {
            declaredType = "বাক্য";
            advance();
        }

        Token name = expect(TokenType.IDENTIFIER);
        expect(TokenType.ASSIGN);
        ASTNode expr = parseExpr();
        expect(TokenType.SEMICOLON);
        return new ASTNode.DeclNode(declaredType, name.lexeme, expr, start.line);
    }

    private ASTNode parseAssignStmt() {
        Token name = advance(); // consume IDENTIFIER
        expect(TokenType.ASSIGN);
        ASTNode expr = parseExpr();
        expect(TokenType.SEMICOLON);
        return new ASTNode.AssignNode(name.lexeme, expr, name.line);
    }

    private ASTNode parsePrintStmt() {
        Token start = advance(); // consume দেখাও
        boolean hasParen = check(TokenType.LEFT_PAREN);
        if (hasParen) advance();

        ASTNode expr = parseExpr();

        if (hasParen) expect(TokenType.RIGHT_PAREN);
        expect(TokenType.SEMICOLON);
        return new ASTNode.PrintNode(expr, start.line);
    }

    private ASTNode parseIfStmt() {
        Token start = advance(); // consume যদি
        expect(TokenType.LEFT_PAREN);
        ASTNode cond = parseExpr();
        expect(TokenType.RIGHT_PAREN);
        List<ASTNode> thenBranch = parseBlock();
        List<ASTNode> elseBranch = new ArrayList<>();
        if (check(TokenType.NAHOLE)) {
            advance(); // consume নাহলে
            elseBranch = parseBlock();
        }
        return new ASTNode.IfNode(cond, thenBranch, elseBranch, start.line);
    }

    private ASTNode parseWhileStmt() {
        Token start = advance(); // consume যখন
        expect(TokenType.LEFT_PAREN);
        ASTNode cond = parseExpr();
        expect(TokenType.RIGHT_PAREN);
        List<ASTNode> body = parseBlock();
        return new ASTNode.WhileNode(cond, body, start.line);
    }

    public ASTNode parseExpr() {
        return parseComparison();
    }

    private ASTNode parseComparison() {
        ASTNode left = parseAddition();
        while (COMP_OPS.contains(current().type)) {
            Token op = advance();
            ASTNode right = parseAddition();
            left = new ASTNode.BinOpNode(left, op.lexeme, right, op.line);
        }
        return left;
    }

    private ASTNode parseAddition() {
        ASTNode left = parseTerm();
        while (check(TokenType.PLUS) || check(TokenType.MINUS)) {
            Token op = advance();
            ASTNode right = parseTerm();
            left = new ASTNode.BinOpNode(left, op.lexeme, right, op.line);
        }
        return left;
    }

    private ASTNode parseTerm() {
        ASTNode left = parseFactor();
        while (check(TokenType.STAR) || check(TokenType.SLASH)) {
            Token op = advance();
            ASTNode right = parseFactor();
            left = new ASTNode.BinOpNode(left, op.lexeme, right, op.line);
        }
        return left;
    }

    private ASTNode parseFactor() {
        Token t = current();
        switch (t.type) {
            case NUMBER:
                advance();
                return new ASTNode.NumberNode(Integer.parseInt(t.lexeme), t.lexeme, t.line);
            case STRING:
                advance();
                return new ASTNode.StringNode(t.lexeme, t.line);
            case IDENTIFIER:
                advance();
                return new ASTNode.VarNode(t.lexeme, t.line);
            case LEFT_PAREN: {
                advance();
                ASTNode inner = parseExpr();
                expect(TokenType.RIGHT_PAREN);
                return inner;
            }
            case MINUS: {
                advance();
                ASTNode operand = parseFactor();
                return new ASTNode.BinOpNode(new ASTNode.NumberNode(0, "0", t.line), "-", operand, t.line);
            }
            default:
                errors.add("Parser", t.line, "Unexpected token '" + t.lexeme + "' in expression");
                advance();
                return new ASTNode.NumberNode(0, "0", t.line);
        }
    }
}
