package org.iem.vocabulary_trainer.overview;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.iem.vocabulary_trainer.R;
import org.iem.vocabulary_trainer.utils.NavigationDrawerActivity;

import java.util.List;

public class OverviewView extends Fragment implements OverviewContract.View {

    // init
    private OverviewContract.Presenter mPresenter;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        mPresenter = new OverviewPresenter(this, getActivity());
        setHasOptionsMenu(true);
        ((NavigationDrawerActivity) getActivity()).updateItem();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPresenter.showElements();
    }

    @Override
    public boolean initLists(List<String> leftList, List<String> rightList) {
        if (getView() == null) return false;
        ListView list = (ListView) getView().findViewById(R.id.overview_left_list);
        if (list == null) return false;
        list.setAdapter(new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, leftList));
        list = (ListView) getView().findViewById(R.id.overview_right_list);
        if (list == null) return false;
        list.setAdapter(new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, rightList));
        return true;
    }
}
