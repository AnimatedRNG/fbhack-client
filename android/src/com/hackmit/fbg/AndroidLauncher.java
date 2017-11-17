package com.hackmit.fbg;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.github.claywilkinson.arcore.gdx.BaseARCoreActivity;
import com.hackmit.fbg.GalaxyRenderer;

public class AndroidLauncher extends BaseARCoreActivity {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new GalaxyScene(), config);
	}
}
