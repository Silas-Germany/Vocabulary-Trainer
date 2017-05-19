package org.iem.vocabulary_trainer.training;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import org.iem.vocabulary_trainer.R;
import org.iem.vocabulary_trainer.data.BasicVocabData;
import org.iem.vocabulary_trainer.data.TrainingData;
import org.iem.vocabulary_trainer.utils.GlobalData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TrainingPresenter implements TrainingContract.Presenter {
    private static final String LOG_TAG = "VT_" + TrainingPresenter.class.getSimpleName();

    private List<TrainingData> mVocabData = new ArrayList<>();
    private int actualEntry = -1;
    private int mSumAskedEntries = 0;

    private static final int[] BOXES_VALUES = {0, 5, 15, 25, 35, 45};
    private static final int MINIMAL_DISTANCE = 4;
    private static final int BOXES_AMOUNT = 3;
    private static final int RANDOM_DEVIATION = 4;

    // init
    private static TrainingContract.View mView = null;
    private final Context mContext;

    TrainingPresenter(TrainingContract.View viewInstance, Context context) {
        mView = viewInstance;
        mContext = context;

        if (GlobalData.sDatabase != null){
            for (BasicVocabData vocabData : GlobalData.sDatabase.getAllBasicVocabData()) {
                TrainingData newEntry = new TrainingData();
                newEntry.basicVocabData = vocabData;
                mVocabData.add(newEntry);
            }
        }
    }

    // starts the training through showing the questionable vocabulary
    @Override
    public void startTraining() {
        if (mVocabData.size() == 0) {
            Log.e(LOG_TAG, "No Vocabulary saved");
            seedEntries();
            return;
        }
        if (!mView.showAmountsOfBoxes(getBoxesAmount(), BOXES_AMOUNT + 1)) {
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
        if (!mView.writeVocab(getActualVocab().origin, false)) {
            Log.e(LOG_TAG, "Error writing vocabulary");
        }
    }

    // writes the answer
    @Override
    public void showAnswer() {
        if (mVocabData.size() == 0) {
            Log.e(LOG_TAG, "No Vocabulary saved");
            return;
        }
        if (!mView.writeVocab(getActualVocab().translation, true)) {
            Log.e(LOG_TAG, "Error writing vocabulary");
        }
    }

    // saves changes and starts next vocabulary
    @Override
    public void vocabAnswered(boolean wasRight) {
        // put in the correct box (from 0=stock directly to 2)
        if (getActualVocabData().box <= 1) {
            if (wasRight) mVocabData.get(actualEntry).box = 2;
            else mVocabData.get(actualEntry).box = 1;
        } else mVocabData.get(actualEntry).box += wasRight? 1 : -1;

        if (!wasRight) mVocabData.get(actualEntry).mistakes++;
        mVocabData.get(actualEntry).asked++;
        Log.d(LOG_TAG, "Vocabulary now in box " + getActualVocabData().box);
        mVocabData.get(actualEntry).lastLearned = mSumAskedEntries;
        mSumAskedEntries++;
        startTraining();
    }

    // adds a new vocab
    private boolean addActualVocab() {
        int newEntry;
        List<Integer> remainingEntries = new ArrayList<>();
        for (int i = 0; i < mVocabData.size(); i++) {
            if (mVocabData.get(i).box == 0) remainingEntries.add(i);
        }
        if (remainingEntries.size() == 0) return false;
        newEntry = getRandomNumber(remainingEntries.size());
        actualEntry = remainingEntries.get(newEntry);
        Log.d(LOG_TAG, "Got index " + remainingEntries.get(newEntry) + " from " + newEntry +
                " out of " + remainingEntries.size());
        Log.d(LOG_TAG, "Asking new entry: " + getActualVocab().origin);
        return true;
    }

    // returns actual vocabulary
    private BasicVocabData getActualVocab() {
        return getActualVocabData().basicVocabData;
    }

    // returns actual vocabulary-data
    private TrainingData getActualVocabData() {
        return mVocabData.get(actualEntry);
    }
    // returns random number from 0 to max-1
    private int getRandomNumber (int max) {
        Random random = new Random(SystemClock.elapsedRealtime());
        return random.nextInt(max);
    }

    // check, if an old entry exists, that has to be asked again
    private boolean checkForOldEntry() {
        for (TrainingData oldEntry : mVocabData) {
            if (oldEntry.box > 0 && oldEntry.box < BOXES_AMOUNT) {
                // use random number between -4 and 4 for mixing stack up
                int mixingStack = getRandomNumber(RANDOM_DEVIATION * 2 + 1) - RANDOM_DEVIATION;
                int whenAgain = mSumAskedEntries - // e.g. 17
                        BOXES_VALUES[oldEntry.box] - // e.g. 15
                        oldEntry.lastLearned + // e.g. 2
                        mixingStack;
                // if an entry was found, make it being the actual one
                Log.d(LOG_TAG, "Again in " + whenAgain);
                if (whenAgain >= 0) {
                    actualEntry = mVocabData.indexOf(oldEntry);
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
        for (TrainingData oldEntry : mVocabData) {
            if (oldEntry.box > 0 && oldEntry.box < BOXES_AMOUNT) {
                int thisValue = BOXES_VALUES[oldEntry.box] + oldEntry.lastLearned;
                // check, whether item was recently done and in case save separately
                int lastDone = mSumAskedEntries - oldEntry.lastLearned;
                if (lastDone < MINIMAL_DISTANCE) {
                    if (lastDone > longestTime) {
                        longestTime = lastDone;
                        longestTimeEntry = mVocabData.indexOf(oldEntry);
                    }
                // check, for greatest value (inclusive according to the box it is in)
                } else if (thisValue < bestValue) {
                    bestValue = thisValue;
                    bestEntry = mVocabData.indexOf(oldEntry);
                }
            }
        }
        if (bestEntry == -1) {
            if (longestTimeEntry == -1) return false;
            else bestEntry = longestTimeEntry;
        }
        actualEntry = bestEntry;
        Log.d(LOG_TAG, "Asking best old entry: " + getActualVocab().origin);
        return true;
    }

    private int[] getBoxesAmount() {
        int[] boxesAmount = new int[6]; // default value is 0
        for (TrainingData entry : mVocabData) {
            if (entry.box < 0 || entry.box > BOXES_AMOUNT) return null;
            boxesAmount[entry.box]++;
        }
        return boxesAmount;
    }

    private void seedEntries() {
        List<BasicVocabData> newEntries = new ArrayList<>();
        BasicVocabData newEntry = new BasicVocabData();
        newEntry.origin = "I";
        newEntry.translation = "Ich";
        newEntries.add(newEntry);
        newEntry = new BasicVocabData();
        newEntry.origin = "You (Sg.)";
        newEntry.translation = "Du";
        newEntries.add(newEntry);
        newEntry = new BasicVocabData();
        newEntry.origin = "He";
        newEntry.translation = "Er";
        newEntries.add(newEntry);
        newEntry = new BasicVocabData();
        newEntry.origin = "She";
        newEntry.translation = "Sie (Sg.)";
        newEntries.add(newEntry);
        newEntry = new BasicVocabData();
        newEntry.origin = "It";
        newEntry.translation = "Es";
        newEntries.add(newEntry);
        newEntry = new BasicVocabData();
        newEntry.origin = "We";
        newEntry.translation = "Wir";
        newEntries.add(newEntry);
        newEntry = new BasicVocabData();
        newEntry.origin = "You (Pl.)";
        newEntry.translation = "Ihr";
        newEntries.add(newEntry);
        newEntry = new BasicVocabData();
        newEntry.origin = "They";
        newEntry.translation = "Sie (Pl.)";
        newEntries.add(newEntry);
        GlobalData.sDatabase.saveBasicEntries(newEntries);
        Log.d(LOG_TAG, "New entries added");
    }
}
