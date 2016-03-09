package com.bitbucket.iddqdteam.smartassstork;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.bitbucket.iddqdteam.smartassstork.game.PlayerData;
import com.bitbucket.iddqdteam.smartassstork.screens.GameScreen;
import com.bitbucket.iddqdteam.smartassstork.util.ResourceManager;

public class SmartassStorkGame extends Game {
	private ResourceManager resourceManager = new ResourceManager();
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		setScreen(new GameScreen(new PlayerData(), new TmxMapLoader().load("map1.tmx"), resourceManager));
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		super.render();
	}
}
