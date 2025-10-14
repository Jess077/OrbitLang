package com.softlocked.orbit.parser.parsers;

import com.softlocked.orbit.core.ast.ASTNode;
import com.softlocked.orbit.interpreter.memory.GlobalContext;
import com.softlocked.orbit.parser.TokenParser;
import com.softlocked.orbit.parser.TokenStream;
import com.softlocked.orbit.opm.ast.pkg.ImportFileASTNode;
import com.softlocked.orbit.opm.ast.pkg.ImportModuleASTNode;

import java.util.function.Predicate;

public class ImportParser implements TokenParser {
    @Override
    public Predicate<String> predicate() {
        return s -> s.equals("import");
    }

    @Override
    public ASTNode parse(TokenStream tokens, GlobalContext context) {
        tokens.expect("import");

        String next = tokens.next();
        if (next == null) {
            throw new RuntimeException("Unexpected end of file after import");
        }

        if (next.equals("module")) {
            String moduleName = tokens.next();
            if (moduleName == null) {
                throw new RuntimeException("Expected module name after 'import module'");
            }

            // Remove quotes if present
            if (moduleName.startsWith("\"") && moduleName.endsWith("\"")) {
                moduleName = moduleName.substring(1, moduleName.length() - 1);
            }

            tokens.consumeSemicolon();
            return new ImportModuleASTNode(moduleName);
        } else {
            // Import file
            String fileName = next;

            // Remove quotes if present
            if (fileName.startsWith("\"") && fileName.endsWith("\"")) {
                fileName = fileName.substring(1, fileName.length() - 1);
            }

            tokens.consumeSemicolon();
            return new ImportFileASTNode(fileName);
        }
    }
}