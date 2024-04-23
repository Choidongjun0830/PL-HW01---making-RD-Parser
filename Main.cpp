#include <iostream>
#include <string>
#include <vector>
#include <unordered_map>
#include <cctype>
#include <sstream>
#include <algorithm>
#include <stdio.h>

using namespace std;


enum TokenType {
    INT_LIT, IDENT, ASSIGN_OP, ADD_OP, SUB_OP, MULT_OP, DIV_OP, LEFT_PAREN, RIGHT_PAREN, EQ_OP, NEQ_OP, LT_OP, GT_OP, LTE_OP, GTE_OP, PRINT, SEMI_COLON, EOF_TOK
};

static unordered_map<string, string> resultMap;
static vector<string> printQueue;
static vector<string> tokens;

static TokenType nextToken;
static std::string currentChar;
static bool errorFlag = false;

void tokenize(const string& input);
void lex();
void lookup(const std::string& currentToken);
void error();
void program();
void statement();
string expr();
string bexpr();
TokenType relop();
int aexpr();
int term();
int factor();
int number();
string var();

void tokenize(const string& input) {
    istringstream iss(input); //공백이나 개행 문자로 구분된 토큰을 순차적으로 읽을 수 있도록. split() 역할
    string token;
    while (iss >> token) {
        tokens.push_back(token);
    }
}

void lex() {
    if (tokens.empty()) {
        nextToken = EOF_TOK;
        return;
    }

    string currentToken = tokens.front(); //이렇게 두개의 과정을 거쳐야.. 아니면 deque 사용
    tokens.erase(tokens.begin());

    if (all_of(currentToken.begin(), currentToken.end(), ::isalpha)) {
        if (currentToken == "print") {
            nextToken = PRINT;
        }
        else {
            nextToken = IDENT;
        }
        currentChar = currentToken;
    }
    else if (all_of(currentToken.begin(), currentToken.end(), ::isdigit)) {
        nextToken = INT_LIT;
        currentChar = currentToken;
    }
    else {
        lookup(currentToken);
    }
}

void lookup(const string& currentToken) { //c++의 switch문은 string을 사용할 수 없음.
    if (currentToken == "(" || currentToken == ")") {
        errorFlag = true;
    }
    else if (currentToken == "+") {
        nextToken = ADD_OP;
    }
    else if (currentToken == "-") {
        nextToken = SUB_OP;
    }
    else if (currentToken == "*") {
        nextToken = MULT_OP;
    }
    else if (currentToken == "/") {
        nextToken = DIV_OP;
    }
    else if (currentToken == "<") {
        nextToken = LT_OP;
    }
    else if (currentToken == "<=") {
        nextToken = LTE_OP;
    }
    else if (currentToken == ">") {
        nextToken = GT_OP;
    }
    else if (currentToken == ">=") {
        nextToken = GTE_OP;
    }
    else if (currentToken == "=") {
        nextToken = ASSIGN_OP;
    }
    else if (currentToken == "==") {
        nextToken = EQ_OP;
    }
    else if (currentToken == "!=") {
        nextToken = NEQ_OP;
    }
    else if (currentToken == ";") {
        nextToken = SEMI_COLON;
    }
    else {
        nextToken = EOF_TOK;
    }
}

void error() {
    errorFlag = true;
}

void program() {
    if (tokens.empty() || tokens.front() == "")
        return;
    lex();
    while (nextToken != EOF_TOK && !errorFlag) {
        statement();
        if (tokens.empty()) {
            nextToken = EOF_TOK;
        }
    }
    if (!tokens.empty())
        error();
}

void statement() {
    string var_name;
    if (nextToken == PRINT) {
        lex();
        var_name = var();
        lex();
        if (nextToken == SEMI_COLON) {
            auto it = resultMap.find(var_name);
            if (it != resultMap.end()) {
                printQueue.push_back(it->second); //first는 키, second는 값
                lex();
            }
            else if (var_name.empty())
                return;
            else if (!errorFlag)
                printQueue.push_back("0");  // Assuming default value as string "0"
        }
        else {
            error();
        }
    }
    else if (nextToken == IDENT) {
        var_name = var();
        lex();
        if (nextToken == ASSIGN_OP) {
            string result = expr();
            lex();
            if (nextToken == SEMI_COLON) {
                lex();
                resultMap[var_name] = result;
            }
            else
                error(); // Assuming we do not return here because we want to process further statements maybe
        }
        else {
            error();
        }
    }
    else {
        error();
    }
}

string expr() { //수정
    string op = tokens[1];
    vector<string> operators = { ">", "<", "==", ">=", "<=", "!=" };
    string result;
    if (find(operators.begin(), operators.end(), op) != operators.end()) { //contain?
        result = bexpr();
    }
    else {
        result = to_string(aexpr());
    }
    return result;
}

string bexpr() {
    bool result = false;
    int left_operand = number();

    lex();
    TokenType op = relop();
    int right_operand = number();

    switch (op) {
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
    return result ? "TRUE" : "FALSE";
}

TokenType relop() {
    vector<TokenType> operators = { EQ_OP, NEQ_OP, LT_OP, GT_OP, LTE_OP, GTE_OP };
    auto it = find(operators.begin(), operators.end(), nextToken);
    if (it == operators.end()) {
        error();
        nextToken = EOF_TOK;
    }
    return nextToken;
}

int aexpr() {
    int result = term();
    vector<string> valid_operators = { "+", "-", "–" };
    while (!tokens.empty() && (tokens.front() == "+" || tokens.front() == "-")) {
        string op = tokens.front(); tokens.erase(tokens.begin());

        int second_operand = term();
        if (op == "+") {
            result += second_operand;
        }
        else {
            result -= second_operand;
        }//잘 안돌면 tokens[0]을 다시 할당
    }
    return result;
}

int term() {
    int result = factor(); // 첫 번째 피연산자
    while (!tokens.empty() && (tokens.front() == "*" || tokens.front() == "/")) {
        string op = tokens.front(); tokens.erase(tokens.begin());

        int second_operand = factor(); // 두 번째 피연산자
        if (op == "*") {
            result *= second_operand;
        }
        else if (op == "/") {
            result /= second_operand;
        }
    }
    return result;
}

int factor() {
    return number(); // number 함수 호출 결과 반환
}

int number() {
    lex();
    if (nextToken == INT_LIT) {
        return stoi(currentChar); // currentChar가 숫자를 포함한다고 가정
    }
    else {
        error();
    }
    return 0;
}

string dec() {
    return currentChar; // 현재 문자열 반환
}

string var() {
    vector<string> validChars = { "x", "y", "z" };
    if (find(validChars.begin(), validChars.end(), currentChar) != validChars.end()) {
        return currentChar;
    }
    else {
        error();
    }
    return "";
}

int main() {
    std::string input;
    while (getline(std::cin, input)) {
        if (input == "terminate") break;
        tokenize(input);
        program();

        if (errorFlag) {
            cout << "syntax error!!" << endl;
        }
        if (!printQueue.empty() and !errorFlag) {
            for (const auto& item : printQueue) {
                cout << item << " ";
            }
            cout << endl;
        }

        errorFlag = false;
        tokens.clear();
        printQueue.clear();
        resultMap.clear();
    }

    return 0;
}