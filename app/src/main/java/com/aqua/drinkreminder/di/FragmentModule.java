package com.aqua.drinkreminder.di;

import com.aqua.drinkreminder.FragmentMain;
import com.aqua.drinkreminder.FragmentSettings;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract FragmentMain bindFragmentMain();

    @ContributesAndroidInjector
    abstract FragmentSettings bindFragmentSettings();

}
