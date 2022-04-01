package io.cmichel.appLauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.app.ActivityManager;

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ReactNativeAppLauncher", "signal recived");
        String alarmID = intent.getAction();
          try{
              if(isMyServiceRunning(context,MyIntentService.class)){
                  Log.e("ReactNativeAppLauncher", "service is running ");
              }else{
                  Log.e("ReactNativeAppLauncher", "starting service: ");
                  Intent i = new Intent(context, MyIntentService.class);
                  i.putExtra("foo", "bar");
                  context.startService(i);
              }

          }catch (Exception e) {
            Log.e("ReactNativeAppLauncher", "Failed to start service: " + e.getMessage());
        }
       // launchApplication(context, alarmID);
    }
    private boolean isMyServiceRunning(Context context,Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            Log.e("ReactNativeAppLauncher", serviceClass.getName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    private void launchApplication(Context context, String alarmID) {
        String packageName = context.getApplicationContext().getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);

        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        launchIntent.putExtra("alarmID", alarmID);

        context.startActivity(launchIntent);
        Log.i("ReactNativeAppLauncher", "AlarmReceiver: Launching: " + packageName);
    }
}
