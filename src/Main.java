import ast.ASTNode;
import lexer.Lexer;
import lexer.Token;
import lexer.TokenType;
import parser.ASTPrinter;
import parser.Parser;
import semantic.SemanticAnalyzer;
import tac.TACGenerator;
import tac.TACInstr;
import utils.ErrorReporter;
import utils.TestRunner;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;


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
            System.out.println("5. Run Three-Address Code Generator (TAC / IR)");
            System.out.println("6. Generate Python Target Code");
            System.out.println("7. Generate WebAssembly Target Code (.wat)");
            System.out.println("8. Run Full Pipeline");
            System.out.println("9. Write Bangla Code");
            System.out.println("10. Test cases");
            System.out.println("11. Exit");
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
                    runTAC(demoSource);
                    break;
                case "6":
                    //runPythonCode(demoSource);
                    break;
                case "7":
                    //runWasmCode(demoSource);
                    break;
                case "8":
                    //runFullPipeline(demoSource);
                    break;
                case "9":
                    handleWriteBanglaCode(scanner);
                    break;
                case "10":
                    new TestRunner().runAll("tests");
                    break;
                case "11":
                    System.out.println();
                    System.out.println("Exiting compiler. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please choose 1-11.");
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

    private static void handleWriteBanglaCode(Scanner scanner) {
        printHeader("YOUR BANGLA CODE");
        System.out.println("Sample syntax:");
        System.out.println("  ধরি সংখ্যা বয়স = ২২;");
        System.out.println("  ধরি বাক্য নাম = \"ঐশী\";");
        System.out.println("  দেখাও(নাম);");
        System.out.println("  যদি (বয়স >= ১৮) {");
        System.out.println("      দেখাও(\"প্রাপ্তবয়স্ক\");");
        System.out.println("  }");
        System.out.println();
        System.out.println("Type your Bangla code below (With or Without Declaring Variable Type). Type 'done' on a new line when done:");
        System.out.println(LINE);

        StringBuilder sb = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if ("done".equalsIgnoreCase(line.trim())) {
                break;
            }
            sb.append(line).append("\n");
        }

        String userSource = sb.toString().trim();
        if (userSource.isEmpty()) {
            System.out.println("No code entered. Returning to Main Menu.");
            return;
        }

        while (true) {
            printHeader("YOUR BANGLA CODE");
           System.out.println("1. Show Source Code");
            System.out.println("2. Run Lexer");
            System.out.println("3. Run Parser");
            System.out.println("4. Run Semantic Analyzer");
            System.out.println("5. Run Three-Address Code Generator (TAC)");
            System.out.println("6. Generate Python Target Code");
            System.out.println("7. Generate WebAssembly Target Code (.wat)");
            System.out.println("8. Run Full Pipeline");
            System.out.println("9. Back to Main Menu");
            System.out.println();
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();

           switch (choice) {
                case "1":
                    showSourceCode(userSource);
                    break;
                case "2":
                    runLexer(userSource);
                    break;
                case "3":
                    runParser(userSource);
                    break;
                case "4":
                    runSemantic(userSource);
                    break;
                case "5":
                    runTAC(userSource);
                    break;
                case "6":
                    //runPythonCode(userSource);
                    break;
                case "7":
                    //runWasmCode(userSource);
                    break;
                case "8":
                    //runFullPipeline(userSource);
                    break;
                case "9":
                    return;
                default:
                    System.out.println("Invalid choice. Please choose 1-5.");
            }
        }
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
    private static boolean runTAC(String source) {
        showSourceCode(source);

        ErrorReporter errors = new ErrorReporter();
        Lexer lexer = new Lexer(source, errors);
        List<Token> tokens = lexer.tokenize();

        Parser parser = new Parser(tokens, errors);
        ASTNode.ProgramNode ast = parser.parseProgram();

        SemanticAnalyzer semantic = new SemanticAnalyzer(errors);
        boolean ok = semantic.analyze(ast);

        if (!ok || errors.hasErrors()) {
            System.out.println("\nCannot generate Three Address Code — semantic errors found:");
            errors.printSummary("Semantic Analysis");
            return false;
        }

        System.out.println();
        System.out.println("THREE ADDRESS CODE (TAC)");
        System.out.println(LINE);
        System.out.printf("%-4s  %-12s  %s%n", "নং", "ধরন", "নির্দেশ");
        System.out.println("------------------------------------------------------------");

        TACGenerator tacGen = new TACGenerator();
        List<TACInstr> instrs = tacGen.generate(ast);

        for (int i = 0; i < instrs.size(); i++) {
            TACInstr instr = instrs.get(i);
            String kind = "[" + instr.getClass().getSimpleName().replace("TAC", "") + "]";
            System.out.printf("%-4d  %-12s  %s%n", i, kind, instr.toString());
        }

        System.out.println("------------------------------------------------------------");
        String outputPath = "output/program.tac";
        System.out.println("Generated: " + outputPath);
        System.out.println("TAC Status: OK");
        return true;
    }

    private static String loadFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "ধরি সংখ্যা বয়স = ১৮;\nদেখাও(বয়স);";
        }
    }
}