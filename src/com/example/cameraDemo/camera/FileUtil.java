package com.example.cameraDemo.camera;

import android.text.TextUtils;

import java.io.*;
import java.text.DecimalFormat;

/**
 * 
 * @Description:描述：文件工具类
 * @Author cuijie
 */
public class FileUtil {
	private static final String sTag = "FileUtil";
	private static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

	/**
	 * Returns a human-readable version of the file size, where the input
	 * represents a specific number of bytes.
	 * 
	 * @param size
	 *            the number of bytes
	 * @return a human-readable display value (includes units)
	 */
	public static String formatSize(long size) {
		float ONE_KB = 1024F;
		float ONE_MB = ONE_KB * ONE_KB;
		float ONE_GB = ONE_KB * ONE_MB;
		String displaySize;
		DecimalFormat df = new DecimalFormat("0.00");
		if (size >= ONE_KB && size < ONE_MB) {
			displaySize = String.valueOf(df.format(size / ONE_KB)) + " KB";
		} else if (size >= ONE_MB && size < ONE_GB) {
			displaySize = String.valueOf(df.format(size / ONE_MB)) + " MB";
		} else if (size >= ONE_GB) {
			displaySize = String.valueOf(df.format(size / ONE_GB)) + " GB";
		} else {
			displaySize = String.valueOf(df.format(size)) + " B";
		}
		return displaySize;
	}

	/**
	 * 递归删除文件目录
	 * 
	 * @param dir
	 *            文件目录
	 */
	public static void deleteFileDir(File dir) {
		try {
			if (dir.exists() && dir.isDirectory()) {// 判断是文件还是目录
				if (dir.listFiles().length == 0) {// 若目录下没有文件则直接删除
					dir.delete();
				} else {// 若有则把文件放进数组，并判断是否有下级目录
					File delFile[] = dir.listFiles();
					int len = dir.listFiles().length;
					for (int j = 0; j < len; j++) {
						if (delFile[j].isDirectory()) {
							deleteFileDir(delFile[j]);// 递归调用deleteFileDir方法并取得子目录路径
						} else {
							boolean isDeltet = delFile[j].delete();// 删除文件
						}
					}
					delFile = null;
				}
				deleteFileDir(dir);// 递归调用
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除单个文件
	 * 
	 * @param file
	 *            文件目录
	 */
	public static void deleteFile(File file) {
		try {
			if (file != null && file.isFile() && file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 拷贝文件
	 * 
	 * @param sourceFile
	 *            源文件
	 * @param destFile
	 *            目标文件
	 * @return 是否拷贝成功
	 */
	public static boolean copyFile(File sourceFile, File destFile) {
		boolean isCopyOk = false;
		byte[] buffer = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		// 如果此时没有文件夹目录就创建
		String canonicalPath = "";
		try {
			canonicalPath = destFile.getCanonicalPath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!destFile.exists()) {
			if (canonicalPath.lastIndexOf(File.separator) >= 0) {
				canonicalPath = canonicalPath.substring(0, canonicalPath.lastIndexOf(File.separator));
				File dir = new File(canonicalPath);
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
		}

		try {
			bis = new BufferedInputStream(new FileInputStream(sourceFile), DEFAULT_BUFFER_SIZE);
			bos = new BufferedOutputStream(new FileOutputStream(destFile), DEFAULT_BUFFER_SIZE);
			buffer = new byte[DEFAULT_BUFFER_SIZE];
			int len = 0;
			while ((len = bis.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
				bos.write(buffer, 0, len);
			}
			bos.flush();
			isCopyOk = sourceFile != null && sourceFile.length() == destFile.length();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) {
					bos.close();
					bos = null;
				}
				if (bis != null) {
					bis.close();
					bis = null;
				}
				buffer = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return isCopyOk;
	}

	/**
	 * 读取文件内容到字节数组
	 * 
	 * @param file
	 * @return
	 */
	public static byte[] readFileToBytes(File file) {
		byte[] bytes = null;
		if (file.exists()) {
			byte[] buffer = null;
			BufferedInputStream bis = null;
			BufferedOutputStream bos = null;
			ByteArrayOutputStream baos = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);
				baos = new ByteArrayOutputStream();
				bos = new BufferedOutputStream(baos, DEFAULT_BUFFER_SIZE);
				buffer = new byte[DEFAULT_BUFFER_SIZE];
				int len = 0;
				while ((len = bis.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
					bos.write(buffer, 0, len);
				}
				bos.flush();
				bytes = baos.toByteArray();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bos != null) {
						bos.close();
						bos = null;
					}
					if (baos != null) {
						baos.close();
						baos = null;
					}
					if (bis != null) {
						bis.close();
						bis = null;
					}
					buffer = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bytes;
	}

	public static byte[] readFileToBytes(File file, long offset, long len) {
		byte[] bytes = null;
		if (file.exists() && offset >= 0 && len > offset && offset < file.length()) {
			RandomAccessFile raf = null;
			ByteArrayOutputStream bos = null;
			try {
				raf = new RandomAccessFile(file, "r");
				raf.seek(offset);
				bos = new ByteArrayOutputStream();
				int b = -1;
				long count = offset;
				while ((b = raf.read()) != -1 && count < len) {
					bos.write(b);
					count++;
				}
				bos.flush();
				bytes = bos.toByteArray();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (raf != null) {
						raf.close();
						raf = null;
					}
					if (bos != null) {
						bos.close();
						bos = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return bytes;
	}

	public static boolean writeBytesToFile(File file, byte[] bytes, long offset) {
		boolean isOk = false;
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (file.exists() && bytes != null && offset >= 0) {
			RandomAccessFile raf = null;
			try {
				raf = new RandomAccessFile(file, "rw");
				raf.seek(offset);
				raf.write(bytes);
				isOk = true;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (raf != null) {
						raf.close();
						raf = null;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return isOk;
	}

	/**
	 * 读取文件内容到字符串
	 * 
	 * @param file
	 * @return
	 */
	public static String readFileToString(File file) {
		return readFileToString(file, null);
	}

	/**
	 * 读取文件内容到字符串
	 * 
	 * @param file
	 * @param encoding
	 * @return
	 */
	public static String readFileToString(File file, String encoding) {
		String result = null;
		if (file.exists()) {
			char[] buffer = null;
			BufferedReader br = null;
			InputStreamReader isr = null;
			BufferedWriter bw = null;
			StringWriter sw = new StringWriter();
			try {
				isr = encoding == null ? new InputStreamReader(new FileInputStream(file)) : new InputStreamReader(
						new FileInputStream(file), encoding);
				br = new BufferedReader(isr);
				bw = new BufferedWriter(sw);
				buffer = new char[DEFAULT_BUFFER_SIZE];
				int len = 0;
				while ((len = br.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
					bw.write(buffer, 0, len);
				}
				bw.flush();
				result = sw.toString();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bw != null) {
						bw.close();
						bw = null;
					}
					if (br != null) {
						br.close();
						br = null;
					}
					if (isr != null) {
						isr.close();
						isr = null;
					}
					if (sw != null) {
						sw.close();
						sw = null;
					}
					buffer = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * 写字符串到文件，文件父目录如果不存在，会自动创建
	 * 
	 * @param file
	 * @param content
	 * @return
	 */
	public static boolean writeStringToFile(File file, String content) {
		return writeStringToFile(file, content, false);
	}

	/**
	 * 写字符串到文件，文件父目录如果不存在，会自动创建
	 * 
	 * @param file
	 * @param content
	 * @param isAppend
	 * @return
	 */
	public static boolean writeStringToFile(File file, String content, boolean isAppend) {
		boolean isWriteOk = false;
		char[] buffer = null;
		int count = 0;
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			if (!file.exists()) {
				createNewFileAndParentDir(file);
			}
			if (file.exists()) {
				br = new BufferedReader(new StringReader(content));
				bw = new BufferedWriter(new FileWriter(file, isAppend));
				buffer = new char[DEFAULT_BUFFER_SIZE];
				int len = 0;
				while ((len = br.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
					bw.write(buffer, 0, len);
					count += len;
				}
				bw.flush();
			}
			isWriteOk = content.length() == count;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null) {
					bw.close();
					bw = null;
				}
				if (br != null) {
					br.close();
					br = null;
				}
				buffer = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return isWriteOk;
	}

	/**
	 * 写字节数组到文件，文件父目录如果不存在，会自动创建
	 * 
	 * @param file
	 * @param bytes
	 * @return
	 */
	public static boolean writeBytesToFile(File file, byte[] bytes) {
		return writeBytesToFile(file, bytes, false);
	}

	/**
	 * 写字节数组到文件，文件父目录如果不存在，会自动创建
	 * 
	 * @param file
	 * @param bytes
	 * @param isAppend
	 * @return
	 */
	public static boolean writeBytesToFile(File file, byte[] bytes, boolean isAppend) {
		boolean isWriteOk = false;
		byte[] buffer = null;
		int count = 0;
		ByteArrayInputStream bais = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			if (!file.exists()) {
				createNewFileAndParentDir(file);
			}
			if (file.exists()) {
				bos = new BufferedOutputStream(new FileOutputStream(file, isAppend), DEFAULT_BUFFER_SIZE);
				bais = new ByteArrayInputStream(bytes);
				bis = new BufferedInputStream(bais, DEFAULT_BUFFER_SIZE);
				buffer = new byte[DEFAULT_BUFFER_SIZE];
				int len = 0;
				while ((len = bis.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1) {
					bos.write(buffer, 0, len);
					count += len;
				}
				bos.flush();
			}
			isWriteOk = bytes.length == count;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) {
					bos.close();
					bos = null;
				}
				if (bis != null) {
					bis.close();
					bis = null;
				}
				if (bais != null) {
					bais.close();
					bais = null;
				}
				buffer = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return isWriteOk;
	}

	/**
	 * 创建文件父目录
	 * 
	 * @param file
	 * @return
	 */
	public static boolean createParentDir(File file) {
		boolean isMkdirs = true;
		if (!file.exists()) {
			File dir = file.getParentFile();
			if (!dir.exists()) {
				isMkdirs = dir.mkdirs();
			}
		}
		return isMkdirs;
	}

	/**
	 * 创建文件及其父目录
	 * 
	 * @param file
	 * @return
	 */
	public static boolean createNewFileAndParentDir(File file) {
		boolean isCreateNewFileOk = true;
		isCreateNewFileOk = createParentDir(file);
		// 创建父目录失败，直接返回false，不再创建子文件
		if (isCreateNewFileOk) {
			if (!file.exists()) {
				try {
					isCreateNewFileOk = file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					isCreateNewFileOk = false;
				}
			}
		}
		return isCreateNewFileOk;
	}

	/**
	 * 根据文件名称获取文件的后缀字符串
	 * 
	 * @param filename
	 *            文件的名称,可能带路径
	 * @return 文件的后缀字符串
	 */
	public static String getFileExtensionFromUrl(String filename) {
		if (!TextUtils.isEmpty(filename)) {
			int dotPos = filename.lastIndexOf('.');
			if (0 <= dotPos) {
				return filename.substring(dotPos + 1);
			}
		}
		return "";
	}

	/**
	 * 指定的文件夹下是否包含指定的文件
	 */
	public static boolean isContainsFile(String filePath,String fileName) {
		boolean isExist=false;
		File f = new File(filePath);
		if (!f.exists()) {
			isExist= false;
		}else{
			File[] fileArray=f.listFiles();
			for(int i=0;i<fileArray.length;i++){
				if(fileArray[i].getName().contains(fileName)){
					isExist= true;
				}

			}
		}
		return  isExist;
	}
	//得到当前外部存储设备的目录
//	private String SDPath= AudioUtils.getCacheAudioPathDown() + "/";
//	private String SDPath= Environment.getExternalStorageDirectory().getAbsolutePath()+"/";;

	/**
	 * 在SD卡上创建文件
	 * @param fileName
	 * @return
	 */
	public static File createSDFile(String fileName){
		File file=new File(fileName);
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}

	/**
	 * 在SD卡上创建目录
	 * @param dirName
	 * @return
	 */
	public static File createSDDir(String dirName){
		File file=new File(dirName);
		file.mkdir();
		return file;
	}

	/**
	 * 判断SD卡上文件是否存在
	 * @param fileName
	 * @return
	 */
	public static boolean isFileExist(String fileName){
		File file=new File(fileName);
		return file.exists();
	}
	public static boolean isFileExist(File file){
		return file.exists();
	}
	/**
	 * 将一个inputStream里面的数据写到SD卡中
	 * @param inputStream
	 * @return
	 */
	public static File writeToSDfromInput(File file,InputStream inputStream){
		OutputStream outStream=null;
		try {
			outStream=new FileOutputStream(file);
//			byte[] buffer=new byte[1024];
//			while(inputStream.read(buffer)!=-1){
//				outStream.write(buffer);
//			}
			// 1K的数据缓冲
			byte[] bs = new byte[1024];
			// 读取到的数据长度
			int len;
			while ((len = inputStream.read(bs)) != -1) {
				outStream.write(bs, 0, len);
			}
			outStream.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				outStream.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}
}