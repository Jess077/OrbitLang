package com.softlocked.orbit.opm.project;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.core.exception.ParsingException;
import com.softlocked.orbit.interpreter.ast.operation.OperationASTNode;
import com.softlocked.orbit.interpreter.ast.value.ValueASTNode;
import com.softlocked.orbit.interpreter.ast.value.VariableASTNode;
import com.softlocked.orbit.interpreter.ast.variable.FunctionCallASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.lexer.Lexer;
import com.softlocked.orbit.parser.Parser;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class OrbitREPL {
    private static final String[] startingBrackets = {"{", "(", "[", "then", "do", "does"};
    private static final String[] endingBrackets = {"}", ")", "]", "end", "fend"};

    private static boolean debug = false;
    public static void main(String[] args) throws ParsingException, URISyntaxException {
        if (args.length > 0) {
            if(Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("--debug") || arg.equalsIgnoreCase("-d"))) {
                debug = true;
            }
            if(Arrays.stream(args).anyMatch(arg -> arg.equalsIgnoreCase("--file") || arg.equalsIgnoreCase("-f"))) {
                int fileIndex = Stream.of(args).toList().indexOf("--file");
                if(fileIndex == -1) {
                    fileIndex = Stream.of(args).toList().indexOf("-f");
                }
                File jarDir = new File(OrbitREPL.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();

                GlobalContext context = new GlobalContext();

                context.setProjectPath(jarDir.getAbsolutePath());
                if(fileIndex != -1 && fileIndex + 1 < args.length) {
                    String filePath = args[fileIndex + 1];
                    // check if the path is relative or absolute
                    if(!new File(filePath).isAbsolute()) {
                        filePath = jarDir.getAbsolutePath() + File.separator + filePath;
                    }
                    File file = new File(filePath);
                    if(file.exists() && file.isFile()) {
                        try {
                            List<String> tokens = new Lexer(java.nio.file.Files.readString(file.toPath())).tokenize();
                            ASTNode ast = Parser.parse(tokens, context);
                            ast.evaluate(context);
                        } catch (Exception e) {
                            System.out.println("Error executing file: " + e.getMessage());
                            if(debug) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        System.out.println("File not found: " + filePath);
                    }
                } else {
                    System.out.println("No file path provided after --file or -f");
                }
                System.exit(0);
            }
        }



        Scanner scanner = new Scanner(System.in);
        System.out.println("Running Orbit REPL. Type 'exit' to quit.");

        GlobalContext context = new GlobalContext();
        Lexer lexer = new Lexer("");

        File jarDir = new File(OrbitREPL.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParentFile();

        context.setProjectPath(jarDir.getAbsolutePath());

        int bracketBalance = 0;
        boolean lastTokenWasOperator = false;

        List<String> tokens = new ArrayList<>();
        while (true) {
            if (bracketBalance == 0 && !lastTokenWasOperator) System.out.print(">> ");
            if(lastTokenWasOperator) lastTokenWasOperator = false;
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            lexer.reset(input);

            List<String> tempTokens = lexer.tokenize();

            for (String token : tempTokens) {
                for (String start : startingBrackets) {
                    if (token.equals(start)) {
                        bracketBalance++;
                    }
                }
                for (String end : endingBrackets) {
                    if (token.equals(end)) {
                        bracketBalance--;
                    }
                }
            }

            tokens.addAll(tempTokens);

            if (bracketBalance > 0) {
                System.out.print(".. "); // Indicate more input is expected
                continue;
            } else if (bracketBalance < 0) {
                System.out.println("Syntax Error: Unmatched closing bracket.");
                // Reset state
                bracketBalance = 0;
                tokens.clear();
                continue;
            }

            // if last token is an operator, expect more input
            if (!tokens.isEmpty()) {
                String lastToken = tokens.getLast();
                if (lastToken.equals("+") || lastToken.equals("-") || lastToken.equals("*") || lastToken.equals("/") ||
                    lastToken.equals("&&") || lastToken.equals("||") || lastToken.equals("=") || lastToken.equals(",") ||
                    lastToken.equals(".") || lastToken.equals("!") || lastToken.equals("<") || lastToken.equals(">") ||
                    lastToken.equals("<=") || lastToken.equals(">=") || lastToken.equals("==") || lastToken.equals("!=") ||
                    lastToken.equals(":")
                ) {
                    System.out.print(".. "); // Indicate more input is expected
                    lastTokenWasOperator = true;
                    continue;
                }
            }

            ASTNode ast = Parser.parse(tokens, context);

            tokens.clear(); // Clear tokens for the next input

            try {
                if(ast instanceof OperationASTNode || ast instanceof VariableASTNode || ast instanceof ValueASTNode || ast instanceof FunctionCallASTNode) {
                    Object result = ast.evaluate(context);
                    if(result != null) {
                        System.out.println(result);
                    }
                } else {
                    ast.evaluate(context);
                }
            } catch (InterruptedException e) {
                System.out.println("Execution interrupted: " + e.getMessage());

                if (debug) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                System.out.println("Error during execution: " + e.getMessage());

                if (debug) {
                    e.printStackTrace();
                }
            }
        }
    }
}
