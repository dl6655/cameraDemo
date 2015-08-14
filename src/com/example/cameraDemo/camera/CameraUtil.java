package com.example.cameraDemo.camera;


import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.provider.Settings;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;

import java.text.DecimalFormat;
import java.util.*;


public class CameraUtil {
	
	private static final float DEFAULT_CAMERA_BRIGHTNESS = 0.7f;
	public static final int SUPPORT_RESOLUTION = 1024;
	public static final int COMP_QUALITY = 90;
	/**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;
	
	public static Camera openCamera(Context activity, int cameraId)
            throws CameraHardwareException, CameraDisabledException {
        //检测用户是否已经禁用了相机
        DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        if (dpm.getCameraDisabled(null)) {
            throw new CameraDisabledException();
        }

        return CameraHolder.getInstance().open(cameraId,DeviceInfoUtils.getScreenWidth(activity),DeviceInfoUtils.getScreenWidth(activity));
    }

	/**
	 * 获取当前相机的角度
	 * @param activity
	 * @return
	 */
	public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
            	     return 0;
            case Surface.ROTATION_90:
            	     return 90;
            case Surface.ROTATION_180:
            	     return 180;
            case Surface.ROTATION_270:
            	     return 270;
        }
        return 0;
    }

	public static void initializeScreenBrightness(Window win, ContentResolver resolver) {
        // Overright the brightness settings if it is automatic
        int mode = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            WindowManager.LayoutParams winParams = win.getAttributes();
            winParams.screenBrightness = DEFAULT_CAMERA_BRIGHTNESS;
            win.setAttributes(winParams);
        }
    }

	public static int getDisplayOrientation(int degrees, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

	/**
	 * 获取最适合的图片的size
	 * @param sizeList
	 * @return
	 */
	public static Size getSupportPictureSize(List<Size> sizeList){
		Size size = null;
		Collections.sort(sizeList,new Comparator<Size>(){

			@Override
			public int compare(Size lhs, Size rhs) {
				if(lhs.width > rhs.width){
					return 1;
				}else if(lhs.width ==rhs.width){
					if(lhs.height >= rhs.height){
						return 1;
					}else{
						return -1;
					}
				}else{
					return -1;
				}
			}
        });
		for (int i = sizeList.size()-1; i >= 0; i--) {
			size = sizeList.get(i);
			if(size.width > SUPPORT_RESOLUTION){
				continue;
			}
			if(size.width <= SUPPORT_RESOLUTION){
				for(int j = i;j>=0;j--){
					if(size.height == SUPPORT_RESOLUTION){
						return size;
					}else if(size.height > SUPPORT_RESOLUTION){
						continue;
					}else if(size.height < SUPPORT_RESOLUTION){
						return size;
					}
				}
			}
		}
		return size;
	}

	public static Size findBestPictureResolution(Size defaultPictureResolution,List<Size> supportPictureSizes,int screenResolutionWidth,int screenResolutionHeight) {
        StringBuilder picResolutionSb = new StringBuilder();
        for (Size supportedPicResolution : supportPictureSizes) {
            picResolutionSb.append(supportedPicResolution.width).append('x').append(supportedPicResolution.height).append(" ");
        }
        // 排序
        List<Size> sortedSupportedPicResolutions = new ArrayList<Size>(supportPictureSizes);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Size>() {
            @Override
            public int compare(Size a, Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return 1;
                }
                if (bPixels > aPixels) {
                    return -1;
                }
                return 0;
            }
        });
        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) screenResolutionWidth / (double) screenResolutionHeight;
        Iterator<Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            DecimalFormat df = new DecimalFormat("#.##");
            if (Double.parseDouble(df.format(distortion)) > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
		if (!sortedSupportedPicResolutions.isEmpty()) {
			// Camera.Size largestPreview =
			// sortedSupportedPicResolutions.get(0);
			// Point largestSize = new Point(largestPreview.width,
			// largestPreview.height);
			// return largestSize;
			for (int i = sortedSupportedPicResolutions.size()-1; i >= 0; i--) {
				Size size = sortedSupportedPicResolutions.get(i);
				if(size.width > SUPPORT_RESOLUTION){
					continue;
				}
				if(size.width <= SUPPORT_RESOLUTION){
					for(int j = i;j>=0;j--){
						if(size.height == SUPPORT_RESOLUTION){
							return size;
						}else if(size.height > SUPPORT_RESOLUTION){
							continue;
						}else if(size.height < SUPPORT_RESOLUTION){
							return size;
						}
					}
				}
			}
		}
        // 没有找到合适的，就返回默认的
        return defaultPictureResolution;
    }

	/**
	 * 根据分辨率获得取景框
	 * @param sizes
	 * @param w 分辨率的width
	 * @param h 分辨率的height
	 * @return
	 */
	public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        Collections.sort(sizes, new Comparator<Size>() {
            @Override
            public int compare(Size a, Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });
        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) 
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
