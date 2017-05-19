package org.iem.vocabulary_trainer.training;

class TrainingContract {

    interface View {
        boolean writeVocab(String vocab, boolean isAnswer);
        boolean showAmountsOfBoxes(int[] amountInBoxes, int boxesAmount);
        boolean trainingFinished();
    }

    interface Presenter {
        void startTraining();
        void vocabAnswered(boolean wasRight);
        void showAnswer();
    }
}
