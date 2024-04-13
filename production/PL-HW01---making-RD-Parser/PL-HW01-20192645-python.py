#Global delclarations
#Variables
charClass = 0
currentChar = ''
nextChar = ''
nextToken = 0
i = -1
error_flag = False

#Character classes
LETTER = 0
DIGIT = 1
UNKNOWN = 99

#Token codes
INT_LIT = 10
IDENT = 11
ASSIGN_OP = 20 # =
ADD_OP = 21 # +
SUB_OP = 22 # -
MULT_OP = 23 # *
DIV_OP = 24 # /
LEFT_PAREN = 25 # (
RIGHT_PAREN = 26 # )
EQ_OP = 27 # ==
NEQ_OP = 28 # !=
LT_OP = 29 # <
GT_OP = 30 # >
LTE_OP = 31 # <=
GTE_OP = 32 # >=
PRINT = 33
SEMI_COLON = 34
EOF = '\0'

results_dict = {}
print_li = []

def lex():
    global tokens, nextToken, currentChar
    if not tokens:
        nextToken = EOF
        return

    currentToken = tokens.pop(0)

    if currentToken.isalpha():
        if currentToken == "print":
            nextToken = PRINT
        else:
            nextToken = IDENT
        currentChar = currentToken
    elif currentToken.isdigit():
        nextToken = INT_LIT
        currentChar = currentToken
    else:
        lookup(currentToken)

def lookup(ch):
    global nextToken, error_flag
    if ch == '(': #괄호 나오면 오류
        error_flag = True
    elif ch == ')':
        error_flag = True
    if ch == '+':
        nextToken = ADD_OP
    elif ch == '-':
        nextToken = SUB_OP
    elif ch == '*':
        nextToken = MULT_OP
    elif ch == '/':
        nextToken = DIV_OP
    elif ch == '<':
        nextToken = LT_OP
    elif ch == '<=':
        nextToken = LTE_OP
    elif ch == '>':
        nextToken = GT_OP
    elif ch == '>=':
        nextToken = GTE_OP
    elif ch == '=':
        nextToken = ASSIGN_OP
    elif ch == '==':
        nextToken = EQ_OP
    elif ch == '!=':
        nextToken = NEQ_OP
    elif ch == ';':
        nextToken = SEMI_COLON
    else:
        nextToken = EOF


def error():
    global error_flag
    error_flag = True

def program():
    global nextToken, results_dict
    if not tokens:
        return
    lex()
    while nextToken != EOF and not error_flag:
        statement()
        if not tokens:
            nextToken = EOF
    if tokens:
        error()
    if print_li:
        print(' '.join(str(result_to_print) for result_to_print in print_li))

def statement():
    global currentChar, nextToken, results_dict, print_li
    if nextToken == PRINT:
        lex()
        var_name = var()
        lex()
        if nextToken == SEMI_COLON:
            if var_name in results_dict:
                print_li.append(results_dict[var_name])
                lex()
            elif var_name is None:
                return
            else:
                print(0)
        else:
            error()
    elif nextToken == IDENT:
        var_name = var()  # Get variable name
        lex()
        if nextToken == ASSIGN_OP:
            result = expr()
            lex()
            if nextToken == SEMI_COLON:
                lex()
                results_dict[var_name] = result
                #print(results_dict)
            else:
                error()
        else:
            error()

    else:
        error()

def expr():
    global nextToken, tokens
    #bexpr인지, aexpr인지 결정하기 위해서 연산자 미리 보기
    op = tokens[1]
    if op in ['>', '<', '==', '>=', '<=', '!=']:  # Starting with a number
        result = bexpr()
    else:
        result = aexpr()
    return result

def bexpr(): #number, relop 호출하도록 변경
    global tokens
    result = False
    left_operand = number()  # Get left operand

    lex()
    op = relop()  # Get relational operator
    right_operand = number()  # Get right operand

    if op == EQ_OP:
        result = left_operand == right_operand
    elif op == NEQ_OP:
        result = left_operand != right_operand
    elif op == LT_OP:
        result = left_operand < right_operand
    elif op == GT_OP:
        result = left_operand > right_operand
    elif op == LTE_OP:
        result = left_operand <= right_operand
    elif op == GTE_OP:
        result = left_operand >= right_operand
    else:
        error()
    return result

def relop():
    global tokens, nextToken
    if nextToken not in [EQ_OP, NEQ_OP, LT_OP, GT_OP, LTE_OP, GTE_OP]:
        error()
        nextToken = EOF
    return nextToken

def aexpr(): #여기 수정
    global nextToken
    # { }니까 계산 안할 경우를 생각해서 첫번째 피연산자를 result에 넣기
    result = term()
    while tokens[0] in ['+', '-', '–']: #과제 명세서에 있는 빼기와 0옆에 있는 하이픈을 모두 빼기로
        op = tokens.pop(0)  # Get operator
        second_operand = term()  # Parse another term
        if op == '+':
            result += second_operand
        else:
            result -= second_operand
    return int(result)
def term():
    global tokens
    #{ }니까 계산 안할 경우를 생각해서 첫번째 피연산자를 result에 넣기
    result = factor()
    while tokens[0] in ['*', '/']:
        op = tokens.pop(0)  # Get operator
        second_operand = factor()  # Parse another factor
        if op == '*':
            result *= second_operand
        else:
            result /= second_operand
    return result
def factor():
    global tokens, nextToken
    result = number()
    return result

def number():
    global tokens, nextToken
    result = 0
    lex()
    if nextToken == INT_LIT:
        result = dec()
    return int(result)

def dec():
    global currentChar
    return currentChar

def var():
    global currentChar
    if currentChar in ['x', 'y', 'z']:
        return currentChar
    else:
        error()

# 입력 받기
while True:
    code = input()
    if code == "terminate":
        break
    tokens = code.split()
    program()
    if error_flag == True:
        print("syntax error!!")
    error_flag = False
    results_dict = {}
    print_li = []