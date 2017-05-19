package org.iem.vocabulary_trainer.training;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.iem.vocabulary_trainer.R;
import org.iem.vocabulary_trainer.data.Vocab;
import org.iem.vocabulary_trainer.utils.GlobalData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TrainingPresenter implements TrainingContract.Presenter {
    private static final String LOG_TAG = "VT_" + TrainingPresenter.class.getSimpleName();

    private List<Vocab> mVocabEntries = new ArrayList<>();
    private List<Integer> mAskedEntries = new ArrayList<>();
    private boolean mIsAnswer = false;
    private int mAllAskedEntriesAmount = 0;
    private static final int[] BOXES_VALUES = {5, 15, 25, 35, 45};
    private static final int MINIMAL_DISTANCE = 4;

    // init
    private static TrainingContract.View mView = null;
    private final Context mContext;

    TrainingPresenter(TrainingContract.View viewInstance, Context context) {
        mView = viewInstance;
        mContext = context;

        if (GlobalData.sDatabase != null){
            mVocabEntries = GlobalData.sDatabase.getAllVocabulary();
        }
    }

    // starts the training through showing the questionable vocabulary
    @Override
    public void startTraining() {
        if (mIsAnswer) {
            Log.e(LOG_TAG, "Shouldn't be an answer");
            return;
        }
        if (mVocabEntries.size() == 0) {
            Log.e(LOG_TAG, "No Vocabulary saved");
            seedEntries();
            return;
        }
        if (!mView.showAmountsOfBoxes(getBoxesAmount())) {
            Log.e(LOG_TAG, "Error showing amount of boxes");
        }
        if (!checkForOldEntry()) {
            if (!addActualVocab()) {
                if (!takeBestOldEntry()) {
                    Toast.makeText(mContext, mContext.getString(R.string.training_toast_all_done),
                            Toast.LENGTH_LONG).show();
                    if (!mView.trainingFinished()) {
                        Log.e(LOG_TAG, "Error finishing training");
                    }
                    return;
                }
            }
        }
        if (!mView.writeVocab(getActualVocab().origin, mIsAnswer)) {
            Log.e(LOG_TAG, "Error writing vocabulary");
        }
    }

    // writes the answer
    @Override
    public void showAnswer() {
        if (mVocabEntries.size() == 0) {
            Log.e(LOG_TAG, "No Vocabulary saved");
            return;
        }
        if (mIsAnswer) {
            Log.e(LOG_TAG, "Shouldn't be an answer");
            return;
        }
        mIsAnswer = true;
        if (!mView.writeVocab(getActualVocab().translation, mIsAnswer)) {
            Log.e(LOG_TAG, "Error writing vocabulary");
        }
    }

    // saves changes and starts next vocabulary
    @Override
    public void vocabAnswered(boolean wasRight) {
        if (!mIsAnswer) {
            Log.e(LOG_TAG, "Should be an answer");
            return;
        }
        mIsAnswer = false;
        mVocabEntries.get(getActualVocabIndex()).box += wasRight? 1 : -1;
        if (getActualVocab().box < 0) {
            mVocabEntries.get(getActualVocabIndex()).box = 0;
        }
        Log.d(LOG_TAG, "Vocab now in box " + getActualVocab().box);
        mVocabEntries.get(getActualVocabIndex()).lastLearned = mAllAskedEntriesAmount;
        mAllAskedEntriesAmount ++;
        startTraining();
    }

    // adds a new vocab
    private boolean addActualVocab() {
        int newEntry;
        List<Integer> remainingEntries = new ArrayList<>();
        for (int i = 0; i < mVocabEntries.size(); i++) {
            if (!mAskedEntries.contains(i)) remainingEntries.add(i);
        }
        if (remainingEntries.size() == 0) return false;
        newEntry = getRandomNumber(remainingEntries.size());
        mAskedEntries.add(remainingEntries.get(newEntry));
        Log.d(LOG_TAG, "Got index " + remainingEntries.get(newEntry) + " from " + newEntry +
                " out of " + remainingEntries.size());
        Log.d(LOG_TAG, "Asking new entry: " + getActualVocab().origin);
        return true;
    }

    // returns actual vocab
    private Vocab getActualVocab() {
        return mVocabEntries.get(getActualVocabIndex());
    }

    // returns the index of the actual vocab
    private int getActualVocabIndex() {
        return mAskedEntries.get(mAskedEntries.size() - 1);
    }

    // returns random number from 0 to max-1
    private int getRandomNumber (int max) {
        Random random = new Random(SystemClock.elapsedRealtime());
        return random.nextInt(max);
    }

    // check, if an old entry exists, that has to be asked again
    private boolean checkForOldEntry() {
        for (int oldEntryIndex : mAskedEntries) {
            Vocab oldEntry = mVocabEntries.get(oldEntryIndex);
            if (oldEntry.box < 5) {
                // use random number between -3 and 3 for mixing stack up
                int mixingStack = getRandomNumber(7) - 3;
                int whenAgain = mAllAskedEntriesAmount - // e.g. 17
                        BOXES_VALUES[oldEntry.box] - // e.g. 15
                        oldEntry.lastLearned + // e.g. 2
                        mixingStack;
                // if an entry was found, make it being the actual one
                if (whenAgain >= 0) {
                    mAskedEntries.remove((Integer) oldEntryIndex);
                    mAskedEntries.add(oldEntryIndex);
                    Log.d(LOG_TAG, "Asking old entry: " + getActualVocab().origin);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean takeBestOldEntry() {
        int bestEntry = -1;
        int bestValue = Integer.MAX_VALUE;
        int longestTimeEntry = -1;
        int longestTime = -1;
        for (int oldEntryIndex : mAskedEntries) {
            Vocab oldEntry = mVocabEntries.get(oldEntryIndex);
            if (oldEntry.box < 5) {
                int thisValue = BOXES_VALUES[oldEntry.box] + oldEntry.lastLearned;
                // check, whether item was recently done and in case save separately
                int lastDone = mAllAskedEntriesAmount - oldEntry.lastLearned;
                if (lastDone < MINIMAL_DISTANCE) {
                    if (lastDone > longestTime) {
                        longestTime = lastDone;
                        longestTimeEntry = oldEntryIndex;
                    }
                // check, for greatest value (inclusive according to the box it is in)
                } else if (thisValue < bestValue) {
                    bestValue = thisValue;
                    bestEntry = oldEntryIndex;
                }
            }
        }
        if (bestEntry == -1) {
            if (longestTimeEntry == -1) return false;
            else bestEntry = longestTimeEntry;
        }
        mAskedEntries.remove((Integer) bestEntry);
        mAskedEntries.add(bestEntry);
        Log.d(LOG_TAG, "Asking best old entry: " + getActualVocab().origin);
        return true;
    }

    private int[] getBoxesAmount() {
        int[] boxesAmount = new int[6]; // default value is 0
        for (Vocab entry : mVocabEntries) {
            if (entry.box < 0 || entry.box > 5) return null;
            boxesAmount[entry.box]++;
        }
        return boxesAmount;
    }

    private void seedEntries() {
        List<Vocab> newEntries = new ArrayList<>();
        Vocab newEntry = new Vocab();
        newEntry.origin = "I";
        newEntry.translation = "Ich";
        newEntry.createdAt = GlobalData.getTimestamp();
        newEntries.add(newEntry);
        newEntry = new Vocab();
        newEntry.origin = "You (Sg.)";
        newEntry.translation = "Du";
        newEntry.createdAt = GlobalData.getTimestamp();
        newEntries.add(newEntry);
        newEntry = new Vocab();
        newEntry.origin = "He";
        newEntry.translation = "Er";
        newEntry.createdAt = GlobalData.getTimestamp();
        newEntries.add(newEntry);
        newEntry = new Vocab();
        newEntry.origin = "She";
        newEntry.translation = "Sie (Sg.)";
        newEntry.createdAt = GlobalData.getTimestamp();
        newEntries.add(newEntry);
        newEntry = new Vocab();
        newEntry.origin = "It";
        newEntry.translation = "Es";
        newEntry.createdAt = GlobalData.getTimestamp();
        newEntries.add(newEntry);
        newEntry = new Vocab();
        newEntry.origin = "We";
        newEntry.translation = "Wir";
        newEntry.createdAt = GlobalData.getTimestamp();
        newEntries.add(newEntry);
        newEntry = new Vocab();
        newEntry.origin = "You (Pl.)";
        newEntry.translation = "Ihr";
        newEntry.createdAt = GlobalData.getTimestamp();
        newEntries.add(newEntry);
        newEntry = new Vocab();
        newEntry.origin = "They";
        newEntry.translation = "Sie (Pl.)";
        newEntry.createdAt = GlobalData.getTimestamp();
        newEntries.add(newEntry);
        GlobalData.sDatabase.saveEntries(newEntries);
        Log.d(LOG_TAG, "New entries added");
    }
}
