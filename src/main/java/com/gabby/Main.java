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
    // donut
    int k;
    double A = 0; double B = 0; double i; double j;
    list z = [];
    list b = [];
    string chars = ".,-~:;=!*#$@";

    long time = time();
    
    for (gay = 0 to 400) {
        z = [];
        for (oz = 0 to 1760) {
           z[oz] = 0;
           b[oz] = ' ';
        }
        for (j in countingRange(0, 6.28, 0.07)) {
            for (i in countingRange(0, 6.28, 0.02)) {
                var c = math.sin(i);
                var d = math.cos(j);
                var e = math.sin(A);
                var f = math.sin(j);
                var g = math.cos(A);
                var h = d + 2;
                var D = 1 / (c * h * e + f * g + 5);
                var l = math.cos(i);
                var m = math.cos(B);
                var n = math.sin(B);
                var t = c * h * g - f * e;
                int x = (40 + 30 * D * (l * h * m - t * n));
                int y = (12 + 15 * D * (l * h * n + t * m));
                int N = (8 * ((f * e - c * d * g) * m - c * d * e - f * g - l * d * n));
                if (y < 22 && y >= 0 && x >= 0 && x < 80) {
                    int o = x + 80 * y;
                    if (D > z[o]) {
                        z[o] = D;
                        b[o] = chars[math.max(N, 0)]
                    }
                }
            }
        }
        for (k = 0 to 1760) {
        }
        A += 0.04;
        B += 0.02;
    }
    print("Took " + (time() - time) + " milliseconds for 400 frames");
    print("Average: " + ((time() - time) / 400) + " milliseconds per frame");
    """;

    public static void main(String[] args) throws ParsingException, InterruptedException {
        System.out.println("\nRunning Orbit...\n");
        List<String> tokens = new Lexer(code).tokenize();

        GlobalContext context = new GlobalContext();

        ASTNode ast = Parser.parse(tokens, context);

        String luaCode = """
            local start = os.clock() * 1000
            print("Hello World from LuaJ!")
            local sum = 0
            for i = 0, 10000 do
                for j = 0, 1000 do
                    sum = sum + i + j
                end
            end
            print("Sum: " .. sum)
            print("Took " .. (os.clock() * 1000 - start) .. " milliseconds")
        """;

        Globals globals = JsePlatform.standardGlobals();
        LuaValue chunk = globals.load(luaCode);

        for(int i = 0; i < 1; i++) {
            long startTime = System.currentTimeMillis();
            //context = new GlobalContext();
            ast.evaluate(context);
            long endTime = System.currentTimeMillis();
            System.out.println("Orbit Execution " + (i + 1) + " took " + (endTime - startTime) + " milliseconds");

//            startTime = System.currentTimeMillis();
//
//            chunk.call();
//            endTime = System.currentTimeMillis();
//            System.out.println("LuaJ Execution " + (i + 1) + " took " + (endTime - startTime) + " milliseconds\n");
        }
    }
}