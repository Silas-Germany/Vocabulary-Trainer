package org.iem.vocabulary_trainer.training;

import android.content.Context;

class TrainingPresenter implements TrainingContract.Presenter {
    private static final String LOG_TAG = "LCIRO_" + TrainingPresenter.class.getSimpleName();

    // init
    private static TrainingContract.View mView = null;
    private final Context mContext;
    TrainingPresenter(TrainingContract.View viewInstance, Context context) {
        mView = viewInstance;
        mContext = context;
    }
}
