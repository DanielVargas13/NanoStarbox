#### User Guide: box.star.shell

##### Blend: Main Ingriedients
A few java-specific-end-user-benefits of the toolkit are:
<br><p><i>reasons to integrate</i></p>
- java class name (in classpath) as commands (instant-run-from-memory in same jvm)
- java classes as functions
- java object as variable
- java/subclass user-defined macro/script interpretation
- java tracing & debugging vs. shell tracing & debugging
- java/enabled Platform Independent Script Solutions with a Universal Command Platform

##### Development Notes

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
Release when the example implementation is capable of executing system
commands and custom functions within an environment which can host object
variables, string variables and stream-sets in a context based manner 
which is consistent with basic shell protocols: `test, eval, source, command, function, (),
{}, background = &, immediate = ;, logical-or = ||, and logical-and = &&`.
The finer aspects of logic and environment control are implementation
dependent.

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

There are no plans to design an "interactive shell", or "login shell".
This may create use case and historical compatibility issues. For example,
the bash shell, can read script component fragments separated by lines, 
while issuing prompts on the standard error handle during an interactive
session. That may be a slight-setback for some uses however, the toolkit
provides everything needed to handle complete script components. 
Additionally in terms of "login shell", there is nothing to "login" to,
so a "login shell" is virtual by virtue of this natural condition, 
imposing no known requirement/limitation on 
basic-text-command-shell-toolkit (generic-type-summary for box.star.shell.*).

Tracing and debugging do concern command execution, but the toolkit
only supports these features as inherent java runtime properties. That
is a not an actual script runtime feature, so it should not affect
production scripts that are not "ugly hacks"/workarounds for features
this toolkit deprecates with sheer operational supremacy. In other words,
such scripts are dependency-trash (arcane-technology), and could be 
written better using the jvm technology, if a genuine script interpreter
is not available for the user's target distribution.

Additionally in regards to compatibility, an actual system command could
used as the script interpreter portion of this shell if a developer so
chose to create the integration port.

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