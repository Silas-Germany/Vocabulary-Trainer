package org.iem.vocabulary_trainer.training;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.iem.vocabulary_trainer.R;
import org.iem.vocabulary_trainer.utils.NavigationDrawerActivity;

import java.util.ArrayList;
import java.util.List;


public class TrainingView extends Fragment implements TrainingContract.View {

    private List<Button> mBoxButtons = new ArrayList<>();

    // init
    private TrainingContract.Presenter mPresenter;
    private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_training, container, false);
        mPresenter = new TrainingPresenter(this, getActivity());
        setHasOptionsMenu(true);
        ((NavigationDrawerActivity) getActivity()).updateItem();

        // initialize buttons
        view.findViewById(R.id.training_show_answer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.showAnswer();
            }
        });
        view.findViewById(R.id.training_wrong).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.vocabAnswered(false);
            }
        });
        view.findViewById(R.id.training_correct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.vocabAnswered(true);
            }
        });

        return view;
    }

    // start training as soon as view is created
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle variables = null;
        if (savedInstanceState != null) {
            variables = savedInstanceState.getBundle("variables");
        }
        mPresenter.startTraining(variables);
    }

    // writes the vocabulary and controls showing of extra buttons
    @Override
    public boolean writeVocab(String vocab, boolean isAnswer) {
        if (getView() == null) return false;
        // show or show not extras
        View extra= getView().findViewById(R.id.training_question_extra);
        if (extra == null) return false;
        extra.setVisibility(!isAnswer ? View.VISIBLE : View.INVISIBLE);
        extra = getView().findViewById(R.id.training_answer_extra);
        if (extra == null) return false;
        extra.setVisibility(isAnswer ? View.VISIBLE : View.INVISIBLE);
        // write vocab in the specific field (if it's not the answer, delete text there)
        TextView vocabField;
        if (!isAnswer) {
            vocabField = (TextView) getView().findViewById(R.id.training_answer_field);
            if (vocabField == null) return false;
            vocabField.setText(null);
            vocabField = (TextView) getView().findViewById(R.id.training_question_field);
        }
        else vocabField = (TextView) getView().findViewById(R.id.training_answer_field);
        if (vocabField == null) return false;
        vocabField.setText(vocab);
        return true;
    }

    // shows the amount of vocabulary in each box
    @Override
    public boolean showAmountsOfBoxes(int[] amountInBoxes, int boxesAmount) {
        if (mBoxButtons.size() == 0) {
            if (!initBoxes(boxesAmount) || mBoxButtons.size() == 0) return false;
        }
        if (amountInBoxes == null) return false;
        for (int i = 0; i < mBoxButtons.size(); i++) {
            mBoxButtons.get(i).setText(String.valueOf(amountInBoxes[i]));
        }
        return true;
    }

    // initialize the buttons for the boxes
    private boolean initBoxes(final int boxesAmount) {
        if (getView() == null) return false;
        final LinearLayout boxesPlace = (LinearLayout) getView().findViewById(R.id.training_boxes);
        if (boxesPlace == null) return false;
        for (int i = 0; i < boxesAmount; i++) {
            final Button box = new Button(mContext);
            box.setClickable(false);
            box.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    box.setWidth(boxesPlace.getWidth() / boxesAmount);
                    box.removeOnLayoutChangeListener(this);
                }
            });
            boxesPlace.addView(box);
            mBoxButtons.add(box);
        }
        return true;
    }

    // hides the extra buttons
    @Override
    public boolean trainingFinished() {
        if (getView() == null) return false;
        View extras = getView().findViewById(R.id.training_question_extra);
        if (extras == null) return false;
        extras.setVisibility(View.INVISIBLE);
        extras = getView().findViewById(R.id.training_answer_extra);
        if (extras == null) return false;
        extras.setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle("variables", mPresenter.saveVariables());
        super.onSaveInstanceState(outState);
    }

}
