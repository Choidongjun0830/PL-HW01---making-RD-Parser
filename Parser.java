import java.util.*;

public class Parser {

    private enum TokenCode {
        INT_LIT, IDENT, ASSIGN_OP, ADD_OP, SUB_OP, MULT_OP, DIV_OP, LEFT_PAREN, RIGHT_PAREN, EQ_OP, NEQ_OP, LT_OP, GT_OP, LTE_OP, GTE_OP, PRINT, SEMI_COLON, EOF
    }

    private static final Map<String, TokenCode> keywords;
    private static final Map<String, Integer> variables = new HashMap<>();
    private static final List<Integer> printQueue = new ArrayList<>();
    private static TokenCode nextToken;
    private static String currentChar;
    private static Queue<String> tokens = new LinkedList<>();

    static {
        keywords = new HashMap<>();
        keywords.put("print", TokenCode.PRINT);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("terminate")) {
                break;
            }

            tokenize(input);
            System.out.println(tokens);
            program();

            printQueue.forEach(System.out::println);
            printQueue.clear();
            variables.clear();
        }

        scanner.close();
    }

    private static void tokenize(String input) { //ok
        String[] splitInput = input.split("\\s+");
        Collections.addAll(tokens, splitInput);
    }

    private static void lex() {
        if (tokens.isEmpty()) {
            nextToken = TokenCode.EOF;
            return;
        }

        currentChar = tokens.poll();
        try {
            nextToken = keywords.getOrDefault(currentChar, TokenCode.valueOf(currentChar.toUpperCase()));
        } catch (IllegalArgumentException e) {
            if (Character.isDigit(currentChar.charAt(0))) {
                nextToken = TokenCode.INT_LIT;
            } else if (Character.isLetter(currentChar.charAt(0))) {
                nextToken = TokenCode.IDENT;
            } else {
                System.out.println("Syntax Error");
                System.exit(1);
            }
        }
    }

    private static void program() {
        lex();
        while (nextToken != TokenCode.EOF) {
            statement();
        }
    }

    // Statement -> print IDENT ; | IDENT = Expr ;
    private static void statement() {
        if (nextToken == TokenCode.PRINT) {
            lex();  // Consume 'print'
            if (nextToken != TokenCode.IDENT)
                throw new RuntimeException("Syntax Error: Expected identifier after print");
            String varName = currentChar;
            lex();  // Consume identifier
            if (nextToken != TokenCode.SEMI_COLON)
                throw new RuntimeException("Syntax Error: Expected semicolon after identifier");
            System.out.println(varName + " = " + variables.getOrDefault(varName, 0));
        } else if (nextToken == TokenCode.IDENT) {
            String varName = currentChar;
            lex();  // Consume identifier
            if (nextToken != TokenCode.ASSIGN_OP)
                throw new RuntimeException("Syntax Error: Expected assignment operator after identifier");
            lex();  // Consume '='
            int value = expr();
            variables.put(varName, value);
            if (nextToken != TokenCode.SEMI_COLON)
                throw new RuntimeException("Syntax Error: Expected semicolon after expression");
        } else {
            throw new RuntimeException("Syntax Error: Expected statement");
        }
    }

    // Expr -> Term { ( + | - ) Term }
    private static int expr() {
        int value = term();
        while (nextToken == TokenCode.ADD_OP || nextToken == TokenCode.SUB_OP) {
            TokenCode op = nextToken;
            lex();  // Consume '+' or '-'
            int nextValue = term();
            value = applyOp(value, nextValue, op);
        }
        return value;
    }

    // Term -> Factor { ( * | / ) Factor }
    private static int term() {
        int value = factor();
        while (nextToken == TokenCode.MULT_OP || nextToken == TokenCode.DIV_OP) {
            TokenCode op = nextToken;
            lex();  // Consume '*' or '/'
            int nextValue = factor();
            value = applyOp(value, nextValue, op);
        }
        return value;
    }

    // Factor -> INT_LIT | IDENT
    private static int factor() {
        int value;
        if (nextToken == TokenCode.INT_LIT) {
            value = Integer.parseInt(currentChar);
            lex();  // Consume literal
        } else if (nextToken == TokenCode.IDENT) {
            value = variables.getOrDefault(currentChar, 0);
            lex();  // Consume identifier
        } else {
            throw new RuntimeException("Syntax Error: Expected factor");
        }
        return value;
    }

    // Apply the operation
    private static int applyOp(int left, int right, TokenCode op) {
        switch (op) {
            case ADD_OP: return left + right;
            case SUB_OP: return left - right;
            case MULT_OP: return left * right;
            case DIV_OP: return left / right;
            default: throw new IllegalArgumentException("Unknown operator");
        }
    }
}
