package com.nehsus.cowinbooker;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.nehsus.cowinbooker.api.RequestObject;
import com.nehsus.cowinbooker.api.RequestType;
import com.nehsus.cowinbooker.api.URLRequest;

import java.io.IOException;

import static com.nehsus.cowinbooker.utils.Constant.REQUEST_OTP;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * MainActivity for CoWIN Booker
 *
 * @author Sushen Kumar
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private Button otpButton;
    private EditText phoneText;
    private TextView otpView;
    private URLRequest api;
    private String phoneNumber;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.otpButton = findViewById(R.id.id_request_otp);
        this.phoneText = findViewById(R.id.id_phone);
        this.otpView = findViewById(R.id.id_otp_view);
        this.api = new URLRequest();

        this.otpButton.setOnClickListener(v -> {
            if (isPhoneEntered()) {
                this.requestOTP();
            }
        });

    }

    private boolean isPhoneEntered() {
        String text = this.phoneText.getText().toString();
        if (!text.equals("")) {
            Log.i(TAG, "Phone: " + text);
            this.phoneNumber = text;
            return true;
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void requestOTP() {
        HashMap<String, String> requestMap = new HashMap<>();
        requestMap.put("mobile", this.phoneNumber);

        Thread thread = new Thread(() -> {
            try  {
                JsonObject otpResponse = api.makeRequest(
                        RequestType.POST,
                        RequestObject.AUTH,
                        REQUEST_OTP,
                        null,
                        requestMap);
                assert otpResponse != null;
                String txnID = otpResponse.get("txnId").toString();
                Log.i(TAG, "Transaction ID: " + txnID);

                this.confirmOTP(txnID, getOtpFromMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void confirmOTP(String txnId, String otp) throws IOException, NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] otpHASH = digest.digest(otp.getBytes(StandardCharsets.UTF_8));

        LinkedHashMap confirmMap = new LinkedHashMap();
        confirmMap.put("otp", otpHASH);
        confirmMap.put("txnId", txnId);
        JsonObject otpResponse = api.makeRequest(
                RequestType.POST,
                RequestObject.AUTH,
                REQUEST_OTP,
                null,
                confirmMap);
        assert otpResponse != null;
        Log.i(TAG, "OTP Confirmed Response: " + otpResponse);
    }

    private String getOtpFromMessage() {
        return "";
    }
}