package org.iem.vocabulary_trainer.training;

import android.os.Bundle;
import android.widget.TextView;

interface TrainingContract {

    interface View {
        boolean writeVocab(String vocab, boolean isAnswer);
        boolean showAmountsOfBoxes(int[] amountInBoxes, int boxesAmount);
        boolean trainingFinished();
        void setTextSize(TextView textView, int textSize);
    }

    interface Presenter {
        void startTraining(Bundle savedInstanceState);
        void vocabAnswered(boolean wasRight);
        void showAnswer();
        void findBestTextSize(TextView textView, String text);
        Bundle saveVariables();
    }
}
