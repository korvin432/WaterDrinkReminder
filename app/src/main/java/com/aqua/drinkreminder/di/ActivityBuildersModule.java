package com.aqua.drinkreminder.di;

import com.aqua.drinkreminder.MainActivity;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ActivityBuildersModule {

    @MainScope
    @ContributesAndroidInjector(
            modules = {FragmentModule.class, MainModule.class}
    )
    abstract MainActivity contributeMainActivity();



}
