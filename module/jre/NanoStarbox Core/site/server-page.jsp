<!MIME text/html, application/x-nano-starbox-javascript-server-page>
<html>
<body>%(src server-page.js)
<h1>%(val server.address)</h1>
<p>Directory: %(val directory)</p>
<p>Local Resource: %(val session.uri)</p>
<p>Session Headers:</p>
<ul><li>%(val "new JSONObject(session.headers).toString();")</li></ul>
<p>Java Packages:</p><ul><li>%(val 'Java.toArray(Java.knownPackages).join("\n");')</li></ul>
%(do 'help()')<br>JavaScript help has been printed on the console.
<p>Server-Side-Hacking: <strong>Mozilla Rhino powered Java Object Hosting and
    Document Generation with Android JIT Support and Runtime Class Path Loader;
    powered by NanoStarbox Core</strong></p>
<p>Analysis: %(val 'result()')
</p></body></html>
