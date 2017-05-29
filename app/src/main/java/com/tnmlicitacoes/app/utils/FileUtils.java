package com.tnmlicitacoes.app.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.tnmlicitacoes.app.mupdf.MuPDFActivity;
import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;

import java.io.File;
import java.io.IOException;
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
}
