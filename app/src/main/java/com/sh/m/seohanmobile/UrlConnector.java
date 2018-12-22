package com.sh.m.seohanmobile;


import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlConnector extends Thread {
    String temp;
    String urlStr = null;
    public UrlConnector(String urlString){
        urlStr = urlString;
    }

    public void run() {
        // http 요청을 쏴서 그에 대한 결과값을 받아옵니다.
        final String output = request(urlStr);
        // 결과값이 temp에 담깁니다.
        temp = output;
    }

    public String getResult(){
        return temp;
    }

    private String request(String urlStr) {
        StringBuilder output = new StringBuilder();
        Log.d("testrequest", urlStr);
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                Log.d("testConnUrlStr", urlStr);
                int resCode = conn.getResponseCode();
                if (resCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream())) ;
                    String line = null;
                    while(true) {
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        output.append(line + "\n");
                    }

                    reader.close();
                    conn.disconnect();
                }
            }
        } catch(Exception ex) {
            Log.e("SampleHTTP", "Exception in processing response.", ex);
            ex.printStackTrace();
        }

        return output.toString();
    }
}
