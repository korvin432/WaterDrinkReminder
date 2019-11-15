package com.aqua.drinkreminder.di;

import android.database.sqlite.SQLiteDatabase;
import com.aqua.drinkreminder.DBHelper;
import com.aqua.drinkreminder.MainActivity;
import dagger.Module;
import dagger.Provides;

@Module
class MainModule {

    @MainScope
    @Provides
    SQLiteDatabase provideSQLiteDatabase(DBHelper dbHelper){
        return dbHelper.getWritableDatabase();
    }

    @MainScope
    @Provides
    DBHelper provideDBHelper(MainActivity activity){
        return new DBHelper(activity);
    }
}
