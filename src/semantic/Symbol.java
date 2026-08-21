package semantic;

/**
 * Symbol representation in the Symbol Table.
 **/
public class Symbol {
    public final String name;
    public final String type; // "সংখ্যা" or "বাক্য"
    public final boolean initialized;
    public final int lineDefined;

    public Symbol(String name, String type, boolean initialized, int lineDefined) {
        this.name = name;
        this.type = type;
        this.initialized = initialized;
        this.lineDefined = lineDefined;
    }
}
