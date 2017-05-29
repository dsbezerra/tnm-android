package com.tnmlicitacoes.app;

import com.tnmlicitacoes.app.model.Modality;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModalityEnumUnitTest {

    @Test
    public void toString_isCorrect() throws Exception {
        assertEquals(Modality.PP.toString(), "Pregão Presencial");
        assertEquals(Modality.PE.toString(), "Pregão Eletrônico");
        assertEquals(Modality.CONCORRENCIA.toString(), "Concorrência");
        assertEquals(Modality.CONVITE.toString(), "Convite");
        assertEquals(Modality.CONCURSO.toString(), "Concurso");
        assertEquals(Modality.LEILAO.toString(), "Leilão");
        assertEquals(Modality.TOMADA_DE_PRECO.toString(), "Tomada de Preço");
    }
}
