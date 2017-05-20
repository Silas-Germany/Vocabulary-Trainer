package org.iem.vocabulary_trainer.utils;

import org.iem.vocabulary_trainer.data.BasicVocabData;
import org.iem.vocabulary_trainer.data.EvaluatedData;

import java.util.List;

public interface UtilsContract {

    interface Database {
        List<BasicVocabData> getAllBasicVocabData();
        boolean saveBasicEntries(List<BasicVocabData> entries);
        List<EvaluatedData> getAllEvaluatedData();
        boolean updateEvaluatedData(List<EvaluatedData> evaluatedDataList);
        boolean resetEvaluation();
    }

}
