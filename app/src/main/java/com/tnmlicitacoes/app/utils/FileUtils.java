package com.tnmlicitacoes.app.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.tnmlicitacoes.app.mupdf.MuPDFActivity;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static com.tnmlicitacoes.app.Config.EXT_STORAGE_DIR_PATH;
import static com.tnmlicitacoes.app.Config.ROOT_FILES_FOLDER_NAME;
import static com.tnmlicitacoes.app.Config.ROOT_FILES_PATH;
import static com.tnmlicitacoes.app.Config.NOTICES_FILES_FOLDER_NAME;
import static com.tnmlicitacoes.app.Config.ATTACHMENTS_FILES_FOLDER_NAME;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class FileUtils {

    private static final String TAG = "FileUtils";

    /* PDF format name */
    private static final String PDF = "PDF";

    public static File createDestinationFolder(String name) {
        if(createRootFolder()) {
            File destFolder = new File(EXT_STORAGE_DIR_PATH, ROOT_FILES_FOLDER_NAME + "/" + name);
            if(!destFolder.exists()) {
                if(!destFolder.mkdir()) {
                    if(BuildConfig.DEBUG)
                        LOG_DEBUG(TAG, "Failed to create folder " + destFolder.getName());
                }
            } else {
                if(BuildConfig.DEBUG)
                    LOG_DEBUG(TAG, destFolder.getName() + " is already created!");
            }

            return destFolder;
        } else {
            return null;
        }
    }

    private static boolean createRootFolder() {
        File rootFolder = new File(EXT_STORAGE_DIR_PATH, ROOT_FILES_FOLDER_NAME);
        if(rootFolder.exists()) {
            return true;
        } else {
            return rootFolder.mkdir();
        }
    }

    public static File createFile(File directory, String fileName) {
        File file = new File(directory, fileName);
        try {
            if(!file.createNewFile()) {
                if(BuildConfig.DEBUG)
                    LOG_DEBUG(TAG, "Failed to create file!");
            }
        } catch (IOException e) {
            if(BuildConfig.DEBUG)
                LOG_DEBUG(TAG, e.getMessage());
        }
        return file;
    }

    public static List<File> getNotices() {
        File noticesDir = new File(ROOT_FILES_PATH + '/' + NOTICES_FILES_FOLDER_NAME);
        if(noticesDir.exists()) {
            return Arrays.asList(noticesDir.listFiles());
        } else {
            if(noticesDir.mkdir()) {
                return Arrays.asList(noticesDir.listFiles());
            }
        }
        return null;
    }

    public static List<File> getAttachments() {
        File noticesDir = new File(ROOT_FILES_PATH + '/' + ATTACHMENTS_FILES_FOLDER_NAME);
        if(noticesDir.exists()) {
            return Arrays.asList(noticesDir.listFiles());
        }
        return null;
    }

    public static void openPdf(Context context, File pdf) {
        Uri uri = Uri.fromFile(pdf);

        Intent intent;
        if(SettingsUtils.isDefaultPdfViewer(context)) {
            intent = new Intent(context, MuPDFActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(uri);
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent = Intent.createChooser(intent, "Abrir com");
        }

        try {
            Toast.makeText(context, context.getString(R.string.opening_pdf_file, pdf.getName()), Toast.LENGTH_SHORT).show();
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, context.getString(R.string.no_pdf_apps_message), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Checks the first 4 bytes of the file to see if it's a PDF format or not
     * @param data pdf file data
     * @return true if pdf otherwise false is returned
     */
    public static boolean isPdf(byte[] data) {
        if (data == null || data.length == 0) {
            return false;
        }
        //
        // PDF format header
        //
        //   %    P    D    F
        // 0x25 0x50 0x44 0x46
        return data[0] == 0x25 &&
               data[1] == 0x50 &&
               data[2] == 0x44 &&
               data[3] == 0x46;
    }

    /**
     * Checks the end of the filename to see if contains .pdf
     * use isPdf(byte[] data) instead for more reliable checks
     * @param filepath the filename/filepath to check
     * @return true if matches pdf otherwise false
     */
    public static boolean isPdf(String filepath) {
        if (TextUtils.isEmpty(filepath)) {
            return false;
        }

        return filepath.endsWith(PDF) || filepath.endsWith(PDF.toLowerCase());
    }

    /**
     * Read file contents to a byte array
     */
    public static byte[] readToByteArray(File file) throws IOException {
        if (file == null) {
            return null;
        }

        byte[] result = new byte[(int) file.length()];

        InputStream inputStream = new FileInputStream(file);
        try {
            int offset = 0, read = 0;
            while (offset < result.length &&
                    (read = inputStream.read(result, offset, result.length - offset)) != -1) {
                offset = read;
            }

        } catch (Exception e) {
            return null;
        } finally {
            inputStream.close();
        }

        return result;
    }


    /**
     * Read first N bytes from file to a byte array
     * @param file The file to be read
     * @param num the number of bytes to read
     */
    public static byte[] readFirstXToByteArray(File file, int num) throws IOException {
        if (file == null) {
            return null;
        }

        byte[] result = new byte[num];
        InputStream inputStream = new FileInputStream(file);
        try {
            inputStream.read(result, 0, num);
        } catch (Exception e) {
            return null;
        } finally {
            inputStream.close();
        }

        return result;
    }
}
