package dexlang;

public enum TokenType {
    // Single-character tokens
    PLUS, MINUS, STAR, SLASH,
    ASSIGN, SEMICOLON,
    LPAREN, RPAREN,
    LBRACE, RBRACE,

    // Comparison
    EQUAL_EQUAL, LESS, GREATER,

    // Literals
    IDENT, NUMBER,

    // Keywords
    PRINT, IF, ELSE, WHILE, INT,

    EOF
}
