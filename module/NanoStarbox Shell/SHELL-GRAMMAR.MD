#### Shell Command Grammar

###### TERMINAL SYMBOLS
<I>Using Java Character Notation, and Regular Expression Syntax</I>

PIPE: '|'

TERMINATOR: '\0', '\n', '\r', '#', ';', '&', '(', ')', '{', '}'

DIGIT: \[0-9]

DIGITS: \[0-9]+

###### RULE-SYNTAX
- `?' = maybe
- `?:' = if-then
- `...' = list
- `()' = group
- ` ' = line-white-space (spaces, tabs, form-feeds)
- `||' = logical-or
- `\[WORD]' = rule

###### RULES
DATA_OPERATION: (label)(operation)(value)

PROGRAM: shell-keyword || full-program-path || program-in-path || function-of-shell

PARAMETER: literal-text || single-quoted-text || double-quoted-text || compound-quoted-text

REDIRECT: (stream-descriptor)?(stream-operation)(stream-descriptor-reference || ((line-white-space)?(here-document || file-path)))

COMMAND: (\[DATA_OPERATION]...) \[PROGRAM]?: ((\[PARAMETER]...) (\[REDIRECT]...)? (PIPE?: [COMMAND])...))? TERMINATOR  
<br>
<i>This document is a supplemental reference that does not yet
describe all technical semantics.</i>