package com.nehsus.cowinbooker.api;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import static com.nehsus.cowinbooker.utils.Constant.API_BASE;
import static com.nehsus.cowinbooker.utils.Constant.CONFIRM_OTP;
import static com.nehsus.cowinbooker.utils.Constant.REQUEST_OTP;
import static com.nehsus.cowinbooker.utils.Constant.SEARCH_SLOTS;

/**
 * URLRequest is the class that is used for all API operations.
 *
 * @author Sushen Kumar
 */
public class URLRequest {

    private final String TAG = "URLRequest";

    public URLRequest() {
    }

    private String getFullPath(String path, String pathType) {
        switch (pathType) {
            case REQUEST_OTP:
                return path+="/public/generateOTP";
            case CONFIRM_OTP:
                return path+="/public/confirmOTP";
            case SEARCH_SLOTS:
                return path+="/appointment/sessions/public/findByPin?";
        }
        return "";
    }

    // date: "", pincode
    public JsonObject makeRequest(RequestType requestType, RequestObject object, String pathType,
                              Map<String, String> queryParams, Map<String, String> bodyParams)
            throws IOException {
        String requestMethod = "";
        String requestBody;
        boolean setDoOutput = false;
        OutputStream os = null;
        OutputStreamWriter osw = null;

        String extraParams = getParamsString(queryParams);

        String path = API_BASE + object + extraParams;

        URL url = new URL(this.getFullPath(path, pathType));

        switch (requestType) {
            case GET:
                requestMethod = "GET";
                break;
            case POST:
                requestMethod = "POST";
                setDoOutput = true;
        }

        Log.i(TAG, "Request: " + requestType + "~~~" + url);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(15000);
        connection.setConnectTimeout(15000);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; PPC; en-US; rv:1.3.1)");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "*/*");

        connection.setDoOutput(setDoOutput);
        connection.setRequestMethod(requestMethod);

        if (setDoOutput) {
            os = connection.getOutputStream();
            osw = new OutputStreamWriter(os, "UTF-8");

            if (bodyParams != null) {
                requestBody = getBodyParams(bodyParams);
                Log.i(TAG, "Request Body: " + requestBody);
                osw.write(requestBody);
            }
        }

        if (queryParams != null && osw != null) {
            String params = getParamsString(queryParams);
            Log.i(TAG, "Request Query Parameters: " + params);
            osw.write(params);
        }

        // Back to connection code
        if (setDoOutput) {

            osw.flush();
            osw.close();

            os.close();
        }

        String line;
        InputStream is = connection.getInputStream();

        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        reader.setLenient(true);

        JsonObject responseObject = JsonParser.parseReader(reader).getAsJsonObject();

        Log.i(TAG,  "Response: " + responseObject);
        return responseObject;
    }

    /**
     * Helper function to convert Map to body parameters
     */
    private String getBodyParams(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null)
            return  "";

        StringBuilder body = new StringBuilder();

        int first = 0;
        body.append("{\"");
        for(Map.Entry<String, String> item : params.entrySet()) {
            if (first == 0)
                first = -1;
            else
                body.append(",");
            body.append(URLEncoder.encode(item.getKey(), "UTF-8"));
            body.append("\":\"");
            body.append(URLEncoder.encode(item.getValue(), "UTF-8"));
            body.append("\"");
            if (!item.equals(params.size()-1)) {
                // Last item
                body.append("}");
            }
        }
        Log.i(TAG, "Made body: " + body.toString());
        return body.toString();
    }

    /**
     * Helper function to convert Map to query parameters
     */
    private String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        if (params == null)
            return "";

        StringBuilder res = new StringBuilder();

        int first = 0;
        res.append("/");
        for (Map.Entry<String, String> item : params.entrySet()) {
            if (first == 0)
                first = -1;
            else
                res.append("/");
            res.append(URLEncoder.encode(item.getKey(), "UTF-8"));
            res.append("=");
            res.append(URLEncoder.encode(item.getValue(), "UTF-8"));
            if (!item.equals(params.size()-1)) {
                // Last item
                res.append("&");
            }
        }
        return res.toString();
    }
}