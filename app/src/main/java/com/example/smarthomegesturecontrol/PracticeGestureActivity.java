package com.example.smarthomegesturecontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthomegesturecontrol.permissions.OnPermission;
import com.example.smarthomegesturecontrol.permissions.Permission;
import com.example.smarthomegesturecontrol.permissions.XXPermissions;

//import org.conscrypt.Conscrypt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.widget.Toast.makeText;

public class PracticeGestureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera mCamera = null;
    private int mPreviewHeight;
    private int mPreviewWidth;
    private static String mediaFileName = null;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private SurfaceView mySurfaceView = null;
    private SurfaceHolder mySurfaceHolder = null;
    private int mySurfaceViewLayoutWidth = 0;

    private boolean myIsRecording = false;

    private static int CAMERA_RIGHT_ORIENTATION = 0;
    private static MediaRecorder myRecorder;
    private static String gestureNameToPractice;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_gesture);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        HashMap<String, String> gestureNameAndValue = new HashMap<>();

        gestureNameAndValue.put("Select a Gesture", "selectGesture");
        gestureNameAndValue.put("Turn On Lights", "lightsOn");
        gestureNameAndValue.put("Turn Off Lights", "lightsOff");
        gestureNameAndValue.put("Turn On Fan", "fanOn");
        gestureNameAndValue.put("Turn Off Fan", "fanOff");
        gestureNameAndValue.put("Increase Fan Speed", "fanUp");
        gestureNameAndValue.put("Decrease Fan Speed", "fanDown");
        gestureNameAndValue.put("Set Thermostat to specified temperature", "setThermo");
        gestureNameAndValue.put("0", "num0");
        gestureNameAndValue.put("1", "num1");
        gestureNameAndValue.put("2", "num2");
        gestureNameAndValue.put("3", "num3");
        gestureNameAndValue.put("4", "num4");
        gestureNameAndValue.put("5", "num5");
        gestureNameAndValue.put("6", "num6");
        gestureNameAndValue.put("7", "num7");
        gestureNameAndValue.put("8", "num8");
        gestureNameAndValue.put("9", "num9");

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            releaseMediaRecorder();
            releaseCamera();
        });

        Intent intent = getIntent();
        gestureNameToPractice = gestureNameAndValue.get(intent.getStringExtra("gesture_name"));

        start();
    }

    private void start(){
        mCamera = getCameraInstance();
        CAMERA_RIGHT_ORIENTATION = getRightCameraDisplayOrientation(this, findFrontFacingCamera(), mCamera);
        mCamera.setDisplayOrientation(CAMERA_RIGHT_ORIENTATION);


        mySurfaceView = findViewById(R.id.camera_surface_view);
        mySurfaceHolder = mySurfaceView.getHolder();
        mySurfaceHolder.addCallback(this);
        mySurfaceViewLayoutWidth = mySurfaceView.getLayoutParams().width;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        findViewById(R.id.btnRecord).setOnClickListener(v -> {
            checkMyPermission();
        }); //the recording button to record video using the phone's front camera

        findViewById(R.id.btnUpload).setOnClickListener(v -> {
            if (progressDialog == null){
                progressDialog = new ProgressDialog(this);
            }
            progressDialog.setMessage("The Video is Being Uploaded");
            progressDialog.show();

            postRequest();
        }); //to upload the recorded video from the above step
    }

    private void checkMyPermission() {
        XXPermissions.with(this)
                .permission(Permission.Group.STORAGE)
                .permission(Permission.CAMERA)
                .permission(Permission.RECORD_AUDIO)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        if (all) {
                            if (myIsRecording) {
                                stopRecording();
                            } else {
                                if (prepareVideoRecorder()) {
                                    myRecorder.start();
                                    //prepare or actually open the video camera, if the camera is detected then the media recorder will start the recording
                                    Toast.makeText(getApplicationContext(), "The Recording Has Been Started", Toast.LENGTH_SHORT).show();
                                    myIsRecording = true;
                                } else {
                                    releaseMediaRecorder();
                                }
                            }
                        } else {
                            makeText(getApplicationContext(), "Permissions Have Not Been Enabled", Toast.LENGTH_SHORT).show();
                            //but if the recording did not start then the camera will be released and a message is shown to let the user know of this
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(getApplicationContext(), "Necessary Permissions to be Allowed", Toast.LENGTH_SHORT).show();

                            XXPermissions.gotoPermissionSettings(PracticeGestureActivity.this);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: Cannot Play and Record Video Without Necessary Permissions", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




                    //now, to stop the recording process and show a message to the user of that:

                    private void stopRecording() {
                        myRecorder.stop();
                        Toast.makeText(this, "The Recording Has Been Completed", Toast.LENGTH_SHORT).show();
                        myIsRecording = false;
                        releaseMediaRecorder();
                        mCamera.lock();
                    }

                    @Override
                    protected void onResume() {
                        super.onResume();
                        start();
                    }

                    @Override
                    protected void onDestroy() {
                        super.onDestroy();
                        releaseMediaRecorder();
                        releaseCamera();
                    }
                    //the media recorder is to be released as well as the camera to stop

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width < size.height) {
                continue; // we are only interested in landscape variants
            }

            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return (result);
    }

    private boolean prepareVideoRecorder() {
        myRecorder = new MediaRecorder();
        myRecorder.setOnInfoListener((mr, what, extra) -> {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                stopRecording();
            }
        }); //i need this when the time limit for the video recording is reached

        mCamera.unlock();
        myRecorder.setCamera(mCamera);
        myRecorder.setOrientationHint(CAMERA_RIGHT_ORIENTATION + 180);
        myRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        myRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        CamcorderProfile profile = CamcorderProfile.get(CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_HIGH);
        myRecorder.setProfile(profile);

        mediaFileName = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
        myRecorder.setOutputFile(mediaFileName);
        myRecorder.setMaxDuration(500);
        // recorder.setMaxFileSize(500000000);
        myRecorder.setVideoSize(mPreviewWidth, mPreviewHeight);
        myRecorder.setPreviewDisplay(mySurfaceHolder.getSurface());


        try {
            myRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Toast.makeText(getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
            releaseMediaRecorder();
            return false;
        } //opening the front camera for recording and setting its orientation for recording the video
        //the recorder video should not exceed 5 seconds which are 500 as declared in set max duration
        //the recorder video should not exceed 500 mb which are 500000000 as declared in set max file size
        //then the recorded video should be shown to the user

        return true;
    }

    private void releaseMediaRecorder() { //recording has been completed now and all to be released
        if (myRecorder != null) {
            myRecorder.reset();
            myRecorder.release();
            myRecorder = null;
            mCamera.lock();
        }
    }


    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }


    public static Camera getCameraInstance() { //this is to get the camera instance
        Camera c = null;
        try {
            c = Camera.open(findFrontFacingCamera());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private static int findFrontFacingCamera() { //this is to get the front camera of the users device not the back one
        int cameraId = 0;
        boolean cameraFront;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }

        return cameraId;
    }

    public static int getRightCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        CameraInfo info = new CameraInfo();
        camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRecordingHint(true);
        Camera.Size size = getBestPreviewSize(parameters);
        mCamera.setParameters(parameters);
        int newHeight = size.height / (size.width / mySurfaceViewLayoutWidth);
        mySurfaceView.getLayoutParams().height = newHeight;
    } //i need this to set the parameters of the video to be shown

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mPreviewHeight = mCamera.getParameters().getPreviewSize().height;
        mPreviewWidth = mCamera.getParameters().getPreviewSize().width;

        mCamera.stopPreview();
        try {
            mCamera.setPreviewDisplay(mySurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (myIsRecording) {
            stopRecording();
        }
        releaseMediaRecorder();
        releaseCamera();
    }


    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    } // i need this to save the video as a link


    private static File getOutputMediaFile(int type) { //i need this to save the video as a file
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                System.out.println("MyCameraApp" + "Failed to Create Storage Directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile; //how the video will be named

        mediaFileName = gestureNameToPractice + "_PRACTICE_" + timeStamp + "_zheng" + ".mp4";
        String mediaDirectory = mediaStorageDir.getPath();

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaDirectory + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaDirectory + File.separator + mediaFileName);
        } else {
            return null;
        }

        return mediaFile;
    }

    public void postRequest() {
        Security.insertProviderAt(Conscrypt.newProvider(), 1);

        String[] dirarray = mediaFileName.split("/");
        String file_name = dirarray[6];

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("video/mp4"), new File(mediaFileName)))
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.0.35:5000/upload")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    try {
                        String response_body = response.body().string();
                        System.out.println(response_body);
                        Toast.makeText(getApplicationContext(), response_body, Toast.LENGTH_LONG).show();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent gotoMainActivity = new Intent(PracticeGestureActivity.this, MainActivity.class);
                        startActivity(gotoMainActivity);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();

                call.cancel();

                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    Toast.makeText(getApplicationContext(), "Something Went Wrong:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });

    }




}