package box.star.web;

import box.star.Parameter;
import box.star.io.Streams;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ResourceBundle;

public class Voyager extends Application {

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    URL location = getClass().getResource("Voyager.fxml");
    FXMLLoader fxmlLoader = new FXMLLoader(location);
    AnchorPane root = fxmlLoader.load();

    FXControl control = fxmlLoader.getController();
    control.setStage(primaryStage);
    String[] parameters = control.getParameterArray(getParameters().getRaw());
    Parameter.parse(control, parameters);
    control.loadSource();

    primaryStage.setTitle("Voyager");
    primaryStage.setScene(new Scene(root));
    primaryStage.show();
  }

  public static class FXControl implements Initializable, Parameter.Parser {

    private final static int SOURCE_STDIN = 0, SOURCE_HTML = 1, SOURCE_FILE = 2, SOURCE_URL = 3, SOURCE_XML = 4, SOURCE_TEXT = 5;

    private Stage stage;
    private String source = "-", userAgent = "Voyager";
    private boolean allowRiskySSL, allowJavaScript = true;
    @FXML
    WebView webView;
    private WebEngine webEngine;

    private static void useAnySSLKey() {
      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
            }
          }
      };

      // Install the all-trusting trust manager
      try {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      }
      catch (GeneralSecurityException e) {}

    }

    public void setStage(Stage stage) {
      this.stage = stage;
    }

    String[] getParameterArray(List<String> raw) {
      String[] out = new String[raw.size()];
      return raw.toArray(out);
    }

    int detectSource() {
      if (source == "-") return SOURCE_STDIN;
      if (source.matches(".*\\.html?$")) return SOURCE_FILE;
      if (source.matches(".*<(HTML|html).*")) return SOURCE_HTML;
      if (source.matches("^...+://.*")) return SOURCE_URL;
      return -1;
    }

    @Override
    public boolean acceptParameter(Parameter.State currentParameter) {
      if ("-s".equals(currentParameter.value)) {
        source = Parameter.getParameterValue(currentParameter);
        return true;
      }
      if ("-h".equals(currentParameter.value)) {return allowRiskySSL = true;}
      return false;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
      webEngine = webView.getEngine();

      // Update the stage title when a new web page title is available
      webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<Worker.State>() {
        public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {

          if (newState == Worker.State.FAILED) {
            throw new RuntimeException("worker failed");
          }
          if (newState == Worker.State.SUCCEEDED) {
            String title = webEngine.getTitle();
            if (title != null) stage.setTitle(title);
           // JSObject window = (JSObject) webEngine.executeScript("window");
           // window.setMember("jfxApp", this);
           // window.setMember("jfxStage", stage);
           // window.setMember("jfxScript", new ScriptEngineManager());
          }
        }
      });
    }

    void loadSource() {

      // Enable Javascript.
      webEngine.setJavaScriptEnabled(allowJavaScript);
      webEngine.setUserAgent(userAgent);

      if (allowRiskySSL) useAnySSLKey();
      try {
        switch (detectSource()) {
          case SOURCE_STDIN: {
            webEngine.load(Streams.readWholeString(System.in));
            return;
          }
          case SOURCE_FILE: {
            webEngine.load(new File(source).toURI().toString());
            return;
          }
          case SOURCE_HTML: {
            webEngine.loadContent(source, "text/html");
            return;
          }
          case SOURCE_URL: {
            webEngine.load(source);
            return;
          }
          default:
            throw new RuntimeException("unknown source type: " + source);
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

  }
}

/*

This code will remove the scrollbars of any page loaded in the WebView once it
is loaded (even when following links to other pages):

 engine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() { public void changed(ObservableValue<? extends State> o, State old, final State state) { if (state == State.SUCCEEDED) { System.out.println("Page loaded: " + engine.getLocation()); engine.executeScript("document.style.overflow = 'hidden';"); } } });

 */