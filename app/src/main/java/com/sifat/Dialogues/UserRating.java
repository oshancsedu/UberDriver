package com.sifat.Dialogues;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.sifat.uberdriver.R;

import static com.sifat.Utilities.CommonUtilities.*;

/**
 * Created by sifat on 2/5/2016.
 */
public class UserRating extends DialogFragment implements View.OnClickListener,
        RatingBar.OnRatingBarChangeListener {

    Communicator communicator;
    private Button btPayment;
    private TextView tvUserName, tvRating;
    private RatingBar rbUserRate;
    private String userName;
    private SharedPreferences sharedPreferences;
    private float rating;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        communicator = (Communicator) activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedPreferences = getSharedPref(getActivity());
        userName = sharedPreferences.getString(SELECTED_USER_NAME, "null");
        rating = sharedPreferences.getFloat(SELECTED_USER_RATING, 0.0f);
        getDialog().setTitle("Rate Your Driver");
        View view = inflater.inflate(R.layout.rate_the_user, null);
        intiViews(view);
        setCancelable(false);
        return view;
    }

    private void intiViews(View view) {
        tvUserName = (TextView) view.findViewById(R.id.tvDriverName);
        tvUserName.setText(userName);
        tvRating = (TextView) view.findViewById(R.id.tvYourRate);
        rbUserRate = (RatingBar) view.findViewById(R.id.rbDriverRate);
        //rbDriverRate.setRating(rating + 0.5f);
        rbUserRate.setRating(rating);
        rbUserRate.setOnRatingBarChangeListener(this);
        btPayment = (Button) view.findViewById(R.id.btPayment);
        btPayment.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btPayment) {
            communicator.RatingDialog();
            dismiss();
        }
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rate, boolean b) {
        tvRating.setText("You Rated: " + rate);
    }


    public interface Communicator {
        public void RatingDialog();
    }
}
