package lexer;

import utils.ErrorReporter;
import java.util.*;


public class Lexer {

    private final String source;
    private final ErrorReporter errors;
    private int pos;
    private int line;
    private int col;
    private final List<Token> tokens;

    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("ধরো",   TokenType.DHORO);
        KEYWORDS.put("ধরি",   TokenType.DHORO);
        KEYWORDS.put("সংখ্যা", TokenType.SONGKHA);
        KEYWORDS.put("বাক্য",  TokenType.BAKKO);
        KEYWORDS.put("দেখাও", TokenType.DEKHAAO);
        KEYWORDS.put("যদি",   TokenType.JODI);
        KEYWORDS.put("নাহলে", TokenType.NAHOLE);
        KEYWORDS.put("যখন",   TokenType.JOKHON);
    }

    public Lexer(String source, ErrorReporter errors) {
        this.source = source;
        this.errors = errors;
        this.pos = 0;
        this.line = 1;
        this.col = 1;
        this.tokens = new ArrayList<>();
    }

    public Lexer(String source) {
        this(source, new ErrorReporter());
    }

    private char current() {
        return (pos < source.length()) ? source.charAt(pos) : '\0';
    }

    private char peek() {
        return (pos + 1 < source.length()) ? source.charAt(pos + 1) : '\0';
    }

    private char advance() {
        char ch = current();
        pos++;
        if (ch == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        return ch;
    }

    private boolean isAsciiDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isBengaliDigit(char ch) {
        return ch >= '\u09E6' && ch <= '\u09EF';
    }

    private int bengaliDigitValue(char ch) {
        return ch - '\u09E6';
    }

    private boolean isIdentStart(char ch) {
        if (ch == '_') return true;
        int t = Character.getType(ch);
        return t == Character.UPPERCASE_LETTER
            || t == Character.LOWERCASE_LETTER
            || t == Character.TITLECASE_LETTER
            || t == Character.MODIFIER_LETTER
            || t == Character.OTHER_LETTER;
    }

    private boolean isIdentPart(char ch) {
        if (isAsciiDigit(ch) || isBengaliDigit(ch) || isIdentStart(ch)) return true;
        int t = Character.getType(ch);
        return t == Character.COMBINING_SPACING_MARK
            || t == Character.NON_SPACING_MARK
            || t == Character.ENCLOSING_MARK;
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            char ch = current();
            int startCol = col;

            // Skip whitespace
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                advance();
            }
            // Comments: // ...
            else if (ch == '/' && peek() == '/') {
                while (current() != '\n' && current() != '\0') advance();
            }
            // Strings: "..."
            else if (ch == '"') {
                tokens.add(readString(startCol));
            }
            // Numbers
            else if (isAsciiDigit(ch)) {
                tokens.add(readAsciiNumber(startCol));
            }
            else if (isBengaliDigit(ch)) {
                tokens.add(readBengaliNumber(startCol));
            }
            // Identifiers / Keywords
            else if (isIdentStart(ch)) {
                tokens.add(readIdentOrKeyword(startCol));
            }
            // Two-character operators
            else if (ch == '=' && peek() == '=') {
                tokens.add(new Token(TokenType.EQUAL_EQUAL, "==", line, startCol));
                advance(); advance();
            }
            else if (ch == '!' && peek() == '=') {
                tokens.add(new Token(TokenType.NOT_EQUAL, "!=", line, startCol));
                advance(); advance();
            }
            else if (ch == '<' && peek() == '=') {
                tokens.add(new Token(TokenType.LESS_EQUAL, "<=", line, startCol));
                advance(); advance();
            }
            else if (ch == '>' && peek() == '=') {
                tokens.add(new Token(TokenType.GREATER_EQUAL, ">=", line, startCol));
                advance(); advance();
            }
            // Single-character operators and delimiters
            else if (ch == '+') { tokens.add(new Token(TokenType.PLUS,          "+", line, startCol)); advance(); }
            else if (ch == '-') { tokens.add(new Token(TokenType.MINUS,         "-", line, startCol)); advance(); }
            else if (ch == '*') { tokens.add(new Token(TokenType.STAR,          "*", line, startCol)); advance(); }
            else if (ch == '/') { tokens.add(new Token(TokenType.SLASH,         "/", line, startCol)); advance(); }
            else if (ch == '=') { tokens.add(new Token(TokenType.ASSIGN,        "=", line, startCol)); advance(); }
            else if (ch == '<') { tokens.add(new Token(TokenType.LESS_THAN,     "<", line, startCol)); advance(); }
            else if (ch == '>') { tokens.add(new Token(TokenType.GREATER_THAN,  ">", line, startCol)); advance(); }
            else if (ch == '(') { tokens.add(new Token(TokenType.LEFT_PAREN,    "(", line, startCol)); advance(); }
            else if (ch == ')') { tokens.add(new Token(TokenType.RIGHT_PAREN,   ")", line, startCol)); advance(); }
            else if (ch == '{') { tokens.add(new Token(TokenType.LEFT_BRACE,    "{", line, startCol)); advance(); }
            else if (ch == '}') { tokens.add(new Token(TokenType.RIGHT_BRACE,   "}", line, startCol)); advance(); }
            else if (ch == ';') { tokens.add(new Token(TokenType.SEMICOLON,     ";", line, startCol)); advance(); }
            else {
                errors.add("Lexer", line, "Unknown character '" + ch + "' (U+" + Integer.toHexString(ch).toUpperCase() + ")");
                advance();
            }
        }

        tokens.add(new Token(TokenType.EOF, "", line, col));
        return tokens;
    }

    private Token readString(int startCol) {
        int startLine = line;
        advance(); // opening "
        StringBuilder sb = new StringBuilder();
        while (current() != '"' && current() != '\0') {
            if (current() == '\\') {
                advance();
                switch (current()) {
                    case '"': sb.append('"'); advance(); break;
                    case 'n': sb.append('\n'); advance(); break;
                    case 't': sb.append('\t'); advance(); break;
                    case '\\': sb.append('\\'); advance(); break;
                    default: sb.append(advance()); break;
                }
            } else {
                sb.append(advance());
            }
        }
        if (current() == '"') {
            advance(); // closing "
        } else {
            errors.add("Lexer", startLine, "Unterminated string literal");
        }
        return new Token(TokenType.STRING, sb.toString(), startLine, startCol);
    }

    private Token readAsciiNumber(int startCol) {
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (isAsciiDigit(current())) sb.append(advance());
        return new Token(TokenType.NUMBER, sb.toString(), startLine, startCol);
    }

    private Token readBengaliNumber(int startCol) {
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (isBengaliDigit(current())) {
            sb.append(bengaliDigitValue(advance()));
        }
        return new Token(TokenType.NUMBER, sb.toString(), startLine, startCol);
    }

    private Token readIdentOrKeyword(int startCol) {
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (isIdentPart(current())) sb.append(advance());
        String word = sb.toString();
        TokenType type = KEYWORDS.getOrDefault(word, TokenType.IDENTIFIER);
        return new Token(type, word, startLine, startCol);
    }
}
