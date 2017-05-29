package com.tnmlicitacoes.app.model;

public enum Modality {

    PP {
        @Override
        public String toString() {
            return "Pregão Presencial";
        }
    },
    PE{
        @Override
        public String toString() {
            return "Pregão Eletrônico";
        }
    },
    CONCORRENCIA {
        @Override
        public String toString() {
            return "Concorrência";
        }
    },
    CONVITE {
        @Override
        public String toString() {
            return "Convite";
        }
    },
    CONCURSO {
        @Override
        public String toString() {
            return "Concurso";
        }
    },
    LEILAO {
        @Override
        public String toString() {
            return "Leilão";
        }
    },
    TOMADA_DE_PRECO {
        @Override
        public String toString() {
            return "Tomada de Preço";
        }
    }
}
