package org.buysa.functions.neworder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;

public class Slack {
    String url = "https://hooks.slack.com/services/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";;

    Slack(){
    }

    public void slackSend(String message) throws IOException {
        URL url = new URL(this.url);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("POST");

            httpConn.setRequestProperty("Content-type", "application/json");

            httpConn.setDoOutput(true);

        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
            writer.write(message);
            writer.flush();
            writer.close();
            httpConn.getOutputStream().close();

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
            System.out.println(response);
    }
}
