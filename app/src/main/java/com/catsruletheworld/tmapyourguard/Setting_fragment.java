package com.catsruletheworld.tmapyourguard;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.MultiSelectListPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.widget.BaseAdapter;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.preference.SeekBarPreference;

import java.util.Set;

public class Setting_fragment extends PreferenceFragment {
    SharedPreferences preference;
    MultiSelectListPreference messageType;
    PreferenceScreen alarmScreen;
    PreferenceScreen vibrateScreen;
    SeekBarPreference alarmShake;
    SeekBarPreference alarmIntensity;

    private Vibrator vibrator;
    private static Context context;
    Activity mActivity;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);

        context = getActivity().getApplicationContext();

        messageType = (MultiSelectListPreference) findPreference("message_type");
        alarmScreen = (PreferenceScreen) findPreference("alarm_screen");
        vibrateScreen = (PreferenceScreen) findPreference("vibrate_screen");

        preference = PreferenceManager.getDefaultSharedPreferences(getActivity());

        if(preference.getBoolean("vibrate", true)) {
            vibrateScreen.setSummary("사용");
        }

        if(preference.getBoolean("alarm", true)) {
            alarmScreen.setSummary("사용");
        }

        preference.registerOnSharedPreferenceChangeListener(prefListener);
    }

    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("alarm")) {
                if(preference.getBoolean("alarm", false)) {
                    alarmScreen.setSummary("사용");
                    vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(2000); //2초간 진동
                    long[] pattern = {1000, 50, 1000, 50}; //1초 진동, 0.05초 대기, 1초 진동, 0.05초 대기
                    vibrator.vibrate(pattern, 0); //-1 = 진동 1번만 발생, 0 = 진동 무한으로 진동
                    vibrator.cancel(); //무한으로 진동할 때 멈추기
                }else {
                    alarmScreen.setSummary("사용 안 함");
                }
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
            }

            if(key.equals("vibrate")) {
                if(preference.getBoolean("vibrate", false)) {
                    vibrateScreen.setSummary("사용");
                }else {
                    vibrateScreen.setSummary("사용 안 함");
                }
                ((BaseAdapter)getPreferenceScreen().getRootAdapter()).notifyDataSetChanged();
            }

            String summary = "";
            String and = "";
            Set<String> values = messageType.getValues();

            for (String value : values) {
                // For each value retrieve index
                int index = messageType.findIndexOfValue(value);
                // Retrieve entry from index 이해안감 ㅎㅎ
                CharSequence mEntry = index >= 0
                        && messageType.getEntries() != null ? messageType
                        .getEntries()[index] : null;

                if (mEntry != null) {
                    summary = summary + and + mEntry;
                    and = ", ";
                }
            }
            messageType.setSummary(summary);
        }
    };
}