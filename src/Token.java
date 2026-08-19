
// kind of token that can appear in a BanglaScript source file.
// lexer produces Token objects; the Parser consumes them.

public class Token {

    public enum Type {

        //Literals
        NUMBER,     // integer  ৪২
        STRING,     // string  নাম
        IDENT,      // variable / identifier name

        //Arithmetic operators
        PLUS,       // +
        MINUS,      // -
        STAR,       // *
        SLASH,      // /

        //Assignment
        EQUALS,     // =

        // Comparison operators
        EQEQ,       // ==
        NEQ,        // !=
        LT,         // <
        GT,         // >
        LTE,        // <=
        GTE,        // >=

        //Delimiters
        LPAREN,     // (
        RPAREN,     // )
        LBRACE,     // {
        RBRACE,     // }
        SEMICOLON,  // ;

        //Bangla Keywords
        DHORAO,     // ধরো   — declare a variable
        DEKHAAO,    // দেখাও  — print
        JODI,       // যদি   — if
        NAHOLE,     // নাহলে — else
        JOKHON,     // যখন  — while

        
        EOF         // end of file
    }

    public final Type   type;    // what kind of token this is
    public final String lexeme;  // exact text from the source file
    public final int    line;    // line number (for error messages)

    public Token(Type type, String lexeme, int line) {
        this.type   = type;
        this.lexeme = lexeme;
        this.line   = line;
    }

    @Override
    public String toString() {
        return String.format("Token(%-12s  %-22s  line=%d)",
                type, "\"" + lexeme + "\"", line);
    }
}
