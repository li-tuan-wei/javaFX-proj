package ldh.common.testui.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextArea;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/3/26.
 */
public class LogUtil {

//    private static PipedWriter pipedOutputStream = new PipedWriter();
//    private static BufferedWriter stringWriter = new BufferedWriter(pipedOutputStream);
//    private static volatile boolean isConnectioned = false;
    private static volatile TextArea textArea = null;
    private static volatile HTMLEditor htmlEditor = null;
    private static volatile WebView webView = null;
    private final static Logger logger = Logger.getLogger(LogUtil.class.getSimpleName());

    private static List<String> dataList = new LinkedList<>();

    public static void setTextArea(TextArea textArea) {
        LogUtil.textArea = textArea;
    }

    public static void setTextArea(WebView webView) {
        LogUtil.webView = webView;
    }

    public static void setTextArea(HTMLEditor textArea) {
        LogUtil.htmlEditor = textArea;
    }

    public static void log(String str) {
        String s = str;
        int t = 0;
        while (s.startsWith("\t")) {
            s = s.substring(1);
            t += 25;
        }
        log(s, t);
    }

    public static void logAppend(String str) {
        log(str, 50);
    }

    public static void logAppend2(String str) {
        log(str, 75);
    }

    public static void logAppend2(String str, boolean isSuccess) {
        if (isSuccess) {
            log(str, 75);
        } else {
            log(str, 75, "red");
        }
    }

    public static void logAppendError(String str) {
        log(str, 50, "red");
    }

    public static void logAppend2Error(String str) {
        log(str, 75, "red");
    }

    public static void log(String str, int textIndent) {
        logger.info(str);
        String newStr = String.format("<span style='display:inline-block;text-indent:%spx;'>%s</span>", textIndent, str);
        logString(newStr);
    }

    public static void log(String str, int textIndent, String color) {
        logger.info(str);
        String newStr = String.format("<span style='display:inline-block;text-indent:%spx;color: %s;'>%s</span>", textIndent, color, str);
        logString(newStr);
    }

    public static void logString(String str) {
        dataList.add(str);

        String data = dataList.stream().collect(Collectors.joining("<br/>"));
        Platform.runLater(()->{
//            htmlEditor.setHtmlText(data);
            final WebEngine engine = webView.getEngine();
            engine.setJavaScriptEnabled(true);
            String webViewContents = (String) engine
                    .executeScript("document.documentElement.outerHTML");
            String appendContent = data + "</body>";

            StringBuilder scrollHtml = scrollWebView(0, 99999);

            engine.loadContent(scrollHtml  + appendContent);
        });
    }

    public static StringBuilder scrollWebView(int xPos, int yPos) {
        StringBuilder script = new StringBuilder().append("<html>");
        script.append("<head>");
        script.append("   <script language=\"javascript\" type=\"text/javascript\">");
        script.append("       function toBottom(){");
        script.append("           window.scrollTo(" + xPos + ", " + yPos + ");");
        script.append("       }");
        script.append("   </script>");
        script.append("</head>");
        script.append("<body onload='toBottom()'>");
        return script;
    }

    public static void clean() {
        dataList.clear();
    }

//    public static void connect(PipedReader pipedInputStream) {
//        try {
//            pipedInputStream.connect(pipedOutputStream);
//            isConnectioned = true;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void log2(String str) {
//        if (isConnectioned) {
//            try {
//                stringWriter.write(str);
//                stringWriter.newLine();
//                stringWriter.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void close() {
//        try {
//            pipedOutputStream.close();
//            stringWriter.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
