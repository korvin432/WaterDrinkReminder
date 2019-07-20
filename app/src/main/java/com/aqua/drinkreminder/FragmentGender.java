package com.aqua.drinkreminder;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Objects;

public class FragmentGender extends Fragment implements View.OnClickListener {

    private ImageView btnMan;
    private ImageView btnWoman;

    private int gender = 0;

    public static FragmentGender newInstance() {
        return new FragmentGender();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_gender, container, false);

        btnMan = rootView.findViewById(R.id.man);
        btnWoman = rootView.findViewById(R.id.wom);
        Button btnNext = rootView.findViewById(R.id.btnNext);

        btnMan.setOnClickListener(this);
        btnWoman.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.man:
                btnMan.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.man_clicked));
                btnWoman.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.wom));
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isMale", true).apply();
                gender = 1;
                break;
            case R.id.wom:
                btnWoman.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.wom_clicked));
                btnMan.setImageDrawable(Objects.requireNonNull(getContext()).getResources().getDrawable(R.drawable.man));
                PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isMale", false).apply();
                gender = 2;
                break;
            case R.id.btnNext:
                if (gender != 0) {
                    FragmentWeight fragment = FragmentWeight.newInstance();
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out_fast)
                            .replace(R.id.container, fragment).replace(R.id.container, fragment)
                            .addToBackStack("w").commit();
                } else {
                    Toast.makeText(getContext(), getContext().getResources().getString(R.string.choose_your_gender), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
