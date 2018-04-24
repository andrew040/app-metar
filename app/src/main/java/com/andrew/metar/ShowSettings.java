package com.andrew.metar;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class ShowSettings extends Activity {

    Button butSaveButton;
	EditText editCrosswind;
	int iCrossWindLimit;


	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.showsettings);
		editCrosswind = (EditText) findViewById(R.id.editCrosswind);
        butSaveButton = (Button) findViewById(R.id.SaveButton);
		View root = editCrosswind.getRootView();
        root.setBackgroundColor(0xFF222222);

		SharedPreferences prefs = getSharedPreferences("com.andrew.metar.settings", MODE_PRIVATE);
		iCrossWindLimit = prefs.getInt("crosswind_limit", 0);
		editCrosswind.setText(String.valueOf(iCrossWindLimit));
	}

    public void SaveSettings(View view){
        if (view == butSaveButton){
            SharedPreferences.Editor editor = getSharedPreferences("com.andrew.metar.settings", MODE_PRIVATE).edit();
            editor.putInt("crosswind_limit", Integer.valueOf(editCrosswind.getText().toString()));
            editor.commit();
            finish();
        }
    }
}
