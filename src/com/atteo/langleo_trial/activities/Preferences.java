package com.atteo.langleo_trial.activities;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;

import com.atteo.langleo_trial.R;
import com.bomot113.langleo.DictSearch.FTSData;

public class Preferences extends PreferenceActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.main_preferences);
		Preference cachePref = (Preference) findPreference("refresh_cache");
		cachePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference paramPreference) {
				FTSData.updateFTSData();
				return false;
			}
		});
	}
}
