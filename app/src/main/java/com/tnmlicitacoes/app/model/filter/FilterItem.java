package com.tnmlicitacoes.app.model.filter;

/**
 * Created by diegobezerra on 5/31/17.
 */

public class FilterItem {

    public interface FilterType {
        int LABEL = 0;
        int VALUE = 1;
    }

    private int mType;
    private String mLabel;
    private String mValue;

    public FilterItem() {

    }

    public FilterItem(String label, String value, int type) {
        this.mLabel = label;
        this.mValue = value;
        this.mType = type;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String label) {
        this.mLabel = label;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        this.mValue = value;
    }
}
