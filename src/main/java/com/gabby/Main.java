package com.gabby;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.parser.Parser;
import org.luaj.vm2.Globals;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.List;

public class Main {
    private final static String code = """
    print("Starting!")
    long start = time()
    var sum = 0
    for (i to 100000) {
        for (j to 100) {
            sum += (i * j) / math.max(i - j + 1, 1)
        }
    }
    long end = time()
    print("Sum: " + sum)
    print("Orbit Execution took " + (end - start) + " milliseconds")
    """;

    public static void main(String[] args) throws ParsingException, InterruptedException {
        System.out.println("\nRunning Orbit...\n");
        List<String> tokens = new Lexer(code).tokenize();

        GlobalContext context = new GlobalContext();

        ASTNode ast = Parser.parse(tokens, context);

        String luaCode = """
            local math = math
                    local os = os
                
                    local A = 0.0
                      
        """;

        Globals globals = JsePlatform.standardGlobals();
        LuaValue chunk = globals.load(luaCode);

        for(int i = 0; i < 1; i++) {
            long startTime = System.currentTimeMillis();
            //context = new GlobalContext();
            ast.evaluate(context);
            long endTime = System.currentTimeMillis();
            System.out.println("Orbit Execution " + (i + 1) + " took " + (endTime - startTime) + " milliseconds");

        }
    }
}