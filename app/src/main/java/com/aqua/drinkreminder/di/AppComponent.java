package com.aqua.drinkreminder.di;

import android.app.Application;

import com.aqua.drinkreminder.App;
import com.aqua.drinkreminder.MainActivity;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

@Singleton
@Component
public interface AppComponent {

    void inject(MainActivity mainActivity);
}
