package org.iem.vocabulary_trainer.utils;

import org.iem.vocabulary_trainer.data.Vocab;

import java.util.List;

public interface UtilsContract {

    interface Database {
        List<Vocab> getAllVocabulary();
        void saveEntries(List<Vocab> entries);
    }

}
