package org.iem.vocabulary_trainer.training;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.iem.vocabulary_trainer.R;
import org.iem.vocabulary_trainer.utils.NavigationDrawerActivity;


public class TrainingView extends Fragment implements TrainingContract.View {

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
        mPresenter.startTraining();
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
    public boolean showAmountsOfBoxes(int[] boxesAmount) {
        if (getView() == null) return false;
        Button box = (Button) getView().findViewById(R.id.training_box_0);
        if (box == null) return false;
        box.setText(String.valueOf(boxesAmount[0]));
        box = (Button) getView().findViewById(R.id.training_box_1);
        if (box == null) return false;
        box.setText(String.valueOf(boxesAmount[1]));
        box = (Button) getView().findViewById(R.id.training_box_2);
        if (box == null) return false;
        box.setText(String.valueOf(boxesAmount[2]));
        box = (Button) getView().findViewById(R.id.training_box_3);
        if (box == null) return false;
        box.setText(String.valueOf(boxesAmount[3]));
        box = (Button) getView().findViewById(R.id.training_box_4);
        if (box == null) return false;
        box.setText(String.valueOf(boxesAmount[4]));
        box = (Button) getView().findViewById(R.id.training_box_5);
        if (box == null) return false;
        box.setText(String.valueOf(boxesAmount[5]));
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
}
