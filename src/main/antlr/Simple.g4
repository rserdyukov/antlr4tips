grammar Simple;

prog:   classDef+ ; // match one or more class definitions

classDef
    :   'class' ID '{' member+ '}' // a class has one or more members
    ;

member
    :   'int' ID ';'                       // field definition
    |   'int' f=ID '(' ID ')' '{' stat '}' // method definition
    ;

stat:   expr ';'
    |   ID '=' expr ';'
    ;

expr:   INT 
    |   ID '(' INT ')'
    ;

INT :   [0-9]+ ;
ID  :   [a-zA-Z]+ ;
WS  :   [ \t\r\n]+ -> skip ;
