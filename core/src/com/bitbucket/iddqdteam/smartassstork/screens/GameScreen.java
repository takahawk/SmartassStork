package com.bitbucket.iddqdteam.smartassstork.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bitbucket.iddqdteam.smartassstork.game.PlayerActor;
import com.bitbucket.iddqdteam.smartassstork.game.PlayerData;
import com.bitbucket.iddqdteam.smartassstork.util.TiledMapParser;

import java.util.Iterator;

/**
 * Created by takahawk on 08.03.16.
 */
public class GameScreen implements Screen {
    private static final float PIXEL_PER_TILES = 64;
    private static final float GRAVITY = -100f;
    private OrthographicCamera camera = initCamera();
    private Viewport port = new FillViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
    private World world = new World(new Vector2(0, GRAVITY), true);
    private TiledMapRenderer mapRenderer;
    private TiledMapParser mapParser = new TiledMapParser();
    private Texture heroTexture = new Texture("sprite.png");
    private PlayerActor playerActor;
    private Stage stage;

    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();


    public GameScreen(PlayerData playerData, TiledMap map) {
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        stage = new Stage(port);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
                (Float) map.getLayers().get("Player").getObjects().get(0).getProperties().get("x"),
                (Float) map.getLayers().get("Player").getObjects().get(0).getProperties().get("y")
        );
        Body playerBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(heroTexture.getWidth() / 2, heroTexture.getHeight() / 2);
        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 0.0f;
        fixture.restitution = 0f;
        playerBody.createFixture(fixture);
        shape.dispose();
        playerActor = new PlayerActor(new TextureRegion(heroTexture), playerBody);
        stage.addActor(playerActor);
        addGround(map);
    }

    public void addGround(TiledMap map) {
        for (MapObject obj : map.getLayers().get("Ground").getObjects()) {

            if (obj instanceof PolygonMapObject) {
                Shape shape = mapParser.getPolygon((PolygonMapObject) obj, PIXEL_PER_TILES);
                BodyDef bd = new BodyDef();
                bd.type = BodyDef.BodyType.StaticBody;
                Body body = world.createBody(bd);
                body.createFixture(shape, 1);
                shape.dispose();
            }


        }
    }

    public OrthographicCamera initCamera() {
        camera = new OrthographicCamera();
        camera.setToOrtho(
                false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
                );
        camera.update();
        return camera;
    }

    public void handleInput() {
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerActor.moveRight();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            playerActor.moveLeft();
        }
        if (Gdx.input.isButtonPressed(Input.Keys.UP)) {
            playerActor.jump();
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        handleInput();
        world.step(delta, 6, 2);
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.position.set(playerActor.getX(), playerActor.getY(), 0);
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();
        stage.act(delta);
        stage.draw();
        debugRenderer.render(world, camera.combined);
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
