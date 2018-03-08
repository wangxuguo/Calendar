package com.oceansky.calendar.example.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileHelper {
    private static final String TAG = "FileHelper";

    /**
     * Close a {@link Closeable} object and ignore the exception.
     * @param target The target to close. Can be null.
     */
    public static void close(Closeable target) {
        try {
            if (target != null)
                target.close();
        } catch (IOException e) {
            LogHelper.w(TAG, "Failed to close the target", e);
        }
    }

    /**
     * Delete a file or a directory
     */
    public static void deleteFile(String dir, String filename) {
        deleteFile(new File(dir, filename));
    }

    /**
     * Delete a file or a directory
     */
    public static void deleteFile(File file) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        deleteFile(files[i]);
                    }
                }
                file.delete();
            }
        } else {
            LogHelper.i(TAG, "Cannot delete " + file.getAbsolutePath() + ", which not found");
        }
    }

    public static void clearFolderFiles(File dir) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File item : files) {
                    if (item.isFile()) {
                        item.delete();
                    }
                }
            }
        }
    }

    /**
     * Read all lines of input stream.
     */
    public static String readStreamAsString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder result = new StringBuilder();
        String line = null;

        boolean first = true;
        while ((line = reader.readLine()) != null) {
            if (!first) {
                result.append('\n');
            } else {
                first = false;
            }
            result.append(line);
        }

        return result.toString();
    }

    /**
     * Save the input stream into a file.</br>
     * Note: This method will close the input stream before return.
     */
    public static void saveStreamToFile(InputStream is, File file) throws IOException {
        File dirFile = file.getParentFile();
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(file);
        try {
            byte[] buffer = new byte[4096];
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        } finally {
            FileHelper.close(fos);
            FileHelper.close(is);
        }
    }

    /**
     * Read all lines of input stream.
     */
    public static void readFileToStringBuilder(String filename, StringBuilder result) {
        if (result.length() > 0) {
            result.delete(0, result.length());
        }
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            boolean first = true;
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!first) {
                    result.append('\n');
                } else {
                    first = false;
                }
                result.append(line);
            }
        } catch (FileNotFoundException e) {
            LogHelper.w(TAG, "Unexpected excetion: ", e);
        } catch (IOException e) {
            LogHelper.w(TAG, "Unexpected excetion", e);
        } finally {
            FileHelper.close(fis);
        }
    }

    /**
     * Read all lines of text file.
     * @return null will be returned if any error happens
     */
    public static String readFileAsString(String filename) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filename);
            return readStreamAsString(fis);
        } catch (FileNotFoundException e) {
            LogHelper.w(TAG, "Unexpected excetion: ", e);
        } catch (IOException e) {
            LogHelper.w(TAG, "Unexpected excetion", e);
        } finally {
            FileHelper.close(fis);
        }
        return null;
    }

    /**
     * Read all lines of text file and trim the result string.
     * The returned string must be non-empty if not null.
     */
    public static String readFileAsStringTrim(String filename) {
        String result = readFileAsString(filename);
        if (result != null) {
            result = result.trim();
            if (result.length() == 0) {
                result = null;
            }
        }
        return result;
    }

    public static File createExternalPath(String path) {
        if (StorageUtils.externalStorageAvailable()) {
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
            return f;
        }
        return null;
    }

    public static File getSafeExternalFile(String path, String name) {
        createExternalPath(path);
        return new File(path, name);
    }

    /**
     * null may be returned if no extension found
     * @return
     */
    public static String replaceFileExtensionName(String filename, String newExt) {
        int index = filename.lastIndexOf('.');
        if (index > 0) {
            return filename.substring(0, index + 1) + newExt;
        }
        return null;
    }
}