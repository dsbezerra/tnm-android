package com.tnmlicitacoes.app.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.mupdf.MuPDFActivity;
import com.tnmlicitacoes.app.ui.main.MainActivity;
import com.tnmlicitacoes.app.utils.FileUtils;
import com.tnmlicitacoes.app.utils.SettingsUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class DownloadService extends IntentService {

    private static final String TAG = "DownloadService";

    private static final int DOWNLOADED_FAILED = -1;
    private static final int DOWNLOAD_START    = 0;
    private static final int DOWNLOAD_ONGOING  = 1;
    private static final int DOWNLOAD_FINISHED = 2;

    private static final int BUFFER_SIZE = 4096;

    private static final long MIN_UPDATE_INTERVAL = 1000;

    private static DecimalFormat mDecimalFormat = new DecimalFormat("#.##");

    private NotificationManager mNotifyManager;

    private NotificationCompat.Builder mBuilder;

    private boolean mExternalStorageAvailable = false;

    private boolean mExternalStorageWriteable = false;

    public DownloadService() {super(TAG); }

    public interface OnDownloadListener {
        void onDownloadStart();
        void onDownloadFailure(final String fileName);
        void onDownloadFinished(final File file);
    }

    public static OnDownloadListener onDownloadListener;

    @Override
    protected void onHandleIntent(Intent intent) {
        LOG_DEBUG(TAG, "Starting download service...");

        initNotification();

        LOG_DEBUG(TAG, "Handling intent...");

        final Bundle b = intent.getExtras();
        if(b != null) {

            final String url = b.getString("LINK");
            final String fileName = b.getString("NAME");
            final int notificationId = (int) b.getDouble("NOTIFICATION_ID");

            LOG_DEBUG(TAG, "Preparing request...");

            OkHttpClient okHttpClient = new OkHttpClient();

            if(url == null) {
                return;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    LOG_DEBUG(TAG, "Failure in download!");
                    if (onDownloadListener != null) {
                        onDownloadListener.onDownloadFailure(fileName);
                    }

                    updateNotification(notificationId,
                            DOWNLOADED_FAILED, 0, fileName, null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        updateExternalStorageState();
                        if (mExternalStorageWriteable && mExternalStorageAvailable) {
                            startDownload(response, notificationId, fileName);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.fail_download_storage_unavailable), Toast.LENGTH_SHORT).show();
                            LOG_DEBUG(TAG, "Armazenamento externo indisponível!");
                            mNotifyManager.cancel(notificationId);
                        }

                    } else {
                        mNotifyManager.cancel(notificationId);
                        LOG_DEBUG(TAG, "Fail!");
                    }
                }
            });
        }

        LOG_DEBUG(TAG, "Finished intent service.");
    }

    private void startDownload(Response response, int notificationId, String fileName) throws IOException {
        LOG_DEBUG(TAG, "Starting download process...");

        InputStream inputStream = null;

        updateNotification(notificationId,
                DOWNLOAD_START, 0, null, null);

        try {

            LOG_DEBUG(TAG, "Getting file size...");


            long fileSize = response.body().contentLength();
            if (fileSize == -1) {
                LOG_DEBUG(TAG, "Failed to get file size!");
            }

            File destFolder = FileUtils.createDestinationFolder("Editais");
            if (destFolder == null) {
                return;
            }

            LOG_DEBUG(TAG, "Getting input stream...");

            inputStream = response.body().byteStream();

            byte[] buffer = new byte[BUFFER_SIZE];
            int bufferLength;
            long totalDownloaded = 0;

            LOG_DEBUG(TAG, "Start downloading...");

            long lastNotificationUpdatedTime = System.currentTimeMillis();
            long downloadStartTime = System.currentTimeMillis();

            LOG_DEBUG(TAG, "Creating downloaded file...");

            File downloadedFile = FileUtils.createFile(destFolder, fileName);

            LOG_DEBUG(TAG, "Initializing output stream...");

            FileOutputStream fileOutputStream = new FileOutputStream(downloadedFile);

            if(onDownloadListener != null) {
                onDownloadListener.onDownloadStart();
            }

            LOG_DEBUG(TAG, "Reading bytes...");

            while ((bufferLength = inputStream.read(buffer)) > 0) {
                totalDownloaded += bufferLength;
                fileOutputStream.write(buffer, 0, bufferLength);

                // Update notification progress by after 1s
                if(System.currentTimeMillis() - lastNotificationUpdatedTime > MIN_UPDATE_INTERVAL) {

                    int progress = (int) ((totalDownloaded * 100) / fileSize);

                    String downloadSpeed = getDownloadSpeed(downloadStartTime, totalDownloaded);
                    String remainingTime = getRemainingTime(downloadStartTime, fileSize, totalDownloaded);

                    updateNotification(notificationId,
                            DOWNLOAD_ONGOING, progress, downloadSpeed + " • " + remainingTime, null);

                    lastNotificationUpdatedTime = System.currentTimeMillis();

                    LOG_DEBUG(TAG, totalDownloaded + "/" + fileSize + " - " + progress + "%");

                }
            }

            LOG_DEBUG(TAG, "Finished reading bytes!");
            LOG_DEBUG(TAG, "Closing output stream...");

            fileOutputStream.close();

            LOG_DEBUG(TAG, "Download complete!");

            updateNotification(notificationId,
                    DOWNLOAD_FINISHED, 0, fileName, downloadedFile);

            if(onDownloadListener != null) {
                onDownloadListener.onDownloadFinished(downloadedFile);
            }

        } catch (IOException e) {
            if(onDownloadListener != null) {
                onDownloadListener.onDownloadFailure(fileName);
            }

            updateNotification(notificationId,
                    DOWNLOADED_FAILED, 0, fileName, null);

            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void initNotification() {
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);

        Intent intent = new Intent(this, MuPDFActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    private void updateNotification(int notificationId, int eventType, int progress, String text, File file) {

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent;

        if(eventType == DOWNLOAD_START) {

            pendingIntent = PendingIntent.getActivity(this, 454545, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentText(getString(R.string.download_starting))
                    .setContentText(getString(R.string.determining_download_time))
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setProgress(100, progress, true)
                    .setSmallIcon(android.R.drawable.stat_sys_download)
                    .setContentIntent(pendingIntent);

        } else if (eventType == DOWNLOAD_ONGOING) {

            pendingIntent = PendingIntent.getActivity(this, 454545, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentTitle(getString(R.string.downloading_file))
                    .setContentText(text)
                    .setAutoCancel(false)
                    .setOngoing(true)
                    .setProgress(100, progress, false)
                    .setContentIntent(pendingIntent);

        } else if (eventType == DOWNLOAD_FINISHED) {

            Uri uri = Uri.fromFile(file);
            if(SettingsUtils.isDefaultPdfViewer(getApplicationContext())) {
                intent = new Intent(getApplicationContext(), MuPDFActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(uri);
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            }

            pendingIntent = PendingIntent.getActivity(this, 454545, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            mBuilder.setContentTitle(getString(R.string.download_finished))
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setProgress(0, progress, false)
                    .setOngoing(false)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent);
        } else {
            mBuilder.setContentTitle(getString(R.string.download_failed))
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setOngoing(false)
                    .setProgress(0, progress, false)
                    .setSmallIcon(R.mipmap.ic_launcher);
        }

        mNotifyManager.notify(notificationId, mBuilder.build());

    }

    private String getDownloadSpeed(long startTime, long totalDownloaded) {
        long elapsedTime = System.currentTimeMillis() - startTime;

        float bytesPerSecond = 1000f * totalDownloaded / elapsedTime;
        float kiloBytesPerSecond = bytesPerSecond / 1024;
        float megaBytesPerSecond = kiloBytesPerSecond / 1024;

        if(megaBytesPerSecond > 1.0f) {
            return mDecimalFormat.format(megaBytesPerSecond) + " MB/s";
        } else if (kiloBytesPerSecond > 1.0f) {
            return mDecimalFormat.format(kiloBytesPerSecond) + " KB/s";
        } else {
            return mDecimalFormat.format(bytesPerSecond) + " B/s";
        }
    }

    private String getRemainingTime(long startTime, long fileSize, long totalDownloaded) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long allTimeForDownloading = (elapsedTime * fileSize / totalDownloaded);
        long remainingTime = allTimeForDownloading - elapsedTime;

        int seconds = (int) remainingTime / 1000;
        int minutes = seconds / 60;
        int hours = minutes / 60;

        if(hours > 0) {
            return hours + "h restantes";
        } else if (minutes > 0) {
            return minutes + "m restantes";
        } else {
            return seconds + "s restantes";
        }
    }

    private void updateExternalStorageState() {
        String currentState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(currentState)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(currentState)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }
}
