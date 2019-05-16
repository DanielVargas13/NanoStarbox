<!MIME text/html, application/x-nano-starbox-javascript-server-page>
<html>
<body>%(js '
load(directory+"/server-page.js") || "";
')
<h1>%(js server.address)</h1>
<p>Directory: %(js directory)</p>
<p>Local Resource: %(js session.uri)</p>
<p>Session Headers:</p>
<ul><li>%(js "
    Java.loadClassPath(directory+'/nano-starbox-json.jar') || '';" "
    new Packages.org.json.JSONObject(session.headers).toString();
    ")</li></ul>
<p>Java Packages:</p><ul><li>%(js '
    Java.toArray(Java.knownPackages).join("\n");
    ')</li></ul>
%(js '
help() || "<br>JavaScript help has been printed on the console.";
')<p>Server-Side-Hacking: <strong>Mozilla Rhino powered Java Object Hosting and
    Document Generation with Android JIT Support and Runtime Class Path Loader;
    powered by NanoStarbox Core
</strong></p>
<p>Analysis: %(js '
    result()
    ')</p></body></html>
