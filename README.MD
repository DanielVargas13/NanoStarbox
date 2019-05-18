#### Hypersoft-Systems: NanoStarbox

This JDK 1.8 API Level 8 [project](LICENSE) is managed with IntelliJ IDEA 
using [git](https://github.com/hypersoft/NanoStarboox/) VCS 
(version-control-system).

NanoStarbox is a working draft specification composed of a headless core module:

* [NanoStarbox Core](module/jre/NanoStarbox%20Core)

Each working draft specification is guaranteed to be compatible with itself.
working drafts may or may not be compatible with working drafts of the future or past.
Compatibility is an issue of expert systems knowledge.

Additionally there are several provided support packages for desktops:

Java [Swing Kits](module/swing/):
* [NanoStarbox Rhino Console](module/swing/NanoStarbox%20Rhino%20Console)
* [NanoStarbox Rhino Debugger](module/swing/NanoStarbox%20Rhino%20Debugger)

Java [JFX Kits](module/jfx/):
* [NanoStarbox Voyager](module/jfx/NanoStarbox%20Voyager)

Each kit is configured for independent usage with IntelliJ IDEA however, the filing-structure must remain in tact for this automatic code dependency feature to remain operational.

The default build method for this IntelliJ IDEA project builds all modules in the [project's jar directory](jar/).
