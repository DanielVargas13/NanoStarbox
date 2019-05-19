#### Hypersoft-Systems: NanoStarbox

This JDK 1.8 API Level 8 [project](LICENSE) is managed with IntelliJ IDEA 
using [git](https://github.com/hypersoft/NanoStarboox/) VCS 
(version-control-system), and requires Apache Maven, to fetch
external build specifications and toolsets.

NanoStarbox is a working draft specification composed of a headless core module:

* [NanoStarbox Core](module/NanoStarbox%20Core)

Browse the core module for an overview of current features. Each NanoStarbox 
working draft specification is guaranteed to be compatible with itself.
working drafts may or may not be compatible with working drafts of the 
future or past. Specification Compatibility is an issue of expert 
NanoStarbox specification knowledge. If you are not requiring a custom
build, you can head over to the [secure release section](https://github.com/hypersoft/NanoStarbox/releases)
on [Github](https://github.com) to download compiled specification artifacts.

In this specification there are several provided support modules for desktop
environments:

Java [Swing Modules](module/ui/swing/):
* [NanoStarbox Rhino Console](module/ui/swing/NanoStarbox%20Rhino%20Console)
* [NanoStarbox Rhino Debugger](module/ui/swing/NanoStarbox%20Rhino%20Debugger)

Java [JFX Modules](module/ui/jfx/):
* [NanoStarbox Voyager](module/ui/jfx/NanoStarbox%20Voyager)

Each module is configured for independent usage with IntelliJ IDEA as a single project, however, the filing-structure must remain in tact for this automatic code dependency feature to remain operational within the main project.

The main project build method for this IntelliJ IDEA project builds all modules in the [project's jar directory](jar/).
