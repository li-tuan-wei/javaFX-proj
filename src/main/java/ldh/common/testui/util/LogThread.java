package ldh.common.testui.util;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.springframework.context.ApplicationContext;

import java.io.*;

/**
 * Created by ldh on 2018/3/26.
 */
public class LogThread {

    private static LogThread instance = new LogThread();

    private PipedReader pipedInputStream = new PipedReader();
    private BufferedReader reader = new BufferedReader(pipedInputStream);
    private TextArea textArea = null;

    public static LogThread getInstance() {
        return instance;
    }

    private LogThread() {
//        LogUtil.connect(pipedInputStream);
        new Thread(()->{
            readLog();
        }).start();
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    public void close() {
        try {
            reader.close();
            pipedInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readLog() {
        System.out.println("log!!!!!!!!!!!!");
        String str = null;
        try {
            while((str=reader.readLine())!=null){
                if (textArea != null) {
                    final String line = str;
                    Platform.runLater(()->textArea.appendText(line + "\n\r"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
