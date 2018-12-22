package com.sh.m.seohanmobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE,Manifest.permission.CALL_PHONE,Manifest.permission.SEND_SMS,Manifest.permission.INTERNET,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA};
    Context context;
    private Activity mainActivity = this;

    public SharedPreferences shortcutSharedPref;
    public boolean isInstalled;


    private Timer mTimer;//메일알림 타이머
    private SharedPreferences shareData;
    private boolean isShortCutInstalled;//바로가기 아이콘 생성여부

    //private static final String TYPE_IMAGE = "image/*";
    private static final String TYPE_IMAGE = "*/*";
    private static final int INPUT_FILE_REQUEST_CODE = 1;
    private ValueCallback<Uri> mUploadMessage;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    ImageButton btnHome, btnPrev, btnNext, btnRenew, btnConfig, btnExit;
    WebView webView;
    private String refreshedToken;
    private String myPhoneNumber;
    String urlHome = "http://op.seo-han.co.kr";
    //String urlHome = "http://192.168.1.15";

    private final Handler handler = new Handler();

    @SuppressLint("JavascriptInterface")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnHome = (ImageButton) findViewById(R.id.btnHome);
        btnPrev = (ImageButton) findViewById(R.id.btnPrev);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnRenew = (ImageButton) findViewById(R.id.btnRenew);
        btnConfig = (ImageButton) findViewById(R.id.btnConfig);
        btnExit = (ImageButton) findViewById(R.id.btnExit);
        webView = (WebView) findViewById(R.id.webView1);

        context = getApplicationContext();

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        Log.d("test", "권한체크");
        //권한:폰상태읽기
        int permissionReadPhoneState= ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
        if(permissionReadPhoneState == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            Log.d("testD","phoneState permission authorized :: " + permissionReadPhoneState);
        }


        //권한:전화걸기
        int permissionCallPhone= ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE);
        if(permissionCallPhone == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
        } else {
            Log.d("testD"," externalStorage permission authorized");
        }



        //권한:파일저장
        int permissionWriteExternalStorage= ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permissionWriteExternalStorage == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
        } else {
            Log.d("testD"," externalStorage permission authorized");
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //Splash
        startActivity(new Intent(this, SplashActivity.class));





        String urlHomeWithToken;
        refreshedToken = FirebaseInstanceId.getInstance().getToken();
        urlHomeWithToken = urlHome + "/?mtoken=" + refreshedToken;
        Log.d("url",urlHomeWithToken);



        // 타이머 세팅
        mTimer = new Timer();
        //mTimer.schedule(new CustomTimer(), 1000); // 한번 실행
        mTimer.schedule(new CheckNewMail(), 2000, 1000*60);


        //바로가기 생성
        shortcutSharedPref = getSharedPreferences("what", MODE_PRIVATE);
        isInstalled = shortcutSharedPref.getBoolean("isInstalled", false);
        Log.d("testShortCut installed:", "installed = " + isInstalled);
        //if (!isInstalled) {
            addAppIconToHomeScreen(this);
        //}







        if (savedInstanceState == null){
            webView.loadUrl(urlHomeWithToken);
        }
        webView.setWebViewClient(new CookWebViewClient(){});
        webView.setDownloadListener(new DownloadListener() {

            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                MimeTypeMap mtm = MimeTypeMap.getSingleton();
                Uri downloadUri = Uri.parse(url);
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    request.setMimeType(mimeType);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription("파일 다운로드");



                    // 파일 이름을 추출한다. contentDisposition에 filename이 있으면 그걸 쓰고 없으면 URL의 마지막 파일명을 사용한다.
                    String fileName = downloadUri.getLastPathSegment();
                    int pos = 0;
                    if ((pos = contentDisposition.toLowerCase().lastIndexOf("filename=")) >= 0) {
                        fileName = contentDisposition.substring(pos + 9);
                        pos = fileName.lastIndexOf(";");
                        if (pos > 0) {
                            fileName = fileName.substring(0, pos - 1);
                        }
                    }
                    // MIME Type을 확장자를 통해 예측한다.
                    String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
                    mimeType = mtm.getMimeTypeFromExtension(fileExtension);

                    request.setTitle(URLDecoder.decode(fileName));
                    request.setMimeType(mimeType);
                    //request.setTitle(URLDecoder.decode(URLUtil.guessFileName(url,contentDisposition,mimeType)));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "다운로드중", Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                    if (ContextCompat.checkSelfPermission(MainActivity.this,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Should we show an explanation?
                        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            Toast.makeText(getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    110);
                        } else {
                            Toast.makeText(getBaseContext(), "첨부파일 다운로드를 위해\n동의가 필요합니다.", Toast.LENGTH_LONG).show();
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    110);
                        }
                    }
                }
            }
        });
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        //webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //웹뷰내의 js의 window.open 허용
        webView.loadUrl(urlHomeWithToken);
        // Bridge 인스턴스 등록
        //webView.addJavascriptInterface(new AndroidBridge(), "shMobile");
        webView.addJavascriptInterface(new JavascriptInterface(), "shMobile");

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            // Hide the zoom controls for HONEYCOMB+
            webSettings.setDisplayZoomControls(false);
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH){
            webSettings.setTextZoom(100);
        }


        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onCloseWindow(WebView w) {
                super.onCloseWindow(w);
                finish();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                final WebSettings settings = view.getSettings();
                settings.setDomStorageEnabled(true);
                settings.setJavaScriptEnabled(true);
                settings.setAllowFileAccess(true);
                settings.setAllowContentAccess(true);
                view.setWebChromeClient(this);
                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(view);
                resultMsg.sendToTarget();
                return false;
            }

            // For Android Version < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                //System.out.println("WebViewActivity OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU), n=1");
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType(TYPE_IMAGE);
                startActivityForResult(intent, INPUT_FILE_REQUEST_CODE);
            }

            // For 3.0 <= Android Version < 4.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                //System.out.println("WebViewActivity 3<A<4.1, OS Version : " + Build.VERSION.SDK_INT + "\t openFC(VCU,aT), n=2");
                openFileChooser(uploadMsg, acceptType, "");
            }

            // For 4.1 <= Android Version < 5.0
            public void openFileChooser(ValueCallback<Uri> uploadFile, String acceptType, String capture) {
                Log.d(getClass().getName(), "openFileChooser : "+acceptType+"/"+capture);
                mUploadMessage = uploadFile;
                imageChooser();
            }

            // For Android Version 5.0+
            // Ref: https://github.com/GoogleChrome/chromium-webview-samples/blob/master/input-file-example/app/src/main/java/inputfilesample/android/chrome/google/com/inputfilesample/MainFragment.java
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                System.out.println("WebViewActivity A>5, OS Version : " + Build.VERSION.SDK_INT + "\t onSFC(WV,VCUB,FCP), n=3");
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;
                imageChooser();
                return true;
            }

            private void imageChooser() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.e(getClass().getName(), "Unable to create Image File", ex);
                    }

                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:"+photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }

                Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                contentSelectionIntent.setType(TYPE_IMAGE);

                Intent[] intentArray;
                if(takePictureIntent != null) {
                    intentArray = new Intent[]{takePictureIntent};
                } else {
                    intentArray = new Intent[0];
                }

                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);
            }
        });









        //홈버튼
        btnHome.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl(urlHome);
            }
        });

        //이전버튼
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goBack();
            }
        });

        //다음버튼
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.goForward();
            }
        });
        //새로고침
        btnRenew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });
        //알림
        btnConfig.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                webView.loadUrl(urlHome + "/?pcode=703");
            }
        });
        //종료
        btnExit.setOnClickListener(new OnClickListener() {


            @Override
            public void onClick(View v) {
                //finish();

                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog
                        .setTitle("종료 알림")
                        .setMessage("정말로 종료 하시겠습니까?")
                        .setPositiveButton("종료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this,"종료하지 않았습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).create().show();
            }
        });


    }



    //바로가기 아이콘 생성
    private void addAppIconToHomeScreen(Context context) {

        Log.d("test 아이콘생성시작","start");

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){

        }

        SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
        pref.getString("check", "");
        if(pref.getString("check", "").isEmpty()){
            Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
            shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            shortcutIntent.setClassName(context, getClass().getName());
            shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                    Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            Intent intent = new Intent();
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name)); //앱 이름
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(context, R.drawable.logo)); //앱 아이콘
            intent.putExtra("duplicate", false);
            intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            sendBroadcast(intent);
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("check", "exist");
        editor.commit();
/*


        shortcutIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcutIntent.setClassName(context, getClass().getName());
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getResources().getString(R.string.app_name)); //앱 이름
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, R.drawable.logo)); //앱 아이콘

        intent.putExtra("duplicate", false);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        sendBroadcast(intent);

        SharedPreferences.Editor editor = shortcutSharedPref.edit();
        editor.putBoolean("isInstalled", true);
        editor.commit();*/

        Log.d("test 아이콘생성완료","end");
    }
    public class CookWebViewClient extends WebViewClient{

       /* @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                startActivity(intent);
                view.reload();
                return true;
            }

            view.loadUrl(url);
            return true;

            //return super.shouldOverrideUrlLoading(view, request);
        }*/

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d("WEB_VIEW_TEST", "error code:" + errorCode + " - " + description);
        }
        @Override
       public boolean shouldOverrideUrlLoading(WebView view, String url) {
           if(url.startsWith("http://")){
               return false;
           }
           else {
               boolean override = false;
               Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
               intent.addCategory(Intent.CATEGORY_BROWSABLE);
               intent.putExtra(Browser.EXTRA_APPLICATION_ID, getPackageName());
               if (url.startsWith("sms:")) {
                   Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                   startActivity(i);
                   return true;
               }
               /*
               if (url.startsWith("tel:")) {
                       Intent i = new Intent(Intent.ACTION_CALL, Uri.parse(url));
                       startActivity(i);
                       return true;
               }*/
               if (url.startsWith("mailto:")) {
                   Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                   startActivity(i);
                   return true;
               }
               try{
                   startActivity(intent);
                   override = true;
               }
               catch(ActivityNotFoundException ex) {}
               return override;
           }
       }



    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return imageFile;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INPUT_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (mFilePathCallback == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Uri[] results = new Uri[]{getResultUri(data)};

                mFilePathCallback.onReceiveValue(results);
                mFilePathCallback = null;
            } else {
                if (mUploadMessage == null) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                Uri result = getResultUri(data);

                Log.d(getClass().getName(), "openFileChooser : "+result);
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        } else {
            if (mFilePathCallback != null) mFilePathCallback.onReceiveValue(null);
            if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
            mFilePathCallback = null;
            mUploadMessage = null;
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private Uri getResultUri(Intent data) {
        Uri result = null;
        if(data == null || TextUtils.isEmpty(data.getDataString())) {
            // If there is not data, then we may have taken a photo
            if(mCameraPhotoPath != null) {
                result = Uri.parse(mCameraPhotoPath);
            }
        } else {
            String filePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                filePath = data.getDataString();
            } else {
                filePath = "file:" + RealPathUtil.getRealPath(this, data.getData());
            }
            result = Uri.parse(filePath);
        }

        return result;
    }





    final class JavascriptInterface {
        @android.webkit.JavascriptInterface
        public void callMethodName(final String str){ // 반드시 final이어야 한다.
            Log.d("testLog", str);
            // 네트워크를 통한 작업임으로 백그라운드 스레드를 써서 작업해야한다.
            // 또한, 백그라운드 스레드는 직접 메인 뷰에 접근해 제어할 수 없음으로
            // 핸들러를 통해서 작업해야하는데
            // 이 때문에 한번에 handler.post()를 통해서 내부에 Runnable을 구현해 작업한다.
            handler.post(new Runnable() {
                @Override
                public void run() {
                    // handle를 통해서 화면에 접근하는 것임으로 가능함
                    //textView.setText("자바스크립트에서 전달받은 문자열을 쓴다 : " + str);
                    Log.d("testLog", str);
                }
            });
        }

        /**
         * 사용자 아이디와 토큰값을 저장한다.
         * @param userId
         */
        @android.webkit.JavascriptInterface
        public void setMobileToken(final String userId){ // 반드시 final이어야 한다.

            handler.post(new Runnable() {
                @Override
                public void run() {
                    // handle를 통해서 화면에 접근하는 것임으로 가능함
                    //textView.setText("자바스크립트에서 전달받은 문자열을 쓴다 : " + str);
                    Log.d("testLog setMobileToken ", userId);
                    //전화번호
                    String PhoneNum = getPhoneNumber();//telManager.getLine1Number();
                    if(PhoneNum.startsWith("+82")){
                        PhoneNum = PhoneNum.replace("+82", "0");
                    }



                    String postData = "userId=" + userId + "&noPhone="+ PhoneNum +"&dsMaker="+ Build.MANUFACTURER +"&dsModel="+ Build.MODEL +"&mToken=" + refreshedToken;
                    String postUrl = urlHome + "/mobile/setMobileToken?" + postData;
                    Log.d("testLog", postUrl);
                    UrlConnector urlConn = new UrlConnector(postUrl);
                    urlConn.start();
                    try {
                        urlConn.join();
                    }catch (InterruptedException e){

                    }
                    //String result = urlConn.getResult();
                    //Log.d("testResult", result);
                    //webView.postUrl(postUrl, postData.getBytes());
                }
            });
        }
    }

    // 첫 번째 TimerTask : 메일체크
    class CheckNewMail extends TimerTask {
        @Override
        public void run() {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd mm:ss");
            String currentTime = sdf.format(new Date());

            Log.d("testLog checkNewMail"," currentTime " + currentTime);

            if(refreshedToken != null){
                String postData = "mtoken=" + refreshedToken;// + userId;
                String postUrl = urlHome + "/mobile/setCheckNewMail?" + postData;

                String setMobileToken;
                UrlConnector urlConn = new UrlConnector(postUrl);
                urlConn.start();
                try {
                    urlConn.join();
                }catch (InterruptedException e){

                }
            }


/*
            Log.d("testTime",currentTime);
            */
        }
    }


    //전화번호 읽어오기
   private String getPhoneNumber() {
       int permissionReadPhoneState= ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE);
       if(permissionReadPhoneState == PackageManager.PERMISSION_DENIED) {
           ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
       } else {
           //권한이 허용되었을 때 처리
           TelephonyManager mgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
           try {

               String tmpPhoneNumber = mgr.getLine1Number();
               myPhoneNumber = tmpPhoneNumber.replace("+82", "0");

           } catch (Exception e) {
               myPhoneNumber = "";
           }
           Log.d("testD","phon Number permission authorized");
       }

        return myPhoneNumber;
}



    @Override
    protected void onDestroy() {
        //mTimer.cancel();//타이머 종료
        super.onDestroy();
    }



    private static final int REQUEST_EXTERNAL_STORAGE = 2;
    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_MICROPHONE = 3;
    private static final int REQUEST_READ_PHONE_STATE = 4;
    private static final int REQUEST_CALL_PHONE = 5;

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CALL_PHONE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CALL_PHONE)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            //Toast.makeText(getApplicationContext(), "카메라 권한 승인됨", Toast.LENGTH_LONG).show();
                            Log.d("testP","CALL_PHONE 권한 승인됨");
                        } else {
                            //Toast.makeText(getApplicationContext(), "카메라 권한 거부됨", Toast.LENGTH_LONG).show();
                            Log.d("testP","CALL_PHONE 권한 거부됨");
                        }
                    }
                }
                break;
            case REQUEST_READ_PHONE_STATE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.READ_PHONE_STATE)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            //Toast.makeText(getApplicationContext(), "카메라 권한 승인됨", Toast.LENGTH_LONG).show();
                            Log.d("testP","REQUEST_READ_PHONE_STATE 권한 승인됨");
                        } else {
                            //Toast.makeText(getApplicationContext(), "카메라 권한 거부됨", Toast.LENGTH_LONG).show();
                            Log.d("testP","REQUEST_READ_PHONE_STATE 권한 거부됨");
                        }
                    }
                }
                break;
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            //Toast.makeText(getApplicationContext(), "카메라 권한 승인됨", Toast.LENGTH_LONG).show();
                            Log.d("testP","CAMERA 권한 승인됨");
                        } else {
                            //Toast.makeText(getApplicationContext(), "카메라 권한 거부됨", Toast.LENGTH_LONG).show();
                            Log.d("testP","CAMERA 권한 거부됨");
                        }
                    }
                }
                break;
            case REQUEST_EXTERNAL_STORAGE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            Log.d("testP","READ_EXTERNAL_STORAGE 권한 승인됨");
                        } else {
                            Log.d("testP","READ_EXTERNAL_STORAGE 권한 거부됨");
                        }
                    }
                }
                break;
            case REQUEST_MICROPHONE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            Log.d("testP","RECORD_AUDIO 권한 승인됨");
                        } else {
                            Log.d("testP","RECORD_AUDIO 권한 거부됨");
                        }
                    }
                }
                break;
        }
    }

}
