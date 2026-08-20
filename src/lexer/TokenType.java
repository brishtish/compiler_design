package lexer;

/**
 * All token types supported in BanglaScript.
 */
public enum TokenType {
    // ── Keywords ───────────────────────────────────────────────
    DHORO,          // ধরো / ধরি
    SONGKHA,        // সংখ্যা (Integer type keyword)
    BAKKO,          // বাক্য (String type keyword)
    DEKHAAO,        // দেখাও (print)
    JODI,           // যদি (if)
    NAHOLE,         // নাহলে (else)
    JOKHON,         // যখন (while)

    // ── Literals ───────────────────────────────────────────────
    NUMBER,         // 42 or ৪২
    STRING,         // "টেক্সট"
    IDENTIFIER,     // Variable or function name

    // ── Arithmetic Operators ───────────────────────────────────
    PLUS,           // +
    MINUS,          // -
    STAR,           // *
    SLASH,          // /

    // ── Assignment & Comparisons ───────────────────────────────
    ASSIGN,         // =
    EQUAL_EQUAL,    // ==
    NOT_EQUAL,      // !=
    LESS_THAN,      // <
    GREATER_THAN,   // >
    LESS_EQUAL,     // <=
    GREATER_EQUAL,  // >=

    // ── Delimiters ────────────────────────────────────────────
    LEFT_PAREN,     // (
    RIGHT_PAREN,    // )
    LEFT_BRACE,     // {
    RIGHT_BRACE,    // }
    SEMICOLON,      // ;

    // ── Special ───────────────────────────────────────────────
    EOF             // End of File
}
