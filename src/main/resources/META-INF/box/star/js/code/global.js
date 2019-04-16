
var
    NanoStarbox = Packages.box.star,
    File = java.io.File,
    FileInputStream = java.io.FileInputStream,
    FileOutputStream = java.io.FileOutputStream,
    FileReader = java.io.FileReader,
    FileWriter = java.io.FileWriter,
    BufferedReader = java.io.BufferedReader,
    BufferedWriter = java.io.BufferedWriter,
    ByteArrayInputStream = java.io.ByteArrayInputStream,
    ByteArrayOutputStream = java.io.ByteArrayOutputStream,
    System = java.lang.System,
    ClassCompiler = Packages.box.star.js.ClassCompiler
;

function help(topic) {
    if (topic) System.err.println(getFileText("META-INF/box/star/js/help/" + topic));
    else help("global");
}

function loadParameterObject(object, parameters) {
  for (var i = 0; i < parameters.length; i++) {
    var p = parameters[i];
    if (i === 0) {
        object["main"] = p;
        continue;
    }
    if (p.endsWith(":")) {
      var n = p.slice(0, -1);
      if (Array.isArray(object[n])) {
        object[n].push(parameters[++i])
      } else object[n] = parameters[++i];
      continue;
    }
    if (p.startsWith("--")) {
      var n = p.substring(2);
      if (Array.isArray(object[n])) {
        object[n].push(parameters[++i])
      } else object[n] = parameters[++i];
      continue;
    }
    if (p.startsWith("/")) {
      var n = p.substring(1);
      if (Array.isArray(object[n])) {
        object[n].push(parameters[++i])
      } else object[n] = parameters[++i];
      continue;
    }
    throw new ReferenceError("wrong parameter command: " + p);
  }
  return object;
}