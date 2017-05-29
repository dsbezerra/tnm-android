package com.tnmlicitacoes.app.utils;

import com.tnmlicitacoes.app.type.Modality;

public class NoticeUtils {

    private static final String[] MODALITIES = {
            "Pregão Presencial",
            "Pregão Eletrônico",
            "Concorrência",
            "Convite",
            "Concurso",
            "Leilão",
            "Tomada de Preço",
    };

    public static String resolveModalityToName(Modality modality) {
        switch (modality) {
            case PP:
                return MODALITIES[0];
            case PE:
                return MODALITIES[1];
            case CONCORRENCIA:
                return MODALITIES[2];
            case CONVITE:
                return MODALITIES[3];
            case CONCURSO:
                return MODALITIES[4];
            case LEILAO:
                return MODALITIES[5];
            case TOMADA_DE_PRECO:
                return MODALITIES[6];
            default:
                return null;
        }
    }

    public static Modality resolveNameToModality(String modality) {
        if (modality.equals(MODALITIES[0])) {
            return Modality.PP;
        } else if (modality.equals(MODALITIES[1])) {
            return Modality.PE;
        } else if (modality.equals(MODALITIES[2])) {
            return Modality.CONCORRENCIA;
        } else if (modality.equals(MODALITIES[3])) {
            return Modality.CONVITE;
        } else if (modality.equals(MODALITIES[4])) {
            return Modality.CONCURSO;
        } else if (modality.equals(MODALITIES[5])) {
            return Modality.LEILAO;
        } else if (modality.equals(MODALITIES[6])) {
            return Modality.TOMADA_DE_PRECO;
        } else {
            return null;
        }
    }

    private NoticeUtils() {}
}
