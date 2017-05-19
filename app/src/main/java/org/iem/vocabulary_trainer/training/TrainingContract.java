package org.iem.vocabulary_trainer.training;

import android.os.Bundle;

class TrainingContract {

    interface View {
        boolean writeVocab(String vocab, boolean isAnswer);
        boolean showAmountsOfBoxes(int[] amountInBoxes, int boxesAmount);
        boolean trainingFinished();
    }

    interface Presenter {
        void startTraining(Bundle savedInstanceState);
        void vocabAnswered(boolean wasRight);
        void showAnswer();
        Bundle saveVariables();
    }
}
