package org.bitbucket.iddqdteam.smartassstork;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.bitbucket.iddqdteam.smartassstork.screens.MenuScreen;
import org.bitbucket.iddqdteam.smartassstork.util.ResourceManager;

public class SmartassStorkGame extends Game {
	private ResourceManager resourceManager = new ResourceManager();
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		setScreen(new MenuScreen(this, resourceManager));
		// setScreen(new GameScreen(new TmxMapLoader().load("map1.tmx"), resourceManager));
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		super.render();
	}
}
