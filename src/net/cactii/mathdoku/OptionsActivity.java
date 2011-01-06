package net.cactii.mathdoku;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class OptionsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.layout.optionsview); 
  }
  
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

  }
}
