package org.iem.vocabulary_trainer.overview;

import java.util.List;

interface OverviewContract {

    interface View {
        boolean initLists(List<String> rightList, List<String> leftList);
    }

    interface Presenter {
        void showElements();
    }

}
