package org.iem.vocabulary_trainer.training;

class TrainingContract {

    interface View {
        int writeVocab(String vocab, boolean isAnswer);
    }

    interface Presenter {
        void startTraining();
        void vocabAnswered(boolean wasRight);
        void showAnswer();
    }
}
