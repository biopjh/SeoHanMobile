package com.sh.m.seohanmobile.fcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.annotation.RequiresPermission;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.sh.m.seohanmobile.Manifest;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService{
    private static final  String TAG = "MyFirebaseIDService";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Start sendRegistrationToServer");

        String phonenumber = getPhoneNumber();
        Log.d("phonenumber",phonenumber);

        sendRegistrationToServer(refreshedToken, phonenumber);


    }


    private void sendRegistrationToServer(String token, String phonenumber)
    {

/*        // 만들어진 토큰을 저장한다
        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("token", token);
        editor.commit();
        Log.d("myToken", token);*/
Log.d("myToken", token);

       OkHttpClient client = new OkHttpClient();
       RequestBody body = new FormBody.Builder()
                .add("mtoken", token)
                .add("idUser", "testUserId")
                .add("nmPhone", phonenumber)
               .add("noPhone", phonenumber)
                .build();
        Request request = new Request.Builder().url("http://op.seo-han.co.kr/common/setUserMobileToken").post(body).build();
        try {
            Log.d("toKen",token);
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getPhoneNumber() {

        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber ="";

        try {
            if (telephony.getLine1Number() != null) {
                phoneNumber = telephony.getLine1Number();
            }
            else {
                if (telephony.getSimSerialNumber() != null) {
                    phoneNumber = telephony.getSimSerialNumber();
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if(phoneNumber.startsWith("+82")){
            phoneNumber = phoneNumber.replace("+82", "0"); // +8210xxxxyyyy 로 시작되는 번호
        }
        phoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);

        return phoneNumber;
    }


}
