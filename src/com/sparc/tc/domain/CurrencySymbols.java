package com.sparc.tc.domain;

public enum CurrencySymbols {

    USD {
        @Override
        public String getSymbol() {
            return "$";
        }
    },
    EUR {
        @Override
        public String getSymbol() {
            return "€";
        }
    },
    GBP {
        @Override
        public String getSymbol() {
            return "£";
        }
    };

    public abstract String getSymbol();

}
