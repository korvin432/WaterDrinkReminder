package com.aqua.drinkreminder;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

public class FragmentActivity extends Fragment implements View.OnClickListener {

    Button btnNext;
    TextView btnBack;

    private RecyclerView recyclerView;
    private boolean isMale;

    private int[] imageList = new int[]{
            R.drawable.low_activity,
            R.drawable.normal_activity,
            R.drawable.high_activity};

    private int[] imageListWoman = new int[]{
            R.drawable.low_activity_wom,
            R.drawable.normal_activity_wom,
            R.drawable.high_activity_wom};

    private String[] activityList;
    private String[] descriptionList;

    public static FragmentActivity newInstance() {
        return new FragmentActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_activity, container, false);
        ImageView imageViewActivity = rootView.findViewById(R.id.image_back_activity);

        activityList = getContext().getResources().getStringArray(R.array.activity_levels);
        descriptionList = getContext().getResources().getStringArray(R.array.activities_description);

        isMale = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isMale", true);
        if(!isMale){
            imageViewActivity.setImageDrawable(getContext().getResources().getDrawable(R.drawable.activity_wom));
        }

        btnNext = rootView.findViewById(R.id.btnNext);
        btnNext.setOnClickListener(this);
        btnBack = rootView.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);

        recyclerView = rootView.findViewById(R.id.recycler);

        ArrayList<ActivityModel> imageModelArrayList = createModel();
        ActivityAdapter adapter = new ActivityAdapter(getContext(), imageModelArrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.scrollToPosition(1);

        return rootView;
    }

    private ArrayList<ActivityModel> createModel(){
        ArrayList<ActivityModel> list = new ArrayList<>();

        for(int i = 0; i < 3; i++){
            ActivityModel fruitModel = new ActivityModel();
            fruitModel.setActivity(activityList[i]);
            fruitModel.setDescription(descriptionList[i]);
            if(isMale) {
                fruitModel.setImageDrawable(imageList[i]);
            } else {
                fruitModel.setImageDrawable(imageListWoman[i]);
            }
            list.add(fruitModel);
        }

        return list;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                getActivity().getSupportFragmentManager().popBackStack();
                break;
            case R.id.btnNext:
                LinearLayoutManager layoutManager = ((LinearLayoutManager )recyclerView.getLayoutManager());
                int firstVisiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (firstVisiblePosition == -1){
                    recyclerView.scrollToPosition(1);
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("physicalActivity", 1).apply();
                } else {
                    recyclerView.scrollToPosition(firstVisiblePosition);
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putInt("physicalActivity", firstVisiblePosition).apply();
                }
                FragmentTime fragment = FragmentTime.newInstance(true, "");
                getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in_bottom, R.anim.fade_out_fast)
                        .replace(R.id.container, fragment).replace(R.id.container, fragment)
                        .addToBackStack("t").commit();
                break;
        }
    }
}
