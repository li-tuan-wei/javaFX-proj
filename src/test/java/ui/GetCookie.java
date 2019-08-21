package ui;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

import org.w3c.dom.html.HTMLDocument;

/**
 * 由 qq群(518914410)  群友 @basicfu (11332116) 贡献
 */
public class GetCookie extends Application {

    public static final String defaultURL = "https://www.baidu.com";

    @Override
    public void start(Stage primaryStage) {
        init(primaryStage);
        primaryStage.show();
    }

    private WebEngine engine;
    private CookieManager manager;
    private TextArea labelCookie;

    private void init(Stage primaryStage) {
        final Stage stage = primaryStage;
        Group group = new Group();
        primaryStage.setScene(new Scene(group));

        WebView webView = new WebView();
//核心代码
        manager = new CookieManager();
        java.net.CookieHandler.setDefault(manager);
        engine = webView.getEngine();
        engine.load(defaultURL);
// showCookie(defaultURL);

        final TextField textField = new TextField(defaultURL);
        engine.locationProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                textField.setText(newValue);
            }
        });
        engine.setJavaScriptEnabled(true);
        engine.confirmHandlerProperty();
        engine.javaScriptEnabledProperty();
        engine.getLoadWorker().stateProperty()
                .addListener(new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(
                            ObservableValue<? extends Worker.State> observable,
                            Worker.State oldValue, Worker.State newValue) {
                        System.out.println(newValue + "---" + oldValue);
                        if (newValue == Worker.State.SUCCEEDED) {
                            stage.setTitle(engine.getTitle());
                            JSObject window = (JSObject) engine
                                    .executeScript("window");
                            HTMLDocument doc = (HTMLDocument) window
                                    .getMember("document");
                            String cookie = doc.getCookie();
                            Map<String, String> cm = new HashMap<String, String>();
                            if (cookie != null && cookie.length() > 0) {
                                String[] cookies = cookie.split(";");
                                for (String item : cookies) {
                                    String key = item.substring(0,
                                            item.indexOf("="));
                                    cm.put(key.trim(), item.substring(item
                                            .indexOf("=") + 1));
                                }
                            }
                            CookieStore cs = manager.getCookieStore();
                            for (HttpCookie c : cs.getCookies()) {
                                if (!cm.containsKey(c.getName())) {
                                    cm.put(c.getName(), c.getValue());
                                }
                            }
                            StringBuffer sb = new StringBuffer();
                            sb.append(doc.getURL()+"\n");
                            for (Entry<String, String> item : cm.entrySet()) {
                                sb.append(item.getKey() + ":" + item.getValue());
                                sb.append("\n");
                            }
                            sb.append("\n\n\n\n\n");
                            System.out.println(sb.toString());
                            labelCookie.setText(sb.toString());
                        }
                    }
                });

        engine.setOnAlert(new EventHandler<WebEvent<String>>() {

            @Override
            public void handle(WebEvent<String> event) {
                System.out.println("this is event" + event);
            }
        });

// 加载新的地址
        EventHandler<ActionEvent> handler = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                manager.getCookieStore().removeAll();
                String url = (textField.getText().startsWith("http://") || textField
                        .getText().startsWith("https://")) ? textField
                        .getText().trim() : "http://"
                        + textField.getText().trim();
                engine.load(url);
// showCookie(url);
            }
        };

        textField.setOnAction(handler);

        Button okButton = new Button("go");
        okButton.setDefaultButton(true);
        okButton.setOnAction(handler);
        Button clearButton = new Button("清空");
        clearButton.setDefaultButton(true);
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                manager.getCookieStore().removeAll();
                labelCookie.setText("");
            }
        });

        HBox hbox = new HBox();
        hbox.getChildren().addAll(textField, okButton, clearButton);

        HBox.setHgrow(textField, Priority.ALWAYS);

        VBox vBox = new VBox();
        webView.setPrefHeight(400);
        vBox.getChildren().addAll(hbox, webView);
// VBox.setVgrow(webView, Priority.ALWAYS);

        labelCookie = new TextArea();
// labelCookie.setPrefHeight(100);
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setPrefHeight(180);

        VBox cBox = new VBox();
        sp.setContent(labelCookie);
        cBox.getChildren().addAll(vBox, sp);
        VBox.setVgrow(sp, Priority.ALWAYS);

        group.getChildren().add(cBox);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }


}
