package com.aqua.drinkreminder;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import com.aqua.drinkreminder.di.AppComponent;
import com.aqua.drinkreminder.di.DaggerAppComponent;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;
import java.util.Locale;
import javax.inject.Inject;
import dagger.android.AndroidInjector;
import dagger.android.DaggerApplication;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasFragmentInjector;

public class App extends MultiDexApplication implements HasActivityInjector, HasFragmentInjector {

    @Inject DispatchingAndroidInjector<Activity> activityInjector;
    @Inject DispatchingAndroidInjector<Fragment> fragmentInjector;
    private AppComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        YandexMetricaConfig config = YandexMetricaConfig.
                newConfigBuilder("9b0e0c8f-eeec-4181-b4a4-30793719bd9e").build();
        YandexMetrica.activate(getApplicationContext(), config);
        YandexMetrica.enableActivityAutoTracking(this);
        mComponent = DaggerAppComponent.builder().application(this).build();
        mComponent.inject(this);
    }

    // Dependency Injection
    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return activityInjector;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(updateBaseContextLocale(base));
    }

    private Context updateBaseContextLocale(Context context) {
        String language = PreferenceManager.getDefaultSharedPreferences(context)
                .getString("currentLocale", "ru");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResourcesLocale(context, locale);
        }

        return updateResourcesLocaleLegacy(context, locale);
    }

    @TargetApi(Build.VERSION_CODES.N)
    private Context updateResourcesLocale(Context context, Locale locale) {
        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private Context updateResourcesLocaleLegacy(Context context, Locale locale) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }

    @Override
    public AndroidInjector<android.app.Fragment> fragmentInjector() {
        return fragmentInjector;
    }
}
