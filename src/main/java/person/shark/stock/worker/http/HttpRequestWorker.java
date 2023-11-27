package person.shark.stock.worker.http;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class HttpRequestWorker {


    public String sendHttpsGetRequest(String urlString) throws IOException, KeyManagementException, NoSuchAlgorithmException {
        URL url = new URL(urlString);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();

        SSLContext sslContext = SSLContext.getInstance("SSL");
        TrustManager[] trustManagers = {new SharkX509TrustManager()};
        sslContext.init(null, trustManagers, new java.security.SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        httpsURLConnection.setDoOutput(true);
        httpsURLConnection.setDoInput(true);
        httpsURLConnection.setUseCaches(false);
        httpsURLConnection.setRequestMethod("GET");
        httpsURLConnection.setSSLSocketFactory(sslSocketFactory);
        httpsURLConnection.connect();

        InputStream inputStream = httpsURLConnection.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line).append("\n");
        }
        return stringBuilder.toString();
    }
}
