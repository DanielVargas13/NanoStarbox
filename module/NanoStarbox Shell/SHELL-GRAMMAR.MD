#### Shell Command Grammar

###### TERMINAL SYMBOLS
<I>Using Java Character Notation, Java String Notation, and Regular Expression Syntax</I>

AMPERSAND: '&'

PIPE: '|'

TERMINATOR: '\0', '\n', '\r', '#', ';', '&', '(', ')', '{', '}'

LOGICAL_CONTINUATION: "&&", "||"

DIGIT: \[0-9]

DIGITS: \[0-9]+

###### RULE-SYNTAX
- `?' = maybe
- `?:' = if-then
- `...' = list (zero or more; implies: maybe)
- `()' = group
- ` ' = line-white-space (spaces, tabs, form-feeds)
- `||' = logical-or
- `\[WORD]' = rule

###### RULES
DATA_OPERATION: (label)(operation)(value)

PROGRAM: shell-keyword || full-program-path || program-in-path || function-of-shell

PARAMETER: literal-text || single-quoted-text || double-quoted-text || compound-quoted-text

STREAM_DESCRIPTOR: DIGIT || DIGITS

STREAM_DESCRIPTOR_REFERENCE: (AMPERSAND)\[STREAM_DESCRIPTOR]

REDIRECT: \[STREAM_DESCRIPTOR]?(stream-operation)(\[STREAM_DESCRIPTOR_REFERENCE] || ((line-white-space)?(here-document || file-path)))

COMMAND: (\[DATA_OPERATION]...) \[PROGRAM]?: ((\[PARAMETER]...) (\[REDIRECT]...) (PIPE?: [COMMAND])...))? (TERMINATOR || LOGICAL_CONTINUATION)

<br>
<i>This document is a supplemental reference that does not yet
describe all technical semantics.</i>