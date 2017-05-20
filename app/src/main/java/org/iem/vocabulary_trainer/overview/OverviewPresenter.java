package org.iem.vocabulary_trainer.overview;

import android.content.Context;
import android.util.Log;

import org.iem.vocabulary_trainer.data.BasicVocabData;
import org.iem.vocabulary_trainer.utils.GlobalData;

import java.util.ArrayList;
import java.util.List;

public class OverviewPresenter implements OverviewContract.Presenter {
    private static final String LOG_TAG = "VT_" + OverviewPresenter.class.getSimpleName();

    // init
    private static OverviewContract.View mView = null;
    private final Context mContext;

    OverviewPresenter(OverviewContract.View viewInstance, Context context) {
        mView = viewInstance;
        mContext = context;
    }

    @Override
    public void showElements() {
        List<BasicVocabData> vocabData = GlobalData.sDatabase.getAllBasicVocabData();
        List<String> originData = new ArrayList<>();
        List<String> translationData = new ArrayList<>();
        for (BasicVocabData vocab : vocabData) {
            originData.add(vocab.origin);
            translationData.add(vocab.translation);
        }
        if (!mView.initLists(originData, translationData)) {
            Log.e(LOG_TAG, "Error initializing lists");
        }
    }
}
