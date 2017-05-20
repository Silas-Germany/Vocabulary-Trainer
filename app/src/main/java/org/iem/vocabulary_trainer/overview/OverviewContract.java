package org.iem.vocabulary_trainer.overview;

import java.util.List;

interface OverviewContract {

    interface View {
        boolean initLists(List<String> leftList, List<String> rightList);
    }

    interface Presenter {
        void showElements();
    }

}
