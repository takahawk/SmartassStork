package com.bitbucket.iddqdteam.smartassstork;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
		setScreen(new GameScreen(new PlayerData(), new TmxMapLoader().load("level1.tmx")));
		batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
	}

	@Override
	public void render () {
		super.render();
	}
}
