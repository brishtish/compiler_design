// converts a raw UTF-8 source string into a flat list of token objects....

// Bangla keywords:  ধরো  দেখাও  যদি  নাহলে  যখন
// Integer literals: ASCII digits (42) OR Bengali digits (৪২)
// String literals:  "anything here"  with \" escape support
//(Bengali letters + combining vowel signs)
//   • Arithmetic:  + - * /
//   • Comparison:  == != < > <= >=
//   • Assignment:  =
//   • Delimiters:  ( ) { } ;
//   • Single-line comments:  // ...
//   • Graceful error: unknown chars are skipped with a warning

import java.util.*;

public class Lexer {

    private final String      source;
    private       int         pos;
    private       int         line;
    private final List<Token> tokens;

    // keyword 
    private static final Map<String, Token.Type> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("ধরো",   Token.Type.DHORAO);
        KEYWORDS.put("দেখাও", Token.Type.DEKHAAO);
        KEYWORDS.put("যদি",   Token.Type.JODI);
        KEYWORDS.put("নাহলে", Token.Type.NAHOLE);
        KEYWORDS.put("যখন",   Token.Type.JOKHON);
    }

    public Lexer(String source) {
        this.source = source;
        this.pos    = 0;
        this.line   = 1;
        this.tokens = new ArrayList<>();
    }

    //character helpers

    private char current() {
        return (pos < source.length()) ? source.charAt(pos) : '\0';
    }

    private char peek() {
        return (pos + 1 < source.length()) ? source.charAt(pos + 1) : '\0';
    }

    private char advance() {
        char ch = current();
        pos++;
        if (ch == '\n') line++;
        return ch;
    }

    //ASCII  0-9 
    private boolean isAsciiDigit(char ch) { return ch >= '0' && ch <= '9'; }

    //Bengali digit o (U+09E6) ..৯ (U+09EF) 
    private boolean isBengaliDigit(char ch) {
        return ch >= '\u09E6' && ch <= '\u09EF';
    }

    private int bengaliDigitValue(char ch) { return ch - '\u09E6'; }

    
     //Valid FIRST character of an identifier.
     // accepts Latin letters, Bengali consonants (Lo), and underscore.
     
    private boolean isIdentStart(char ch) {
        if (ch == '_') return true;
        int t = Character.getType(ch);
        return t == Character.UPPERCASE_LETTER       // Lu
            || t == Character.LOWERCASE_LETTER       // Ll
            || t == Character.TITLECASE_LETTER        // Lt
            || t == Character.MODIFIER_LETTER         // Lm
            || t == Character.OTHER_LETTER;           // Lo  
    }

    
     //Valid CONTINUATION character of an identifier.
     //Also accepts Bengali vowel signs (Mc) and hasanta (Mn) because
     //those are combining marks that form part of a Bengali word,
     //e.g.  ো (U+09CB Mc)  in  ধরো, দেখাও, নাহলে.
     
    private boolean isIdentPart(char ch) {
        if (isAsciiDigit(ch) || isBengaliDigit(ch)) return true;
        if (isIdentStart(ch)) return true;
        int t = Character.getType(ch);
        return t == Character.COMBINING_SPACING_MARK   // Mc (ো, া, ে, ...)
            || t == Character.NON_SPACING_MARK          // Mn (hasanta ্)
            || t == Character.ENCLOSING_MARK;           // Me
    }

    public List<Token> tokenize() {
        while (pos < source.length()) {
            char ch = current();

            // skip whitespace
            if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
                advance();
            }
            // single-line comment  // ...
            else if (ch == '/' && peek() == '/') {
                while (current() != '\n' && current() != '\0') advance();
            }
            // string literal
            else if (ch == '"') {
                tokens.add(readString());
            }
            // number literals 
            else if (isAsciiDigit(ch)) {
                tokens.add(readAsciiNumber());
            }
            else if (isBengaliDigit(ch)) {
                tokens.add(readBengaliNumber());
            }
            // identifier or keyword
            else if (isIdentStart(ch)) {
                tokens.add(readIdentOrKeyword());
            }
            // two-character operators (must check BEFORE single-char)
            else if (ch == '=' && peek() == '=') {
                tokens.add(new Token(Token.Type.EQEQ,  "==", line)); advance(); advance();
            }
            else if (ch == '!' && peek() == '=') {
                tokens.add(new Token(Token.Type.NEQ,   "!=", line)); advance(); advance();
            }
            else if (ch == '<' && peek() == '=') {
                tokens.add(new Token(Token.Type.LTE,   "<=", line)); advance(); advance();
            }
            else if (ch == '>' && peek() == '=') {
                tokens.add(new Token(Token.Type.GTE,   ">=", line)); advance(); advance();
            }
            // single-character operators and delimiters
            else if (ch == '+') { tokens.add(new Token(Token.Type.PLUS,      "+", line)); advance(); }
            else if (ch == '-') { tokens.add(new Token(Token.Type.MINUS,     "-", line)); advance(); }
            else if (ch == '*') { tokens.add(new Token(Token.Type.STAR,      "*", line)); advance(); }
            else if (ch == '/') { tokens.add(new Token(Token.Type.SLASH,     "/", line)); advance(); }
            else if (ch == '=') { tokens.add(new Token(Token.Type.EQUALS,    "=", line)); advance(); }
            else if (ch == '<') { tokens.add(new Token(Token.Type.LT,        "<", line)); advance(); }
            else if (ch == '>') { tokens.add(new Token(Token.Type.GT,        ">", line)); advance(); }
            else if (ch == '(') { tokens.add(new Token(Token.Type.LPAREN,    "(", line)); advance(); }
            else if (ch == ')') { tokens.add(new Token(Token.Type.RPAREN,    ")", line)); advance(); }
            else if (ch == '{') { tokens.add(new Token(Token.Type.LBRACE,    "{", line)); advance(); }
            else if (ch == '}') { tokens.add(new Token(Token.Type.RBRACE,    "}", line)); advance(); }
            else if (ch == ';') { tokens.add(new Token(Token.Type.SEMICOLON, ";", line)); advance(); }
            else {
                // Graceful error recovery: skip and warn (no crash)
                System.err.println("[Lexer] Unknown character '" + ch
                        + "' (U+" + Integer.toHexString(ch).toUpperCase()
                        + ") on line " + line + " — skipped.");
                advance();
            }
        }

        tokens.add(new Token(Token.Type.EOF, "", line));
        return tokens;
    }

    //individual token readers
    private Token readString() {
        int startLine = line;
        advance();  // consume opening "
        StringBuilder sb = new StringBuilder();
        while (current() != '"' && current() != '\0') {
            if (current() == '\\') {
                advance(); // consume backslash
                switch (current()) {
                    case '"':  sb.append('"');  advance(); break;
                    case 'n':  sb.append('\n'); advance(); break;
                    case 't':  sb.append('\t'); advance(); break;
                    case '\\': sb.append('\\'); advance(); break;
                    default:   sb.append(advance()); break;
                }
            } else {
                sb.append(advance());
            }
        }
        if (current() == '"') {
            advance(); // consume closing "
        } else {
            System.err.println("[Lexer] Unterminated string literal on line " + startLine);
        }
        return new Token(Token.Type.STRING, sb.toString(), startLine);
    }

    private Token readAsciiNumber() {
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (isAsciiDigit(current())) sb.append(advance());
        return new Token(Token.Type.NUMBER, sb.toString(), startLine);
    }

    private Token readBengaliNumber() {
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (isBengaliDigit(current())) {
            sb.append(bengaliDigitValue(advance())); // convert to ASCII digit value
        }
        return new Token(Token.Type.NUMBER, sb.toString(), startLine);
    }

    private Token readIdentOrKeyword() {
        int startLine = line;
        StringBuilder sb = new StringBuilder();
        while (isIdentPart(current())) sb.append(advance());
        String word = sb.toString();
        Token.Type type = KEYWORDS.getOrDefault(word, Token.Type.IDENT);
        return new Token(type, word, startLine);
    }
}
