<html>
<body>
<h1>%(js "server.host+':'+server.port")</h1>
<p>Local Resource: %(js session.uri)</p>
<p>JavaScript Object Keys in global object: "session":</p>
<ul><li>%(js "Object.keys(session).join('\\n');")</li></ul>
%(js '
    help() || "<br>JavaScript help has been printed on the console.";
')
<p>Server-Side-Hacking: <strong>Mozilla Rhino powered Java Object Hosting and Document Generation with Android JIT Support and Runtime Class Path Loader; powered by NanoStarbox Core</strong>
</p>
<p>Analysis: EXPERT-LEVEL-SYSTEM-HACKING</p>
</body>
</html>
