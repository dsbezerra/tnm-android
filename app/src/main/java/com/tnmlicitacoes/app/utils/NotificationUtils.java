package com.tnmlicitacoes.app.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import com.tnmlicitacoes.app.BuildConfig;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.service.TipRatingService;
import com.tnmlicitacoes.app.ui.main.MainActivity;
import com.tnmlicitacoes.app.ui.activity.WebviewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.tnmlicitacoes.app.utils.AndroidUtilities.sOnUpdateListener;

public class NotificationUtils {

    private static final String TAG = "NotificationUtils";

    private interface RequestCodes {
        int TIP_ACTION1_REQUEST_CODE    = 30000;
        int TIP_ACTION2_REQUEST_CODE    = 35000;
        int TIPS_REQUEST_CODE           = 40000;
        int NEWS_REQUEST_CODE           = 50000;
        int NEW_BIDDING_REQUEST_CODE    = 60000;
        int MULTICAST_REQUEST_CODE      = 70000;
        int UPDATE_REQUEST_CODE         = 80000;
    }

    private interface NotificationIdentifiers {
        int TIP             = 10000;
        int NEWS            = 20000;
        int NEW_BID         = 30000;
        int MULTICAST       = 40000;
        int UPDATE          = 50000;
    }

    private interface Devices {
        int ALL         = 0;
        int ANDROID     = 1;
        int IOS         = 2; // Not used
    }

    public static final short NOTIFICATION_SYSTEM_VERSION_CODE = 2;
    public static final String NOTIFICATION_VERSION_CODE = "nVersionCode";
    public static final String DEVICE = "device";

    public interface Topics {
        String TOPICS   = "/topics/";
        String GLOBAL   = "global";
        String TIPS     = "tips";
        String NEWS     = "news";
        String SALES    = "sales";
        String GENERAL  = "general";
        String UPDATES  = "updates";
        String DEV      = "dev";
    }

    private interface TipNotification {
        String TIP_CONTENT = "tipContent";

        interface IntentExtras {
            String LIKE = "LIKE";
            String TIP_CONTENT = "TIP_CONTENT";
            String NOTIFICATION_ID = "NOTIFICATION_ID";
        }
    }

    private interface NewsNotification {
        String NEWS_TITLE = "newsTitle";
        String NEWS_LINK  = "newsLink";

        interface IntentExtras {
            String PAGE_TITLE = WebviewActivity.PAGE_TITLE;
            String PAGE_LINK = WebviewActivity.PAGE_LINK;
        }
    }

    private interface NewBiddingNotification {
        String PAYLOAD              = "payload";

        interface Payload {
            String AMOUNT           = "valor";
            String AGENCIES         = "orgaos";
            String DESCRIPTION      = "objeto";
            String AGENCY_NAME      = "nome";
        }

        interface IntentExtras {
            String FROM             = "FROM";
            String NOTICE_ID        = "NOTICE_ID";
        }
    }

    private interface MulticastNotification {
        String TITLE                    = "title";
        String MESSAGE                  = "message";
        String ACTIVITY_CLASS_ID        = "activityClassId";
        String INTENT_EXTRAS            = "intentExtras";
        String TOPIC                    = "topic";
    }

    private interface UpdateNotification {
        String NEWEST_VERSION_CODE = "newestVersionCode";
        String TYPE                = "type";
        String NOTIFICATION        = "notification";
        String DIALOG              = "dialog";
    }



    private static int mNotificationCount = 0;

    private static NotificationManager mNotificationManager;

    private static final Uri mDefaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    public static void handleGlobalTopic(Context context, Map data) {
        // TODO(diego): Implement later, using multicast notification for now
        try {
            sendNotification(context, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void handleTipsTopic(Context context, Map data) {
        if(data == null) {
            return;
        }

        String tipContent = data.get(TipNotification.TIP_CONTENT).toString();
        int notificationId = NotificationIdentifiers.TIP + mNotificationCount;

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, RequestCodes.TIPS_REQUEST_CODE + mNotificationCount, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Intent likeIntent = new Intent(context, TipRatingService.class);
        likeIntent.putExtra(TipNotification.IntentExtras.LIKE, true);
        likeIntent.putExtra(TipNotification.IntentExtras.TIP_CONTENT, tipContent);
        likeIntent.putExtra(TipNotification.IntentExtras.NOTIFICATION_ID, notificationId);
        PendingIntent likePendingIntent = PendingIntent.getService(context, RequestCodes.TIP_ACTION1_REQUEST_CODE + mNotificationCount, likeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent dislikeIntent = new Intent(context, TipRatingService.class);
        dislikeIntent.putExtra(TipNotification.IntentExtras.LIKE, false);
        dislikeIntent.putExtra(TipNotification.IntentExtras.TIP_CONTENT, tipContent);
        dislikeIntent.putExtra(TipNotification.IntentExtras.NOTIFICATION_ID, notificationId);
        PendingIntent dislikePendingIntent = PendingIntent.getService(context, RequestCodes.TIP_ACTION2_REQUEST_CODE + mNotificationCount, dislikeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_tips_title))
                .setContentText(Html.fromHtml(tipContent))
                .setAutoCancel(true)
                .setSound(mDefaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(tipContent))
                .addAction(new NotificationCompat.Action(R.drawable.ic_thumb_up, context.getString(R.string.notification_tips_action1), likePendingIntent))
                .addAction(new NotificationCompat.Action(R.drawable.ic_thumb_down, context.getString(R.string.notification_tips_action2), dislikePendingIntent));

        if(mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.notify(notificationId, notificationBuilder.build());
        mNotificationCount++;
    }

    /**
     * Handle news notifications
     * */
    public static void handleNewsTopic(Context context, Map data) {
        if(data == null) {
            return;
        }

        String newsTitle = data.get(NewsNotification.NEWS_TITLE).toString();
        String newsLink = data.get(NewsNotification.NEWS_LINK).toString();

        Intent intent = new Intent(context, WebviewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(NewsNotification.IntentExtras.PAGE_TITLE, newsTitle);
        intent.putExtra(NewsNotification.IntentExtras.PAGE_LINK, newsLink);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, RequestCodes.NEWS_REQUEST_CODE + mNotificationCount, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_news_title))
                .setContentText(newsTitle)
                .setAutoCancel(true)
                .setSound(mDefaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(newsTitle));

        if(mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.notify(NotificationIdentifiers.NEWS + mNotificationCount++, notificationBuilder.build());
    }

    /**
     * Handle new biddings notifications
     */
    public static void handleNewBiddingsTopic(Context context, Map data) throws JSONException {
        return;
//        if(data == null) {
//            return;
//        }
//
//        if(data.getString(NewBiddingNotification.PAYLOAD) == null) {
//            return;
//        }
//
//        Realm realm = Realm.getDefaultInstance();
//
//        JSONObject payload = new JSONObject(data.getString(NewBiddingNotification.PAYLOAD));
//        JSONObject orgaos = new JSONObject(payload.getString(NewBiddingNotification.Payload.AGENCIES));
//
//        LocationRealm locationRealm = realm.where(LocationRealm.class).equalTo("id", orgaos.getString("cidadeId")).findFirst();
//        if(locationRealm == null) {
//            realm.close();
//            return;
//        }
//
//        String objeto = payload.getString(NewBiddingNotification.Payload.DESCRIPTION);
//        String local = orgaos.getString(NewBiddingNotification.Payload.AGENCY_NAME);
//
//        String contentText = objeto + "\nLocal: " + local;
//        int valor = payload.getInt(NewBiddingNotification.Payload.AMOUNT);
//        if(valor > 0) {
//            String valorStr = NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(valor);
//            contentText += "\nValor estimado: " + valorStr;
//        }
///*
//        Realm realm = Realm.getDefaultInstance();
//        try {
//            persistNotification(realm, title, contentText.toUpperCase());
//        }
//        catch(RealmPrimaryKeyConstraintException e) {
//            persistNotification(realm, title, contentText.toUpperCase());
//        }
//
//        RealmResults<NotificationRealm> notificationsList = realm.where(NotificationRealm.class).findAll();
//        int unreadCount = notificationsList.size();
//
//
//        if(unreadCount > 1) {
//            // Inbox Style notification
//            NotificationCompat.Builder summaryNotification = new NotificationCompat.Builder(context);
//
//            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
//            for(int i = 0; i < unreadCount; i++) {
//                NotificationRealm item = notificationsList.get(i);
//                inboxStyle.addLine(item.getTitle() + " - " + item.getBody());
//            }
//
//            summaryNotification.setContentTitle(unreadCount + " novas oportunidades")
//                    .setSound(mDefaultSoundUri)
//                    .setStyle(inboxStyle)
//                    .setAutoCancel(true)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setGroup(GROUP_KEY_NEW_BIDDINGS)
//                    .setGroupSummary(true);
//
//            notificationManager.notify(NOTIFICATION_NEW_BIDDINGS_ID, summaryNotification.build());
//
//        } else {
//            // Single notification
//            NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
//
//            notification.setContentTitle(title)
//                    .setContentText(contentText.toUpperCase())
//                    .setSound(mDefaultSoundUri)
//                    .setAutoCancel(true)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setGroup(GROUP_KEY_NEW_BIDDINGS)
//                    .setGroupSummary(true)
//                    .setStyle(new NotificationCompat.BigTextStyle()
//                            .bigText(setBoldStyle(contentText)));
//
//            notificationManager.notify(NOTIFICATION_NEW_BIDDINGS_ID, notification.build());
//        }*/
//
//        // Single notification
//
//
//        Intent intent = new Intent(context, DetailsActivity.class);
//        intent.putExtra(NewBiddingNotification.IntentExtras.FROM, "NOTIFICATION");
//        intent.putExtra(NewBiddingNotification.IntentExtras.NOTICE_ID, payload.getString("id"));
//
//        TaskStackBuilder stackBuilder= TaskStackBuilder.create(context);
//        stackBuilder.addParentStack(DetailsActivity.class);
//        stackBuilder.addNextIntent(intent);
//
//        PendingIntent pendingIntent = stackBuilder.getPendingIntent(RequestCodes.NEW_BIDDING_REQUEST_CODE + mNotificationCount, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
//        notification.setContentTitle(context.getString(R.string.notification_new_bidding_title))
//                .setContentText(contentText.toUpperCase())
//                .setSound(mDefaultSoundUri)
//                .setAutoCancel(true)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentIntent(pendingIntent)
//                .setStyle(new NotificationCompat.BigTextStyle()
//                        .bigText(setBoldStyle(contentText, "Local:", "Valor estimado:")));
//
//        if(mNotificationManager == null) {
//            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//        }
//        mNotificationManager.notify(NotificationIdentifiers.NEW_BID + mNotificationCount++, notification.build());
    }

    public static void handleSalesTopic(Context context, Map data) {
        // TODO(diego): Implement later, using multicast notification for now
        try {
            sendNotification(context, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void handleGeneralTopic(Context context, Map data) {
        // TODO(diego): Implement later, using multicast notification for now
        try {
            // Handle message to a specific user here
            sendNotification(context, data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void handleUpdatesTopic(Context context, Map data) {

        try {
            int newestVersionCode = Integer.parseInt(data.get(UpdateNotification.NEWEST_VERSION_CODE).toString());
            if(newestVersionCode > BuildConfig.VERSION_CODE) {

                String type = data.get(UpdateNotification.TYPE).toString();
                if(TextUtils.isEmpty(type)) {
                    sendUpdateNotification(context, data);
                }
                else {
                    if(type.contains(UpdateNotification.NOTIFICATION)) {
                        sendUpdateNotification(context, data);
                    }
                    else if (type.contains(UpdateNotification.DIALOG)) {
                        if(sOnUpdateListener != null) {
                            sOnUpdateListener.onNewUpdate();
                        }

                        SettingsUtils.putInt(context, SettingsUtils.PREF_NEWEST_VERSION_CODE, newestVersionCode);
                    }
                }
            }
            else {
                LogUtils.LOG_DEBUG(TAG, "nothing to do");
            }
        } catch (NumberFormatException e) {
            if(BuildConfig.DEBUG) {
                e.printStackTrace();
            }
        }

}

    private static void sendUpdateNotification(Context context, Map data) {
        Intent intent = Utils.createPlayStoreIntent(context);
        String title    = data.get(MulticastNotification.TITLE).toString();
        String message  = data.get(MulticastNotification.MESSAGE).toString();

        PendingIntent pendingIntent = PendingIntent.getActivity(context, RequestCodes.UPDATE_REQUEST_CODE + mNotificationCount, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(TextUtils.isEmpty(title) ? context.getString(R.string.notification_update_title) : title)
                .setContentText(TextUtils.isEmpty(message) ? context.getString(R.string.notification_update_message) : message)
                .setAutoCancel(true)
                .setSound(mDefaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(TextUtils.isEmpty(message) ? context.getString(R.string.notification_update_message) : message));

        if(mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.notify(NotificationIdentifiers.UPDATE + mNotificationCount++, notificationBuilder.build());
    }

    public static void handleMulticastNotification(Context context, Map data) {

        if (data == null)
            return;

        try {

            String versionCode = data.get(NOTIFICATION_VERSION_CODE).toString();
            if(TextUtils.isEmpty(versionCode))
                return;

            short notificationVersionCode = Short.parseShort(versionCode);

            if(notificationVersionCode == NOTIFICATION_SYSTEM_VERSION_CODE) {
                // Check if is coming from a topic
                // (not the default topics implementation of Google!!!)
                String topic = data.get(MulticastNotification.TOPIC).toString();
                if(!TextUtils.isEmpty(topic)) {
                    if(topic.contains(Topics.GLOBAL)) {
                        handleGlobalTopic(context, data);
                    }
                    else if (topic.contains(Topics.GENERAL)) {
                        handleGeneralTopic(context, data);
                    }
                    else if (topic.contains(Topics.NEWS)) {
                        handleNewsTopic(context, data);
                    }
                    else if (topic.contains(Topics.TIPS)) {
                        handleTipsTopic(context, data);
                    }
                    else if (topic.contains(Topics.SALES)) {
                        handleSalesTopic(context, data);
                    }
                    else if (topic.contains(Topics.UPDATES)) {
                        handleUpdatesTopic(context, data);
                    }
                    else {
                        // Message came from a category topic
                        try {
                            if(SettingsUtils.isBiddingsNotificationsEnabled(context)) {
                                handleNewBiddingsTopic(context, data);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    try {
                        sendNotification(context, data);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                if(BuildConfig.DEBUG) {
                    LogUtils.LOG_DEBUG(TAG, "Version does not match");
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            if(BuildConfig.DEBUG) {
                LogUtils.LOG_DEBUG(TAG, "Number format wrong");
            }
        }
    }

    private static void sendNotification(Context context, Map data) throws JSONException {
        String title = data.get(MulticastNotification.TITLE).toString();
        String message = data.get(MulticastNotification.MESSAGE).toString();
        short activityClassId = Short.parseShort(data.get(MulticastNotification.ACTIVITY_CLASS_ID).toString());

        JSONArray intentExtrasArray = new JSONArray(data.get(MulticastNotification.INTENT_EXTRAS));

        HashMap<String, Object> intentExtras = new HashMap<>();
        for(int i = 0; i < intentExtrasArray.length(); ++i) {
            JSONObject jsonObject = intentExtrasArray.getJSONObject(i);
            String key   = jsonObject.getString("key");
            Object value = jsonObject.get("value");
            intentExtras.put(key, value);
        }

        Notification notification = buildNotification(context,
                title, message,
                RequestCodes.MULTICAST_REQUEST_CODE,
                activityClassId,
                intentExtras);

        if(mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.notify(NotificationIdentifiers.MULTICAST + mNotificationCount++, notification);
    }

    /**
     * Build a general notification
     * @param context App context
     * @param title Notification title
     * @param message Notification message
     * @param requestCode PendingIntent requestCode
     * @param classId Activity ID (see AndroidUtilities.getClassById method for more info)
     * @param intentExtras Intent extras hash map
     * @return Notification object
     */
    public static Notification buildNotification(Context context,
                                                 String title,
                                                 String message,
                                                 int requestCode,
                                                 short classId,
                                                 HashMap<String, ?> intentExtras) {

        Intent intent = new Intent(context, AndroidUtilities.getClassById(classId));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        for(String key : intentExtras.keySet()) {
            Object object = intentExtras.get(key);
            if(object instanceof String) {
                intent.putExtra(key, (String) object);
            }
            else if (object instanceof Integer) {
                intent.putExtra(key, (Integer) object);
            }
            else if (object instanceof Float) {
                intent.putExtra(key, (Float) object);
            }
            else if (object instanceof Boolean) {
                intent.putExtra(key, (Boolean) object);
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(context, requestCode + mNotificationCount, intent,
                PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(mDefaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message));

        return notificationBuilder.build();
    }

    public static Spannable setBoldStyle(String text, String... strings) {
        Spannable sb = new SpannableString(text);
        for(int i = 0; i < strings.length; i++) {
            int indexStr = text.indexOf(strings[i]);
            if(indexStr > -1) {
                sb.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), indexStr,
                        indexStr + strings[i].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
       return sb;
    }

    public static boolean checkForDevice(int deviceType) {
        return deviceType == Devices.ANDROID || deviceType == Devices.ALL;
    }

    private NotificationUtils() {}
}
