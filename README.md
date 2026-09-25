# BanglaScript Compiler

### A Multi-Target Bengali Programming Language Compiler

BanglaScript is an end-to-end compiler developed in Java for a custom Bengali programming language. The compiler translates native Bengali source programs into structured Abstract Syntax Trees (AST), Three-Address Code (TAC) intermediate representations, and multiple target backends including Python and WebAssembly Text Format (.wat).

---

## Project Overview

BanglaScript replaces traditional English keywords with intuitive Bengali programming constructs. Instead of writing constructs like `int`, `if`, and `while`, developers write in native script:

```text
ধরি সংখ্যা বয়স = ২০;
যদি (বয়স >= ১৮) {
    দেখাও("প্রাপ্তবয়স্ক");
}
```

The compiler processes source code through a sequential multi-phase architecture:

```text
Bangla Source Code
        |
        v
 Lexical Analysis (UTF-8 Tokenizer)
        |
        v
 Syntax Analysis (Recursive Descent Parser)
        |
        v
 Abstract Syntax Tree (AST)
        |
        v
 Semantic Analysis (Scoped Symbol Table & Type Checking)
        |
        +-----------------------------------+
        |                                   |
        v                                   v
Three-Address Code (TAC)             Target Code Generation
(Intermediate Representation)        +----------------------+
                                     | Python (.py)         |
                                     | WebAssembly (.wat)   |
                                     +----------------------+
```

---

## Objectives

The core objectives of the BanglaScript compiler project are:

* Build a multi-phase, end-to-end compiler from scratch in Java without external parser generators.
* Support native Bengali keywords, identifiers, string literals, and Bengali numerals (০-৯).
* Implement lexical analysis with Unicode normalization and character tracking (line and column).
* Construct an Abstract Syntax Tree (AST) using an operator-precedence recursive-descent parser.
* Implement static semantic analysis with scoped symbol resolution, type validation, and constant folding.
* Detect compile-time errors such as undeclared variables, type mismatches, and division-by-zero.
* Generate Three-Address Code (TAC) with synthetic temporaries and jump labels.
* Transpile validated AST into executable Python source code.
* Generate WebAssembly Text Format (.wat) using stack-machine operations, structured blocks, and loops.
* Provide an interactive Dual-Menu Command Line Interface (CLI) and an automated test suite.

---

## Language Specifications and Grammar

### Keywords

| Bengali Keyword | English Equivalent | Purpose                      |
|-----------------|--------------------|------------------------------|
| ধরি / সংখ্যা      | let / int          | Variable declaration (integer) |
| বাক্য            | string             | Variable declaration (string)  |
| দেখাও           | print              | Output display to console      |
| যদি             | if                 | Conditional branch             |
| নইলে / নাহলে    | else               | Alternative conditional branch |
| যখন / যতক্ষণ    | while              | Iterative loop construct       |

### Operators and Delimiters

* Arithmetic: `+`, `-`, `*`, `/`
* Comparison: `==`, `!=`, `<`, `>`, `<=`, `>=`
* Assignment: `=`
* Delimiters: `;`, `(`, `)`, `{`, `}`

### Number Normalization

BanglaScript natively accepts both standard ASCII digits (`0-9`) and Bengali digits (`০-৯`). Bengali numerals are normalized internally during lexical scanning so that arithmetic operations remain consistent.

---

## Project Structure

```text
BanglaCompiler/
├── run.bat                     # Windows CLI launcher script
├── run.sh                      # Unix/Linux/macOS launcher script
├── sources.txt                 # Compilation manifest
├── README.md                   # Project documentation
│
├── src/
│   ├── Main.java               # Dual-menu driver and execution pipeline
│   │
│   ├── lexer/
│   │   ├── Lexer.java          # UTF-8 lexical scanner
│   │   ├── Token.java          # Token data model
│   │   └── TokenType.java      # Token enumerations
│   │
│   ├── parser/
│   │   ├── Parser.java         # Recursive-descent parser with error recovery
│   │   └── ASTPrinter.java     # Visual hierarchy formatter
│   │
│   ├── ast/
│   │   └── ASTNode.java        # AST node class hierarchy
│   │
│   ├── semantic/
│   │   ├── SemanticAnalyzer.java # Type checker and static analyzer
│   │   ├── SymbolTable.java    # Scoped symbol table implementation
│   │   └── Symbol.java         # Symbol table entry model
│   │
│   ├── tac/
│   │   ├── TACGenerator.java   # AST to Three-Address Code transformer
│   │   └── TACInstr.java       # TAC instruction hierarchy
│   │
│   ├── codegen/
│   │   ├── CodeGenerator.java     # Python target code generator
│   │   └── WasmCodeGenerator.java # WebAssembly (.wat) code generator
│   │
│   └── utils/
│       ├── BanglaUtil.java     # Unicode digit conversion utilities
│       ├── ErrorReporter.java  # Formatted compiler diagnostic logger
│       └── TestRunner.java     # Automated test suite runner
│
├── tests/
│   ├── valid/                  # Valid syntactic and semantic test cases
│   └── errors/                 # Expected error test cases (syntax and semantic)
│
└── output/
    ├── program.tac             # Generated Three-Address Code
    ├── program.py              # Generated Python target script
    └── program.wat             # Generated WebAssembly text module
```

---

## Compiler Phases

### 1. Lexical Analysis

The Lexer reads raw UTF-8 Bengali source input and emits a sequential stream of tokens. It tracks:

* Token type category
* Lexeme value
* Exact line and column coordinates for compiler error diagnostics

Example:

```text
ধরি সংখ্যা বয়স = ২০;
```

Produces:

```text
DHORI       : 'ধরি'       (Line 1, Col 1)
SONGKHA     : 'সংখ্যা'    (Line 1, Col 6)
IDENTIFIER  : 'বয়স'      (Line 1, Col 13)
ASSIGN      : '='         (Line 1, Col 18)
NUMBER      : '20'        (Line 1, Col 20)
SEMICOLON   : ';'         (Line 1, Col 22)
```

### 2. Syntax Analysis & AST Construction

The Parser enforces grammar rules using recursive-descent parsing. It incorporates operator precedence (multiplication and division bind tighter than addition and subtraction) and constructs an Abstract Syntax Tree (AST).

Example expression:

```text
১০ + ৫ * ২
```

Constructs the following AST structure:

```text
       +
      / \
    10   *
        / \
       5   2
```

The parser also features semicolon synchronization recovery, allowing it to log a missing delimiter and continue parsing subsequent statements without crashing.

### 3. Semantic Analysis & Symbol Table

The Semantic Analyzer traverses the AST to verify program validity:

* Scope Verification: Ensures identifiers are declared before reference.
* Duplicate Detection: Prevents conflicting variable declarations within the same scope.
* Type Consistency: Validates that arithmetic is not performed between strings and numbers.
* Division-by-Zero Detection: Statically detects constant division by zero during compilation.
* Scoped Symbol Table: Maintains variable names, types, initialization states, and scope depths.

### 4. Intermediate Representation: Three-Address Code (TAC)

The compiler flattens hierarchical AST expressions into a linearized Three-Address Code format. Each instruction contains at most one operator, utilizing synthetic temporaries (`t0`, `t1`, etc.) and branch labels (`L1`, `L2`, etc.).

Example output:

```text
[BinOp]       t0 = ক + খ
[Copy]        গ = t0
[Print]       print গ
```

### 5. Python Target Code Generation

The Python generator walks the AST to output clean, idiomatic Python code with proper 4-space block indentation:

* Variable declarations translate to Python assignments.
* Conditional structures translate to `if:`, `elif:`, and `else:` blocks.
* Loops translate to standard `while:` loops.
* Output commands translate to `print()` calls.

### 6. WebAssembly Text Format (.wat) Generation

The WebAssembly backend maps high-level AST constructs into 32-bit stack-machine instructions:

* Variables are declared as local 32-bit integers: `(local $var_x i32)`.
* Expressions push values onto the stack (`i32.const`, `local.get`) and pop them via arithmetic instructions (`i32.add`, `i32.sub`, `i32.mul`, `i32.div_s`).
* Conditionals are mapped to WebAssembly `if / else / end` blocks.
* Iterations are implemented using structured `block`, `loop`, `br_if`, and `br` constructs.
* Output calls invoke imported runtime helpers (`$print_i32`).

---

## Compiler Phase Implementation Matrix

| Phase                   | Component              | Implementation Status |
|-------------------------|------------------------|-----------------------|
| Lexical Analysis        | Lexer.java             | Completed             |
| Syntax Analysis         | Parser.java            | Completed             |
| AST Construction        | ASTNode.java           | Completed             |
| Semantic Analysis       | SemanticAnalyzer.java  | Completed             |
| Scoped Symbol Table     | SymbolTable.java       | Completed             |
| Operator Precedence     | Parser.java            | Completed             |
| Error Diagnostics       | ErrorReporter.java     | Completed             |
| Intermediate Code (TAC) | TACGenerator.java      | Completed             |
| Target Code: Python     | CodeGenerator.java     | Completed             |
| Target Code: WebAssembly| WasmCodeGenerator.java | Completed             |
| Full Pipeline CLI       | Main.java              | Completed             |
| Automated Test Suite    | TestRunner.java        | Completed             |

---

## Error Handling

The compiler provides detailed diagnostics with source coordinates and descriptive messages:

* Syntax Errors: Missing semicolons, unmatched parentheses, unexpected tokens.
* Semantic Errors:
  * Undeclared identifier usage.
  * Incompatible operand types (e.g. attempting to subtract text from numbers).
  * Division by zero in constant expressions.
  * Redeclaration of variables within the same scope.

---

## How to Compile and Run

### Prerequisites

* Java Development Kit (JDK 11 or higher)
* Terminal supporting UTF-8 character encoding

### Compilation

From the root project directory:

```bash
# Set terminal encoding to UTF-8 (Windows)
chcp 65001

# Compile all source files into the out directory
javac -encoding UTF-8 -d out src/Main.java src/lexer/*.java src/parser/*.java src/ast/*.java src/semantic/*.java src/tac/*.java src/codegen/*.java src/utils/*.java
```

### Running the Compiler

Launch the interactive CLI:

```bash
java -Dfile.encoding=UTF-8 -cp out Main
```

Alternatively, on Windows systems, execute the automated batch launcher:

```cmd
.\run.bat
```

---

## Interactive Menu

Upon launching, the compiler presents an interactive console interface:

```text
============================================================
              BANGLASCRIPT COMPILER MAIN MENU
============================================================
  1. Run Lexical Analysis (Tokens)
  2. Run Syntax Analysis (AST)
  3. Run Semantic Analysis (Symbol Table)
  4. Run Three-Address Code (TAC)
  5. Run Python Code Generation
  6. Run WebAssembly Code Generation
  7. Run Full Pipeline (End-to-End Execution)
  8. Run Automated Test Suite
  9. Custom Source Code Submenu
  10. Change Source File
  11. Exit
============================================================
```

Selecting Option 7 sequentially triggers all 6 compiler phases, generating target files in the `output/` directory and displaying step-by-step pipeline output in the terminal.

---

## Automated Testing

The project includes an automated test harness in `TestRunner.java`. It validates standard benchmark programs against:

* Arithmetic expressions and precedence
* String declarations and concatenation
* Conditional branching logic (`if-else`)
* Iterative loops (`while`)
* Semantic error detection (type mismatch, scope resolution, division by zero)

To run the test suite, select Option 8 from the main menu, or compile and execute the runner directly:

```bash
java -Dfile.encoding=UTF-8 -cp out utils.TestRunner
```

---

## Academic Context

* Course: Compiler Design and Construction (CSE-4114)
* Department: Department of Computer Science and Engineering
* Institution: Leading University
