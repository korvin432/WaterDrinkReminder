package com.aqua.drinkreminder;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.os.ConfigurationCompat;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String NAME = "drinksDB";
    public static final String TAG = "qwwe";
    public static final int VERSION = 1;

    public static final String TABLE_DRINKS = "drinks";
    public static final String TABLE_DAYS = "days";

    public static class COLUMN {
        public static final String ID = "_id";
        public static final String TIME = "time";
        public static final String DATE = "date";
        public static final String SUM = "sum";
        public static final String DRINKS_COUNT = "drinksCount";
        public static final String VOLUME = "volume";
        public static final String LIST_POSITION = "listPosition";
    }

    public DBHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_SCRIPT);
        sqLiteDatabase.execSQL(CREATE_DAYS_SCRIPT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public List<Integer> getMonths(Context context) {
        SQLiteDatabase db = this.getWritableDatabase();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM",
                ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0));
        List<Integer> monthsSum = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            cal.set(Calendar.MONTH, i);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            Date end = cal.getTime();
            String endDate = dateFormat.format(end);
            String query = "SELECT sum(" + COLUMN.SUM + ") from " + TABLE_DAYS + " where " + COLUMN.DATE + "" +
                    " LIKE '%" + endDate + "%'";
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                monthsSum.add(cursor.getInt(cursor.getColumnIndex("sum(sum)")));
            }
        }
        return monthsSum;
    }

    public HashMap<String, Integer> getMonthsDays(String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        HashMap<String, Integer> days = new HashMap<>();

        String query = "SELECT " + COLUMN.SUM + ", " + COLUMN.DATE + " from " + TABLE_DAYS + " where " + COLUMN.DATE + "" +
                " LIKE '%" + date + "%'";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            days.put(cursor.getString(cursor.getColumnIndex("date"))
                            .substring(Math.max(cursor.getString(cursor.getColumnIndex("date")).length() - 2, 0)),
                    cursor.getInt(cursor.getColumnIndex("sum")));
        }
        return days;
    }

    public String getAverage(String curDate, Context context, int days) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result;
        int sum = 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_YEAR, -days);
        Date weekAgo = cal.getTime();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",
                ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0));
        String strWeekAgo = dateFormat.format(weekAgo);

        String query = "SELECT sum(" + COLUMN.SUM + ") from " + TABLE_DAYS + " where " + COLUMN.DATE + "" +
                " BETWEEN '" + strWeekAgo + "' AND '" + curDate + "'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            sum = cursor.getInt(cursor.getColumnIndex("sum(sum)"));
        }
        result = sum / days;
        return String.valueOf(result);
    }

    public HashMap getWeekValues(List<String> dates) {
        SQLiteDatabase db = this.getWritableDatabase();
        HashMap values = getWeekDates();

        for (int i = 0; i < 7; i++) {
            String query = "SELECT " + COLUMN.SUM + " from " + TABLE_DAYS + " WHERE " + COLUMN.DATE + " = " +
                    "'" + dates.get(i) + "'";
            Cursor cursor = db.rawQuery(query, null);
            if (cursor.moveToFirst()) {
                int result = cursor.getInt(cursor.getColumnIndex(COLUMN.SUM));
                values.put(dates.get(i), result);
            }
        }
        return values;
    }

    private HashMap getWeekDates() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        HashMap dates = new HashMap();

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            dates.put(df.format(c.getTime()), 0);
            c.add(Calendar.DATE, 1);
        }
        return dates;
    }


    public int getAverageDrinksCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;

        String query = "SELECT avg(" + COLUMN.DRINKS_COUNT + ") from " + TABLE_DAYS;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex("avg(" + COLUMN.DRINKS_COUNT + ")"));
        }
        return result;
    }

    public boolean dbIsEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        String countQuery;
        countQuery = "SELECT " + COLUMN.ID + " FROM " + TABLE_DRINKS;
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        return count == 0;
    }

    public String getVolume(String curDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        String result = "";

        String query = "SELECT sum(" + COLUMN.VOLUME + ") from " + TABLE_DRINKS + " where " + COLUMN.TIME + "" +
                " LIKE '%" + curDate + "%'";
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            result = cursor.getString(cursor.getColumnIndex("sum(volume)"));
        }
        return result;
    }

    public void addDrink(String time, String volume, int listPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "INSERT INTO " + TABLE_DRINKS + " (" + COLUMN.TIME + ", " + COLUMN.VOLUME + ", " + COLUMN.LIST_POSITION + ") " +
                " VALUES ('" + time + "', '" + volume + "', " + listPosition + ") ";
        db.execSQL(query);
    }

    public void addDay(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String countQuery;
        countQuery = "SELECT " + COLUMN.ID + " FROM " + TABLE_DAYS + " WHERE " + COLUMN.DATE + " = '" + date + "'";
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        if (count == 0) {
            String query = "INSERT INTO " + TABLE_DAYS + " (" + COLUMN.DATE + ", " + COLUMN.SUM + ", " + COLUMN.DRINKS_COUNT + ") " +
                    " VALUES ('" + date + "', 0, 0) ";
            db.execSQL(query);
        }
    }

    public void updateDrink(int listPosition, int volume, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_DRINKS + " SET " + COLUMN.VOLUME + " = '" +
                volume + "' WHERE " + COLUMN.LIST_POSITION + " = " + listPosition + " AND " +
                COLUMN.TIME + " LIKE '%" + date + "%'";
        db.execSQL(query);
    }


    public void deleteDrink(int listPosition, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "DELETE FROM " + TABLE_DRINKS + " WHERE " + COLUMN.LIST_POSITION + " = " + listPosition + " AND " +
                COLUMN.TIME + " LIKE '%" + date + "%'";
        db.execSQL(query);
    }

    public void updateDay(String date, int volume) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_DAYS + " SET " + COLUMN.SUM + " = '" +
                volume + "' WHERE " + COLUMN.DATE + " LIKE '%" + date + "%'";
        db.execSQL(query);
    }

    public void updateDrinksCount(String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_DAYS + " SET " + COLUMN.DRINKS_COUNT + " = " +
                COLUMN.DRINKS_COUNT + " + 1 WHERE " + COLUMN.DATE + " LIKE '%" + date + "%'";
        db.execSQL(query);
    }

    public void changeData(boolean toMetrics){
        if (!toMetrics) {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + TABLE_DAYS + " SET " + COLUMN.SUM + " = " +
                    COLUMN.SUM + " / 29.5";
            String query2 = "UPDATE " + TABLE_DRINKS + " SET " + COLUMN.VOLUME + " = " +
                    "(SELECT CAST(" + COLUMN.VOLUME + " as int ) / 29.5)";

            db.execSQL(query);
            db.execSQL(query2);
        } else {
            SQLiteDatabase db = this.getWritableDatabase();
            String query = "UPDATE " + TABLE_DAYS + " SET " + COLUMN.SUM + " = " +
                    COLUMN.SUM + " * 29.5";
            String query2 = "UPDATE " + TABLE_DRINKS + " SET " + COLUMN.VOLUME + " = " +
                    "(SELECT CAST(" + COLUMN.VOLUME + " as int ) * 29.5)";

            db.execSQL(query);
            db.execSQL(query2);
        }
    }

    public static final String CREATE_SCRIPT =
            String.format("create table %s ("
                            + "%s integer primary key autoincrement, "
                            + "%s text, "
                            + "%s integer, "
                            + "%s text" + ");",
                    TABLE_DRINKS, COLUMN.ID, COLUMN.TIME, COLUMN.LIST_POSITION,
                    COLUMN.VOLUME);

    public static final String CREATE_DAYS_SCRIPT =
            String.format("create table %s ("
                            + "%s integer primary key autoincrement, "
                            + "%s text, "
                            + "%s integer, "
                            + "%s integer" + ");" +
                            "UNIQUE(" + COLUMN.DATE + ")",
                    TABLE_DAYS, COLUMN.ID, COLUMN.DATE, COLUMN.DRINKS_COUNT, COLUMN.SUM);

}