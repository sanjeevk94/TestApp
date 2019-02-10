package com.scubearena.testapp;
import android.app.Application;

public class InitApplication extends Application{

    private boolean isNightModeEnabled = false;
    private static InitApplication singleton = null;


    public static InitApplication getInstance()
    {
        if(singleton == null)
        {
            singleton = new InitApplication();
        }
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
    public boolean isNightModeEnabled()
    {
        return isNightModeEnabled;
    }
    public void setIsNightModeEnabled(boolean isNightModeEnabled)
    {
        this.isNightModeEnabled = isNightModeEnabled;


    }
}
