package com.example.cathy.camera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;
import android.view.View;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static java.lang.Thread.sleep;

import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private final int REQUEST_PERMISSION_CAMERA = 100;

    private boolean mbFaceDetAvailable;
    private int miMaxFaceCount = 0;
    private int miFaceDetMode;

    private TextureView mTextureView = null;

    private Size mPreviewSize = null;
    private CameraDevice mCameraDevice = null;
    private CaptureRequest.Builder mPreviewBuilder = null;
    private CameraCaptureSession mCameraPreviewCaptureSession = null;
    private CameraCaptureSession mCameraTakePicCaptureSession = null;

    Socket socket_out;
    Socket socket_in;

    public static final String file_name = "/sdcard/Pictures/photo.jpg";

    private float pitchRate = 1f, speedRate = 1f;
    private String a;
    List<String>[] lists=new ArrayList[80];
    HashMap<String,ArrayList<String>> hashMap = new HashMap<>();
    final int WIDTH = 2560;
    final int HEIGHT= 1920;
    private TextToSpeech engine;

    public long startTime;
    public long endTime;
    public long runTime;


    //當UI的TextureView建立時，會執行onSurfaceTextureAvailable()
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //檢查是否取得camera權限
            if (askForPermissions())
                openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        engine = new TextToSpeech(this, this);

        //請求權限
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        mTextureView = (TextureView) findViewById(R.id.textureView);
        Button btnTakePicture = (Button) findViewById(R.id.btnTakePicture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTime = System.currentTimeMillis();//起始时间
                Log.d("startTime",Long.toString(startTime));
                askForPermissions();
                takePicture();

            }
        });

    }

    public boolean onKeyDown(int keycode,KeyEvent event){
        super.onKeyDown(keycode, event);
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER){
            askForPermissions();
            takePicture();
            return true;
        }
        return false;
    }

    private void takePicture() {
        if (mCameraDevice == null) {
            Toast.makeText(MainActivity.this, "Camera錯誤", Toast.LENGTH_LONG).show();
            return;
        }

        // 準備影像檔
        final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), "photo.jpg");

        // 準備OnImageAvailableListener
        ImageReader.OnImageAvailableListener imgReaderOnImageAvailable =
                new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        // 把影像資料寫入檔案
                        Image image = null;
                        try {
                            image = imageReader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);

                            OutputStream output = null;
                            try {
                                output = new FileOutputStream(file);
                                output.write(bytes);
                            } finally {
                                if (null != output)
                                    Log.d("output","123");
                                    output.flush();
                                    output.close();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (image != null){
                                Log.d("image","456");
                                image.close();
                                updownload.run();
                            }
                        }
                    }
                };

        // 取得 CameraManager
        CameraManager camMgr = (CameraManager) getSystemService(CAMERA_SERVICE);

        try {
            CameraCharacteristics camChar = camMgr.getCameraCharacteristics(mCameraDevice.getId());

            // 設定拍照的解析度
            Size[] jpegSizes = null;
            if (camChar != null)
                jpegSizes = camChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);

            int picWidth = 640;
            int picHeight = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                picWidth = jpegSizes[0].getWidth();
                picHeight = jpegSizes[0].getHeight();
            }

            // 設定照片要輸出給誰
            // 1. 儲存為影像檔； 2. 輸出給UI的TextureView顯示
            ImageReader imgReader = ImageReader.newInstance(picWidth, picHeight, ImageFormat.JPEG, 1);

            // 準備拍照用的thread
            HandlerThread thread = new HandlerThread("CameraTakePicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());

            // 把OnImageAvailableListener和thread設定給ImageReader
            imgReader.setOnImageAvailableListener(imgReaderOnImageAvailable, backgroudHandler);

            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(imgReader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder =
                    mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imgReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // 決定照片的方向（直的或橫的）
           /* SparseIntArray PICTURE_ORIENTATIONS = new SparseIntArray();
            PICTURE_ORIENTATIONS.append(Surface.ROTATION_0, 90);
            PICTURE_ORIENTATIONS.append(Surface.ROTATION_90, 0);
            PICTURE_ORIENTATIONS.append(Surface.ROTATION_180, 270);
            PICTURE_ORIENTATIONS.append(Surface.ROTATION_270, 180);

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, PICTURE_ORIENTATIONS.get(rotation));*/

            // 準備拍照的callback
            final CameraCaptureSession.CaptureCallback camCaptureCallback =
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);

                            Integer mode = result.get(CaptureResult.STATISTICS_FACE_DETECT_MODE);
                            Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
                            if (faces != null && mode != null)
                                Toast.makeText(MainActivity.this, "人臉: " + faces.length, Toast.LENGTH_SHORT).show();

                            // 播放快門音效檔
                           /* Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" +R.raw.sound_camera_shutter);
                            MediaPlayer mp = MediaPlayer.create(MainActivity.this, uri);
                            mp.start();*/

                            Toast.makeText(MainActivity.this, "拍照完成\n影像檔: " + file, Toast.LENGTH_SHORT).show();
                            startPreview();
                        }

                        @Override
                        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                        }
                    };
            // 最後一步就是建立Capture Session
            // 然後啟動拍照
            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            try {
                                closeAllCameraCaptureSession();

                                // 記下這個capture session，使用完畢要刪除
                                mCameraTakePicCaptureSession = cameraCaptureSession;

                                cameraCaptureSession.capture(captureBuilder.build(), camCaptureCallback, backgroudHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {

                        }
                    },
                    backgroudHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // 建立新的Camera Capture Session之前
    // 呼叫這個方法，清除舊的Camera Capture Session
    private void closeAllCameraCaptureSession() {
        if (mCameraPreviewCaptureSession != null) {
            mCameraPreviewCaptureSession.close();
            mCameraPreviewCaptureSession = null;
        }

        if (mCameraTakePicCaptureSession != null) {
            mCameraTakePicCaptureSession.close();
            mCameraTakePicCaptureSession = null;
        }
    }

    private void startPreview() {
        // 從UI元件的TextureView取得SurfaceTexture
        // 依照 camera的解析度，設定TextureView的解析度
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        // 依照TextureView的解析度建立一個 surface 給camera使用
        Surface surface = new Surface(surfaceTexture);

        // 設定camera的CaptureRequest和CaptureSession
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mPreviewBuilder.addTarget(surface);

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), mCameraCaptureSessionCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraCaptureSession.StateCallback mCameraCaptureSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            closeAllCameraCaptureSession();

            // 記下這個capture session，使用完畢要刪除
            mCameraPreviewCaptureSession = cameraCaptureSession;

            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            mPreviewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, miFaceDetMode);

            HandlerThread backgroundThread = new HandlerThread("CameraPreview");
            backgroundThread.start();
            Handler backgroundHandler = new Handler(backgroundThread.getLooper());

            try {
                mCameraPreviewCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Toast.makeText(MainActivity.this, "Camera預覽錯誤", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    openCamera();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openCamera() {
        //取得CameraManager
        CameraManager camMgr = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            //取得相機背後的camera
            String cameraId = camMgr.getCameraIdList()[0];
            CameraCharacteristics camChar = camMgr.getCameraCharacteristics(cameraId);

            //取得解析度
            StreamConfigurationMap map = camChar.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            //檢查是否有人臉偵測功能
            int[] iFaceDetModes = camChar.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
            if (iFaceDetModes == null) {
                mbFaceDetAvailable = false;
                Toast.makeText(MainActivity.this, "不支援人臉偵測", Toast.LENGTH_LONG).show();
            } else {
                mbFaceDetAvailable = false;
                for (int mode : iFaceDetModes) {
                    if (mode == CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE) {
                        mbFaceDetAvailable = true;
                        miFaceDetMode = CameraMetadata.STATISTICS_FACE_DETECT_MODE_SIMPLE;
                        break; //find the disiredmode,so stop searching
                    } else if (mode == CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL) {
                        //this is acandidate mode,keep searching
                        mbFaceDetAvailable = true;
                        miFaceDetMode = CameraMetadata.STATISTICS_FACE_DETECT_MODE_FULL;
                    }
                }
            }
            if (mbFaceDetAvailable) {
                miMaxFaceCount = camChar.get(CameraCharacteristics.STATISTICS_INFO_MAX_FACE_COUNT);
                Toast.makeText(MainActivity.this, "人臉偵測功能：" + String.valueOf(miFaceDetMode) + "\n人臉樹最大值：" + String.valueOf(miMaxFaceCount), Toast.LENGTH_LONG).show();
            }

            //啟動camera
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                camMgr.openCamera(cameraId, mCameraStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback mCameraStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
        }

        // Camera的CaptureSession狀態改變時執行
        private CameraCaptureSession.StateCallback mCameraCaptureSessionCallback = new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                closeAllCameraCaptureSession();

                // 記下這個capture session，使用完畢要刪除
                mCameraPreviewCaptureSession = cameraCaptureSession;

                mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                mPreviewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, miFaceDetMode);

                HandlerThread backgroundThread = new HandlerThread("CameraPreview");
                backgroundThread.start();
                Handler backgroundHandler = new Handler(backgroundThread.getLooper());

                try {
                    mCameraPreviewCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                Toast.makeText(MainActivity.this, "Camera預覽錯誤", Toast.LENGTH_LONG).show();
            }
        };

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Toast.makeText(MainActivity.this, "無法使用camera", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            Toast.makeText(MainActivity.this, "Camera開啟錯誤", Toast.LENGTH_LONG).show();
        }
    };

    private boolean askForPermissions() {
        //APP需要的功能權限
        String[] permissions = new String[]{
                Manifest.permission.CAMERA
        };
        //檢查是否已經取得權限
        final List<String> listPermissionsNeeded = new ArrayList<>();
        boolean bShowPermissionRationale = false;

        for (String p : permissions) {
            int result = ContextCompat.checkSelfPermission(MainActivity.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);

                //檢查是否需要顯示說明
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, p))
                    bShowPermissionRationale = true;
            }

            //向使用者詢問還沒有的許可權限
            if (!listPermissionsNeeded.isEmpty()) {
                if (bShowPermissionRationale) {
                    AlertDialog.Builder altDlgBuilder = new AlertDialog.Builder(MainActivity.this);
                    altDlgBuilder.setTitle("提示");
                    altDlgBuilder.setMessage("APP需要您的許可才能執行。");
                    altDlgBuilder.setCancelable(false);
                    altDlgBuilder.setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_PERMISSION_CAMERA);
                        }
                    });
                    altDlgBuilder.show();
                } else
                    ActivityCompat.requestPermissions(MainActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_PERMISSION_CAMERA);
                return false;
            }
            return true;
        }
        return true;
    }

    Runnable updownload = new Runnable() {
        @Override
        public void run() {
            try {
       //         int flag=0;
       //         for(int i=0;i<=1;i++){
       //             if(flag==0){
                        Thread pic_out = new Thread(client_picture);
                        pic_out.start();
                        pic_out.join();
        //            }
       //             else{
                        sleep(3800);
                        Thread txt_in = new Thread(client_txt);
                        txt_in.start();
                        txt_in.join();
       //             }
       //         }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    Runnable client_picture = new Runnable() {
        @Override
        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName("210.240.163.64");
                Log.d("Socket pic", "Client: Connecting...");

                try {

                    socket_out = new Socket(serverAddr, 5555);
                    Log.d("1","1");
                    OutputStream outputstream = socket_out.getOutputStream();

                    File myFile = new File(file_name);
                    while(true){
                        if (myFile.exists()) {

                            byte[] mybytearray = new byte[(int) myFile.length()];
                            FileInputStream fis = new FileInputStream(myFile);
                            Log.d("2","2");
                            BufferedInputStream bis = new BufferedInputStream(fis, 8 * 1024);
                            bis.read(mybytearray, 0, mybytearray.length);
                            Log.d("3","3");
                            //輸出到server
                            outputstream.write(mybytearray, 0, mybytearray.length);
                            outputstream.flush();
                            outputstream.close();
                            bis.close();
                            fis.close();
                            Log.d("4","4");
                            break;
                        } else{
                            Log.e("Socket", "file doesn't exist!");
                        }
                    }


                } catch (Exception e) {

                    Log.e("Socket", "Client: Error", e);

                } finally {
                    Log.d("notify","notify");
                    socket_out.close();
                    Log.d("socket_out","close");
                }
            } catch (Exception e) {

            }

        }
    };

   Runnable client_txt = new Runnable() {

        @Override
        public void run() {

            try {
                Log.d("txt","wait");
                InetAddress serverAddr = InetAddress.getByName("210.240.163.64");
                ConnectivityManager CM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netinfo = CM.getActiveNetworkInfo();
                try {
                        socket_in = new Socket(serverAddr, 5555);
                        Log.d("Socket txt", "Client: Connecting...");
                    if (netinfo.isConnected()) {
                        //接收文字檔
                        File accept_file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), "back.txt");
                        Log.d("path",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
                        //InputStreamReader isr =new  InputStreamReader(socket_in.getInputStream());
                        // Log.d("isr",isr.toString());

                        BufferedReader br = new BufferedReader(new InputStreamReader(socket_in.getInputStream()));
                        FileOutputStream output = new FileOutputStream(accept_file);
                        OutputStreamWriter osw = new OutputStreamWriter(output);
                        //osw.write(" ");
                        while(true) {
                            String a=br.readLine();
                            Log.d("a","a");
                            if(a==null) {
                                break;
                            }
                            else {
                                Log.d("br", a);
                                osw.append(a+"\n");
                            }
                        }
                        Log.d("txt","close");
                        osw.close();
                        output.flush();
                        output.close();
                    }
                } catch (Exception e) {

                    Log.e("Socket", "Client: Error", e);
                }finally {
                    socket_in.close();
                    text_to_speach.run();
                }
            } catch (Exception e) {

            }

        }

    };

    Runnable text_to_speach = new Runnable() {
        @Override
        public void run() {
            try{
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), "back.txt");
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                String s;
                while ((s=bufferedReader.readLine())!=null){
                    if(!hashMap.containsKey(s)){
                        System.out.println(s);
                        ArrayList<String> list =new ArrayList<>();
                        String coor = bufferedReader.readLine();
                        System.out.println(coor);
                        list.add(coor);
                        hashMap.put(s,list);
                    }
                    else {
                        hashMap.get(s).add(bufferedReader.readLine());
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
            engine.setPitch(pitchRate);
            engine.setSpeechRate(speedRate);
            //editText.setText(fileContent.toString());
            Iterator it = hashMap.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                speak((String)pair.getKey(),(ArrayList<String>)pair.getValue(),((ArrayList)pair.getValue()).size());
                endTime = System.currentTimeMillis();
                Log.d("endTime",Long.toString(endTime));
                runTime = endTime - startTime;
                //Toast.makeText(MainActivity.this,Long.toString(runTime),Toast.LENGTH_LONG).show();
                Log.d("time",Long.toString(runTime));
            }
            hashMap.clear();
        }
    } ;

    @Override
    public void onInit(int status){
        if (status == TextToSpeech.SUCCESS){
            engine.setLanguage(Locale.CHINESE);
        }
    }


    void speak(String key,ArrayList<String> coors,int size){
        String unit="";
        String object="";
        switch (key){
            case "dog":
                object="狗";
                unit="隻";
                break;
            case "horse":
                object="馬";
                unit="隻";
                break;
            case "truck":
                object="卡車";
                unit="輛";
                break;
            case "bicycle":
                object="腳踏車";
                unit="輛";
                break;
        }
        engine.setPitch(pitchRate);
        engine.setSpeechRate(speedRate);

        engine.speak("有"+size+unit+object+"", TextToSpeech.QUEUE_ADD,null ,null);
        StringBuilder relativePosition=new StringBuilder();
        for(int i=0;i<size;i++){
            if(i==size-1 && size >1){
                relativePosition.append("和");
            }
            String coor =coors.get(i);//[x y w h] is string
            coor = coor.substring(1,coor.length()-1);//1~7\ x y w h
            String[] temp=coor.split(" ");//array[0]=x;array[1]=y ....
            int[] intCoorArray = strArrayToIntArray(temp);
            int x = intCoorArray[0]+intCoorArray[2]/2;
            int y = intCoorArray[1]+intCoorArray[3]/2;
            System.out.println(x+","+y);

            if((x>=0 && x<=960) && (y>=0 && y<=640)){
                //LEFT-TOP
                relativePosition.append("左上 ");
            }else if((x>=1600 && x<=WIDTH) && (y>=0 && y<=640)){
                //RIGHT-TOP
                relativePosition.append("右上 ");
            }else if((x>960 && x<1600) && (y>0 && y<640)){
                //TOP
                relativePosition.append("上方 ");
            }else if((x>=0 && x<960) && (y>640 && y>1280)){
                //LEFT
                relativePosition.append("左邊 ");
            }else if((x>1600 && x<=WIDTH) && (y>640 && y<1280)){
                //RIGHT
                relativePosition.append("右邊 ");
            }else if((x>=960 && x<=1600) && (y>=640 && y<=1280)){
                //CENTER
                relativePosition.append("中間 ");
            }else if((x>=0 && x<=960) && (y>=1280 && y<=HEIGHT)){
                //Bottom left
                relativePosition.append("左下 ");
            }else if((x>=1600 && x<=WIDTH) && (y>=1280 && y<=HEIGHT)){
                //Bottom RIGHT
                relativePosition.append("右下 ");
            }else if((x>960 && x<1600) && (y>1280 && y<=HEIGHT)){
                //BOTTOM
                relativePosition.append("下方 ");
            }
        }
        if(size>1)
            engine.speak("分別在"+relativePosition.toString(), TextToSpeech.QUEUE_ADD,null ,null);
        else
            engine.speak("在"+relativePosition.toString(), TextToSpeech.QUEUE_ADD,null ,null);
    }
    int[] strArrayToIntArray(String[] a){
        int[] b = new int[a.length];
        for (int i = 0; i < a.length; i++) {

            b[i] = (int)Float.parseFloat(a[i]);
        }

        return b;
    }

}

