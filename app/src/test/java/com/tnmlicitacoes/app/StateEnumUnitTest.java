package com.tnmlicitacoes.app;

import com.tnmlicitacoes.app.model.State;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StateEnumUnitTest {
    @Test
    public void toString_isCorrect() throws Exception {
        assertEquals(State.AC.toString(), "Acre");
        assertEquals(State.AL.toString(), "Alagoas");
        assertEquals(State.AP.toString(), "Amapá");
        assertEquals(State.AM.toString(), "Amazonas");
        assertEquals(State.BA.toString(), "Bahia");
        assertEquals(State.CE.toString(), "Ceará");
        assertEquals(State.DF.toString(), "Distrito Federal");
        assertEquals(State.ES.toString(), "Espírito Santo");
        assertEquals(State.GO.toString(), "Goiás");
        assertEquals(State.MA.toString(), "Maranhão");
        assertEquals(State.MS.toString(), "Mato Grosso do Sul");
        assertEquals(State.MT.toString(), "Mato Grosso");
        assertEquals(State.MG.toString(), "Minas Gerais");
        assertEquals(State.PA.toString(), "Pará");
        assertEquals(State.PB.toString(), "Paraíba");
        assertEquals(State.PR.toString(), "Paraná");
        assertEquals(State.PE.toString(), "Pernambuco");
        assertEquals(State.PI.toString(), "Piauí");
        assertEquals(State.RJ.toString(), "Rio de Janeiro");
        assertEquals(State.RN.toString(), "Rio Grande do Norte");
        assertEquals(State.RS.toString(), "Rio Grande do Sul");
        assertEquals(State.RO.toString(), "Rondônia");
        assertEquals(State.RR.toString(), "Roraima");
        assertEquals(State.SC.toString(), "Santa Catarina");
        assertEquals(State.SP.toString(), "São Paulo");
        assertEquals(State.SE.toString(), "Sergipe");
        assertEquals(State.TO.toString(), "Tocantins");
    }
}
