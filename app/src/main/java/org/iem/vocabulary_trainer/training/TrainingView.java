package org.iem.vocabulary_trainer.training;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.iem.vocabulary_trainer.R;
import org.iem.vocabulary_trainer.utils.NavigationDrawerActivity;


public class TrainingView extends Fragment implements TrainingContract.View {

    private TextView mVocabField;

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
        mVocabField = (TextView) view.findViewById(R.id.training_vocab_field);
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

    @Override
    public int writeVocab(String text, boolean isAnswer) {
        if (mVocabField == null) return 3;
        if (getView() == null) return 1;
        View questionExtra= getView().findViewById(R.id.training_question_extra);
        if (questionExtra == null) return 4;
        View answerExtra = getView().findViewById(R.id.training_answer_extra);
        if (answerExtra == null) return 2;
        questionExtra.setVisibility(!isAnswer ? View.VISIBLE : View.INVISIBLE);
        answerExtra.setVisibility(isAnswer ? View.VISIBLE : View.INVISIBLE);
        mVocabField.setText(text);
        return 0;
    }
}
