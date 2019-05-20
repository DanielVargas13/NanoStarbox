#### NanoStarbox: Module: NanoStarbox Core

*How do you like your coffee?*

##### Specification Sheet

* Mozilla Rhino JavaScript Library with Android JIT Extensions and 
Aggressive Runtime Package Loading, Runtime Package Introspection and URL Classloading Hacks (Core/[Mozilla Rhino](https://github.com/mozilla/rhino) 1.7.9)
* Nano JNA Library ([JNA](https://github.com/java-native-access/jna) 4.5.2)
* JSON Object Transformation Library ([JSON-java](https://github.com/stleary/JSON-java) 20180813)
* Native Java XML Object Interface Library ([SimpleXML](http://simple.sourceforge.net/) 2.7.1)
* HTTPServer Interface Library (Core/[NanoHTTPD](https://github.com/NanoHttpd/nanohttpd) 20190515)
* HTTPServer Content Provider Interface Library (Core, uses HTTPServer Interface)
* WebServer Interface Library (Core, uses HTTPServer Interface)
* WebServer Archive/Directory Mounting (Core, uses HTTPServer Content Provider Interface)
* Command Interface Library (Core)
* TextScanner Library (Core)
* Text Macro Shell Library (Core, uses TextScanner)
* MimeType Map Library (Core)
* MimeType Driver Interface Library (Core)
* JavaScript Server Page Library (Core, for WebServer Interface, uses MimeType Driver Interface and Text Macro Shell)
* Runtime State Library (Core)
* Timer Library (Core)
* JDBC SQL Client Library ala (Nano [HSQLDB](http://hsqldb.org/) 2.4.1)
* JDBC MS Access SQL Client Library ala (Nano [jackcess](https://jackcess.sourceforge.io/) 3.0.1, and Nano [UCanAccess](http://ucanaccess.sourceforge.net/site.html#home) 4.0.4)

*Nano HSQLDB and Nano jackcess do not feature any support not requried by ucanaccess, such as HTTP SQL Server.*

*Nano Packages provided courtesy of Hypersoft-Systems: U.-S.-A. NanoStarbox Project*

*Rhino Android JIT Extensions and Aggressive Runtime Package Loading, Runtime Package Introspection and URL Classloading Hacks provided courtesy of [Android Dalvik](https://source.android.com/devices/tech/dalvik), [Faendir](https://www.faendir.com/wordpress/), and Hypersoft-Systems: U.-S.-A. NanoStarbox Project*