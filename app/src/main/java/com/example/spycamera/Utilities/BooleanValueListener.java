package com.example.spycamera.Utilities;

public class BooleanValueListener {
    private boolean myValue;

    public BooleanValueListener(boolean value) {
        setValue(value);
    }


    public boolean getValue() {
        return myValue;
    }

    public void setValue(boolean value) {
        if (value != myValue) {
            myValue = value;
            signalChanged();
        }
    }

    public interface VariableChangeListener {
        void onVariableChanged(Object... variableThatHasChanged);
    }

    private VariableChangeListener variableChangeListener;

    public void setVariableChangeListener(VariableChangeListener variableChangeListener) {
        this.variableChangeListener = variableChangeListener;
    }

    private void signalChanged() {
        if (variableChangeListener != null)
            variableChangeListener.onVariableChanged(myValue);
    }
}