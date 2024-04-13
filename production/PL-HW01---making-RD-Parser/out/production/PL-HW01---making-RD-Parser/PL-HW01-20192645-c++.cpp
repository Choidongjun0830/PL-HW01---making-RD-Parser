#include <iostream>
#include <sstream>
#include <string>
#include <vector>
#include <unordered_map>
#include <algorithm>
#include <cctype>

enum TokenCode {
    INT_LIT = 10,
    IDENT = 11,
    ASSIGN_OP = 20,
    ADD_OP = 21,
    SUB_OP = 22,
    MULT_OP = 23,
    DIV_OP = 24,
    PRINT = 33,
    SEMI_COLON = 34,
    EOF_TOKEN = 35
};

std::unordered_map<std::string, int> results_dict;
std::vector<std::string> tokens;
std::vector<int> print_li;
std::string currentChar;
TokenCode nextToken;

void lookup(const std::string& ch);
void lex();
void program();
void statement();
int expr();
int bexpr();
int aexpr();
int term();
int factor();
int number();
std::string var();

int main() {
    std::string line;
    while (std::getline(std::cin, line)) {
        if (line == "terminate") {
            break;
        }

        std::istringstream iss(line);
        std::string token;
        while (iss >> token) {
            tokens.push_back(token);
        }

        program();

        if (!print_li.empty()) {
            for (int result : print_li) {
                std::cout << result << ' ';
            }
            std::cout << std::endl;
            print_li.clear();
        }

        results_dict.clear();
        tokens.clear();
    }

    return 0;
}

void lookup(const std::string& ch) {
    if (ch == "+") {
        nextToken = ADD_OP;
    } else if (ch == "-") {
        nextToken = SUB_OP;
    } else if (ch == "*") {
        nextToken = MULT_OP;
    } else if (ch == "/") {
        nextToken = DIV_OP;
    } else if (ch == ";") {
        nextToken = SEMI_COLON;
    } else {
        nextToken = EOF_TOKEN;
    }
}

void lex() {
    if (tokens.empty()) {
        nextToken = EOF_TOKEN;
        return;
    }

    currentChar = tokens.front();
    tokens.erase(tokens.begin());

    if (std::isalpha(currentChar[0])) {
        if (currentChar == "print") {
            nextToken = PRINT;
        } else {
            nextToken = IDENT;
        }
    } else if (std::isdigit(currentChar[0])) {
        nextToken = INT_LIT;
    } else {
        lookup(currentChar);
    }
}

// 나머지 함수들의 구현...
// 이들 함수는 주어진 파이썬 코드의 로직을 C++로 번역해야 합니다.
// Python에서의 딕셔너리는 C++에서 unordered_map으로 대체됩니다.
// Python에서의 리스트는 C++에서 vector로 대체됩니다.
// Python의 동적 타이핑은 C++의 정적 타이핑으로 대체되어야 하므로,
// 각 변수와 함수의 타입을 명확히 지정해야 합니다.
