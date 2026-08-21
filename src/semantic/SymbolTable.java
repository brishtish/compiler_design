package semantic;

import java.util.*;

/**
 * Scoped Symbol Table implementation
 **/
public class SymbolTable {

    private final LinkedList<Map<String, Symbol>> scopes = new LinkedList<>();
    private final Map<String, Symbol> allRecordedSymbols = new LinkedHashMap<>();

    public SymbolTable() {
        enterScope();
    }

    public void enterScope() {
        scopes.addFirst(new LinkedHashMap<>());
    }

    public void exitScope() {
        if (!scopes.isEmpty()) {
            scopes.removeFirst();
        }
    }

    public boolean declare(String name, String type, boolean initialized, int line) {
        Map<String, Symbol> currentScope = scopes.getFirst();
        if (currentScope.containsKey(name)) {
            return false;
        }
        Symbol sym = new Symbol(name, type, initialized, line);
        currentScope.put(name, sym);
        allRecordedSymbols.put(name, sym);
        return true;
    }

    public Symbol lookup(String name) {
        for (Map<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    public Map<String, Symbol> getAllSymbols() {
        return allRecordedSymbols;
    }

    public void printTable() {
        System.out.println("Symbol Table:");
        System.out.printf("%-16s | %-12s%n", "Name", "Type");
        System.out.println("-----------------+-------------");
        if (allRecordedSymbols.isEmpty()) {
            System.out.printf("%-16s | %-12s%n", "(none)", "-");
        } else {
            for (Symbol sym : allRecordedSymbols.values()) {
                System.out.printf("%-16s | %-12s%n", sym.name, sym.type);
            }
        }
        System.out.println();
    }
}
