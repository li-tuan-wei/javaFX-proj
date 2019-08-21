package common;

import java.io.*;

/**
 * Created by ldh on 2018/3/26.
 */
public class PipedTest {

    private PipedInputStream pipedInputStream = new PipedInputStream();
    private BufferedReader reader = new BufferedReader(new InputStreamReader(pipedInputStream));

    private PipedOutputStream pipedOutputStream = new PipedOutputStream();
    private BufferedWriter stringWriter = new BufferedWriter(new OutputStreamWriter(pipedOutputStream));

    public PipedTest() throws Exception {
        pipedInputStream.connect(pipedOutputStream);
        new Thread(()->{
            try {
                for (int i=0; i<100; i++) {
                    stringWriter.write("sdafsa " + i);
                    stringWriter.newLine();
//                    Thread.sleep(100);
                }
                stringWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(()->{
            try {
                for (int i=0; i<100; i++) {
                    String line = reader.readLine();
                    System.out.println(line);
                }
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        new PipedTest();
    }

}
