package dexlang;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DexMain {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java -cp out dexlang.DexMain <file.dex>");
            return;
        }

        String source = Files.readString(Path.of(args[0]));
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.lex();
        Parser parser = new Parser(tokens);
        Ast.Block program = parser.parse();
        Interpreter interpreter = new Interpreter();
        String output = interpreter.interpret(program);
        System.out.print(output);
    }
}
