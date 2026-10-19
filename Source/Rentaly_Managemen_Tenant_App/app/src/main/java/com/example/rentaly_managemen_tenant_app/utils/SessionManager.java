package com.example.rentaly_managemen_tenant_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(
                Constants.SESSION_PREFS, Context.MODE_PRIVATE);
    }

    public void saveSession(String userId, String userName) {
        preferences.edit()
                .putString(Constants.KEY_USER_ID, userId)
                .putString(Constants.KEY_USER_NAME, userName)
                .apply();
    }

    public String getUserId() {
        return preferences.getString(Constants.KEY_USER_ID, null);
    }

    public String getUserName() {
        return preferences.getString(Constants.KEY_USER_NAME, null);
    }

    public void saveTenantId(String tenantId) {
        preferences.edit().putString(Constants.KEY_TENANT_ID, tenantId).apply();
    }

    public String getTenantId() {
        return preferences.getString(Constants.KEY_TENANT_ID, null);
    }

    public boolean isLoggedIn() {
        return getUserId() != null;
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
