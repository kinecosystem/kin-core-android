package kin.sdk.core.sample;

import android.content.Context;
import android.view.View;
import kin.sdk.core.ResultCallback;
import kin.sdk.core.sample.kin.sdk.core.sample.dialog.KinAlertDialog;

/**
 * Will hide a progressBar and display result on a displayView passed at constructor
 * Holds the views as weakReferences and clears the references when canceled
 */
public abstract class DisplayCallback<T> implements ResultCallback<T> {

    private View progressBar;
    private View displayView;

    public DisplayCallback(View progressBar, View displayView) {
        this.progressBar = progressBar;
        this.displayView = displayView;
    }

    public DisplayCallback(View progressBar) {
        this.progressBar = progressBar;
    }

    /**
     * displayView will be null if DisplayCallback was constructed using the single parameter constructor.
     */
    abstract public void displayResult(Context context, View displayView, T result);

    @Override
    public void onResult(T result) {
        progressBar.setVisibility(View.GONE);
        displayResult(progressBar.getContext(), displayView, result);
    }

    @Override
    public void onError(Exception e) {
        progressBar.setVisibility(View.GONE);
        KinAlertDialog.createErrorDialog(progressBar.getContext(), e.getMessage()).show();
    }
}