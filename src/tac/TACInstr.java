package tac; // Java syntax: Declares this file belongs to the "tac" package folder.

// Java syntax: Abstract base class representing a single Three-Address Code (TAC) instruction.
// Project context: Every flat TAC instruction (like t0 = 5 * 2, x = t1, print z) inherits from TACInstr.
public abstract class TACInstr {

    // Java syntax: Subclass for binary operation TAC instructions (e.g., dest = left op right).
    // Project context: Represents instructions like t0 = x + 5 or t1 = a * b.
    public static class TACBinOp extends TACInstr {
        public final String dest;  // Java syntax: Destination variable or temporary name (e.g. "t0").
        public final String left;  // Java syntax: Left operand string (e.g. "x" or "10").
        public final String op;    // Java syntax: Operator string (e.g. "+", "-", "*", "/").
        public final String right; // Java syntax: Right operand string (e.g. "t1" or "5").

        // Java syntax: Constructor initializing all fields of a binary operation instruction.
        public TACBinOp(String dest, String left, String op, String right) {
            this.dest = dest;   // Java syntax: Stores destination temp name.
            this.left = left;   // Java syntax: Stores left operand.
            this.op = op;       // Java syntax: Stores operator string.
            this.right = right; // Java syntax: Stores right operand.
        }

        // Java syntax: Overrides toString() to format instruction as "dest = left op right".
        @Override
        public String toString() {
            return String.format("%s = %s %s %s", dest, left, op, right);
        }
    }

    // Java syntax: Subclass for copy/assignment TAC instructions (e.g., dest = src).
    // Project context: Used to copy constants into temporaries (t0 = 42) or temporaries into variables (x = t0).
    public static class TACCopy extends TACInstr {
        public final String dest; // Java syntax: Destination variable or temp name.
        public final String src;  // Java syntax: Source variable, temp, or number literal.

        // Java syntax: Constructor initializing copy instruction fields.
        public TACCopy(String dest, String src) {
            this.dest = dest; // Java syntax: Stores destination name.
            this.src = src;   // Java syntax: Stores source value string.
        }

        // Java syntax: Overrides toString() to format instruction as "dest = src".
        @Override
        public String toString() {
            return String.format("%s = %s", dest, src);
        }
    }

    // Java syntax: Subclass for print statement TAC instructions (e.g., print src).
    // Project context: Translates BanglaScript 'দেখাও' statements into flat TAC print commands.
    public static class TACPrint extends TACInstr {
        public final String src; // Java syntax: Name of variable or temp whose value will be printed.

        // Java syntax: Constructor initializing print instruction fields.
        public TACPrint(String src) {
            this.src = src; // Java syntax: Stores target value name.
        }

        // Java syntax: Overrides toString() to format instruction as "print src".
        @Override
        public String toString() {
            return String.format("print %s", src);
        }
    }

    // Java syntax: Subclass for branch target label instructions (e.g., L1:).
    // Project context: Marks jump targets for conditional branch instructions in 'if' and 'while' blocks.
    public static class TACLabel extends TACInstr {
        public final String label; // Java syntax: Label identifier string (e.g. "L1", "L2").

        // Java syntax: Constructor initializing label instruction fields.
        public TACLabel(String label) {
            this.label = label; // Java syntax: Stores label name.
        }

        // Java syntax: Overrides toString() to format instruction as "label:".
        @Override
        public String toString() {
            return String.format("%s:", label);
        }
    }

    // Java syntax: Subclass for unconditional jump instructions (e.g., goto L1).
    // Project context: Forces execution flow to jump directly to a label.
    public static class TACJump extends TACInstr {
        public final String target; // Java syntax: Target label name to jump to.

        // Java syntax: Constructor initializing jump target.
        public TACJump(String target) {
            this.target = target; // Java syntax: Stores target label string.
        }

        // Java syntax: Overrides toString() to format instruction as "goto target".
        @Override
        public String toString() {
            return String.format("goto %s", target);
        }
    }

    // Java syntax: Subclass for conditional jump instructions (e.g., ifFalse condition goto label).
    // Project context: Jumps to target label if condition evaluates to false (0 / false).
    public static class TACJumpIfFalse extends TACInstr {
        public final String condition; // Java syntax: Temporary or variable holding condition result.
        public final String target;    // Java syntax: Target label to jump to if false.

        // Java syntax: Constructor initializing conditional jump fields.
        public TACJumpIfFalse(String condition, String target) {
            this.condition = condition; // Java syntax: Stores condition name.
            this.target = target;       // Java syntax: Stores target label.
        }

        // Java syntax: Overrides toString() to format instruction as "ifFalse condition goto target".
        @Override
        public String toString() {
            return String.format("ifFalse %s goto %s", condition, target);
        }
    }
}
