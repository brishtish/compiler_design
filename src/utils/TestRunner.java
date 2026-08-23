package utils;

import ast.ASTNode;
import lexer.Lexer;
import lexer.Token;
import parser.Parser;
import semantic.SemanticAnalyzer;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

 
public class TestRunner {

    public static void main(String[] args) {
        String dir = (args.length > 0) ? args[0] : "tests";
        new TestRunner().runAll(dir);
    }

    public void runAll(String testDirPath) {
        File dir = new File(testDirPath);
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("[ERROR] Test directory not found: " + testDirPath);
            return;
        }

        List<File> testFiles = new ArrayList<>();
        collectFiles(dir, testFiles);
        Collections.sort(testFiles, (a, b) -> a.getPath().compareTo(b.getPath()));

        System.out.println();
        System.out.println("============================================================");
        System.out.println("                   TEST CASES                     ");
        System.out.println("============================================================");

        int passed = 0;
        int failed = 0;
        int testIndex = 1;

        for (File f : testFiles) {
            String path = f.getPath().replace("\\", "/");
            boolean expectSuccess = !path.contains("error");

            System.out.println();
            System.out.println("------------------------------------------------------------");
            System.out.printf("TEST %d: %s%n", testIndex++, path);
            System.out.println("------------------------------------------------------------");

            String source = "";
            try {
                source = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            } catch (Exception e) {
                System.out.println("Could not read file: " + e.getMessage());
            }

            printSourceWithLineNumbers(source);

            boolean actualSuccess = runPipelineOn(source);
            boolean isTestPass = (expectSuccess == actualSuccess);

            if (isTestPass) passed++;
            else failed++;

            System.out.println("------------------------------------------------------------");
            System.out.println("Expected : " + (expectSuccess ? "SUCCESS" : "ERROR"));
            System.out.println("Actual   : " + (actualSuccess ? "SUCCESS" : "ERROR"));
            System.out.println("Status   : " + (isTestPass ? "PASSED" : "FAILED"));
            System.out.println("------------------------------------------------------------");
        }

        System.out.println();
        System.out.println("============================================================");
        System.out.printf("TESTS SUMMARY: Total = %d | Passed = %d | Failed = %d%n",
                testFiles.size(), passed, failed);
        System.out.println("============================================================");
    }

    private void printSourceWithLineNumbers(String source) {
        System.out.println("SOURCE CODE:");
        String[] lines = source.split("\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.printf("%2d | %s%n", (i + 1), lines[i].replace("\r", ""));
        }
    }


    private boolean runPipelineOn(String source) {
        try {
            ErrorReporter errors = new ErrorReporter();

            Lexer lexer = new Lexer(source, errors);
            List<Token> tokens = lexer.tokenize();
            if (errors.hasErrors()) return false;

            Parser parser = new Parser(tokens, errors);
            ASTNode.ProgramNode program = parser.parseProgram();
            if (errors.hasErrors()) return false;

            SemanticAnalyzer analyzer = new SemanticAnalyzer(errors);
            analyzer.analyze(program);
            return !errors.hasErrors();

        } catch (Exception e) {
            return false;
        }
    }

    private void collectFiles(File dir, List<File> list) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectFiles(f, list);
            } else if (f.getName().endsWith(".bangla")) {
                list.add(f);
            }
        }
    }
}