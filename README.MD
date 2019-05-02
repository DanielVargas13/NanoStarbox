#### Hypersoft-Systems: NanoStarbox

This JDK 1.8 API Level 8 [project](LICENSE) is managed with IntelliJ IDEA using [git](https://github.com/hypersoft/NanoStarboox/) VCS.

NanoStarbox is composed of several [Base Modules](module/jre/):

* [NanoStarbox Core](module/jre/NanoStarbox%20Core)
* [NanoStarbox Rhino](module/jre/NanoStarbox%20Rhino)
* [NanoStarbox Web Server](module/jre/NanoStarbox%20Web%20Server)

Additionally there are several provided support packages for desktops:

Java [Swing Kits](module/swing/):
* [NanoStarbox Rhino Console](module/swing/NanoStarbox%20Rhino%20Console)
* [NanoStarbox Rhino Debugger](module/swing/NanoStarbox%20Rhino%20Debugger)

Java [JFX Kits](module/jfx/):
* [NanoStarbox Voyager](module/jfx/NanoStarbox%20Voyager)

And Miscellaneous Core support Kits:
* [JNA](module/jna/)
* [Simple XML](module/jre/Simple%20XML/)

Core support kits are "recommended" addons.

Each kit and base-module is configured for independent usage with IntelliJ IDEA however, the filing-structure must remain in tact for this automatic code dependency feature to remain operational.

The default build method for this IntelliJ IDEA project builds all modules in the [project's jar directory](jar/).

If you need a headless configuration, simply select the Base Modules you need from the jar directory.
