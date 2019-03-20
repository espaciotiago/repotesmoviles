package utilities;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Tiago on 15/08/16.
 */
public class JSONParser {

    /**
     * Logical elemnts
     */
    private Context context;

    /**
     * Constructor
     * @param context
     */
    public JSONParser(Context context){
        this.context = context;
    }


    /**
     * Send a POST request to the server
     * @param data
     * @param urlStr
     * @return
     */
    public JSONObject sendPOST(JSONObject data, String urlStr){
        try {

            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setConnectTimeout(Constants.TIMEOUTS);
            httpConn.setReadTimeout(Constants.TIMEOUTS);
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("POST");
            httpConn.connect();

            OutputStream os = httpConn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(data.toString());
            osw.flush();
            osw.close();

            InputStream is = httpConn.getInputStream();
            String parsedString = Constants.convertinputStreamToString(is);

            Log.e("Stream",parsedString);
            JSONObject jsnobject = new JSONObject(parsedString);

            return jsnobject;

        } catch (SocketTimeoutException se){
            Log.e("TIMEOUT","Timeout error");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Send a GET request to the server
     * @param urlStr
     * @return
     */
    public JSONObject sendGET(String urlStr){
        try {

            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection();
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setConnectTimeout(Constants.TIMEOUTS);
            httpConn.setReadTimeout(Constants.TIMEOUTS);
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();

            InputStream is = httpConn.getInputStream();
            String parsedString = Constants.convertinputStreamToString(is);

            Log.d("Stream",parsedString);
            JSONObject jsnobject = new JSONObject(parsedString);

            return jsnobject;

        } catch (SocketTimeoutException se){
            Log.e("TIMEOUT","Timeout error");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
