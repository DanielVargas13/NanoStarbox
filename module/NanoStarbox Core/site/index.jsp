<!MIME text/html, application/x-nano-starbox-javascript-server-page>
<html>
<body>%(src server-page.js)
<h1>%(val server.address)</h1>
<p>Content Root: %(val directory)</p>
<p>Content Path: %(val session.uri)</p>
<p>Session Headers:</p>
<ul><li>%(<script>
    new JSONObject(session.headers).toString()
</script>)</li></ul>
<p>Java Packages:</p><ul><li>%(<script>
    Java.toArray(Java.knownPackages).join("\n");
</script>)</li></ul>
%(do 'help()')<br>JavaScript help has been printed on the console.
<p>Server-Side-Hacking: <strong>Mozilla Rhino powered Java Object Hosting and
    Document Generation with Android JIT Support and Runtime Class Path Loader;
    powered by NanoStarbox Core</strong></p>
<p>Analysis: %(val 'result()')
</p></body></html>
