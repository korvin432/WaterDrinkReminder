package com.aqua.drinkreminder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.os.ConfigurationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DrinksAdapter extends RecyclerView.Adapter<DrinksAdapter.DrinksViewHolder> {
    private Context mContext;
    private Cursor mCursor;
    private boolean is24format;
    private final ClickListener listener;

    public interface ClickListener {
        void onPositionClicked(int position, int firstVolume, String date, boolean isDeleting);
    }

    public DrinksAdapter(Context context, Cursor cursor, ClickListener listener) {
        this.listener = listener;
        mContext = context;
        mCursor = cursor;
        is24format = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("is24", true);
    }

    public class DrinksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView timeText;
        private TextView volumeText;
        private TextView idText;
        private TextView dateText;
        private ImageView itemImage;
        private ImageView imageSettings;
        private ImageView imageDelete;
        private WeakReference<ClickListener> listenerRef;

        public DrinksViewHolder(View itemView) {
            super(itemView);

            listenerRef = new WeakReference<>(listener);
            timeText = itemView.findViewById(R.id.drink_time);
            volumeText = itemView.findViewById(R.id.drink_volume);
            idText = itemView.findViewById(R.id.itemId);
            dateText = itemView.findViewById(R.id.itemDate);
            itemImage = itemView.findViewById(R.id.drink_image);
            imageSettings = itemView.findViewById(R.id.image_settings);
            imageDelete = itemView.findViewById(R.id.image_delete);
            imageSettings.setOnClickListener(this);
            imageDelete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.image_settings:
                listenerRef.get().onPositionClicked(Integer.valueOf(idText.getText().toString()),
                        Integer.valueOf(volumeText.getText().toString().substring(0, volumeText.getText().toString().length() - 3)),
                        dateText.getText().toString(), false);
                break;
                case R.id.image_delete:
                    listenerRef.get().onPositionClicked(Integer.valueOf(idText.getText().toString()),
                            Integer.valueOf(volumeText.getText().toString().substring(0, volumeText.getText().toString().length() - 3)),
                            dateText.getText().toString(), true);
                    break;

            }
        }
    }

    @Override
    public DrinksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.drinks_item, parent, false);
        return new DrinksViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DrinksViewHolder holder, int position) {
        if (!mCursor.moveToPosition(position)) {
            return;
        }

        String date = mCursor.getString(mCursor.getColumnIndex(DBHelper.COLUMN.TIME));

        Date dateT = null;
        try {
            if (is24format) {
                dateT = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                        ConfigurationCompat.getLocales(mContext.getResources().getConfiguration()).get(0)).parse(date);
            } else {
                dateT = new SimpleDateFormat("yyyy-MM-dd hh:mm",
                        ConfigurationCompat.getLocales(mContext.getResources().getConfiguration()).get(0)).parse(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String time;
        String dateString;
        if (is24format) {
            time = new SimpleDateFormat("HH:mm",
                    ConfigurationCompat.getLocales(mContext.getResources().getConfiguration()).get(0)).format(dateT); // 9:00
            dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                    ConfigurationCompat.getLocales(mContext.getResources().getConfiguration()).get(0)).format(dateT);
        } else {
            time = new SimpleDateFormat("hh:mm",
                    ConfigurationCompat.getLocales(mContext.getResources().getConfiguration()).get(0)).format(dateT); // 9:00
            dateString = new SimpleDateFormat("yyyy-MM-dd hh:mm",
                    ConfigurationCompat.getLocales(mContext.getResources().getConfiguration()).get(0)).format(dateT);
        }

        String volume = mCursor.getString(mCursor.getColumnIndex(DBHelper.COLUMN.VOLUME));
        if (volume.contains(".")){
            volume = volume.substring(0, volume.indexOf("."));
        }
        Drawable drawable ;

        switch (volume) {
            case "100":
                drawable = mContext.getResources().getDrawable(R.drawable.ic_100);
                break;
            case "200":
                drawable = mContext.getResources().getDrawable(R.drawable.ic_200);
                break;
            case "300":
                drawable = mContext.getResources().getDrawable(R.drawable.ic_300);
                break;
            case "400":
                drawable = mContext.getResources().getDrawable(R.drawable.ic_400);
                break;
            case "500":
                drawable = mContext.getResources().getDrawable(R.drawable.ic_500);
                break;
            default:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_300);
                break;

        }

        boolean isMetrics = PreferenceManager.getDefaultSharedPreferences(mContext).
                getBoolean("isMetrics", true);

        holder.timeText.setText(time);
        holder.dateText.setText(dateString);
        holder.itemImage.setImageDrawable(drawable);
        holder.idText.setText(String.valueOf(mCursor.getInt(mCursor.getColumnIndex(DBHelper.COLUMN.LIST_POSITION))));
        if (isMetrics) {
            holder.volumeText.setText(volume + " ml");
        } else {
            holder.volumeText.setText(volume + " fl");
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

}