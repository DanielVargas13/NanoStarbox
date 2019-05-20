#### NanoStarbox: Module: UI: JFX: NanoStarbox Voyager

NanoStarbox Voyager provides a simple way to display Web Content,
using the Java JFX platform. This is useful for applications that seek
a small foot print, but require modern ui capabilities. Combined with
the NanoStarbox Core, the realistic capabilities of Voyager are as endless as the
Java Platform and beyond combined with the power of JNA.

HTML content to be displayed by the Voyager application is specified by
a user, using the <tt>-s URL, FILE or SOURCE-TEXT</tt> command line
switch. The user may specify <tt>-</tt> as the parameter to this switch to
signify standard-input as the source, which is the default.

Additional command switches are available for this application.
See the source code for more options.

Voyager does not feature any Java/Rhino scripting extensions. If you need a Voyager
application profile such as Microsoft HTML Application, you should seek
another product, create a new module, modify the source, OR use the 
NanoStarbox WebServer to provide your application user interface. The 
latter is the ideal profile, which is portable to all platforms,
regardless of Voyager/JFX availability.

If you are targeting a Headless Environment, there is a wonderful
[selection of text-mode-web-browsers](https://en.wikipedia.org/wiki/Text-based_web_browser)
that can be found on the web with features such as mouse and JavaScript support
to deliver your application capabilities to your users wherever they are,
on whatever device they use.

As a cautionary tale, it should be noted that NOT relying on Voyager
powered capabilities, allows seamless employment and enjoyment of external
JavaScript Module Platforms like the kind enjoyed by users of
'[Node.js](https://nodejs.org/en/about/)'.
