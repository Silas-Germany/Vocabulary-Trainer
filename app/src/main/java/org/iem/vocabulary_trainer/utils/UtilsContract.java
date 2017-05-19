package org.iem.vocabulary_trainer.utils;

import org.iem.vocabulary_trainer.data.BasicVocabData;

import java.util.List;

public interface UtilsContract {

    interface Database {
        List<BasicVocabData> getAllBasicVocabData();
        void saveBasicEntries(List<BasicVocabData> entries);
    }

}
