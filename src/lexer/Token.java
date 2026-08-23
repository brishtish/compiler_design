package lexer;


public class Token {

    public final TokenType type;
    public final String lexeme;
    public final int line;
    public final int col;

    public Token(TokenType type, String lexeme, int line, int col) {
        this.type = type;
        this.lexeme = lexeme;
        this.line = line;
        this.col = col;
    }

    public Token(TokenType type, String lexeme, int line) {
        this(type, lexeme, line, 1);
    }

    @Override
    public String toString() {
        return String.format("%-16s : '%s'%s(Line %d, col %d)",
                type,
                lexeme,
                " ".repeat(Math.max(1, 16 - lexeme.length())),
                line,
                col);
    }
}
