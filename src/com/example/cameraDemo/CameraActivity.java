package com.example.cameraDemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import com.example.cameraDemo.camera.*;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CameraActivity extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener,Camera.AutoFocusCallback,FocusManager.FocusListener{
    private Camera cameraDevice;

    private int numberOfCameras;
    private int currentCameraId;
    private int frontCameraId;
    private int backCameraId;

    private boolean openCameraFail;

    private boolean cameraDisabled;

    private SurfaceHolder surfaceHolder;

    private Camera.Parameters parameters;

    /**
     * 照相机的四种状态
     */
    private static final int PREVIEW_STOPPED = 0;
    private static final int IDLE = 1;  // preview is active
    // Focus is in progress. The exact focus state is in Focus.java.
    private static final int FOCUSING = 2;
    private static final int SNAPSHOT_IN_PROGRESS = 3;
    private int cameraState = PREVIEW_STOPPED;

    private int displayRotation;
    //android.hardware.Camera.setDisplayOrientation.
    private int displayOrientation;

    private boolean pausing;

    private int previewWidth;

    private Button cameraSwitchBtn;

    private Button takePictureBtn;

    private LinearLayout flashTypeLayout;

    private LinearLayout flashTypeSelLayout;

    private TextView flashModeAutoBtn;

    private TextView flashModeOnBtn;

    private TextView flashModeOffBtn;

    private TextView flashTypeNameTv;

    private LinearLayout thumbimagesView;

    private HorizontalScrollView thumbimagesScrollView;

    private FrameLayout previewFrameLayout;

    private TextView completeBtn;

    private TextView cancelBtn;

    private LinearLayout picAndShutterLayout;

    private RelativeLayout turnCameraLayout;

    private View squareView;

    private CameraOrientationEventListener cameraOrientationEventListener;

    private CameraDealPictureThread cameraDealPictureThread;


    private int thumbImageWidth;
    private int thumbinterval;
    private int screenWidth;
    private int screenHeight;
    private SurfaceView preview;

    private int remainCount;
    private int canTakeCount;
    private final static int MAX_CAN_SEL_PIC_COUNT = 8;
    private int notCameraPic = 0;//这个字段是代表非相机的照片数量;
    private ArrayList<PublicProductPicItem> transferPic = new ArrayList<PublicProductPicItem>();
    private List<Integer> cameraIndexInPicList;//这个字段是记录从相机选择的照片 在整个照片list的位置

    private final static int REQUEST_CODE_TO_PIC_SHOW = 1;
    public final static String BUNDLE_KEY_IS_DEL_IMAGE = "is_del_image";


    private final static int HANDLER_MESSAGE_ADD_THUMB_IMAGES = 0;

    private final static int HANDLER_MESSAGE_ADD_THUMB_IMAGE_ERROR = 1;

    private FocusManager focusManager;

    private boolean isSurfaceDestory = false;


    private Handler cameraHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {

            if(msg.what == HANDLER_MESSAGE_ADD_THUMB_IMAGES){
                Bitmap thumbBitmap = (Bitmap)msg.obj;
                int index = msg.arg1;
                final CameraThumbImageView thumbImage = new CameraThumbImageView(CameraActivity.this
                        , thumbImageWidth, thumbImageWidth, thumbBitmap,new CameraThumbImageView.ThumbListener() {

                    @Override
                    public void del(Bitmap bitmap,
                                    CameraThumbImageView cameraThumbImageView,int index) {
                        thumbimagesView.removeView(cameraThumbImageView);
                        thumbimagesView.invalidate();
                        int indexPic = CameraPicManager.getInstance().getThumbBitmapList().indexOf(bitmap);
                        CameraPicManager.getInstance().removeThumbBitmaps(bitmap);
                        CameraPicManager.getInstance().getCameraPicInfoList().remove(indexPic);
                        remainCount ++;
                        completeBtn.setText("完成("+(canTakeCount - remainCount)+"/"+canTakeCount+")");
                    }

                    @Override
                    public void click(CameraThumbImageView cameraThumbImageView,int index) {
                        Intent intent = new Intent(CameraActivity.this, CameraPicViewActivity.class);
                        Bundle bundle = new Bundle();
                        int currentPosition = thumbimagesView.indexOfChild(cameraThumbImageView);
                        bundle.putInt("position", currentPosition);
//								bundle.putSerializable("cameraList", cameraPicInfoList);
                        intent.putExtras(bundle);
                        startActivityForResult(intent, REQUEST_CODE_TO_PIC_SHOW);
                    }
                });
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(thumbImageWidth+ DeviceInfoUtils.dip2px(CameraActivity.this, 5), thumbImageWidth+DeviceInfoUtils.dip2px(CameraActivity.this, 5));
                if(thumbimagesView.getChildCount() == 0){
                    param.setMargins(thumbinterval, 0, thumbinterval, 0);
                }else{
                    param.setMargins(0, 0, thumbinterval, 0);
                }
                thumbImage.setIndex(index);
                thumbImage.setLayoutParams(param);
                thumbimagesView.addView(thumbImage);
                thumbimagesView.invalidate();
                thumbimagesScrollView.post(new Runnable() {

                    @Override
                    public void run() {
                        thumbimagesScrollView.fullScroll(View.FOCUS_RIGHT);
                    }
                });
                thumbimagesScrollView.invalidate();
                remainCount --;
                completeBtn.setText("完成("+(canTakeCount - remainCount)+"/"+canTakeCount+")");
                takePictureBtn.setEnabled(true);
            }else if(msg.what == HANDLER_MESSAGE_ADD_THUMB_IMAGE_ERROR){
                takePictureBtn.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
//		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        CameraPicManager.getInstance().init();
        cameraOrientationEventListener = new CameraOrientationEventListener(this);


//		openCamera();
//		cameraOpenThread.start();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // ensure landscape orientation
        // set to fullscreen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        setContentView(R.layout.camera);
        picAndShutterLayout = (LinearLayout)findViewById(R.id.pics_shutter);
        previewFrameLayout = (FrameLayout)findViewById(R.id.preview_frame_id);
        thumbimagesView = (LinearLayout)findViewById(R.id.thumb_images);
        thumbimagesScrollView = (HorizontalScrollView)findViewById(R.id.thumb_images_scrollview);
        turnCameraLayout = (RelativeLayout)findViewById(R.id.trun_camera_layout);
        CameraUtil.initializeScreenBrightness(getWindow(), getContentResolver());
        numberOfCameras = CameraHolder.getInstance().getNumOfCamera();
        frontCameraId = CameraHolder.getInstance().getFrontCameraId();
        backCameraId = CameraHolder.getInstance().getBackCameraId();
        if(backCameraId != -1){
            currentCameraId = backCameraId;
        }else{
            currentCameraId = frontCameraId;
        }
        flashTypeSelLayout = (LinearLayout)findViewById(R.id.flash_type_sel_layout);
        flashTypeLayout = (LinearLayout)findViewById(R.id.flash_type_layout);
        flashTypeLayout.setClickable(true);
        flashTypeLayout.setOnClickListener(this);
        flashTypeNameTv = (TextView)findViewById(R.id.flash_type_name);
        squareView = findViewById(R.id.square_view);
        flashModeAutoBtn = (TextView)findViewById(R.id.flash_mode_auto);
        flashModeAutoBtn.setClickable(true);
        flashModeAutoBtn.setOnClickListener(this);
        flashModeOnBtn = (TextView)findViewById(R.id.flash_mode_on);
        flashModeOnBtn.setClickable(true);
        flashModeOnBtn.setOnClickListener(this);
        flashModeOffBtn = (TextView)findViewById(R.id.flash_mode_off);
        flashModeOffBtn.setClickable(true);
        flashModeOffBtn.setOnClickListener(this);
        takePictureBtn = (Button)findViewById(R.id.takepicture_btn);
        takePictureBtn.setOnClickListener(this);
        takePictureBtn.setEnabled(true);
        cameraSwitchBtn = (Button)findViewById(R.id.switch_camera);
        if(numberOfCameras < 2){
            cameraSwitchBtn.setVisibility(View.GONE);
        }
        cameraSwitchBtn.setOnClickListener(this);

        screenHeight = DeviceInfoUtils.getScreenHeight(this);
        screenWidth = DeviceInfoUtils.getScreenWidth(this);
        thumbinterval = DeviceInfoUtils.dip2px(getApplicationContext(), 7);
        thumbImageWidth = (screenWidth-(thumbinterval * 5))/4;


        Intent intent = this.getIntent();
        Bundle bundle;
        if(intent != null){
            bundle = intent.getExtras();
        }else{
            bundle = savedInstanceState;
        }
        if(bundle != null){
            transferPic = (ArrayList<PublicProductPicItem>)bundle.getSerializable("transfer_pics");
        }

        completeBtn = (TextView)findViewById(R.id.complete_btn);
        completeBtn.setClickable(true);
        cancelBtn = (TextView)findViewById(R.id.cancel_btn);
        cancelBtn.setClickable(true);
        completeBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        initThumbimagesView();
        canTakeCount = MAX_CAN_SEL_PIC_COUNT - notCameraPic;
        remainCount = canTakeCount;
        completeBtn.setText("完成("+(canTakeCount - remainCount)+"/"+canTakeCount+")");


    }

    private void initThumbimagesView() {
        //计算从商品发布页过来的有那些是在相机的照片
        notCameraPic = 0;
        CameraPicManager.getInstance().init();
        if(transferPic != null  && transferPic.size() > 0){

            for(int i = 0;i<transferPic.size();i++){
                PublicProductPicItem item = transferPic.get(i);
                if(item != null && item.fromWhere == PublicProductPicItem.FROM_CAMERA){
                    if(cameraIndexInPicList == null){
                        cameraIndexInPicList = new ArrayList<Integer>();
                    }
                    cameraIndexInPicList.add(i);
                    if(cameraDealPictureThread == null){
                        cameraDealPictureThread = new CameraDealPictureThread();
                    }
                    cameraDealPictureThread.addList(0, 0, item.data,item.path,true);
                }else{
                    notCameraPic ++;
                }
            }
        }

    }


    /**
     * satrt preview
     */
    private void startPreView() {
        previewFrameLayout.setVisibility(View.VISIBLE);
        try{
            screenHeight = DeviceInfoUtils.getScreenHeight(this);
            screenWidth = DeviceInfoUtils.getScreenWidth(this);
            if(screenWidth > screenHeight){
                previewWidth = screenHeight;
            }else{
                previewWidth = screenWidth;
            }
            cameraDevice.startPreview();
            parameters  = cameraDevice.getParameters();
            List<Camera.Size> supportPictureSizeList = parameters.getSupportedPictureSizes();
            Camera.Size defaultPictureResolution = parameters.getPictureSize();
            Camera.Size pictureSize = CameraUtil.findBestPictureResolution(defaultPictureResolution, supportPictureSizeList, screenWidth, screenHeight);
            if (pictureSize != null) {
                parameters.setPictureSize(pictureSize.width, pictureSize.height);
            }
            List<Camera.Size> supportPreviewSizeList = parameters.getSupportedPreviewSizes();
            Camera.Size preViewSize = CameraUtil.getOptimalPreviewSize(supportPreviewSizeList, pictureSize.width, pictureSize.height);
            if(preViewSize != null){
                parameters.setPreviewSize(preViewSize.width, preViewSize.height);
            }
            //对取景框的等比放大缩小 因为是竖屏的 所以width和height要调换
            //算出取景框的高度
            int previewHeight = preViewSize.width * screenWidth /  preViewSize.height;
            preview.setLayoutParams(new FrameLayout.LayoutParams(screenWidth,previewHeight));
            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(screenWidth, screenWidth);
            squareView.setLayoutParams(param);

            setPreviewDisplay(surfaceHolder);

            setDisplayOrientation();
            cameraDevice.setParameters(parameters);
//			这段先注释掉，开始就不能自动对焦了，但是移动可以自动对焦，这段有的手机会报错
            List<String> foucsModes = parameters.getSupportedFocusModes();
            if (foucsModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                cameraDevice.setParameters(parameters);
                cameraDevice.autoFocus(this);
            }else if(foucsModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                cameraDevice.setParameters(parameters);
                cameraDevice.autoFocus(this);
            }
            cameraState = IDLE;
//	        focusManager.setParameters(parameters);
        }catch(Exception e){
            closePreviewAndCamera();
            YmToastUtils.showToast(this, "相机出错了,请重新进一次吧");
            this.finish();
        }

    }

    private void setDisplayOrientation() {
        displayRotation = CameraUtil.getDisplayRotation(this);
        displayOrientation = CameraUtil.getDisplayOrientation(displayRotation, currentCameraId);
        cameraDevice.setDisplayOrientation(displayOrientation);
    }




    private void openCamera(){
//		cameraOpenThread = new Thread(new Runnable() {
//	        public void run() {
        try {
            cameraDevice = CameraUtil.openCamera(CameraActivity.this, currentCameraId);
        } catch (CameraHardwareException e) {
            openCameraFail = true;
        } catch (CameraDisabledException e) {
            cameraDisabled = true;
        }
//	        }
//	    });
//		cameraOpenThread.start();
        try {
//            cameraOpenThread.join();
            if (openCameraFail) {
                closePreviewAndCamera();
                showToast(this, "无法连接到相机");
                this.finish();
            } else if (cameraDisabled) {
                closePreviewAndCamera();
                showToast(this, "由于安全政策的限制，相机已被停用");
                this.finish();
            }
        } catch (Exception ex) {
            closePreviewAndCamera();
            showToast(this, "相机出错了,请重新进一次吧");
            this.finish();
        }
//		focusManager = new FocusManager(this);
//        focusManager.setFocusListener(this);
//        focusManager.onCameraOpen();
//        focusManager.onResume();
    }

    private void showToast(Context ctx,String msg){
        Toast.makeText(ctx,msg,Toast.LENGTH_LONG).show();
    }
    private void stopPreview() {
        if (cameraDevice != null && cameraState != PREVIEW_STOPPED) {
            cameraDevice.cancelAutoFocus(); // Reset the focus.
            cameraDevice.stopPreview();
        }
        cameraState = PREVIEW_STOPPED;
    }

    private void setPreviewDisplay(SurfaceHolder holder) {
        try {
            cameraDevice.setPreviewDisplay(holder);
        } catch (Throwable ex) {
            closePreviewAndCamera();
            showToast(this, "相机出错了,请重新进一次吧");
            this.finish();
        }
    }

    private void closeCamera() {
        if (cameraDevice != null) {
            CameraHolder.getInstance().release();
            cameraDevice = null;
            cameraState = PREVIEW_STOPPED;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isSurfaceDestory = false;
    }

    @Override
    public void surfaceChanged(final SurfaceHolder holder, int format, int width,
                               int height) {
        if (holder.getSurface() == null) {
            return;
        }
        surfaceHolder = holder;

        if (pausing || isFinishing()) return;

        if (cameraState == PREVIEW_STOPPED) {
            openCamera();
            startPreView();
        } else {
            if (CameraUtil.getDisplayRotation(CameraActivity.this) != displayRotation) {
                setDisplayOrientation();
            }
            if (holder.isCreating()) {

                setPreviewDisplay(holder);
            }
        }

        if(cameraDevice!= null){
            parameters = cameraDevice.getParameters();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        preview = (SurfaceView)findViewById(R.id.camera_preview);
        if(surfaceHolder != null && !isSurfaceDestory){
            openCamera();
            startPreView();

        }else{
            surfaceHolder = preview.getHolder();
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        pausing = false;
        takePictureBtn.setEnabled(true);
        keepScreenOnAwhile();
//
        if(cameraDealPictureThread == null){
            cameraDealPictureThread = new CameraDealPictureThread();
        }
        cameraOrientationEventListener.enable();
        if(focusManager != null){
//		    focusManager.onResume();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        previewFrameLayout.setVisibility(View.INVISIBLE);
        pausing = true;
        cameraState = IDLE;
//		focusManager.onPause();
        closePreviewAndCamera();
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        closePreviewAndCamera();
    }


    /**
     * 这个方法主要是释放相机的资源使用
     */
    private void closePreviewAndCamera() {
        stopPreview();
        if(cameraDealPictureThread != null){
            cameraDealPictureThread.finish();
        }
        cameraDealPictureThread = null;
        closeCamera();
        resetScreenOn();
        cameraOrientationEventListener.disable();
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        closePreviewAndCamera();
        surfaceHolder = null;
        isSurfaceDestory = true;
    }


    @Override
    public void onClick(View v) {
        if(v.equals(cameraSwitchBtn)){
            if(currentCameraId == frontCameraId){
                currentCameraId = backCameraId;
                flashTypeLayout.setVisibility(View.VISIBLE);
            }else{
                currentCameraId = frontCameraId;
                flashTypeLayout.setVisibility(View.GONE);
            }
            openCamera();
            startPreView();

        }else if(v.equals(takePictureBtn)){
//			cameraHeight = screenHeight - turnCameraLayout.getMeasuredHeight() - picAndShutterLayout.getMeasuredHeight();
            if(cameraDevice != null){
                if(canTakeCount - CameraPicManager.getInstance().getCameraPicInfoList().size() >0){
                    android.hardware.Camera.CameraInfo info =
                            new android.hardware.Camera.CameraInfo();
                    android.hardware.Camera.getCameraInfo(currentCameraId, info);
                    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {//用前置摄像头照相
                        try{
                            takePictureBtn.setEnabled(false);
                            cameraDevice.takePicture(null, null, new Camera.PictureCallback() {

                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    stopPreview();
                                    startPreView();
                                    if(data != null){
                                        cameraDealPictureThread.addList(screenWidth, screenWidth, data,"",false);
                                    }else{
                                        takePictureBtn.setEnabled(true);
                                        showToast(CameraActivity.this, "拍照失败，请重新再拍");
                                    }

//									takePictureBtn.setEnabled(true);
                                }
                            });
                            cameraState = SNAPSHOT_IN_PROGRESS;
                        }catch (Exception e) {
                            stopPreview();
                            startPreView();
                            takePictureBtn.setEnabled(true);
                            showToast(CameraActivity.this, "拍照失败，请重新再拍");
                        }

                    }else{
                        takePictureBtn.setEnabled(false);
                        try{
                            cameraDevice.takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    stopPreview();
                                    startPreView();
                                    if(data != null){
                                        cameraDealPictureThread.addList(screenWidth, screenWidth, data,"",false);
                                    }else{
                                        YmToastUtils.showToast(CameraActivity.this, "拍照失败，请重新再拍");
                                    }
                                    takePictureBtn.setEnabled(true);
                                }
                            });
                            cameraState = SNAPSHOT_IN_PROGRESS;
                        }catch (Exception e) {
                            stopPreview();
                            startPreView();
                            takePictureBtn.setEnabled(true);
                            YmToastUtils.showToast(CameraActivity.this, "拍照失败，请重新再拍");
                        }
                    }
                }else{
                    YmToastUtils.showToast(this, "您最多只能添加"+MAX_CAN_SEL_PIC_COUNT+"张图片");
                }
            }
        }else if(v.equals(flashTypeLayout)){
            if(flashTypeNameTv.getVisibility() == View.GONE){
                flashTypeSelLayout.setVisibility(View.GONE);
                flashTypeNameTv.setVisibility(View.VISIBLE);
            }else{
                flashTypeSelLayout.setVisibility(View.VISIBLE);
                flashTypeNameTv.setVisibility(View.GONE);
            }

        }else if(v.equals(flashModeAutoBtn)){
            flashTypeSelLayout.setVisibility(View.GONE);
            flashTypeNameTv.setVisibility(View.VISIBLE);
            parameters = cameraDevice.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            cameraDevice.setParameters(parameters);
            flashTypeNameTv.setText("自动");
        }else if(v.equals(flashModeOnBtn)){
            flashTypeSelLayout.setVisibility(View.GONE);
            flashTypeNameTv.setVisibility(View.VISIBLE);
            parameters = cameraDevice.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            cameraDevice.setParameters(parameters);
            flashTypeNameTv.setText("开启");
        }else if(v.equals(flashModeOffBtn)){
            flashTypeSelLayout.setVisibility(View.GONE);
            flashTypeNameTv.setVisibility(View.VISIBLE);
            parameters = cameraDevice.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cameraDevice.setParameters(parameters);
            flashTypeNameTv.setText("关闭");
        }else if(v.equals(completeBtn)){
            Intent intent = new Intent();
            Bundle bundle = new Bundle();
            //调整图片的位置 把整个图片list全部传回商品发布页
            //如果第一个原来是从相机的图片 判断这个图片是否还在 如果在 就把他放到主图的位置
            List<CameraPicInfo> cameraPinInfoList = CameraPicManager.getInstance().getCameraPicInfoList();
            ArrayList<PublicProductPicItem> backPicList = new ArrayList<PublicProductPicItem>();
            if(cameraPinInfoList != null){
                if(cameraIndexInPicList!= null && cameraIndexInPicList.size()> 0 && cameraIndexInPicList.get(0) == 0){//主图是从相机中选的 判断那张图片是否在选择的图片中 如果有 它还是主图
                    if(cameraPinInfoList != null && cameraPinInfoList.size() > 0 && transferPic != null && transferPic.size() > 0){
                        PublicProductPicItem item = transferPic.get(0);
                        for(int i =0;i<cameraPinInfoList.size();i++){
                            CameraPicInfo picInfo = cameraPinInfoList.get(i);
                            if(TextUtils.equals(item.path, picInfo.path)){
                                item.picWidth = picInfo.picWidth;
                                item.picHeight = picInfo.picHeight;
                                backPicList.add(item);
                                transferPic.remove(item);
                                cameraPinInfoList.remove(picInfo);
                                break;
                            }
                        }
                    }
                }
                //合并剩下的图片
                if(transferPic != null && transferPic.size() > 0){
                    for(int i =0;i<transferPic.size();i++){
                        PublicProductPicItem item = transferPic.get(i);
                        if(item.fromWhere == PublicProductPicItem.FROM_ALBUM || item.fromWhere == PublicProductPicItem.FROM_NETWORK){
                            backPicList.add(item);
                            continue;
                        }else{
                            for(int j = 0;j<cameraPinInfoList.size();j++){
                                CameraPicInfo picInfo = cameraPinInfoList.get(j);
                                if(TextUtils.isEmpty(item.path) && !TextUtils.isEmpty(picInfo.path) && TextUtils.equals(item.path, picInfo.path)){
                                    item.picWidth = picInfo.picWidth;
                                    item.picHeight = picInfo.picHeight;
                                    backPicList.add(item);
                                    cameraPinInfoList.remove(picInfo);
                                    break;
                                }
                            }
                        }
                    }
                    if(cameraPinInfoList.size() > 0){
                        for(int i =0;i<cameraPinInfoList.size();i++){
                            CameraPicInfo picInfo = cameraPinInfoList.get(i);
                            PublicProductPicItem item = new PublicProductPicItem();
                            item.fromWhere = PublicProductPicItem.FROM_CAMERA;
                            item.picWidth = picInfo.picWidth;
                            item.picHeight = picInfo.picHeight;
                            item.path = picInfo.path;
                            backPicList.add(item);
                        }
                    }
                }else{
                    for(int i = 0;i<cameraPinInfoList.size();i++){
                        CameraPicInfo picInfo = cameraPinInfoList.get(i);
                        PublicProductPicItem item = new PublicProductPicItem();
                        item.fromWhere = PublicProductPicItem.FROM_CAMERA;
                        item.path = picInfo.path;
                        item.picWidth = picInfo.picWidth;
                        item.picHeight = picInfo.picHeight;
                        backPicList.add(item);
                    }
                }
            }else{
                if(transferPic != null){
                    for(int i =0;i<transferPic.size();i++){
                        PublicProductPicItem item = transferPic.get(i);
                        if(item.fromWhere == PublicProductPicItem.FROM_ALBUM){
                            backPicList.add(item);
                        }
                    }
                }
            }

            bundle.putSerializable("selectPics", (Serializable)backPicList);
            intent.putExtras(bundle);
            this.setResult(RESULT_OK,intent);
            closePreviewAndCamera();
            this.finish();
        }else if(v.equals(cancelBtn)){
            closePreviewAndCamera();
            this.finish();
        }
    }

    private void resetScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private void keepScreenOnAwhile() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private int mOrientation = 3;//初始化为0度 为了避免有时候这个监控屏幕方向比较慢得问题
    public static final int ORIENTATION_PORTRAIT_NORMAL =  1;//90
    public static final int ORIENTATION_PORTRAIT_INVERTED =  2;//270
    public static final int ORIENTATION_LANDSCAPE_NORMAL =  3;//0
    public static final int ORIENTATION_LANDSCAPE_INVERTED =  4;//180
    /**
     * 监控屏幕方向的 主要用来计算相机成像的Rotation
     * @author cuijie
     */
    class CameraOrientationEventListener extends OrientationEventListener{

        public CameraOrientationEventListener(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
//			if (orientation == ORIENTATION_UNKNOWN) return;
//		     android.hardware.Camera.CameraInfo info =
//		            new android.hardware.Camera.CameraInfo();
//		     android.hardware.Camera.getCameraInfo(currentCameraId, info);
//		     orientation = (orientation + 45) / 90 * 90;
//		     if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
//		         rotation = (info.orientation - orientation + 360) % 360;
//		     } else {  // back-facing camera
//		         rotation = (info.orientation + orientation) % 360;
//		     }
//			if (cameraDevice != null) {
//				parameters = cameraDevice.getParameters();
//				parameters.setRotation(rotation);
//				cameraDevice.setParameters(parameters);
//			}
            // determine our orientation based on sensor response

            if(orientation == ORIENTATION_UNKNOWN){
                return;
            }
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(currentCameraId, info);

            if (orientation >= 315 || orientation < 45) {
                if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){//前置摄像头减去180度
                    if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                        mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                    }
                }else{
                    if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                        mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                    }
                }

            } else if (orientation < 315 && orientation >= 225) {
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                        mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                    }
                }else{
                    if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                        mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                    }
                }

            } else if (orientation < 225 && orientation >= 135) {
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    if (mOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                        mOrientation = ORIENTATION_PORTRAIT_NORMAL;
                    }
                }else{
                    if (mOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                        mOrientation = ORIENTATION_PORTRAIT_INVERTED;
                    }
                }

            } else { // orientation <135 && orientation > 45
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    if (mOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                        mOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                    }
                }else{
                    if (mOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                        mOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                    }
                }

            }

        }
    }

    /**
     * 这是一个线程队列 专门处理相机照完之后处理图片以及让主线程显示缩略图
     * @author cuijie
     *
     */
    private class CameraDealPictureThread extends Thread{
        private int QUEUE_LIMIT = 1;
        private LinkedList<CameraPicInfo> picList;
        private boolean stop;
        @Override
        public void run() {
            while(true){
                CameraPicInfo cameraPicInfo;
                synchronized (this) {
                    if (picList.isEmpty()) {
                        notifyAll();
                        if (stop){
                            break;
                        }
                        try {
                            wait();
                        } catch (InterruptedException ex) {

                        }
                        continue;
                    }
                    cameraPicInfo = picList.getFirst();
                }
                int index = CameraPicManager.getInstance().getCameraPicInfoList().indexOf(cameraPicInfo);
                addPic(cameraPicInfo.width, cameraPicInfo.height, cameraPicInfo.data,index,cameraPicInfo.path);
                synchronized(this) {
                    picList.remove(0);
                    notifyAll();
                }
            }
        }

        public CameraDealPictureThread(){
            picList = new LinkedList<CameraPicInfo>();
            this.start();
        }

        /**
         * 这个方法是在主线程中运行的
         * @param width
         * @param height
         * @param data
         */
        public void addList(int width,int height,byte[] data,String path,boolean isCroped){

            CameraPicInfo cameraPicInfo = new CameraPicInfo();
            cameraPicInfo.data = data;
            cameraPicInfo.width = width;
            cameraPicInfo.height = height;
            cameraPicInfo.path = path;
            cameraPicInfo.isCroped = isCroped;
            CameraPicManager.getInstance().addCameraPicInfo(cameraPicInfo);

            synchronized (this) {
                while(picList.size() > QUEUE_LIMIT){
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                picList.addLast(cameraPicInfo);
                notifyAll();
            }
        }

        /**
         * 子线程中运行的方法 主要处理图片的旋转和剪裁
         * @param width
         * @param height
         * @param data
         * @param index
         * @param path
         */
        public synchronized void addPic(int width,int height,byte[] data,int index,String path){
            Bitmap thumbBitmap = null;
            try{
                if(TextUtils.isEmpty(path) && data != null){
                    File picFile = ImageUtils.generateTempPictureFilePath();
                    CameraPicManager.getInstance().getCameraPicInfoList().get(index).path = picFile.getAbsolutePath();
                    Bitmap bitmap;
                    if(CameraPicManager.getInstance().getCameraPicInfoList().get(index).isCroped){
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        CameraPicManager.getInstance().getCameraPicInfoList().get(index).picWidth = bitmap.getWidth();
                        CameraPicManager.getInstance().getCameraPicInfoList().get(index).picHeight = bitmap.getHeight();
                        thumbBitmap = ImageUtils.decodeSampledBitmap565FromFile(CameraPicManager.getInstance().getCameraPicInfoList().get(index).path, thumbImageWidth, thumbImageWidth);
                    }else{
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        Bitmap rotatedBitmap = null;
                        if(mOrientation == ORIENTATION_PORTRAIT_NORMAL){//90
                            rotatedBitmap = ImageUtils.rotateImage(bitmap, 90);
                        }else if(mOrientation == ORIENTATION_LANDSCAPE_NORMAL){//0
                            rotatedBitmap = bitmap;
                        }else if(mOrientation == ORIENTATION_PORTRAIT_INVERTED){//270
                            rotatedBitmap = ImageUtils.rotateImage(bitmap, 270);
                        }else if(mOrientation == ORIENTATION_LANDSCAPE_INVERTED){//180
                            rotatedBitmap = ImageUtils.rotateImage(bitmap, 180);
                        }
                        Bitmap lastBitmap;
                        boolean isFront = false;
                        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                        android.hardware.Camera.getCameraInfo(currentCameraId, info);
                        if(info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                            isFront = true;
                        }
                        if(rotatedBitmap != null){
                            lastBitmap = ImageUtils.cropBitmap(rotatedBitmap, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), mOrientation,isFront);
                            CameraPicManager.getInstance().getCameraPicInfoList().get(index).picWidth = lastBitmap.getWidth();
                            CameraPicManager.getInstance().getCameraPicInfoList().get(index).picHeight = lastBitmap.getHeight();
                            ImageUtils.writeBitmapToFile(lastBitmap, Bitmap.CompressFormat.JPEG, CameraUtil.COMP_QUALITY, picFile);
                            thumbBitmap = ImageUtils.decodeSampledBitmap565FromFile(picFile.getAbsolutePath(), thumbImageWidth, thumbImageWidth);
                        }
                    }

                }
                if(data == null && !TextUtils.isEmpty(path)){
                    File file = new File(CameraPicManager.getInstance().getCameraPicInfoList().get(index).path);
                    data = FileUtil.readFileToBytes(file);
                    CameraPicManager.getInstance().getCameraPicInfoList().get(index).data = data;
                    thumbBitmap = ImageUtils.decodeSampledBitmap565FromFile(path, thumbImageWidth, thumbImageWidth);
                }
                if(thumbBitmap != null){
                    CameraPicManager.getInstance().getThumbBitmapList().add(thumbBitmap);
                    Message message = Message.obtain(cameraHandler);
                    message.what = HANDLER_MESSAGE_ADD_THUMB_IMAGES;
                    message.obj = thumbBitmap;
                    message.arg1 = index;
                    cameraHandler.sendMessageDelayed(message, 30);
                }else{
                    if(CameraPicManager.getInstance().getCameraPicInfoList().size() > index){
                        CameraPicManager.getInstance().getCameraPicInfoList().remove(index);
                        cameraHandler.sendEmptyMessage(HANDLER_MESSAGE_ADD_THUMB_IMAGE_ERROR);
                    }
                }
            }catch(Exception e){
                closePreviewAndCamera();
                YmToastUtils.showToast(CameraActivity.this, "相机出错了,请重新进一次吧");
                CameraActivity.this.finish();
            }


        }


        /**
         * 停止线程 一般是这个页面退出了 就要停了
         */
        public void finish() {
            synchronized (this) {
                stop = true;
                notifyAll();
            }
            try {
                join();
            } catch (InterruptedException ex) {
            }
        }

        public void startRun(){
            stop = false;
        }
    }

    public static class CameraPicInfo{
        public int width;
        public int height;
        public byte[] data;
        public Bitmap bitmap;
        public String path;
        public boolean isCroped;
        public int picWidth;
        public int picHeight;

    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if(success){
            camera.cancelAutoFocus();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE_TO_PIC_SHOW && resultCode == this.RESULT_OK){
            if(data != null){
                Bundle bundle = data.getExtras();
                if(bundle != null){
                    boolean isDelImage = bundle.getBoolean(BUNDLE_KEY_IS_DEL_IMAGE);
                    if(isDelImage){
                        //这么做是因为从大图浏览回来如果删除图片了要重新初始化缩略图 还有要重新计算可拍照的数量
                        canTakeCount = MAX_CAN_SEL_PIC_COUNT - notCameraPic;
                        remainCount = canTakeCount;
                        thumbimagesView.removeAllViews();
                        if(CameraPicManager.getInstance().getThumbBitmapList() != null && CameraPicManager.getInstance().getThumbBitmapList().size() > 0){
                            takePictureBtn.setEnabled(false);
                            for(int i =0;i< CameraPicManager.getInstance().getThumbBitmapList().size() ; i++){
                                Bitmap thumbBitmap = CameraPicManager.getInstance().getThumbBitmapList().get(i);
                                Message message = Message.obtain(cameraHandler);
                                message.what = HANDLER_MESSAGE_ADD_THUMB_IMAGES;
                                message.obj = thumbBitmap;
                                message.arg1 = i;
                                cameraHandler.sendMessageDelayed(message, 30);
                            }
                        }else{
                            completeBtn.setText("完成("+(canTakeCount - remainCount)+"/"+canTakeCount+")");
                        }
                        takePictureBtn.setEnabled(true);
                    }
                }
            }
        }

//		openCamera();
//        startPreView();
    }


    @Override
    public void autoFocus() {
        try{
            if(cameraDevice != null){
                cameraState = FOCUSING;
                takePictureBtn.setEnabled(false);
                cameraDevice.autoFocus(new Camera.AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        takePictureBtn.setEnabled(true);
                        cameraState = IDLE;
                        focusManager.onAutoFocus(success);
                    }
                });
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }




}
