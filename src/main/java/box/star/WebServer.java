package box.star;

import box.star.etc.MimeTypeMap;
import box.star.etc.MimeTypeReader;
import box.star.io.protocols.http.HTTPServer;
import box.star.io.protocols.http.IHTTPSession;
import box.star.io.protocols.http.response.Response;
import box.star.io.protocols.http.response.Status;

import box.star.util.Template;
import box.star.util.Timer;
import org.jetbrains.annotations.NotNull;

import javax.activation.MimeType;
import java.io.*;
import java.util.*;

public class WebServer extends HTTPServer {

    private Timer timer = new Timer();

    public interface IResponseHandler {
        Response generateServiceResponse(WebServer webServer, File file, String mimeType, IHTTPSession ihttpSession);
    }

    public TimerTask createTimeout(int time, Timer.ITimerCallback<Object> callback, Object... parameter) {
        return timer.createTimeout(time, callback, parameter);
    }

    public TimerTask createInterval(int time, Timer.ITimerCallback<Object> callback, Object... parameter) {
        return timer.createInterval(time, callback, parameter);
    }

    public TimerTask createAlarm(Date time, Timer.ITimerCallback<Object> callback, Object... parameter) {
        return timer.createAlarm(time, callback, parameter);
    }

    public MimeTypeMap getMimeTypeMap() {
        return (MimeTypeMap) configuration.get("mimeTypeMap");
    }

    public Stack<String> getStaticIndexFiles() {
        return (Stack<String>) configuration.get("staticIndexFiles");
    }

    public Stack<MimeTypeReader> getMimeTypeReaders() {
        return (Stack<MimeTypeReader>) configuration.get("mimeTypeReaders");
    }

    public Stack<String> getTemplateMimeTypes() {
        return (Stack<String>)configuration.get("templateMimeTypes");
    }

    public Hashtable<File, Template> getTemplateCache() {
        return (Hashtable<File, Template>) configuration.get("templateCache");
    }

    public void addStaticIndexFile(String filename) {
        getStaticIndexFiles().push(filename);
    }

    public void addTemplateMimeType(String mimeType) {
        getTemplateMimeTypes().push(mimeType);
    }

    public boolean isTemplateMimeType(String mimeType) {
        return getTemplateMimeTypes().contains(mimeType);
    }

    public Template getTemplate(File source) {
        Hashtable<File, Template> templateCache = getTemplateCache();
        if (templateCache.containsKey(source)) {
            return templateCache.get(source);
        } else {
            Template data;
            templateCache.put(source, data = new Template(source));
            return data;
        }
    }

    @Override
    public void stop() {
        timer.cancel();
        super.stop();
    }

    public Hashtable<String, MimeTypeDriver> getMimeTypeDriverTable() {
        return (Hashtable<String, MimeTypeDriver>) configuration.get("mimeTypeDriverTable");
    }

    /**
     * Provides a basic-mime-type-driver-system.
     *
     * Drivers work with existing, or non-existing files, and custom implementations.
     *
     * Some mime-types may have multiple formats, thus, multiple-processors.
     * if a driver doesn't handle a format, it returns null, and the server-backend,
     * selects the next driver in the linked list, repeating the process until
     * the request is completed or no drivers succeed.
     *
     * The server's response to a failed mime type driver chain is MEDIA_NOT_SUPPORTED
     *
     * execution-order: first come, first serve, per request.
     * circular references are not checked.
     * driver's can't be unloaded, because no method is provided.
     */
    public static class MimeTypeDriver implements IResponseHandler {
        private MimeTypeDriver next;
        // override this method.
        public Template.Filler getTemplateFiller(){return null;};
        public Response generateServiceResponse(WebServer webServer, File file, String mimeType, IHTTPSession ihttpSession){
            return null;
        }
    }

    /**
     * Registers a custom-mime-type-driver.
     *
     * Drivers are loaded in update=override order.
     *
     * The mime-type does not have to exist in the server's-mime-type-map.
     * This allows custom back-ends to be called, by each plugin that can access
     * the server's getMimeTypeResponse method.
     *
     * This method does not affect the server's-mime-type-map.
     *
     * @param mimeType
     * @param driver
     */
    public final void registerMimeTypeDriver(String mimeType, MimeTypeDriver driver) {
        Hashtable<String, MimeTypeDriver> mimeTypeDriverTable = getMimeTypeDriverTable();
        if (mimeTypeDriverTable.containsKey(mimeType)) {
            driver.next = mimeTypeDriverTable.get(mimeType);
            mimeTypeDriverTable.put(mimeType, driver);
            return;
        }
        mimeTypeDriverTable.put(mimeType, driver);
    }

    private Hashtable<String, Template.Filler> getTemplateFillerTable(){
        return (Hashtable<String, Template.Filler>) configuration.get("templateFillerTable");
    }

    public final void registerTemplateFiller(String mimeType, Template.Filler filler) {
        getTemplateFillerTable().put(mimeType, filler);
    }

    public WebServer() {

        Stack<String> staticIndexFiles;

        configuration.put("mimeTypeMap", new MimeTypeMap());
        configuration.put("staticIndexFiles", staticIndexFiles = new Stack<>());
        configuration.put("mimeTypeReaders", new Stack<>());
        configuration.put("templateMimeTypes", new Stack<>());
        configuration.put("templateCache", new Hashtable<>());
        configuration.put("mimeTypeDriverTable", new Hashtable<>());
        configuration.put("templateFillerTable", new Hashtable<>());

        configuration.put("documentRoot", new File("."));

        // this field is public...
        staticIndexFiles.add("index.html");
        staticIndexFiles.add("index.htm");
        staticIndexFiles.add("index.xml");

        addTemplateMimeType(MIME_HTML);

        getMimeTypeReaders().add(new MimeTypeReader() {
            @Override
            public String getMimeTypeMagic(RandomAccessFile data) {
                String line;
                try {
                    line = data.readLine();
                    if (line.startsWith("//->mime-type: "))
                        return line.split(": ")[1];
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });

    }

    public void start(String hostname, int port) throws IOException {
        configuration.put(HOST_KEY, hostname);
        configuration.put(PORT_KEY, port);
        start();
    }

    public File getDocumentRoot() {
        return (File) configuration.get("documentRoot");
    }

    public void setDocumentRoot(String documentRoot) {
        setDocumentRoot(new File(documentRoot));
    }

    public void setDocumentRoot(File documentRoot) {
        if (this.wasStarted())
            throw new IllegalStateException("cannot change the web-server-document-root after starting the service");
        configuration.put("documentRoot", documentRoot);
    }

    public boolean blacklistRequest(String uri, File file, String mimeType, IHTTPSession session) {
        return false;
    }

    public File locateServerFile(String uri) {
        return new File(getDocumentRoot(), uri);
    }

    public String getFileExtensionMimeType(String extension) {
        return getMimeTypeMap().get(extension);
    }

    public String getFileExtension(File file) {
        return getFileExtension(file.getName());
    }

    public String getFileExtension(String path) {
        return getMimeTypeMap().getFileExtension(path);
    }

    public String mimeTypeMagic(@NotNull File check) {
        String mimeType = null;
        if (check.isDirectory()) return "text/directory";
        else if (check.exists()) {
            RandomAccessFile stream = null;
            try {
                stream = new RandomAccessFile(check, "r");
                for(MimeTypeReader reader: getMimeTypeReaders()) {
                    mimeType = reader.getMimeTypeMagic(stream);
                    if (mimeType != null) { break; }
                    stream.seek(0);
                }
                stream.close();
            } catch (FileNotFoundException infinity) {} catch (IOException e) {
                throw new RuntimeException("Stream seek failed on: "+check, e);
            }
        }
        return (mimeType != null)?
                mimeType : getFileExtensionMimeType(getFileExtension(check));
    }

    public String listDirectory(File file, IHTTPSession query) {
        ByteArrayOutputStream listing = new ByteArrayOutputStream();
        Command shell;
        String program;
        if (System.getProperty("os.name").startsWith("Windows")) {
            shell = new Command("cmd", "/c");
            program = "dir";
        } else {
            shell = new Command("sh", "-c");
            program = "ls";
        }
        shell.setDirectory(file.getPath())
                .writeOutputTo(listing)
                    .writeErrorTo(listing);
        try {
            shell.start(program);
        } catch (Exception e) {e.printStackTrace(new PrintStream(listing));}

        return listing.toString();
    }

    /**
     * Override this method, to serve custom index files.
     * @param directory the existing-file-directory to serve
     * @param query the user's query
     * @return the server's response
     */
    public Response serveDirectory(File directory, IHTTPSession query) {
        for(String indexType : getStaticIndexFiles()) {
            File test = new File(directory, indexType);
            if (test.exists()) return serveFile(test, mimeTypeMagic(test), query);
        }
        return plainTextResponse(Status.OK, listDirectory(directory, query));
    }

    public Response getMimeTypeResponse(File file, String mimeType, IHTTPSession query) {
        Hashtable<String, MimeTypeDriver> mimeTypeDriverTable = getMimeTypeDriverTable();
        MimeTypeDriver mimeTypeDriver = mimeTypeDriverTable.get(mimeType);
        Response out = null;

        while (out == null) {
            if (mimeTypeDriver == null) break;
            out = mimeTypeDriver.generateServiceResponse(this, file, mimeType, query);
            if (out != null) return out;
            mimeTypeDriver = mimeTypeDriver.next;
        }

        if (mimeTypeDriverTable.containsKey(mimeType)) {
            // all processors failed
            // we don't know at this point if information within the file is
            // sensitive, or capable of transmission, so we won't serve anything.
            return blankResponse(Status.UNSUPPORTED_MEDIA_TYPE);
        }

        return null;

    }

    /**
     * Serves an existing file.
     *
     *
     * @param file the server-file requested
     * @param mimeType the server's knowledge of the file's type
     * @param query the user's query
     * @return the server's response
     */
    public Response serveFile(File file, String mimeType, IHTTPSession query) {

        Response magic = getMimeTypeResponse(file, mimeType, query);
        if (magic != null) return magic;

        if (isTemplateMimeType(mimeType)) {
            Hashtable<String, Template.Filler> table = getTemplateFillerTable();
            Template.Filler filler = table.get(mimeType);
            if (filler == null) return staticFileResponse(file, mimeType, query);
            Template template = getTemplate(file);
            return  stringResponse(Status.OK, mimeType, template.fill(filler));
        }

        return staticFileResponse(file, mimeType, query);

    }

    /**
     * Critical preliminary response logic.
     * @param query the user's query
     * @return the server's response
     */
    @Override
    protected Response serviceRequest(IHTTPSession query) {
        try {
            String uri = query.getUri().substring(1);
            File file = locateServerFile(uri);
            String mimeType = mimeTypeMagic(file);
            if(blacklistRequest(uri, file, mimeType, query)) return forbiddenResponse();
            return (file.isDirectory())?
                    serveDirectory(file, query) : serveFile(file, mimeType, query);
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return plainTextResponse(Status.OK, sw.toString());
        }
    }

}
