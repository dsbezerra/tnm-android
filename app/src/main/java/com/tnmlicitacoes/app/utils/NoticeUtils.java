package com.tnmlicitacoes.app.utils;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.tnmlicitacoes.app.NoticeByIdQuery;
import com.tnmlicitacoes.app.NoticesQuery;
import com.tnmlicitacoes.app.R;
import com.tnmlicitacoes.app.details.DetailsActivity;
import com.tnmlicitacoes.app.details.DetailsFragment;
import com.tnmlicitacoes.app.model.realm.Agency;
import com.tnmlicitacoes.app.model.realm.Notice;
import com.tnmlicitacoes.app.model.realm.Segment;
import com.tnmlicitacoes.app.service.DownloadService;
import com.tnmlicitacoes.app.type.Modality;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import io.realm.internal.android.ISO8601Utils;
import okhttp3.HttpUrl;

import static com.tnmlicitacoes.app.utils.AndroidUtilities.PERMISSION_REQUEST_WRITE_EXT_STORAGE;
import static com.tnmlicitacoes.app.utils.LogUtils.LOG_DEBUG;

public class NoticeUtils {

    /* The logging tag */
    private static final String TAG = "NoticeUtils";

    /* Google docs view doc URI */
    private static final String GOOGLE_DOCS_VIEW_DOC_URI = "http://docs.google.com/gview?embedded=true&url=";

    /* Keys used in the DownloadService */
    public static final String NAME_KEY = "NAME";
    public static final String LINK_KEY = "LINK";
    public static final String NOTIFICATION_KEY = "NOTIFICATION_ID";

    private static final String[][] MODALITIES = {
            {"Pregão Presencial", "PP"},
            {"Pregão Eletrônico", "PE" },
            {"Concorrência", "CONCORRENCIA"},
            {"Convite", "CONVITE"},
            {"Concurso", "CONCURSO"},
            {"Leilão", "LEILAO"},
            {"Tomada de Preço", "TOMADA_DE_PRECO"},
    };

    public static String resolveModalityToName(Modality modality, boolean enumValue) {

        int index = 0;
        if (enumValue) {
            index = 1;
        }

        switch (modality) {
            case PP:
                return MODALITIES[0][index];
            case PE:
                return MODALITIES[1][index];
            case CONCORRENCIA:
                return MODALITIES[2][index];
            case CONVITE:
                return MODALITIES[3][index];
            case CONCURSO:
                return MODALITIES[4][index];
            case LEILAO:
                return MODALITIES[5][index];
            case TOMADA_DE_PRECO:
                return MODALITIES[6][index];
            default:
                return null;
        }
    }

    public static Modality resolveNameToModality(String modality, boolean enumValue) {

        int index = 0;

        if (enumValue) {
            index = 1;
        }

        if (modality.equals(MODALITIES[0][index])) {
            return Modality.PP;
        } else if (modality.equals(MODALITIES[1][index])) {
            return Modality.PE;
        } else if (modality.equals(MODALITIES[2][index])) {
            return Modality.CONCORRENCIA;
        } else if (modality.equals(MODALITIES[3][index])) {
            return Modality.CONVITE;
        } else if (modality.equals(MODALITIES[4][index])) {
            return Modality.CONCURSO;
        } else if (modality.equals(MODALITIES[5][index])) {
            return Modality.LEILAO;
        } else if (modality.equals(MODALITIES[6][index])) {
            return Modality.TOMADA_DE_PRECO;
        } else {
            return null;
        }
    }

    public static String resolveEnumNameToName(String modality) {
        if (modality.equals(MODALITIES[0][1])) {
            return MODALITIES[0][0];
        } else if (modality.equals(MODALITIES[1][1])) {
            return MODALITIES[1][0];
        } else if (modality.equals(MODALITIES[2][1])) {
            return MODALITIES[2][0];
        } else if (modality.equals(MODALITIES[3][1])) {
            return MODALITIES[3][0];
        } else if (modality.equals(MODALITIES[4][1])) {
            return MODALITIES[4][0];
        } else if (modality.equals(MODALITIES[5][1])) {
            return MODALITIES[5][0];
        } else if (modality.equals(MODALITIES[6][1])) {
            return MODALITIES[6][0];
        } else {
            return null;
        }
    }

    public static Notice mapToRealmFromGraphQL(NoticesQuery.Node node) {
        Notice notice = new Notice(node.id(), node.object(), node.number(), node.link(), node.url(),
                node.modality().name(), node.exclusive(), node.amount(),
                Agency.mapToRealmFromGraphQL(node.agency()),
                Segment.mapToRealmFromGraphQL(node.segment()),
                DateUtils.parse(node.disputeDate().toString()));
        notice.setSegId(node.segment().id());
        notice.setAgencyId(node.agency().id());
        return notice;
    }

    public static Notice mapToRealmFromGraphQL(NoticeByIdQuery.Notice node) {
        Notice notice = new Notice(node.id(), node.object(), node.number(), node.link(), node.url(),
                node.modality().name(), node.exclusive(), node.amount(),
                Agency.mapToRealmFromGraphQL(node.agency()),
                Segment.mapToRealmFromGraphQL(node.segment()),
                DateUtils.parse(node.disputeDate().toString()));
        notice.setSegId(node.segment().id());
        notice.setAgencyId(node.agency().id());
        return notice;
    }

    /**
     * Starts the seeDetails screen
     */
    public static void seeDetails(Context context, Notice notice) {
        if (notice != null) {
            Intent intent = new Intent(context, DetailsActivity.class);
            intent.putExtra(DetailsFragment.NOTICE_ID, notice.getId());
            context.startActivity(intent);
        }
    }

    /**
     * Download a new notice
     */
    public static void download(Context context, Notice notice, DownloadService.OnDownloadListener listener) {
        if (!AndroidUtilities.verifyConnection(context)) {
            Toast.makeText(context, context.getString(R.string.no_connection_try_again), Toast.LENGTH_SHORT).show();
            return;
        }

        String link = notice.getLink();
        if (TextUtils.isEmpty(link) || HttpUrl.parse(link) == null) {
            LOG_DEBUG(TAG, "Invalid link.");
            Toast.makeText(context, context.getString(R.string.download_unavailable), Toast.LENGTH_SHORT).show();
            return;
        }

        final String name = resolveEnumNameToName(notice.getModality()) + " - " +
                notice.getNumber().replaceAll("/", "-") + ".pdf";

        DownloadService.onDownloadListener = listener;
        Intent intent = new Intent(context, DownloadService.class);
        intent.putExtra(LINK_KEY, link);
        intent.putExtra(NAME_KEY, name);
        intent.putExtra(NOTIFICATION_KEY, link.length() + (Math.random() * 10001) + 10000);
        context.startService(intent);
    }

    /**
     * See online
     */
    public static boolean seeOnline(Context context, Notice notice) {
        String link = notice.getLink();
        if (HttpUrl.parse(link) == null || !FileUtils.isPdf(link)) {
            return false;
        }

        link = GOOGLE_DOCS_VIEW_DOC_URI + link;
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, android.R.anim.slide_in_left,
                        android.R.anim.slide_out_right)
                .build();

        // Try to open with chrome.
        customTabsIntent.intent.setPackage("com.android.chrome");
        try {
            customTabsIntent.launchUrl(context, Uri.parse(link));
        } catch (ActivityNotFoundException e) {
            // If chrome isn't available, then show chooser
            // without animations...
            customTabsIntent.intent.setPackage(null);
            customTabsIntent = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary))
                    .build();
            customTabsIntent.launchUrl(context, Uri.parse(link));
        }

        return true;
    }

    private NoticeUtils() {}
}
