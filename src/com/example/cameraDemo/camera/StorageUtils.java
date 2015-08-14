package com.example.cameraDemo.camera;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 存储工具类
 * Created by Zhp on 2014/6/10.
 */
public final class StorageUtils extends BaseStorageUtils {

    private StorageUtils() {
    }


    public static long getFileSize(File file) {
        if (!file.exists()) {
            return 0;
        }

        if (file.isDirectory()) {
            return getDirSize(file);
        } else {
            return file.length();
        }
    }

    /**
     *
     * @param directory
     * @return
     */
    public static long getDirSize(File directory) {
        checkDirectory(directory);

        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        long size = 0;

        for (final File file : files) {
            size += getFileSize(file);
            if (size < 0) {
                break;
            }
        }

        return size;
    }



    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file  file or directory to delete, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException in case deletion is unsuccessful
     */
    private static void forceDelete(File file) throws IOException {
        if (file.isDirectory()) {
            deleteDirectory(file);
        } else {
            boolean filePresent = file.exists();
            if (!file.delete()) {
                if (!filePresent){
                    throw new FileNotFoundException("File does not exist: " + file);
                }
                String message =
                        "Unable to delete file: " + file;
                throw new IOException(message);
            }
        }
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory  directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    private static void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);

        if (!directory.delete()) {
            String message =
                    "Unable to delete directory " + directory + ".";
            throw new IOException(message);
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    private static void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            String message = directory + " does not exist";
            throw new IllegalArgumentException(message);
        }

        if (!directory.isDirectory()) {
            String message = directory + " is not a directory";
            throw new IllegalArgumentException(message);
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (null != exception) {
            throw exception;
        }
    }

    /**
     * Checks that the given {@code File} exists and is a directory.
     *
     * @param directory The {@code File} to check.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static void checkDirectory(File directory) {
        if (!directory.exists()) {
            throw new IllegalArgumentException(directory + " does not exist");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
    }

    public static interface FileIOCallback {
        public void onSuccess(long sizeInBytes);

        public void onFailed(Exception e);
    }
}

