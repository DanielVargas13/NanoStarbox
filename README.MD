#### Hypersoft-Systems: NanoStarbox

This JDK 1.8 API Level 8 [project](LICENSE) is managed with IntelliJ IDEA using [git](https://github.com/hypersoft/NanoStarboox/) VCS (version-control-system).

NanoStarbox is a working draft specification composed of a headless core module:

* [NanoStarbox Core](module/jre/NanoStarbox%20Core)

Each working draft specification is guaranteed to be compatible with itself.
working drafts may or may not be compatible with working drafts of the future or past.
Compatibility is an issue of expert systems knowledge.

NanoStarbox has 3 Optional Support Libraries to provide a basic common
framework:

* [JSON-java](module/jre/JSON-java)
* [Simple XML](module/jre/NanoStarbox%20Core/src/lib/feature/simple-xml-2.7.1.jar)
* [JNA](module/jre/NanoStarbox%20Core/src/lib/feature/jna-4.5.2.jar)

The NanoStarbox core will automatically load these modules if they are available.

For Android JIT scripting there is a required scripting module. See the release page 
[on the github site](https://github.com/hypersoft/NanoStarbox/releases) for details.

Additionally there are several provided support packages for desktops:

Java [Swing Kits](module/swing/):
* [NanoStarbox Rhino Console](module/swing/NanoStarbox%20Rhino%20Console)
* [NanoStarbox Rhino Debugger](module/swing/NanoStarbox%20Rhino%20Debugger)

Java [JFX Kits](module/jfx/):
* [NanoStarbox Voyager](module/jfx/NanoStarbox%20Voyager)

Each kit is configured for independent usage with IntelliJ IDEA however, the filing-structure must remain in tact for this automatic code dependency feature to remain operational.

The default build method for this IntelliJ IDEA project builds all modules in the [project's jar directory](jar/).
