#### NanoStarbox: Module: Support

*[Specification Staging Area](./)*

[archive](./archive) is the directory which houses the core libraries,
their development sources, and documentation for this NanoStarbox
specification's external core libraries.

*About Library Shrinking*

Some NanoStarbox modules require shrinking to fit within the 5MB
limit of the current specification. Each of these modules, are
pre-shrunk, and will require custom-pre-shrinking with every 
library source update to maintain specification parameters. Each
of these so-called "shrunken" libraries are prefixed with the name
'nano' to signify their form.

Library shrinking is performed with a custom per library project
which can be found in the archive [sources folder](./archive/src). Each project shrinks a
target library, and intends to remove only classes, methods and data
which are not required for specification purposes. Defacto shrinking 
projects use the yGuard library to perform shrinking, however the
method of shrinking is not a part of any Specification as these
libraries are pre-built in the purview of the
[NanoStarbox Core](../NanoStarbox%20Core).

[jna-4.5.2-full.jar](./jna-4.5.2-full.jar) is a historical compiled source
record, which contains the libary that was used to compose the
[nano-jna](./archive/builtins/nano-jna-4.5.2.jar), and 
[nano-jna-extended](../../net/nano-jna-4.5.2-extended.jar) features of
of this NanoStarbox specification.
