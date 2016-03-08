package com.bitbucket.iddqdteam.smartassstork;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.bitbucket.iddqdteam.smartassstork.game.PlayerData;
import com.bitbucket.iddqdteam.smartassstork.screens.GameScreen;

public class SmartassStorkGame extends Game {
	SpriteBatch batch;
	Texture img;
	
	@Override
	public void create () {
		setScreen(new GameScreen(new PlayerData(), new TmxMapLoader().load("map1.tmx")));
		batch = new SpriteBatch();
	}

	@Override
	public void render () {
		super.render();
	}
}
