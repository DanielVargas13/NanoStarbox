
function result(){
    return "this is output from my server-side-script-include";
}

// Setup JSON
Java.loadClassPath(directory+"/nano-starbox-json.jar");
var JSONObject = Packages.org.json.JSONObject;
