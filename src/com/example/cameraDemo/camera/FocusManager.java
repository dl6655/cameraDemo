package com.example.cameraDemo.camera;

import android.content.Context;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.text.TextUtils;

import java.util.List;

public class FocusManager {
    
    
    int SMART_FOCUS_HOLD_STABILIZE_DISTANCE = 5;
    int SMART_FOCUS_HOLD_STABILIZE_DISTANCE_SUM = 10;
    /**
     * 智能聚焦的移动范围阀值,实际用的是加速度判断的
     */
    int SMART_FOCUS_DELTA_DISTANCE = 3;
    int SMART_FOCUS_DELTA_DISTANCE_SUM = 8;
    
    /**
     * 智能聚焦的稳定时间——传感器稳定
     */
    long SMART_FOCUS_HOLD_MILLIS = 500;
    
    /**
     * FocusManager的总开关，设计为FocusManager中优先级最高开关，用于实时预览中检测时所有聚焦使用
     * 默认值应给为 ture
     */
    private boolean mEnableFocus = true;
    private FocusState mFocusSatate = FocusState.IDLE;
    private String mFocusMode;
    private String[] mDefaultFocusModes;
    private String mOverrideFocusMode;
    private Parameters mParameters = null;
    /**
     * 相机是否在聚焦
     */
    private boolean mIsFocusing = false;
    /**
     * 上次聚焦状态
     */
    private boolean mLastFocusSuccess = false;
    
    /**
     * 上次自动聚焦的时间.
     */
    private long mLastAutoFocusTime = 0;

    //上次聚焦失败的时间
    private long mLastAutoFocusFailTime = 0;
    private FocusListener focusListener;
    public enum FocusState{
        /**
         * Focus is not active, camera can call auto focus
         */
        IDLE, /**
         * Focus is in progress
         */
        FOCUSING, /**
         * Focus is in progress and the camera will take a picture
         * after focus finishes
         */
        FOCUSING_SNAP_ON_FINISH, /**
         * camera is close
         */
        CAMERA_CLOSE, SUCCESS, FAIL
    }
    
    
    /**
     * 上次聚焦的时间
     */
    private long mLastFocusMillis = 0;
    
    private static DistanceManager mDistanceManager;
    
    
    public FocusManager(Context context){
        mDistanceManager = new DistanceManager(context);
    }
    
    public void setParameters(Parameters parameters){
        this.mParameters = parameters;
    }
    
    /**
     * 每次打开相机的时候，必须调用此方法，保证FocusManager的状态是正确的
     */
    public void onCameraOpen() {
        mLastFocusSuccess = false;
        mDistanceManager.reset();
    }
    
    public void onResume() {
        mDistanceManager.request();
    }

    public void onPause() {
        mDistanceManager.unrequest();
    }
    
    public void setFocusListener(FocusListener focusListener){
        this.focusListener = focusListener;
    }
    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }
    
    private void autoFocus(boolean forceFocus) {
        
        if (!mEnableFocus) {
            return;
        }
        
        if (forceFocus) {
            // do focus by force
            if (mFocusSatate == FocusState.IDLE) {
                doAutoFocus();
                
            } else {
//              onAutoFocus(false);
            }
        } else {
            if (mDistanceManager.needFocus()) {
                long currentTime = System.currentTimeMillis();
                long intervalAutoFocusTime = currentTime - mLastAutoFocusTime;
                long intervalAutoFocusFailTime = currentTime - mLastAutoFocusFailTime;
                
                //相邻2次聚焦的时间 1秒中
                if (intervalAutoFocusTime < 1000) {
                    onAutoFocus(false);
                    return;
                    //上次聚焦失败后2秒内，不再聚焦
                }else if (intervalAutoFocusFailTime < 2000) {
                    onAutoFocus(false);
                    return;
                }else {
                    mLastAutoFocusTime = currentTime;
                    doAutoFocus();
                }
            } else {
                
                onAutoFocus(mLastFocusSuccess);
            }
        }
    }
    
    public void resetFocusState() {
        //重启聚焦状态
        mDistanceManager.reset();
    }
    
    private void doAutoFocus() {

        mDistanceManager.onDoFocus();
        mIsFocusing = true;
        focusListener.autoFocus();
        if (mFocusSatate != FocusState.FOCUSING_SNAP_ON_FINISH) {
            mFocusSatate = FocusState.FOCUSING;
        }

//        updateFocusUI();
    }
    
    private class DistanceManager implements SensorEventListener{
        
        private SensorManager mSensorManager;
        private Sensor mSenorOrien;

        public DistanceManager(Context context) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }

        public void request() {
            mSenorOrien = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            if (mSenorOrien != null) {
                //不支持的话，register动作在部分设备上会导致程序异常
                mSensorManager.registerListener(this, mSenorOrien, SensorManager.SENSOR_DELAY_NORMAL);
            }

        }

        public void unrequest() {
            if (mSenorOrien != null) {
                mSensorManager.unregisterListener(this);
            }
        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
            
        }
        
        public void reset() {
            //切换相机或者离开相机模块再返回相机，不会触发自动对焦 ++
//          if (mLastFocusPosition != null) {
//              for (int i = 0; i < mLastFocusPosition.length; i++) {
//                  mLastFocusPosition[i] = -9999f;
//              }
//          }
            //切换相机或者离开相机模块再返回相机，不会触发自动对焦 --
            mLastFocusPosition = null;
            // 重置稳定时间
            mLastHoldMillis = INVILID;
        }

        public void onDoFocus() {
//          // 清除上次稳定时间
            mLastHoldMillis = INVILID;
            
            if (mCurrOriValues != null) {
                // 此处不能直接这样： mLastValues = mCurrValues;
                mLastFocusPosition = mCurrOriValues.clone();
            } else {
                // 设备传感器不支持
            }
        }

        // 尽可能的提升性能，
        private int mI = 0;
        private float[] mLastFocusPosition;
        private int mDelta = 0;
        private static final long INVILID = -1; 
        private long mLastHoldMillis = INVILID;

        public boolean needFocus() {

            if (mLastHoldMillis == INVILID) {
                //如果值为INVIID，那么说明传感器不能正常工作，那么每次都应该聚焦
                return true;
            }
            
            if (mCurrOriValues == null) {
                return true;
            }

            if (mLastFocusPosition == null) {
                return true;
            } else {
                int sum = 0;
                for (int i = 0; i < mCurrOriValues.length; i++) {
                    mDelta = Math.abs(((int) mCurrOriValues[i]) - ((int) mLastFocusPosition[i]));
                    sum += mDelta;

                    if (mDelta > SMART_FOCUS_HOLD_STABILIZE_DISTANCE) { // 45
                        // update values
                        mLastFocusPosition = mCurrOriValues.clone();
                        return true;
                    } else if (sum > SMART_FOCUS_HOLD_STABILIZE_DISTANCE_SUM) { // 80
                        // update values
                        mLastFocusPosition = mCurrOriValues.clone();
                        return true;
                    } else {
                    }
                }
                return false;
            }

        }

        private boolean mIsHoldOn = false;
        private float[] mCurrOriValues = new float[3];
        
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (! canCallAutoFocus()) {
                return;
            }
            
            final float[] newValues = event.values;
            switch (event.sensor.getType()) {
            case Sensor.TYPE_ORIENTATION:
                // Sensor.TYPE_ACCELEROMETER 用于侦测防抖动

                // ===== 智能聚焦 =====
                //step.1 检查是否已经稳定
                mIsHoldOn = checkStabilize(newValues, mCurrOriValues);
                
                
                // ==== 更新参数值 =====
                if (mCurrOriValues == null) {
                    // 此处不能直接这样： mCurrValues = values;
                    mCurrOriValues = newValues.clone();
                } else {
                    // 重复利用对象，不必每次clone
                    this.mI = 0;
                    while (mI < newValues.length) {
                        mCurrOriValues[mI] = newValues[mI];
                        mI++;
                    }
                }
                // 达到智能聚焦的稳定条件, 检查设备距离上次聚焦的偏离位置是否满足智能聚焦条件。
                if (mIsHoldOn) {
                    // 已经发生聚焦 TODO
                    if (mLastFocusPosition != null) {
                        // 移动范围是否大于阀值
                        if (beyondRound(newValues, mLastFocusPosition, 
                                SMART_FOCUS_DELTA_DISTANCE,
                                SMART_FOCUS_DELTA_DISTANCE_SUM)) {
                            // 请求聚焦
                            autoFocus(false);
                        }
                    } else {
                        // 没有发生过聚焦，直接聚焦
                        autoFocus(false);
                    }
                }
                break;
            default:
                break;
            }
        
        }

        private boolean checkStabilize(final float[] newValues, final float[] oldValues) {
            final boolean isHold;
            // step.1 检测设备是否稳定
            if (oldValues == null) {
                isHold = false;
            } else {
                isHold = !(beyondRound(newValues, oldValues, 
                SMART_FOCUS_HOLD_STABILIZE_DISTANCE,
                SMART_FOCUS_HOLD_STABILIZE_DISTANCE_SUM));
            }

            // 传感器值已经达到稳定条件，判断稳定时间是否达到
            final boolean isHoldOn;
            if (isHold) {
                if (mLastHoldMillis == INVILID) {
                    // 移动后，第一次稳定
                    mLastHoldMillis = System.currentTimeMillis();
                    isHoldOn = false;
                } else {
                    if (System.currentTimeMillis() - mLastHoldMillis > SMART_FOCUS_HOLD_MILLIS) {
                        isHoldOn = true;
                        mLastFocusMillis = INVILID;
                    } else {
                        isHoldOn = false;
                    }
                }
            } else {
                // 清除上次稳定时间
                mLastHoldMillis = INVILID;
                isHoldOn = false;
            }
            
            return isHoldOn;
        }
        
        private float mOrienSum = 0;//尽可能的减少不必要的消耗，避免在栈上反复创建数据
        private float mOrienDelta = 0;
        private boolean beyondRound(float[] newValues, float[] oldValues, int delta, int deltaSum) {
            boolean isBeyond = false;
            //恢复默认值
            mOrienSum = 0;
            mOrienDelta = 0;
            // 判断是否稳定
            for (int i = 0; i < newValues.length; i++) {
                mOrienDelta = Math.abs(newValues[i] - oldValues[i]); 
                if (mOrienDelta > 180) {
                    //求补
                    mOrienDelta = 360 - mOrienDelta;
                }
                //判断一个坐标轴上的位置
                if (mOrienDelta > delta) {                                                                                                                                                           
                    isBeyond = true;
                    break;
                }
                //累加多个坐标轴上的位置，然后进行判断
                mOrienSum += mOrienDelta;
                if (mOrienSum > deltaSum) {
                    isBeyond = true;
                    break;
                }
            }
            return isBeyond;
        }
        
        private boolean canCallAutoFocus() {
            String focusMode = getFocusMode();
            if(!TextUtils.isEmpty(focusMode)){
                final boolean canFocus = focusMode.equals(Parameters.FOCUS_MODE_AUTO) || focusMode.equals(Parameters.FOCUS_MODE_MACRO);
                return canFocus;
            }else{
                return false; 
            }
            
            
        }
        
        // This can only be called after mParameters is initialized.
        public String getFocusMode() {
            if (mOverrideFocusMode != null)
                return mOverrideFocusMode;
            if(mParameters != null){
                List<String> supportedFocusModes = mParameters.getSupportedFocusModes();
                // The default is continuous autofocus.
                mFocusMode = mParameters.getFocusMode();
                if (mFocusMode == null) {
                    for (int i = 0; i < mDefaultFocusModes.length; i++) {
                        String mode = mDefaultFocusModes[i];
                        if (isSupported(mode, supportedFocusModes)) {
                            mFocusMode = mode;
                            break;
                        }
                    }
                }
                if (!isSupported(mFocusMode, supportedFocusModes)) {
                    // For some reasons, the driver does not support the current
                    // focus mode. Fall back to auto.
                    if (isSupported(Parameters.FOCUS_MODE_AUTO, mParameters.getSupportedFocusModes())) {
                        mFocusMode = Parameters.FOCUS_MODE_AUTO;
                    } else {
                        mFocusMode = mParameters.getFocusMode();
                    }
                }
            }
            
            return mFocusMode;
        }
        
    }
    
    
    public void onAutoFocus(boolean success) {
        mIsFocusing = false;
        mLastFocusSuccess = success;
        mLastFocusMillis = System.currentTimeMillis();
        
        if (mFocusSatate == FocusState.FOCUSING_SNAP_ON_FINISH) {
            
            if (success) {
                mFocusSatate = FocusState.SUCCESS;
            } else {
                //在聚焦失败的情况下依然要拍照，那么重置聚焦状态
                this.resetFocusState();
                mFocusSatate = FocusState.FAIL;
            }
//            updateFocusUI();
//            capture();
        } else if (mFocusSatate == FocusState.FOCUSING) {
            // This happens when (1) user is half-pressing the focus key or
            // (2) touch focus is triggered. Play the focus tone. Do not
            // take the picture now.
            if (success) {
                mFocusSatate = FocusState.SUCCESS;
            } else {
                mFocusSatate = FocusState.FAIL;
            }
        } else if (mFocusSatate == FocusState.IDLE) {
        }
    }
    
    
    public interface FocusListener{
        public void autoFocus();
    } 
        

}
