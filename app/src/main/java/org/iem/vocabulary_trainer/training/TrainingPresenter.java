package org.iem.vocabulary_trainer.training;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.iem.vocabulary_trainer.R;
import org.iem.vocabulary_trainer.data.BasicVocabData;
import org.iem.vocabulary_trainer.data.EvaluatedData;
import org.iem.vocabulary_trainer.data.TrainingData;
import org.iem.vocabulary_trainer.utils.GlobalData;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class TrainingPresenter implements TrainingContract.Presenter {
    private static final String LOG_TAG = "VT_" + TrainingPresenter.class.getSimpleName();

    private final List<TrainingData> mVocabData = new ArrayList<>();
    private int mActualEntry = -1;
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
                newEntry.vocabData = vocabData;
                mVocabData.add(newEntry);
            }
        }
        Log.d(LOG_TAG, "Data loaded");
    }

    // starts the training through showing the questionable vocabulary
    @Override
    public void startTraining(Bundle savedInstanceState) {
        if (mVocabData.size() == 0) {
            Log.e(LOG_TAG, "No Vocabulary saved");
            seedEntries();
            return;
        }
        boolean trainingFinished = false;
        if (savedInstanceState != null) restoreVariables(savedInstanceState);
        else if (!checkForOldEntry()) {
            if (!addActualVocab()) {
                if (!takeBestOldEntry()) {
                    Toast.makeText(mContext, mContext.getString(R.string.training_toast_all_done),
                            Toast.LENGTH_LONG).show();
                    if (!trainingFinished() || !mView.trainingFinished()) {
                        Log.e(LOG_TAG, "Error finishing training");
                    }
                    trainingFinished = true;
                }
            }
        }
        if (!mView.showAmountsOfBoxes(getAmountInBoxes(), BOXES_AMOUNT + 1)) {
            Log.e(LOG_TAG, "Error showing amount of boxes");
        }
        if (!trainingFinished) {
            if (!mView.writeVocab(getActualVocab().origin, false)) {
                Log.e(LOG_TAG, "Error writing vocabulary");
            }
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
            if (wasRight) mVocabData.get(mActualEntry).box = 2;
            else mVocabData.get(mActualEntry).box = 1;
        } else mVocabData.get(mActualEntry).box += wasRight? 1 : -1;

        if (!wasRight) mVocabData.get(mActualEntry).mistakes++;
        mVocabData.get(mActualEntry).asked++;
        Log.d(LOG_TAG, "Vocabulary now in box " + getActualVocabData().box);
        mVocabData.get(mActualEntry).lastLearned = mSumAskedEntries;
        mSumAskedEntries++;
        startTraining(null);
    }

    @Override
    public void findBestTextSize(TextView textView, String text) {
        // wait for one minute, if textView wasn't initialized yet (width is necessary)
        int actualTimeStamp = GlobalData.getTimestamp();
        while (textView.getWidth() == 0) {
            if (actualTimeStamp + 1 < GlobalData.getTimestamp()) return;
        }

        // check for allowed line-number through a new TextView
        TextView measureTextView = new TextView(mContext);
        measureTextView.setTypeface(textView.getTypeface());
        int freeSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int widthSpec = View.MeasureSpec.makeMeasureSpec(textView.getWidth(), View.MeasureSpec.EXACTLY);
        measureTextView.setText(text);
        int textSize = (int) textView.getTextSize() + 5; // undo the -5
        do {
            textSize -= 5;
            measureTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            measureTextView.measure(widthSpec, freeSpec);
        } while (measureTextView.getMeasuredHeight() > textView.getHeight());

        // check for too long words through a paint object
        Paint paint = new Paint();
        paint.setTypeface(textView.getTypeface());
        // init variables here necessary
        String subText;
        int amount;
        textSize += 5; // undo the -5
        do {
            subText = text;
            textSize -= 5;
            paint.setTextSize(textSize);
            int splitPoint;
            do {
                // check, how many characters can be drawn in one line
                amount = paint.breakText(subText, true, textView.getWidth(), null);
                // split text at a space (like textView does it) and take second part
                splitPoint = subText.substring(0, amount).lastIndexOf(' ');
                subText = subText.substring(splitPoint + 1, subText.length());
            } while (splitPoint != -1);
        } while (subText.length() != amount);
        Log.d(LOG_TAG, "Found best text size: " + textSize);
        mView.setTextSize(textView, textSize);
    }

    // returns a bundle of all variables for saving (for screen rotation)
    @Override
    public Bundle saveVariables() {
        ArrayList<Integer> id = new ArrayList<>();
        ArrayList<Integer> box = new ArrayList<>();
        ArrayList<Integer> lastLearned = new ArrayList<>();
        ArrayList<Integer> mistakes = new ArrayList<>();
        ArrayList<Integer> asked = new ArrayList<>();
        for (TrainingData entry : mVocabData) {
            id.add(entry.vocabData.id);
            box.add(entry.box);
            lastLearned.add(entry.lastLearned);
            mistakes.add(entry.mistakes);
            asked.add(entry.asked);
        }
        Bundle result = new Bundle();
        result.putIntegerArrayList("id", id);
        result.putIntegerArrayList("box", box);
        result.putIntegerArrayList("lastLearned", lastLearned);
        result.putIntegerArrayList("mistakes", mistakes);
        result.putIntegerArrayList("asked", asked);
        result.putInt("actualEntry", mActualEntry);
        result.putInt("sumAskedEntries", mSumAskedEntries);
        Log.d(LOG_TAG, "Variables saved");
        return result;
    }

    // restores all variables (after screen rotation)
    private void restoreVariables(Bundle variables) {
        if (!variables.containsKey("id")) {
            Log.e(LOG_TAG, "Error restoring variables. Nothing saved");
            return;
        }
        List<Integer> id = variables.getIntegerArrayList("id");
        List<Integer> box = variables.getIntegerArrayList("box");
        List<Integer> lastLearned = variables.getIntegerArrayList("lastLearned");
        List<Integer> mistakes = variables.getIntegerArrayList("mistakes");
        List<Integer> asked = variables.getIntegerArrayList("asked");
        if (id == null || box == null || lastLearned == null ||
                mistakes == null || asked == null ||
                id.size() != box.size() ||
                id.size() != lastLearned.size() ||
                id.size() != mistakes.size() ||
                id.size() != asked.size()) {
            Log.e(LOG_TAG, "Error restoring variables. Not existing / not same amount");
            return;
        }
        List<BasicVocabData> allVocabData = GlobalData.sDatabase.getAllBasicVocabData();
        mVocabData.clear();
        for (BasicVocabData vocabData : allVocabData) {
            int index = id.indexOf(vocabData.id);
            TrainingData entry = new TrainingData();
            entry.vocabData = vocabData;
            entry.box = box.get(index);
            entry.lastLearned = lastLearned.get(index);
            entry.mistakes = mistakes.get(index);
            entry.asked = asked.get(index);
            mVocabData.add(entry);
        }
        mActualEntry = variables.getInt("actualEntry");
        mSumAskedEntries = variables.getInt("sumAskedEntries");
        Log.d(LOG_TAG, "Variables restored");
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
        mActualEntry = remainingEntries.get(newEntry);
        Log.d(LOG_TAG, "Asking new entry: " + getActualVocab().origin);
        return true;
    }

    // returns actual vocabulary
    private BasicVocabData getActualVocab() {
        return getActualVocabData().vocabData;
    }

    // returns actual vocabulary-data
    private TrainingData getActualVocabData() {
        return mVocabData.get(mActualEntry);
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
                if (whenAgain >= 0) {
                    mActualEntry = mVocabData.indexOf(oldEntry);
                    Log.d(LOG_TAG, "Asking old entry: " + getActualVocab().origin);
                    return true;
                }
            }
        }
        return false;
    }

    // returns best old entry (used if none is ready yet)
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
        mActualEntry = bestEntry;
        Log.d(LOG_TAG, "Asking best old entry: " + getActualVocab().origin);
        return true;
    }

    // get amounts of vocabulary of each box
    private int[] getAmountInBoxes() {
        int[] boxesAmount = new int[BOXES_AMOUNT + 1]; // default value is 0
        for (TrainingData entry : mVocabData) {
            if (entry.box < 0 || entry.box > BOXES_AMOUNT) return null;
            boxesAmount[entry.box]++;
        }
        return boxesAmount;
    }

    // save results of the training in the database
    private boolean trainingFinished() {
        List<EvaluatedData> evaluatedDataList = GlobalData.sDatabase.getAllEvaluatedData();
        for (TrainingData trainingData : mVocabData) {
            // get fitting (same id) evaluating data
            EvaluatedData evaluatedData = null;
            for (EvaluatedData data : evaluatedDataList) {
                if ((evaluatedData = data).vocabId == trainingData.vocabData.id) break;
            }
            if (evaluatedData == null) return false;
            // save correctness
            if (trainingData.mistakes > trainingData.asked) return false;
            float actualCorrectness = 1 - (trainingData.mistakes / (float) trainingData.asked);
            if (evaluatedData.correctness < 0 || evaluatedData.correctness > 1) {
                evaluatedData.correctness = actualCorrectness;
            }
            else {
                evaluatedData.correctness *= 0.8;
                evaluatedData.correctness += actualCorrectness * 0.2;
            }
            // save box
            if (trainingData.mistakes <= 2) evaluatedData.box--;
            else evaluatedData.box++;
            // save trained
            evaluatedData.trained++;
            // save date
            evaluatedData.lastTrained = GlobalData.getTimestamp();
        }
        Log.d(LOG_TAG, "Vocabulary evaluated");
        return GlobalData.sDatabase.updateEvaluatedData(evaluatedDataList);
    }

    private void seedEntries() {
        String fileContent = null;
        try {
            InputStream input = mContext.getAssets().open("vocabulary.txt");
            byte[] buffer = new byte[input.available()];
            if (input.read(buffer) == -1) throw new IOException("Empty file");
            input.close();
            fileContent = new String(buffer);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error reading file");
        }
        if (fileContent == null) return;

        List<BasicVocabData> vocabDataList = new ArrayList<>();
        String[] fileLines = fileContent.split("\n");
        for (String line : fileLines) {
            String[] vocabData = line.split("\t");
            BasicVocabData vocab = new BasicVocabData();
            vocab.translation = vocabData[0];
            vocab.origin = vocabData[1];
            vocabDataList.add(vocab);
        }
        GlobalData.sDatabase.saveBasicEntries(vocabDataList);
        Log.d(LOG_TAG, "New entries added");
    }
}
