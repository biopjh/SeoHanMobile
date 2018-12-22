package com.sh.m.seohanmobile.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.sh.m.seohanmobile.MainActivity;
import com.sh.m.seohanmobile.R;

import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService{

    Bitmap bigPicture;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        /*Map<String, String> pushDataMap = remoteMessage.getData();
        sendNotification(pushDataMap);*/

        // 이거 추가 하면
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE );
        PowerManager.WakeLock wakeLock = pm.newWakeLock( PowerManager.SCREEN_DIM_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG" );
        wakeLock.acquire(5000);


        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        String imgUrl = remoteMessage.getData().get("imgurllink");

    Log.d("testimgUrl", imgUrl);
        sendNotification(title, body, imgUrl);

    }
/*    private void sendNotification(Map<String, String> dataMap) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder nBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(dataMap.get("title"))
                .setContentText(dataMap.get("msg"))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{1000, 1000})
                .setLights(Color.WHITE, 1500, 1500)
                .setContentIntent(contentIntent);

        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(0 *//* ID of notification *//*, nBuilder.build());
    }*/

    private void sendNotification(String title, String body, String imgUrl) {

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1){
            /**
             * 누가버전 이하 노티처리
             */
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);


            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            //이미지 온라인 링크를 가져와 비트맵으로 바꾼다.
            try {
                URL url = new URL(imgUrl);
                bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }


            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.logo).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                    .setContentTitle(title)
                    .setContentText("알림탭을 아래로 천천히 드래그 하세요.")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setVibrate(new long[]{1000, 1000})
                    .setLights(Color.BLUE,1,1)
                    /*//BigTextStyle
                    .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(body))
                    */
                    //이미지를 보내는 스타일 사용하기
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bigPicture)
                            .setBigContentTitle(title)
                            .setSummaryText(body))

                    .setContentIntent(pendingIntent);
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());


        }else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d("test","오레오 이상 메시징");
            /**
             * 오레오 이상 노티처리
             */
            /**
             * 오레오 버전부터 노티를 처리하려면 채널이 존재해야합니다.
             */

            //Toast.makeText(getApplicationContext(),"오레오이상",Toast.LENGTH_SHORT).show();
            /**
             * 오레오 이상 노티처리
             */
//                    BitmapDrawable bitmapDrawable = (BitmapDrawable)getResources().getDrawable(R.mipmap.ic_launcher);
//                    Bitmap bitmap = bitmapDrawable.getBitmap();
            /**
             * 오레오 버전부터 노티를 처리하려면 채널이 존재해야합니다.
             */

            int importance = NotificationManager.IMPORTANCE_HIGH;
            String Noti_Channel_ID = "Noti";
            String Noti_Channel_Group_ID = "Noti_Group";

            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(Noti_Channel_ID,Noti_Channel_Group_ID,importance);

//                    notificationManager.deleteNotificationChannel("testid"); 채널삭제

            /**
             * 채널이 있는지 체크해서 없을경우 만들고 있으면 채널을 재사용합니다.
             */
            if(notificationManager.getNotificationChannel(Noti_Channel_ID) != null){
                Log.d("test","채널이 이미 존재합니다.");
                //Toast.makeText(getApplicationContext(),"채널이 이미 존재합니다.",Toast.LENGTH_SHORT).show();
            }
            else{
                //Toast.makeText(getApplicationContext(),"채널이 없어서 만듭니다.",Toast.LENGTH_SHORT).show();
                Log.d("test","채널이 없어서 만듭니다.");
                notificationManager.createNotificationChannel(notificationChannel);
            }

            notificationManager.createNotificationChannel(notificationChannel);




            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            //이미지 온라인 링크를 가져와 비트맵으로 바꾼다.
            try {
                URL url = new URL(imgUrl);
                bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }


            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(),Noti_Channel_ID)
                    .setSmallIcon(R.drawable.logo).setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))
                    .setWhen(System.currentTimeMillis()).setShowWhen(true)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentTitle(title)
                    .setContentText("알림탭을 아래로 천천히 드래그 하세요.")
                    .setSound(defaultSoundUri)
                    .setVibrate(new long[]{1000, 1000})
                    .setLights(Color.BLUE,1,1)
                    /*//BigTextStyle
                    .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(title).bigText(body))
                    */
                    //이미지를 보내는 스타일 사용하기
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(bigPicture)
                            .setBigContentTitle(title)
                            .setSummaryText(body))
                    .setContentIntent(pendingIntent);
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

  /*          NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),Noti_Channel_ID)
                    .setLargeIcon(null).setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(System.currentTimeMillis()).setShowWhen(true).
                            setAutoCancel(true).setPriority(NotificationCompat.PRIORITY_MAX)
                    .setContentTitle("노티테스트!!");
                    notificationManager.notify(0,builder.build());
                    */


        }



    }

}
