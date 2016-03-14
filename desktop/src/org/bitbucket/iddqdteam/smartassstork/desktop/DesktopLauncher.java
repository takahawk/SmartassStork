package org.bitbucket.iddqdteam.smartassstork.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import org.bitbucket.iddqdteam.smartassstork.SmartassStorkGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = SmartassStorkGame.SCREEN_WIDTH;
		config.height = SmartassStorkGame.SCREEN_HEIGHT;
		new LwjglApplication(new SmartassStorkGame(), config);
	}
}
