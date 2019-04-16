package box.star.etc;

import box.star.io.Streams;

import java.io.IOException;
import java.io.InputStream;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class MimeTypeMap extends HashMap<String, String> {

    public static final String MIME_SEPARATOR = " ";
    public static final String DEFAULT_MIME_TYPE = "application/octet-stream";

    public void loadMimeTypesPropertyStream(InputStream stream) {
        Properties properties = new Properties();
        try {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException("can't load mime types from stream", e);
        } finally { try { stream.close(); } catch (Exception e){} }
        this.putAll((Map) properties);
    }

    public MimeTypeMap() {
        super(64);
        this.loadMimeTypesPropertyStream(
                Streams.getResourceAsStream(
                        "META-INF/mime-type/default-mimetypes.properties"
                )
        );
    }

    @Override
    public String put(String key, String value) {
        if (this.containsKey(key)) value = super.get(key) + MIME_SEPARATOR + value;
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        for (String key:map.keySet()) this.put(key, map.get(key));
    }

    @Override
    public String get(Object key) {
        if (this.containsKey(key)) return super.get(key);
        return DEFAULT_MIME_TYPE;
    }

    public String getFileExtension(String fileName) {
        String name = new java.io.File(fileName).getName();
        if (name.contains(".")) {
            for (String extension:super.keySet())
                if (name.endsWith(extension)) return extension;
            return name.substring(name.lastIndexOf('.') + 1);
        }
        return "";
    }

    public String getMimeTypeExtension(String mimeType) {
        if (mimeType != null || mimeType.length() != 0) {
            for (String extension:super.keySet())
                if (super.get(extension).contains(mimeType)) return extension;
        }
        return DEFAULT_MIME_TYPE;
    }

    public String[] parseMultiPartMimeType(String mimeType) {
        return mimeType.split(MIME_SEPARATOR);
    }

}
