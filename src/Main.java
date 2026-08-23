import ast.ASTNode;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.ASTPrinter;
import parser.Parser;
import semantic.SemanticAnalyzer;
import utils.ErrorReporter;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Main Controller for Bangla Compiler.
 */
public class Main {

    private static final String LINE = "------------------------------------------------------------";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
        String demoPath = "tests/demo.bangla";
        String demoSource = loadFile(demoPath);

        while (true) {
            printHeader("BANGLA COMPILER");
            System.out.println("1. Show demo Source Code");
            System.out.println("2. Run Lexer");
            System.out.println("3. Run Parser");
            System.out.println("4. Run Semantic Analyzer");
            System.out.println("5. Exit");
            System.out.println();
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    showSourceCode(demoSource);
                    break;
                case "2":
                    runLexer(demoSource);
                    break;
                case "3":
                    runParser(demoSource);
                    break;
                case "4":
                    runSemantic(demoSource);
                    break;
                case "5":
                    System.out.println("\nExiting compiler. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please choose 1-5.");
            }
        }
    }

    private static void printHeader(String title) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.printf("║%s║%n", centerText(title, 42));
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();
    }

    private static String centerText(String text, int width) {
        int pad = (width - text.length()) / 2;
        int rightPad = width - text.length() - pad;
        return " ".repeat(Math.max(0, pad)) + text + " ".repeat(Math.max(0, rightPad));
    }

    private static void showSourceCode(String source) {
        System.out.println();
        System.out.println(LINE);
        System.out.println("SOURCE CODE");
        System.out.println(LINE);
        String[] lines = source.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.printf("%2d | %s%n", (i + 1), lines[i].replace("\r", ""));
        }
        System.out.println(LINE);
    }

    private static void runLexer(String source) {
        System.out.println();
        ErrorReporter errors = new ErrorReporter();
        Lexer lexer = new Lexer(source, errors);
        List<Token> tokens = lexer.tokenize();

        for (Token t : tokens) {
            if (t.type != TokenType.EOF) {
                System.out.println(t);
            }
        }

        System.out.println();
        int count = (tokens.size() > 0 && tokens.get(tokens.size() - 1).type == TokenType.EOF) ? tokens.size() - 1 : tokens.size();
        System.out.println("Total tokens: " + count);
        errors.printSummary("Lexer");
    }

    private static void runParser(String source) {
        showSourceCode(source);

        ErrorReporter errors = new ErrorReporter();
        Lexer lexer = new Lexer(source, errors);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens, errors);
        ASTNode.ProgramNode ast = parser.parseProgram();

        System.out.println();
        System.out.println("PARSER OUTPUT - ABSTRACT SYNTAX TREE");
        System.out.println(LINE);
        new ASTPrinter().print(ast);
        errors.printSummary("Parser");
    }

    private static void runSemantic(String source) {
        showSourceCode(source);

        ErrorReporter errors = new ErrorReporter();
        Lexer lexer = new Lexer(source, errors);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens, errors);
        ASTNode.ProgramNode ast = parser.parseProgram();

        SemanticAnalyzer semantic = new SemanticAnalyzer(errors);
        semantic.analyze(ast);

        System.out.println();
        System.out.println("SEMANTIC ANALYSIS");
        System.out.println(LINE);
        semantic.getSymbolTable().printTable();

        if (!errors.hasErrors()) {
            System.out.println("Semantic analysis completed successfully.");
        } else {
            errors.printSummary("Semantic Analysis");
        }
    }

    private static String loadFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "ধরি সংখ্যা বয়স = ১৮;\nদেখাও(বয়স);";
        }
    }
}