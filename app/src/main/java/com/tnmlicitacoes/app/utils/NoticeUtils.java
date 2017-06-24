package com.tnmlicitacoes.app.utils;

import com.tnmlicitacoes.app.NoticesQuery;
import com.tnmlicitacoes.app.model.realm.Agency;
import com.tnmlicitacoes.app.model.realm.Notice;
import com.tnmlicitacoes.app.model.realm.Segment;
import com.tnmlicitacoes.app.type.Modality;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import io.realm.internal.android.ISO8601Utils;

public class NoticeUtils {

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

        Date disputeDate = new Date();
        try {
            disputeDate = ISO8601Utils.parse(node.disputeDate().toString(),
                    new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Notice notice = new Notice(node.id(), node.object(), node.number(), node.link(), node.url(),
                node.modality().name(), node.exclusive(), node.amount(),
                Agency.mapToRealmFromGraphQL(node.agency()),
                Segment.mapToRealmFromGraphQL(node.segment()),
                disputeDate);
        notice.setSegId(node.segment().id());
        notice.setAgencyId(node.agency().id());
        return notice;
    }

    private NoticeUtils() {}
}
