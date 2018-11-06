package kin.sdk.sample;

import android.content.Context;
import android.view.View;
import kin.sdk.ResultCallback;

/**
 * Will hide a progressBar and display result on a displayView passed at constructor
 * Holds the views as weakReferences and clears the references when canceled
 */
public abstract class DisplayCallback<T> implements ResultCallback<T> {

    private static final String TAG = DisplayCallback.class.getSimpleName();
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
        Utils.logError(e, "DisplayCallback");
        progressBar.setVisibility(View.GONE);
        KinAlertDialog.createErrorDialog(progressBar.getContext(), e.getMessage()).show();
    }
}