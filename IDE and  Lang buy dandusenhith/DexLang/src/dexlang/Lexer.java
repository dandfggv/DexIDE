package dexlang;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Lexer(String source) {
        this.source = source;
    }

    public List<Token> lex() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '+': add(TokenType.PLUS); break;
            case '-': add(TokenType.MINUS); break;
            case '*': add(TokenType.STAR); break;
            case '/': add(TokenType.SLASH); break;
            case '=':
                if (match('=')) add(TokenType.EQUAL_EQUAL);
                else add(TokenType.ASSIGN);
                break;
            case ';': add(TokenType.SEMICOLON); break;
            case '(': add(TokenType.LPAREN); break;
            case ')': add(TokenType.RPAREN); break;
            case '{': add(TokenType.LBRACE); break;
            case '}': add(TokenType.RBRACE); break;
            case '<': add(TokenType.LESS); break;
            case '>': add(TokenType.GREATER); break;
            case ' ':
            case '\r':
            case '\t':
                // ignore whitespace
                break;
            case '\n':
                line++;
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    throw new RuntimeException("Unexpected character '" + c + "' at line " + line);
                }
        }
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private void add(TokenType type) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, line));
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void number() {
        while (!isAtEnd() && isDigit(peek())) advance();
        add(TokenType.NUMBER);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private void identifier() {
        while (!isAtEnd() && isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = switch (text) {
            case "print" -> TokenType.PRINT;
            case "if" -> TokenType.IF;
            case "else" -> TokenType.ELSE;
            case "while" -> TokenType.WHILE;
            case "int" -> TokenType.INT;
            default -> TokenType.IDENT;
        };
        tokens.add(new Token(type, text, line));
    }
}
