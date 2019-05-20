#### NanoStarbox: Module: NanoStarbox Core

*How do you like your coffee?*

##### Specification Sheet

###### External Sources
* Mozilla Rhino JavaScript Library with Android JIT Extensions and 
Aggressive Runtime Package Loading, Runtime Package Introspection and URL Classloading Hacks (Core/[Mozilla Rhino](https://github.com/mozilla/rhino) 1.7.9)
* Nano JNA Library ([JNA](https://github.com/java-native-access/jna) 4.5.2)
* JSON Object Transformation Library ([JSON-java](https://github.com/stleary/JSON-java) 20180813)
* Native Java XML Object Interface Library ([SimpleXML](http://simple.sourceforge.net/) 2.7.1)
* HTTPServer Interface Library (Core/[NanoHTTPD](https://github.com/NanoHttpd/nanohttpd) 20190515)
* JDBC SQL Client Library ala (Nano [HSQLDB](http://hsqldb.org/) 2.4.1)
* JDBC MS Access SQL Client Library ala (Nano [Jackcess](https://jackcess.sourceforge.io/) 3.0.1, and Nano [UCanAccess](http://ucanaccess.sourceforge.net/site.html#home) 4.0.4)

###### Internal Sources
* HTTPServer Content Provider Interface Library (uses HTTPServer Interface)
* WebServer Interface Library (uses HTTPServer Interface)
* WebServer Archive/Directory Mounting (uses HTTPServer Content Provider Interface)
* Unix Command Shell Emulation Library
* TextScanner Library
* Text Macro Shell Library (uses TextScanner)
* MimeType Map Library
* MimeType Driver Interface Library
* JavaScript Server Page Library (for WebServer Interface, uses Mozilla Rhino JavaScript, MimeType Driver Interface and Text Macro Shell, features Mozilla-Rhino/[REQUIRE.JS](https://requirejs.org/) support for server-side-script-includes)
* Runtime State Library
* Timer Library

*Nano HSQLDB and Nano Jackcess do not feature any support not required by UCanAccess, such as HTTP SQL Server.*

*Nano Libraries are safe for use alongside their full-featured-equivalents*

*Nano Packages provided courtesy of Hypersoft-Systems: U.-S.-A. NanoStarbox Project*

*Rhino Android JIT Extensions and Aggressive Runtime Package Loading, Runtime Package Introspection and URL Classloading Hacks provided courtesy of Android [Dalvik](https://source.android.com/devices/tech/dalvik/jit-compiler), [Faendir](https://www.faendir.com/wordpress/), and Hypersoft-Systems: U.-S.-A. NanoStarbox Project*
