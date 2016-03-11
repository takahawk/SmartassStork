package org.bitbucket.iddqdteam.smartassstork.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import org.bitbucket.iddqdteam.smartassstork.SmartassStorkGame;
import org.bitbucket.iddqdteam.smartassstork.game.PlayerData;
import org.bitbucket.iddqdteam.smartassstork.util.ResourceManager;

/**
 * Created by takahawk on 10.03.16.
 */
public class MenuScreen implements Screen {

    private SmartassStorkGame game;
    private ResourceManager resourceManager;
    private Skin skin;
    private Stage stage;
    private Texture hero1Texture, hero2Texture;
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Image hero1Image, hero2Image;
    private Label easyModeLabel, hardModeLabel;
    private boolean hero1Picked = true;
    private boolean hardMode = true;

    private void initResources() {
        skin = resourceManager.get("uiskin.json", Skin.class);
        hero1Texture = resourceManager.get("hero2.1.png", Texture.class);
        hero2Texture = resourceManager.get("hero2(2).png", Texture.class);
    }

    public MenuScreen(final SmartassStorkGame game, final ResourceManager resourceManager) {
        this.game = game;
        this.resourceManager = resourceManager;
        stage = new Stage();
        initResources();

        Table table = new Table();
        table.setFillParent(true);
        table.top();
        table.add().expandX();
        table.add(new Label("PICK HERO: ", skin)).align(Align.center).colspan(2);
        table.add().expandX();
        table.row();

        hero1Image = new Image(hero1Texture);
        hero1Image.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                hero1Picked = true;
            }
        });
        hero2Image = new Image(hero2Texture);
        hero2Image.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                hero1Picked = false;
            }
        });
        table.add();
        table.add(hero1Image).padRight(100f);
        table.add(hero2Image);
        table.add(); table.row();


        easyModeLabel = new Label("EASY MODE", skin);
        hardModeLabel = new Label("HARD MODE", skin);
        easyModeLabel.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                hardMode = false;
            }
        });
        hardModeLabel.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                hardMode = true;
            }
        });

        table.add();
        table.add(new Label("PICK MODE: ", skin)).colspan(2);
        table.add(); table.row();
        table.add();
        table.add(easyModeLabel).align(Align.left);
        table.add(hardModeLabel).align(Align.left);
        table.add(); table.row();

        TextButton playButton = new TextButton("PLAY!", skin);
        playButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                MenuScreen.this.game.setScreen(new GameScreen(
                        new PlayerData(),
                        hero1Picked,
                        hardMode,
                        new TmxMapLoader().load("map1.tmx"),
                        resourceManager,
                        game
                        ));
            }
        });
        playButton.getLabel().setFontScale(3, 3);
        table.add(); table.add(playButton).colspan(2).padTop(50f).padBottom(50f); table.add(); table.row();
        TextButton scoreButton = new TextButton("SCORES", skin);
        scoreButton.getLabel().setFontScale(2, 2);
        table.add(); table.add(scoreButton).colspan(2); table.add();
        stage.addActor(table);
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.CYAN);
        if (hero1Picked) {
            shapeRenderer.rect(hero1Image.getX(), hero1Image.getY(), hero1Image.getWidth(), hero1Image.getHeight());
        } else {
            shapeRenderer.rect(hero2Image.getX(), hero2Image.getY(), hero2Image.getWidth(), hero2Image.getHeight());
        }
        if (hardMode) {
            shapeRenderer.rect(
                    hardModeLabel.getX(),
                    hardModeLabel.getY(),
                    hardModeLabel.getWidth(),
                    hardModeLabel.getHeight()
            );
        } else {
            shapeRenderer.rect(
                    easyModeLabel.getX(),
                    easyModeLabel.getY(),
                    easyModeLabel.getWidth(),
                    easyModeLabel.getHeight()
            );
        }
        shapeRenderer.end();
        stage.act(delta);
        stage.draw();

    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {

    }
}
