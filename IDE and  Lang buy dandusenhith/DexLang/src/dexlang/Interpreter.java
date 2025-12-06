package dexlang;

import dexlang.Ast.*;

import java.util.HashMap;
import java.util.Map;

public class Interpreter {
    private final Map<String, Integer> env = new HashMap<>();
    private final StringBuilder output = new StringBuilder();

    public String interpret(Block program) {
        try {
            executeBlock(program);
        } catch (RuntimeException e) {
            output.append("Runtime error: ").append(e.getMessage()).append("\n");
        }
        return output.toString();
    }

    private void executeBlock(Block block) {
        for (Stmt s : block.statements) {
            execute(s);
        }
    }

    private void execute(Stmt stmt) {
        if (stmt instanceof Block b) executeBlock(b);
        else if (stmt instanceof Var v) executeVar(v);
        else if (stmt instanceof ExprStmt e) eval(e.expr);
        else if (stmt instanceof If i) executeIf(i);
        else if (stmt instanceof While w) executeWhile(w);
        else throw new RuntimeException("Unknown statement type");
    }

    private void executeVar(Var v) {
        int value = 0;
        if (v.initializer != null) {
            value = eval(v.initializer);
        }
        env.put(v.name, value);
    }

    private void executeIf(If i) {
        int cond = eval(i.condition);
        if (cond != 0) {
            execute(i.thenBranch);
        } else if (i.elseBranch != null) {
            execute(i.elseBranch);
        }
    }

    private void executeWhile(While w) {
        while (eval(w.condition) != 0) {
            execute(w.body);
        }
    }

    private int eval(Expr expr) {
        if (expr instanceof Literal l) return l.value;
        if (expr instanceof VarExpr v) {
            if (!env.containsKey(v.name)) throw new RuntimeException("Undefined variable " + v.name);
            return env.get(v.name);
        }
        if (expr instanceof Assign a) {
            int value = eval(a.value);
            if (!env.containsKey(a.name)) throw new RuntimeException("Undefined variable " + a.name);
            env.put(a.name, value);
            return value;
        }
        if (expr instanceof Binary b) {
            int left = eval(b.left);
            int right = eval(b.right);
            return switch (b.op) {
                case PLUS -> left + right;
                case MINUS -> left - right;
                case STAR -> left * right;
                case SLASH -> right == 0 ? 0 : left / right;
                case EQUAL_EQUAL -> left == right ? 1 : 0;
                case LESS -> left < right ? 1 : 0;
                case GREATER -> left > right ? 1 : 0;
                default -> throw new RuntimeException("Unknown binary operator");
            };
        }
        if (expr instanceof CallPrint p) {
            int value = eval(p.argument);
            output.append(value).append("\n");
            return value;
        }
        throw new RuntimeException("Unknown expression type");
    }
}
