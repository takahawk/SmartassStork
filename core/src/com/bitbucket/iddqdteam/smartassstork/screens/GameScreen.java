package com.bitbucket.iddqdteam.smartassstork.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bitbucket.iddqdteam.smartassstork.game.PlayerActor;
import com.bitbucket.iddqdteam.smartassstork.game.PlayerData;
import com.bitbucket.iddqdteam.smartassstork.game.Stork;
import com.bitbucket.iddqdteam.smartassstork.util.ResourceManager;
import com.bitbucket.iddqdteam.smartassstork.util.TiledMapParser;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by takahawk on 08.03.16.
 */
public class GameScreen implements Screen {
    public static final float PIXELS_TO_METERS = 100;
    private static final float GRAVITY = -5f;
    private ResourceManager resourceManager;
    private OrthographicCamera camera = initCamera();
    private OrthographicCamera camera2 = new OrthographicCamera();
    private Viewport port = new FillViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
    private World world = new World(new Vector2(0, GRAVITY), true);
    private TiledMapRenderer mapRenderer;
    private TiledMapParser mapParser = new TiledMapParser();
    private Texture heroTexture;
    private Texture moonTexture;
    private Texture storkTexture;
    private Animation heroMoveAnimation;
    private PlayerActor playerActor;
    private Stage stage;
    private int mapWidth, mapHeight;
    private int tileWidth, tileHeight;
    private SpriteBatch batch;

    private boolean gameIsOver = false;

    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();

    public void initResources() {
        heroTexture = resourceManager.get("hero1.png", Texture.class);
        heroMoveAnimation = new Animation(
                0.1f,
                new Array<TextureRegion>(new TextureRegion[] {
                        new TextureRegion(resourceManager.get("hero1.png", Texture.class)),
                        new TextureRegion(resourceManager.get("hero2.1.png", Texture.class)),
                        new TextureRegion(resourceManager.get("hero2.png", Texture.class))
                })
                );
        moonTexture = resourceManager.get("sky_tileset1.png", Texture.class);
        storkTexture = resourceManager.get("stork-flying.png", Texture.class);
    }

    public GameScreen(PlayerData playerData, TiledMap map, ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        this.batch = new SpriteBatch();
        TiledMap foregroundMap = new TmxMapLoader().load("light.tmx");
        initResources();
        tileWidth = (Integer) map.getProperties().get("tilewidth");
        tileHeight = (Integer) map.getProperties().get("tileheight");
        mapWidth = (Integer) map.getProperties().get("width") * tileWidth;
        mapHeight = (Integer) map.getProperties().get("height") * tileHeight;
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        stage = new Stage(port);
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
                (Float) map.getLayers().get("Player").getObjects().get(0).getProperties().get("x") / PIXELS_TO_METERS,
                (Float) map.getLayers().get("Player").getObjects().get(0).getProperties().get("y") / PIXELS_TO_METERS
        );
        final Body playerBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(heroTexture.getWidth() / 2 / PIXELS_TO_METERS, heroTexture.getHeight() / 2 / PIXELS_TO_METERS);
        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 0.0f;
        fixture.restitution = 0f;
        playerBody.createFixture(fixture);
        playerBody.setUserData("hero");
        shape.dispose();
        playerActor = new PlayerActor(new TextureRegion(heroTexture), playerBody);
        playerActor.setMoveAnimation(heroMoveAnimation);
        stage.addActor(playerActor);
        world.setContactListener(new ContactListener() {
            private boolean with(Contact contact, String that) {
                return  that.equals(contact.getFixtureA().getBody().getUserData().toString()) ||
                        that.equals(contact.getFixtureB().getBody().getUserData().toString());
            }

            @Override
            public void beginContact(Contact contact) {
                if (with(contact, "hero"))
                    if (with(contact, "ground")) {
                        if (MathUtils.isEqual(MathUtils.floor(contact.getWorldManifold().getNormal().y), -1))
                            playerActor.setOnTheGround(true);
                    }
                    else if (with(contact, "stork")) {
                        gameIsOver = true;
                    }
            }

            @Override
            public void endContact(Contact contact) {
                if (with(contact, "ground") && with(contact, "hero"))
                    playerActor.setOnTheGround(false);
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {

            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {

            }
        });
        addGround(map);
        addStorks(map);
    }

    public void addGround(TiledMap map) {
        for (MapObject obj : map.getLayers().get("Ground").getObjects()) {

            if (obj instanceof PolygonMapObject) {
                Shape shape = mapParser.getPolygon((PolygonMapObject) obj, 1 / PIXELS_TO_METERS);
                BodyDef bd = new BodyDef();
                bd.type = BodyDef.BodyType.StaticBody;
                Body body = world.createBody(bd);
                body.createFixture(shape, 1);
                body.setUserData("ground");
                shape.dispose();
            }


        }
    }

    public void addStorks(TiledMap map) {
        for (MapObject obj : map.getLayers().get("Enemies").getObjects()) {
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.gravityScale = 0;
            bodyDef.position.set(
                    (Float) obj.getProperties().get("x") / PIXELS_TO_METERS,
                    (Float) obj.getProperties().get("y") / PIXELS_TO_METERS
            );
            final Body storkBody = world.createBody(bodyDef);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(storkTexture.getWidth() / 2 / PIXELS_TO_METERS, storkTexture.getHeight() / 2 / PIXELS_TO_METERS);
            FixtureDef fixture = new FixtureDef();
            fixture.shape = shape;
            fixture.density = 0.0f;
            fixture.restitution = 0f;
            storkBody.createFixture(fixture);
            shape.dispose();
            storkBody.setUserData("stork");
            stage.addActor(new Stork(
                    new TextureRegion(storkTexture),
                    storkBody,
                    (Integer.parseInt((String) obj.getProperties().get("routeDistance"))) * tileWidth / PIXELS_TO_METERS
            ));
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
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerActor.moveLeft();
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerActor.jump();
        } else {
        }
    }

    public void centerCamera() {
        camera.position.set(playerActor.getX(), playerActor.getY(), 0);
        camera.update();
        camera.position.x = MathUtils.clamp(
                camera.position.x,
                camera.viewportWidth / 2,
                mapWidth - camera.viewportWidth / 2
                );
        camera.position.y = MathUtils.clamp(
                camera.position.y,
                camera.viewportHeight / 2,
                mapHeight - camera.viewportHeight / 2
        );
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        if (!gameIsOver) {
            handleInput();
            world.step(delta, 6, 2);
        }
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        mapRenderer.setView(camera);
        mapRenderer.render();
        centerCamera();
        stage.act(delta);
        stage.draw();

        Matrix4 debugMatrix = camera.combined.cpy().scale(PIXELS_TO_METERS, PIXELS_TO_METERS, 0);
        debugRenderer.render(world, debugMatrix);
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
