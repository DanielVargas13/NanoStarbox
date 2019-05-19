#### Hypersoft-Systems: NanoStarbox

This JDK 1.8 API Level 8 [project](LICENSE) is managed with IntelliJ IDEA 
using [git](https://github.com/hypersoft/NanoStarboox/) VCS 
(version-control-system).

NanoStarbox is a working draft specification composed of a headless core module:

* [NanoStarbox Core](module/NanoStarbox+Core)

Each working draft specification is guaranteed to be compatible with itself.
working drafts may or may not be compatible with working drafts of the future or past.
Compatibility is an issue of expert systems knowledge.

Additionally there are several provided support packages for desktops:

Java [Swing Kits](module/ui/swing/):
* [NanoStarbox Rhino Console](module/ui/swing/NanoStarbox+Rhino+Console)
* [NanoStarbox Rhino Debugger](module/ui/swing/NanoStarbox+Rhino+Debugger)

Java [JFX Kits](module/ui/jfx/):
* [NanoStarbox Voyager](module/ui/jfx/NanoStarbox+Voyager)

Each kit is configured for independent usage with IntelliJ IDEA however, the filing-structure must remain in tact for this automatic code dependency feature to remain operational.

The default build method for this IntelliJ IDEA project builds all modules in the [project's jar directory](jar/).
