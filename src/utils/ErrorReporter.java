package utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Clean, emoji-free error collector and reporter.
 */
public class ErrorReporter {

    public static class ErrorItem {
        public final String phase;
        public final int line;
        public final String message;

        public ErrorItem(String phase, int line, String message) {
            this.phase = phase;
            this.line = line;
            this.message = message;
        }

        @Override
        public String toString() {
            if (line > 0) {
                return String.format("[%s Error] Line %d: %s", phase, line, message);
            }
            return String.format("[%s Error] %s", phase, message);
        }
    }

    private final List<ErrorItem> errors = new ArrayList<>();

    public void add(String phase, int line, String message) {
        errors.add(new ErrorItem(phase, line, message));
    }

    public void add(String phase, String message) {
        errors.add(new ErrorItem(phase, 0, message));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int count() {
        return errors.size();
    }

    public List<ErrorItem> getErrors() {
        return errors;
    }

    public void clear() {
        errors.clear();
    }

    public void printSummary(String phaseName) {
        System.out.println();
        if (errors.isEmpty()) {
            System.out.println(phaseName + " Status: OK");
        } else {
            System.out.println(phaseName + " Status: FAILED (" + errors.size() + " error(s))");
            for (ErrorItem err : errors) {
                System.out.println("  " + err);
            }
        }
    }
}
