package dexlang;

import dexlang.Ast.*;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Block parse() {
        List<Stmt> stmts = new ArrayList<>();
        while (!isAtEnd()) {
            stmts.add(declaration());
        }
        return new Block(stmts);
    }

    private Stmt declaration() {
        if (match(TokenType.INT)) {
            Token name = consume(TokenType.IDENT, "Expected variable name.");
            Expr init = null;
            if (match(TokenType.ASSIGN)) {
                init = expression();
            }
            consume(TokenType.SEMICOLON, "Expected ';' after variable declaration.");
            return new Var(name.lexeme, init);
        }
        return statement();
    }

    private Stmt statement() {
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.LBRACE)) return blockStatement();
        return exprStatement();
    }

    private Stmt blockStatement() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RBRACE, "Expected '}' after block.");
        return new Ast.Block(statements);
    }

    private Stmt ifStatement() {
        consume(TokenType.LPAREN, "Expected '(' after 'if'.");
        Expr cond = expression();
        consume(TokenType.RPAREN, "Expected ')' after condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }
        return new If(cond, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(TokenType.LPAREN, "Expected '(' after 'while'.");
        Expr cond = expression();
        consume(TokenType.RPAREN, "Expected ')' after condition.");
        Stmt body = statement();
        return new While(cond, body);
    }

    private Stmt exprStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression.");
        return new ExprStmt(expr);
    }

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();
        while (match(TokenType.EQUAL_EQUAL)) {
            TokenType op = previous().type;
            Expr right = comparison();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(TokenType.LESS, TokenType.GREATER)) {
            TokenType op = previous().type;
            Expr right = term();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            TokenType op = previous().type;
            Expr right = factor();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(TokenType.STAR, TokenType.SLASH)) {
            TokenType op = previous().type;
            Expr right = unary();
            expr = new Binary(expr, op, right);
        }
        return expr;
    }

    private Expr unary() {
        return primary();
    }

    private Expr primary() {
        if (match(TokenType.NUMBER)) {
            int value = Integer.parseInt(previous().lexeme);
            return new Literal(value);
        }

        if (match(TokenType.PRINT)) {
            consume(TokenType.LPAREN, "Expected '(' after print.");
            Expr arg = expression();
            consume(TokenType.RPAREN, "Expected ')' after argument.");
            return new CallPrint(arg);
        }

        if (match(TokenType.IDENT)) {
            Token name = previous();
            if (match(TokenType.ASSIGN)) {
                Expr value = expression();
                return new Assign(name.lexeme, value);
            }
            return new VarExpr(name.lexeme);
        }

        if (match(TokenType.LPAREN)) {
            Expr expr = expression();
            consume(TokenType.RPAREN, "Expected ')' after expression.");
            return expr;
        }

        throw error(peek(), "Expected expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType t : types) {
            if (check(t)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private RuntimeException error(Token token, String message) {
        return new RuntimeException("Parse error at line " + token.line + ": " + message);
    }
}
