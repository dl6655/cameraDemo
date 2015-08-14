/**
 * 图片工具类
 */
package com.example.cameraDemo.camera;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import com.example.cameraDemo.CameraActivity;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageUtils {

	public static final String TEMP_PATH = "TempCache";

    public static final String JPG = ".jpg";

	public static File generateTempPictureFilePath() {
        String dir = StorageUtils.getOwnCacheDirectory(YmApp.getInstance(), TEMP_PATH).getAbsolutePath();
        return new File(dir, generatePictureFilename());
    }
	
	public static File getPictureFileDir(){
		String dir = StorageUtils.getOwnCacheDirectory(YmApp.getInstance(), TEMP_PATH).getAbsolutePath();
		return new File(dir);
	}
	

	public static String generatePictureFilename() {
		long dateTake = System.currentTimeMillis();
		Date date = new Date(dateTake);
		SimpleDateFormat sdf = new SimpleDateFormat("'pic'_yyyyMMdd_HHmmss");
        return sdf.format(date) + "_" + dateTake + JPG;
	}

    public static boolean storeCapturedImage(byte[] data, String filePath) {
        Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length, getBitmapOptions(false));
        return storeCapturedImage(bm, filePath);
    }

    public static boolean storeCapturedImage(Bitmap bm, String filePath) {
        if (bm == null)
            return false;
        OutputStream outputStream = null;
        try {
            File file = new File(filePath);
            File dir = file.getParentFile();
            if (!dir.exists())
                dir.mkdirs();
            outputStream = new FileOutputStream(file);
            bm.compress(CompressFormat.JPEG, 90, outputStream);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static File storeCapturedImageFile(Bitmap bm, String filePath) {
    	File file=null;
        OutputStream outputStream = null;
        try {
             file = new File(filePath);
            File dir = file.getParentFile();
            if (!dir.exists())
                dir.mkdirs();
            outputStream = new FileOutputStream(file);
            bm.compress(CompressFormat.JPEG, 80, outputStream);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * Decode and sample down a bitmap from resources to the requested width and height.
     *
     * @param res The resources object containing the image data
     * @param resId The resource id of the image data
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /**
     * Decode and sample down a bitmap from a byte[] to the requested width and height.
     *
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromByte(byte[] bytes,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename The full path of the file to decode
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmap565FromFile(String filename,
            int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inPreferredConfig = Config.RGB_565;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Decode and sample down a bitmap from a file input stream to the requested width and height.
     *
     * @param fileDescriptor The file descriptor to read from
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     *         that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromDescriptor(
            FileDescriptor fileDescriptor, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    /**
     * Calculate an inSampleSize for use in a {@link Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that will result in the final decoded bitmap having a width and
     * height equal to or larger than the requested width and height. This implementation does not
     * ensure a power of 2 is returned for inSampleSize which can be faster when decoding but
     * results in a larger bitmap which isn't as useful for caching purposes.
     *
     * @param options An options object with out* params already populated (run through a decode*
     *            method with inJustDecodeBounds==true
     * @param reqWidth The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(Options options,
            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (height < width) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger
            // inSampleSize).

            final float totalPixels = width * height;

            // Anything more than 2x the requested pixels we'll sample down
            // further.
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    public static Options getBitmapOptions(boolean in565) {
        Options opts = new Options();
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        if (in565) {
            opts.inPreferredConfig = Config.RGB_565;
        }
        return opts;
    }

    /**
     * @param bitmap The source bitmap.
     * @param opacity a value between 0 (completely transparent) and 255 (completely
     * opaque).
     * @return The opacity-adjusted bitmap.  If the source bitmap is mutable it will be
     * adjusted and returned, otherwise a new bitmap is created.
     */
    private Bitmap adjustOpacity(Bitmap bitmap, int opacity)
    {
        Bitmap mutableBitmap = bitmap.isMutable()
                ? bitmap
                : bitmap.copy(Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        int colour = (opacity & 0xFF) << 24;
        canvas.drawColor(colour, Mode.DST_IN);
        return mutableBitmap;
    }


    /**
	 * 保存Bitmap到本地文件
	 *
	 * @param bitmap
	 * @param format
	 * @param quality
	 *            1-100
	 * @param file
	 */
	public static boolean writeBitmapToFile(Bitmap bitmap, CompressFormat format, int quality, File file) {
		boolean isSave = false;
		if (file != null && bitmap != null && !bitmap.isRecycled()) {
			FileOutputStream fos = null;
			try {
				if (!file.exists()) {
					String canonicalPath = file.getCanonicalPath();
					if (canonicalPath.lastIndexOf(File.separator) >= 0) {
						canonicalPath = canonicalPath.substring(0, canonicalPath.lastIndexOf(File.separator));
						File dir = new File(canonicalPath);
						if (!dir.exists()) {
							dir.mkdirs();
						}
					}
					file.createNewFile();
				}
				if (file.exists()) {
					fos = new FileOutputStream(file);
					isSave = bitmap.compress(format, quality, fos);
					fos.flush();
//					bitmap.recycle();
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fos != null) {
						fos.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isSave;
	}


	public static Bitmap cropImage(int left,int top,int right,int bottom,byte[] data,int width,int height){
		Rect r = new Rect(left, top, right, bottom);
		int originalWidth = r.width();
        int originalHeight = r.height();
        Bitmap croppedImage;
        try {
            croppedImage = Bitmap.createBitmap(originalWidth, originalHeight, Config.ARGB_8888);
        } catch (Exception e) {
            return null;
        }
        if (croppedImage == null) {

            return null;
        }
        Bitmap b = null;
        InputStream in = null;
		try {
	        in = new ByteArrayInputStream(data);
	        //Decode image size
	        Options o = new Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(in, null, o);
	        in.close();

	        int scale = 1;
	        if (o.outHeight > 1024 || o.outWidth > 1024) {
	            scale = (int) Math.pow(2, (int) Math.round(Math.log(1024 / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
	        }

	        Options o2 = new Options();
	        in = new ByteArrayInputStream(data);
	        o2.inSampleSize = scale;
	        b = BitmapFactory.decodeStream(in, null, o2);
		} catch (IOException E) {

		}finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		{
			Canvas canvas = new Canvas(croppedImage);
			Rect dstRect = new Rect(0,0,width,height);
			canvas.drawBitmap(b, r, dstRect, null);
		}
        return croppedImage;
	}


    public static Bitmap rotateImage(Bitmap src, float degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        src = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        return src;
    }

	public static byte[] bitmap2ByteArray(Bitmap bitmap) {
		if(bitmap != null){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(CompressFormat.JPEG, 80, baos);
			return baos.toByteArray();
		}else{
			return null;
		}
	}

    /**
     * 将长方形图从左上角按照长宽中小的那个纬度进行截取
     * @param srcBm
     * @param srcW
     * @param srcH
     * @return
     */
    public static Bitmap cropBitmap(Bitmap srcBm, int srcW, int srcH, int mOrientation,boolean isFront) {

        int objWidth = 0;
        int srcWidth = srcBm.getWidth();
        int srcHeight = srcBm.getHeight();
        int xoff = 0;
        int yoff = 0;
        objWidth = srcWidth > srcHeight ? srcHeight : srcWidth;
		if (!isFront) {
			if (mOrientation == CameraActivity.ORIENTATION_PORTRAIT_NORMAL) {// 90
			} else if (mOrientation == CameraActivity.ORIENTATION_LANDSCAPE_NORMAL) {// 0
			} else if (mOrientation == CameraActivity.ORIENTATION_PORTRAIT_INVERTED) {// 270
				yoff = Math.abs(srcHeight - srcWidth) * -1;
			} else if (mOrientation == CameraActivity.ORIENTATION_LANDSCAPE_INVERTED) {// 180
				objWidth = srcWidth > srcHeight ? srcHeight : srcWidth;
				xoff = Math.abs(srcHeight - srcWidth) * -1;

			}
		}else{
			if(mOrientation == CameraActivity.ORIENTATION_PORTRAIT_INVERTED){

			}
		}
        float compenstionScaleX = 1f;
        float compenstionScaleY = 1f;

        Bitmap resultBm = Bitmap.createBitmap(objWidth, objWidth, Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBm);
        Matrix cropMatrix = new Matrix();
        cropMatrix.postTranslate(xoff, yoff);

        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
        cropMatrix.postScale(compenstionScaleX, compenstionScaleY);
        try {
            canvas.drawBitmap(srcBm, cropMatrix, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 由外边的调用者来recycle裁剪的原始图
        // this.mBitmap.recycle();
        return resultBm;
    }



	public static boolean compBitmap(String fromPath,File saveFile,int degree,PublicProductPicItem picInfo,int quality) {
		Options newOpts = new Options();
        newOpts.inJustDecodeBounds = true;//只读边,不读内容  
        Bitmap bitmap = BitmapFactory.decodeFile(fromPath, newOpts);
        newOpts.inJustDecodeBounds = false;  
        int width = newOpts.outWidth;  
        int height = newOpts.outHeight;  
        int hh = 1280;//  
        int ww = 1280;//  
        int reqWidth = 0;
        int reqHeight = 0;
        if(width <= 1280 || width <= 1280){
            if (width > height && width > ww) {
                reqWidth = ww;
                reqHeight = ww * newOpts.outHeight / newOpts.outWidth;
                
            } else if (width < height && height > hh) {  
                reqHeight = hh;
                reqWidth = hh * newOpts.outWidth / newOpts.outHeight;
            }
        }else{
            reqWidth = width;
            reqHeight = height;
        }
        
             
        newOpts.inPreferredConfig = Config.ARGB_8888;  
        newOpts.inPurgeable = true;  
        newOpts.inInputShareable = true;
        int simpleSize = calculateInSampleSize(newOpts, reqWidth, reqHeight);
        newOpts.inSampleSize = simpleSize;
//            newOpts.inSampleSize = simpleSizePow(simpleSize);
        bitmap = BitmapFactory.decodeFile(fromPath, newOpts);
        picInfo.picCompressWidth = bitmap.getWidth();
        picInfo.picCompressHeight = bitmap.getHeight();
        
		if (degree == 0) {
			return writeBitmapToFile(bitmap, CompressFormat.JPEG, quality, saveFile);
		} else {
			Bitmap roteBitmap = ImageUtils.rotateImage(bitmap, degree);
			return writeBitmapToFile(roteBitmap, CompressFormat.JPEG, quality, saveFile);
		}
        
	}
	
	private static int simpleSizePow(int simpleSize){
        if (simpleSize <= 1){
            return 1;
        }else {
            int i = 0;
            int t = 1;
            while ((t = (int) Math.pow(2, i)) <= simpleSize) {
                i++;
            }
            return t;
        }
	}

}
