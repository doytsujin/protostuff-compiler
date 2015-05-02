parser grammar ProtoParser;

options {
    tokenVocab = ProtoLexer;
}

proto
    // syntax should be first statement in the file
    : syntax? statement*
    ;
syntax
    // only proto3 is supported
    : SYNTAX ASSIGN STRING_VALUE SEMICOLON
    ;
statement
    : packageStatement
    | importStatement
    | optionEntry
    | enumBlock
    | messageBlock
    ;
packageStatement
    : PACKAGE declarationRef SEMICOLON
    ;
importStatement
    : IMPORT STRING_VALUE SEMICOLON
    ;
optionEntry
    : OPTION option SEMICOLON
    ;
enumBlock
    : ENUM NAME LCURLY enumBlockEntry* RCURLY SEMICOLON?
    ;
enumBlockEntry
    : enumConstant
    | optionEntry
    ;
enumConstant
    : NAME ASSIGN INTEGER_VALUE fieldOptions? SEMICOLON
    ;
extendBlock
    : EXTEND typeReference LCURLY extendBlockEntry* RCURLY SEMICOLON?
    ;
extendBlockEntry
    : typeReference NAME ASSIGN INTEGER_VALUE fieldOptions? SEMICOLON
    ;
messageBlock
    : MESSAGE NAME LCURLY messageBlockEntry* RCURLY SEMICOLON?
    ;
messageBlockEntry
    : messageField
    | optionEntry
    | messageBlock
    | enumBlock
    | messageExtensions
    ;
messageExtensions
    : EXTENSIONS INTEGER_VALUE TO (INTEGER_VALUE | MAX) SEMICOLON
    ;
messageField
    : fieldModifier? typeReference fieldName ASSIGN INTEGER_VALUE fieldOptions? SEMICOLON
    ;
fieldName
    : NAME
    | PACKAGE
    | SERVICE
    | EXTENSION
    | SYNTAX
    ;
fieldModifier
    : OPTIONAL
    | REQUIRED
    | REPEATED
    ;
typeReference
    : DOT? declarationRef
    ;
fieldOptions
    : LSQUARE (option (COMMA option)* )? RSQUARE
    ;
option
    : optionName ASSIGN optionValue
    ;
optionName
    : NAME
    | customOptionName
    ;
customOptionName
    : LPAREN declarationRef RPAREN (DOT declarationRef)?
    ;
declarationRef
    : NAME (DOT NAME)*
    ;
optionValue
    : INTEGER_VALUE
    | BOOLEAN_VALUE
    | STRING_VALUE
    | NAME
    | textFormat
    ;
textFormat
    : LCURLY textFormatEntry* RCURLY
    ;
textFormatEntry
    : NAME COLON optionValue
    ;
