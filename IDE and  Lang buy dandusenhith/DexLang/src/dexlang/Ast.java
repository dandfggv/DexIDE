package dexlang;

import java.util.List;

public class Ast {
    public interface Stmt {}

    public static class Block implements Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> statements) { this.statements = statements; }
    }

    public static class Var implements Stmt {
        public final String name;
        public final Expr initializer;
        public Var(String name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }
    }

    public static class ExprStmt implements Stmt {
        public final Expr expr;
        public ExprStmt(Expr expr) { this.expr = expr; }
    }

    public static class If implements Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }
    }

    public static class While implements Stmt {
        public final Expr condition;
        public final Stmt body;
        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }
    }

    public interface Expr {}

    public static class Binary implements Expr {
        public final Expr left;
        public final TokenType op;
        public final Expr right;
        public Binary(Expr left, TokenType op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }
    }

    public static class Literal implements Expr {
        public final int value;
        public Literal(int value) { this.value = value; }
    }

    public static class VarExpr implements Expr {
        public final String name;
        public VarExpr(String name) { this.name = name; }
    }

    public static class Assign implements Expr {
        public final String name;
        public final Expr value;
        public Assign(String name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class CallPrint implements Expr {
        public final Expr argument;
        public CallPrint(Expr argument) { this.argument = argument; }
    }
}
