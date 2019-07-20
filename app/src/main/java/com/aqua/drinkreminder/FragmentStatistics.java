package com.aqua.drinkreminder;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.os.ConfigurationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class FragmentStatistics extends Fragment implements View.OnClickListener {

    private DBHelper dbHelper;
    private TextView tvWeekAverage;
    private TextView tvMonthAverage;
    private TextView tvMl1;
    private TextView tvMl2;
    private HashMap readyDates;
    private BarChart daysChart;
    private BarChart currentMonthsDaysChart;
    private static final String TAG = "qwwe";

    public static FragmentStatistics newInstance() {
        return new FragmentStatistics();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_statistics, container, false);

        MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544~3347511713");
        AdView adView = new AdView(getContext());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

        AdView mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        dbHelper = new DBHelper(getContext());

        boolean isMetrics = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getBoolean("isMetrics", true);

        tvMl1 = rootView.findViewById(R.id.ml);
        tvMl2 = rootView.findViewById(R.id.m2);

        if (!isMetrics) {
            tvMl1.setText("fl");
            tvMl2.setText("fl");
        }


        tvWeekAverage = rootView.findViewById(R.id.week_average_text);
        setWeekChart(rootView);

        tvMonthAverage = rootView.findViewById(R.id.month_average_text);
        setMonthChart(rootView);

        TextView tvFrequency = rootView.findViewById(R.id.frequency);
        tvFrequency.setText(String.format("%d " + getContext().getResources().getString(R.string.times)
                , dbHelper.getAverageDrinksCount()));

        readyDates = dbHelper.getWeekValues(getWeekDates());

        TextView tvMonth = rootView.findViewById(R.id.month);
        tvMonth.setOnClickListener(this);
        TextView tvYear = rootView.findViewById(R.id.year);
        tvYear.setOnClickListener(this);

        setDayOfWeeks();
        setDaysChart(rootView);

        BarChart monthsChart = populateBarChart(rootView, R.id.monthsChart, 12);
        daysChart = populateBarChart(rootView, R.id.monthsDaysChart, 31);
        currentMonthsDaysChart = populateBarChart(rootView, R.id.currentMonthsDaysChart, 31);
        setMonthsChart(monthsChart);


        return rootView;
    }


    private List<String> getWeekDates() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        List<String> dates = new ArrayList<>();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            dates.add(df.format(c.getTime()));
            c.add(Calendar.DATE, 1);
        }
        return dates;
    }

    private void setDaysChart(View rootView) {
        PieChart mondayChart = populatePieChart(rootView, R.id.mondayChart, 3);
        PieChart tuesdayChart = populatePieChart(rootView, R.id.tuesdayChart, 3);
        PieChart wednesdayChart = populatePieChart(rootView, R.id.wednesdayChart, 3);
        PieChart thursdayChart = populatePieChart(rootView, R.id.thursdayChart, 3);
        PieChart fridayChart = populatePieChart(rootView, R.id.fridayChart, 3);
        PieChart saturdayChart = populatePieChart(rootView, R.id.saturdayChart, 3);
        PieChart sundayChart = populatePieChart(rootView, R.id.sundayChart, 3);

        HashMap days = setDayOfWeeks();
        for (int i = 0; i < 7; i++) {
            switch (String.valueOf(days.keySet().toArray()[i])){
                case "1":
                    createChart(mondayChart, String.valueOf(days.get(days.keySet().toArray()[i])));
                    break;
                case "2":
                    createChart(tuesdayChart, String.valueOf(days.get(days.keySet().toArray()[i])));
                    break;
                case "3":
                    createChart(wednesdayChart, String.valueOf(days.get(days.keySet().toArray()[i])));
                    break;
                case "4":
                    createChart(thursdayChart, String.valueOf(days.get(days.keySet().toArray()[i])));
                    break;
                case "5":
                    createChart(fridayChart, String.valueOf(days.get(days.keySet().toArray()[i])));
                    break;
                case "6":
                    createChart(saturdayChart, String.valueOf(days.get(days.keySet().toArray()[i])));
                    break;
                case "7":
                    createChart(sundayChart, String.valueOf(days.get(days.keySet().toArray()[i])));
                    break;
            }
        }
    }

    private HashMap setDayOfWeeks() {
        HashMap result = new HashMap();
        for (int i = 0; i < 7; i++) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            Date date = null;
            try {
                date = format.parse(String.valueOf(readyDates.keySet().toArray()[i]));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Calendar calendar = Calendar.getInstance((TimeZone.getTimeZone("GMT")));
            calendar.setTime(date);
            result.put(calendar.get(Calendar.DAY_OF_WEEK), readyDates.get(readyDates.keySet().toArray()[i]));
        }
        return result;
    }

    private void createChart(PieChart chart, String value){
        int norm = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("norm", 0);
        float progress = Integer.valueOf(value);
        float empty = norm - progress;
        float k = norm / progress;
        float percent = 100 / k;
        chart.setCenterText(Math.round(percent) + "%");

        ArrayList<PieEntry> yValues = new ArrayList<>();
        if (percent >= 100) {
            yValues.add(new PieEntry(100));
            yValues.add(new PieEntry(0));
        } else {
            yValues.add(new PieEntry(progress));
            yValues.add(new PieEntry(empty));
        }
        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSelectionShift(5f);
        dataSet.setColors(
                getResources().getColor(R.color.colorCircle), getResources().getColor(R.color.colorTransparentWhite));
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        chart.setData(data);
    }


    private void setWeekChart(View rootView) {
        PieChart weekChart = populatePieChart(rootView, R.id.weekChart, 0);

        int norm = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("norm", 0);
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        String strDate = dateFormat.format(date);
        float progress = 0;
        if (!dbHelper.dbIsEmpty()) {
            try {
                progress = Math.round(Float.valueOf(dbHelper.getAverage(strDate, getContext(), 7)));
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
        }
        tvWeekAverage.setText(String.valueOf((int) progress));
        float empty = norm - progress;
        float k = norm / progress;
        float percent = 100 / k;
        weekChart.setCenterText(Math.round(percent) + "%");

        ArrayList<PieEntry> yValues = new ArrayList<>();
        if (percent >= 100) {
            yValues.add(new PieEntry(100));
            yValues.add(new PieEntry(0));
        } else {
            yValues.add(new PieEntry(progress));
            yValues.add(new PieEntry(empty));
        }
        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSelectionShift(5f);
        dataSet.setColors(
                getResources().getColor(R.color.colorCircle), getResources().getColor(R.color.colorWhiteSuperTrans));
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        weekChart.setData(data);
    }

    private void setMonthChart(View rootView) {
        PieChart weekChart = populatePieChart(rootView, R.id.monthChart, 0);

        int norm = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("norm", 0);
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
        String strDate = dateFormat.format(date);
        float progress = 0;
        if (!dbHelper.dbIsEmpty()) {
            progress = Math.round(Float.valueOf(dbHelper.getAverage(strDate, getContext(), 30)));
        }
        tvMonthAverage.setText(String.valueOf((int) progress));
        float empty = norm - progress;
        float k = norm / progress;
        float percent = 100 / k;
        weekChart.setCenterText(Math.round(percent) + "%");

        ArrayList<PieEntry> yValues = new ArrayList<>();
        if (percent >= 100) {
            yValues.add(new PieEntry(100));
            yValues.add(new PieEntry(0));
        } else {
            yValues.add(new PieEntry(progress));
            yValues.add(new PieEntry(empty));
        }
        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSelectionShift(5f);
        dataSet.setColors(
                getResources().getColor(R.color.colorCircle), getResources().getColor(R.color.colorWhiteSuperTrans));
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);
        weekChart.setData(data);
    }

    public PieChart populatePieChart(View rootView, int id, int size) {
        PieChart chart = rootView.findViewById(id);
        chart.setRotationEnabled(false);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(-10, size, -10, -size);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleRadius(0);
        chart.setHoleRadius(46f);
        chart.setDrawCenterText(true);
        chart.setCenterTextColor(getResources().getColor(R.color.colorTextDark));
        if (size == 0) {
            chart.setCenterTextSize(8);
        } else {
            chart.setCenterTextSize(6);
        }
        chart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        chart.setRotationAngle(-90);
        chart.animateY(1000, Easing.EaseInOutQuad);
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setForm(Legend.LegendForm.EMPTY);
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(19f);
        return chart;
    }

    private void setMonthsChart(BarChart weekChart) {
        final ArrayList<BarEntry> yValues = new ArrayList<>();
        List<Integer> monthsValues = dbHelper.getMonths(getContext());
        Calendar cal = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("LLLL",
                ConfigurationCompat.getLocales(getContext().getResources().getConfiguration()).get(0));
        for (int i = 0; i < 12; i++) {
            cal.set(Calendar.MONTH, i);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date end = cal.getTime();
            String monthString = dateFormat.format(end).toLowerCase();
            yValues.add(new BarEntry(i, monthsValues.get(i), monthString));
        }


        BarDataSet dataSet = new BarDataSet(yValues, "");
        dataSet.setColors(
                getResources().getColor(R.color.colorBarChart1), getResources().getColor(R.color.colorBarChart2));
        BarData data = new BarData(dataSet);
        data.setDrawValues(false);
        data.setBarWidth(1f);
        weekChart.setData(data);
        weekChart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) { return yValues.get((int) value).getData().toString(); }
        });
        weekChart.setBackgroundColor(getResources().getColor(R.color.colorBarChartBack));
        weekChart.setRenderer(new CustomRenderer(weekChart, weekChart.getAnimator(), weekChart.getViewPortHandler()));
        weekChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
        weekChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int month = ((int) e.getX());
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_YEAR, 1);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM",
                        ConfigurationCompat.getLocales(getContext().getResources().getConfiguration()).get(0));
                Date end = cal.getTime();
                String date = dateFormat.format(end);
                setMonthDaysChart(daysChart, dbHelper.getMonthsDays(date));
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    private void setMonthDaysChart(BarChart weekChart, HashMap<String, Integer> daysData) {
        weekChart.setVisibility(View.VISIBLE);
        final ArrayList<BarEntry> yValues = new ArrayList<>();

        for (int i = 0; i < 31; i++) {
            if (i < 10) {
                if(daysData.get("0" + i) != null){
                    yValues.add(new BarEntry(i, daysData.get("0" + i), i));
                } else {
                    yValues.add(new BarEntry(i, 0, i));
                }
            } else {
                if(daysData.get(""+i) != null){
                    yValues.add(new BarEntry(i, daysData.get(""+i), i));
                } else {
                    yValues.add(new BarEntry(i, 0, i));
                }
            }
        }


        BarDataSet dataSet = new BarDataSet(yValues, "");
        dataSet.setColors(
                getResources().getColor(R.color.colorBarChart1), getResources().getColor(R.color.colorBarChart2));
        BarData data = new BarData(dataSet);
        data.setDrawValues(false);
        weekChart.setData(data);
        weekChart.setBackgroundColor(getResources().getColor(R.color.colorBarChartBack));
        weekChart.setRenderer(new CustomRenderer(weekChart, weekChart.getAnimator(), weekChart.getViewPortHandler()));
    }

    public BarChart populateBarChart(View rootView, int id, int labels) {
        BarChart chart = rootView.findViewById(id);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setScaleEnabled(false);
        chart.setDrawBarShadow(false);
        chart.setDrawBorders(false);
        chart.setDrawGridBackground(false);
        chart.setDrawMarkers(false);
        chart.setDrawValueAboveBar(false);
        chart.getDescription().setEnabled(false);
        chart.getAxisRight().setDrawGridLines(false);
        chart.getAxisRight().setDrawAxisLine(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setDrawAxisLine(true);
        chart.getAxisLeft().setAxisLineColor(getResources().getColor(R.color.colorTextDark));
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setAxisLineColor(getResources().getColor(R.color.colorTextDark));
        chart.getXAxis().setLabelRotationAngle(-90);
        chart.getXAxis().setLabelCount(labels);
        chart.getXAxis().setTextColor(getResources().getColor(R.color.colorTextDark));
        if(labels == 31) {
            chart.getXAxis().setTextSize(10);
        } else {
            chart.getXAxis().setTextSize(19);
        }
        chart.animateY(1000, Easing.EaseInOutQuad);
        Legend l = chart.getLegend();
        l.setForm(Legend.LegendForm.EMPTY);
        return chart;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.month:
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM",
                        ConfigurationCompat.getLocales(getResources().getConfiguration()).get(0));
                String strDate = dateFormat.format(date);
                setMonthDaysChart(currentMonthsDaysChart, dbHelper.getMonthsDays(strDate));
                break;
            case R.id.year:
                daysChart.setVisibility(View.INVISIBLE);
                currentMonthsDaysChart.setVisibility(View.INVISIBLE);
                break;
        }
    }
}
