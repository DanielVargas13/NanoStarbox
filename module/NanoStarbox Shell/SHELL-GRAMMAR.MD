#### User Guide: box.star.shell

##### Develpment Notes

The NanoStarbox Shell toolkit aims to provide a common shell parser,
which can be used to create text-based-shells with different language
drivers, and command environments. There are many ways to create a
shell, so this toolkit will provide its own default implementation as
an example of this toolkit's usage.

The shell basics are pretty much laid out in the command specification
of a generic unix shell such as Dash. Using the generic foundation, of 
how to interpret commands, this toolkit can provide basic command support
to any program, or custom script interpreter.

This project will be fully integrated into the NanoStarbox Core NSR9
Release when the example implementation is capable of executing system,
commands and custom functions within an environment which can host object
variables, string variables and stream-sets in a context based manner,
that is consistent with basic shell protocols: `command, function, (),
{}, background = &, immediate = ;, logical-or = ||, and logical-and = &&`.

It is beyond this implementation to aim to be compatible with any particular external
shell, meaning all extensions to this toolkit will aim to be compatible
with this shell. This technology allows for a modern shell to be bourne,
so with the object oriented capabilities of java, this toolkit provides
the jumping-off-point for a modern shell with a standard command
interface, or a classic shell using the standard command interface.

A shell can have macro commands embedded within its variable-labels,
variable-values, command-names, redirects and parameters in addition
to the capacity to define custom functions with local variables and
streams. Macro commands are language (shell) sensitive, so this toolkit, 
doesn't interpret them. Before command execution the shell is given a
chance to evaluate and expand all text which could contain a macro.

A shell might want to use C style comments, or a custom ScriptParser,
which pre-dates command processing, thereby using command processing,
as the fallback method of some other language interpreter. In this role,
the interpreter will be given a chance to compile a result. When the time
to execute the compiled result comes about, an execution method will be
called, with the shell context as a parameter, and that operation is
expected to return an integer result of success = 0.

#### Shell Command Grammar

###### TERMINALS, SYMBOLS, AND WORDS
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
<i>This document is a supplemental reference which does not cover all shell grammar semantics.</i>