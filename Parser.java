import java.util.*;

public class Parser {

    /* Global declarations */
    /* Variables */
    static int charClass;
    static String currentChar;
    static String nextChar;
    static TokenType nextToken;
    static boolean errorFlag;

    //Character classes
    static final int LETTER = 0;
    static final int DIGIT = 1;
    static final int UNKNOWN = 99;

    //Token codes
    private enum TokenType {
        INT_LIT, IDENT, ASSIGN_OP, ADD_OP, SUB_OP, MULT_OP, DIV_OP,
        LEFT_PAREN, RIGHT_PAREN, EQ_OP, NEQ_OP, LT_OP, GT_OP, LTE_OP, GTE_OP, PRINT, SEMI_COLON, EOF
    }

    private static final Map<String, Integer> resultMap = new HashMap<>();
    private static final List<Integer> printQueue = new ArrayList<>();
    private static List<String> tokens = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();
            if (input.equals("terminate")) {
                break;
            }

            tokenize(input);
            program();
            if(errorFlag == true){
                System.out.println("syntax error!!");
            }
            if(!printQueue.isEmpty()) {
                printQueue.forEach(System.out::println);
            }
            printQueue.clear();
            resultMap.clear();
        }
        scanner.close();
    }

    //입력 배열 split()하는 함수
    private static void tokenize(String input) { //ok
        String[] splitInput = input.split("\\s+");
        Collections.addAll(tokens, splitInput);
    }

    private static void lex() {
        if (tokens.isEmpty()) {
            nextToken = TokenType.EOF;
            return;
        }
        //첫번째 토큰 가져오기
        String currentToken;
        currentToken = tokens.remove(0);
        System.out.println(currentChar);

        if(currentToken.chars().allMatch(Character::isLetter)) {
            if (currentToken.equals("print")) {
                nextToken = TokenType.PRINT;
            } else {
                nextToken = TokenType.IDENT;
            }
            currentChar = currentToken;
        } else if (currentToken.chars().allMatch(Character::isDigit)) {
            nextToken = TokenType.INT_LIT;
            currentChar = currentToken;
        } else {
            lookup(currentToken);
        }
    }

    private static void lookup(String currentToken) {
        switch (currentToken) {
            case "(", ")":
                errorFlag = true; break;
            case "+":
                nextToken = TokenType.ADD_OP; break;
            case "-":
                nextToken = TokenType.SUB_OP; break;
            case "*":
                nextToken = TokenType.MULT_OP; break;
            case "/":
                nextToken = TokenType.DIV_OP; break;
            case "<":
                nextToken = TokenType.LT_OP; break;
            case "<=":
                nextToken = TokenType.LTE_OP; break;
            case ">":
                nextToken = TokenType.GT_OP; break;
            case ">=":
                nextToken = TokenType.GTE_OP; break;
            case "=":
                nextToken = TokenType.ASSIGN_OP; break;
            case "==":
                nextToken = TokenType.EQ_OP; break;
            case "!=":
                nextToken = TokenType.NEQ_OP; break;
            case ";":
                nextToken = TokenType.SEMI_COLON; break;
            default:
                nextToken = TokenType.EOF;
                error();
                break;
        }
    }

    private static void error() {
        errorFlag = true;
    }

    private static void program() {
        if(tokens.isEmpty()) {
            return;
        }
        lex();
        while (nextToken != TokenType.EOF && !errorFlag) {
            statement();
            if (tokens.isEmpty()) {
                nextToken = TokenType.EOF;
            }
        }
        if(!tokens.isEmpty()) {
            error();
        }
        if(!printQueue.isEmpty()) {
            System.out.println(); //프린트 출력하기
        }
    }

    // Statement -> print IDENT ; | IDENT = Expr ;
    private static void statement() {
        if (nextToken == TokenType.PRINT) {
            lex();
            String var_name = var();
            lex();
            if (nextToken == TokenType.SEMI_COLON){
                if (resultMap.containsKey(var_name)) { //해시맵 자료형도 contains가 있나
                    printQueue.add(resultMap.get(var_name));
                    lex();
                } else if(var_name == null)
                    return;
                else {
                    System.out.print(0);
                }
            } else {
                error();
            }
        } //다른 경우 만들기.
    }

    private static int expr() {
        String op = tokens.get(1);
        int result;
        boolean resultForBexpr;
        List<String> operators = Arrays.asList(">", "<", "==", ">=", "<=", "!=");
        if(operators.contains(op)) {
            resultForBexpr = bexpr();
            result = resultForBexpr ? 1 : 0;
        } else {
            result = aexpr();
        }
        return result;
    }

    private static boolean bexpr() {
        boolean result = false;

        int left_operand = number();
        lex();
        TokenType op = relop();
        int right_operand = number();

        switch(op) {
            case EQ_OP:
                result = left_operand == right_operand;
                break;
            case NEQ_OP:
                result = left_operand != right_operand;
                break;
            case LT_OP:
                result = left_operand < right_operand;
                break;
            case LTE_OP:
                result = left_operand <= right_operand;
                break;
            case GT_OP:
                result = left_operand > right_operand;
                break;
            case GTE_OP:
                result = left_operand >= right_operand;
                break;
            default:
                error();
        }
        return result;

    }

    private static TokenType relop() {
        List<TokenType> operators = Arrays.asList(TokenType.EQ_OP, TokenType.NEQ_OP, TokenType.LT_OP, TokenType.GT_OP, TokenType.LTE_OP, TokenType.GTE_OP);
        if(!operators.contains(nextToken)){
            error();
            nextToken = TokenType.EOF;
        }
        return nextToken;
    }

    private static int aexpr() {
        int result;
        result = term(); //first_operand
        String op = tokens.get(0);
        List<String> validOperators = Arrays.asList("+", "-", "–");
        while(validOperators.contains(op)){
            op = tokens.remove(0);
            int second_operand = term();
            if(op.equals('+')) {
                result += second_operand;
            } else {
                result -= second_operand;
            }
        }
        return result;
    }

    private static int term() {
        int result;
        result = factor();
        String op = tokens.get(0);
        List<String> validOperators = Arrays.asList("*", "/");
        while(validOperators.contains(op)){
            op = tokens.remove(0);
            int second_operand = factor();
            if(op.equals('*')) {
                result *= second_operand;
            } else {
                result /= second_operand;
            }
        }
        return result;
    }

    private static int factor() {
        int result = number();
        return result;
    }

    private static int number() {
        String result = "0";
        lex();
        if(nextToken == TokenType.INT_LIT) {
            result = dec();
        }
        return Integer.parseInt(result);
    }

    private static String dec() {
        return currentChar;
    }

    private static String var() {
        List<String> validChars = Arrays.asList("x", "y", "z");
        if (validChars.contains(currentChar)) {
            return currentChar;
        }
        return "";
    }
}



