
var System = java.lang.System;

var ws = new Packages.box.star.net.WebServer();
var zipFile = "module/jre/NanoStarbox Core/src/java/lib/feature/doc/jna-4.5.2-javadoc.jar"
var zipDriver = new Packages.box.star.net.tools.ZipSiteDriver(new java.io.File("javadoc"), new java.io.File(zipFile));
ws.addVirtualDirectory("javadoc", zipDriver);

ws.start();

System.out.println("Running Test Server on http://localhost:8080");

