package com.aqua.drinkreminder.di;

import com.aqua.drinkreminder.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityModule {
    @ContributesAndroidInjector
    abstract MainActivity bindMainActivity();
}
