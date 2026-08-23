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

        int passed = 0;
        int failed = 0;

        for (File f : testFiles) {
            String path = f.getPath().replace("\\", "/");
            boolean expectSuccess = !path.contains("error");

            String source = "";
            try {
                source = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
            } catch (Exception e) {
                System.out.println("Could not read file: " + e.getMessage());
            }

            boolean actualSuccess = runPipelineOn(source);
            boolean isTestPass = (expectSuccess == actualSuccess);

            if (isTestPass) passed++;
            else failed++;

            System.out.printf("Test: %-35s | Status: %s%n", path, (isTestPass ? "PASSED" : "FAILED"));
        }

        System.out.printf("%nSummary: Total = %d | Passed = %d | Failed = %d%n",
                testFiles.size(), passed, failed);
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