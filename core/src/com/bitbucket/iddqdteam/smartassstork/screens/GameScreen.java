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
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bitbucket.iddqdteam.smartassstork.SmartassStorkGame;
import com.bitbucket.iddqdteam.smartassstork.game.Killable;
import com.bitbucket.iddqdteam.smartassstork.game.PlayerActor;
import com.bitbucket.iddqdteam.smartassstork.game.PlayerData;
import com.bitbucket.iddqdteam.smartassstork.game.Stork;
import com.bitbucket.iddqdteam.smartassstork.util.ResourceManager;
import com.bitbucket.iddqdteam.smartassstork.util.TiledMapParser;

import java.util.*;

/**
 * Created by takahawk on 08.03.16.
 */
public class GameScreen implements Screen {
    public static final float PIXELS_TO_METERS = 100;
    private static final float GRAVITY = -5f;
    private boolean firstHero;
    private boolean hardMode;
    private PlayerData playerData;
    private ResourceManager resourceManager;
    private OrthographicCamera camera = initCamera();
    private OrthographicCamera camera2 = new OrthographicCamera();
    private Viewport port = new FillViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
    private World world = new World(new Vector2(0, GRAVITY), true);
    private TiledMapRenderer mapRenderer;
    private TiledMapParser mapParser = new TiledMapParser();
    private Texture heroTexture;
    private Texture heroMissileTexture;
    private Texture moonTexture;
    private Texture storkTexture;
    private Texture deathStorkTexture;
    private Animation heroMoveAnimation;
    private PlayerActor playerActor;
    private Stage stage;
    private Stage hud;
    private Skin hudSkin;
    private int mapWidth, mapHeight;
    private int tileWidth, tileHeight;
    private SpriteBatch batch;
    private Label scoreLabel, livesLabel;

    private Map<Body, Killable> entities = new HashMap<Body, Killable>();
    private Queue<Body> bodiesToBeRemoved = new ArrayDeque<Body>();

    private boolean gameIsOver = false;

    private Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();

    public void initResources() {
        if (firstHero) {
            heroTexture = resourceManager.get("hero1.png", Texture.class);
            heroMoveAnimation = new Animation(
                    0.1f,
                    new Array<TextureRegion>(new TextureRegion[]{
                            new TextureRegion(resourceManager.get("hero1.png", Texture.class)),
                            new TextureRegion(resourceManager.get("hero2.1.png", Texture.class)),
                            new TextureRegion(resourceManager.get("hero2.png", Texture.class))
                    })
            );
        } else {
            heroTexture = resourceManager.get("hero2(1).png", Texture.class);
            heroMoveAnimation = new Animation(
                    0.1f,
                    new Array<TextureRegion>(new TextureRegion[]{
                            new TextureRegion(resourceManager.get("hero2(1).png", Texture.class)),
                            new TextureRegion(resourceManager.get("hero2(2).png", Texture.class)),
                            new TextureRegion(resourceManager.get("hero2(3).png", Texture.class))
                    })
            );
        }
        moonTexture = resourceManager.get("sky_tileset1.png", Texture.class);
        storkTexture = resourceManager.get("stork-flying.png", Texture.class);
        heroMissileTexture = resourceManager.get("molotok.png", Texture.class);
        deathStorkTexture = resourceManager.get("stork.png", Texture.class);
        hudSkin = resourceManager.get("uiskin.json", Skin.class);
    }

    private void initHud() {
        hud = new Stage();
        Table table = new Table();
        table.setFillParent(true);
        table.top();
        if (!hardMode) {
            livesLabel = new Label("LIVES: " + playerData.getLives(), hudSkin);
            table.add(livesLabel).align(Align.left).expandX().padLeft(20f).padTop(20f);
        }
        else
            table.add().expandX();
        scoreLabel = new Label("STORKS KILLED: " + playerData.getScore(), hudSkin);
        table.add(scoreLabel).align(Align.right).padRight(20f).padTop(20f);
        hud.addActor(table);

    }

    public GameScreen(
            final PlayerData playerData,
            final boolean firstHero,
            final boolean hardMode,
            final TiledMap map,
            final ResourceManager resourceManager,
            final SmartassStorkGame game
        ) {
        this.playerData = playerData;
        this.resourceManager = resourceManager;
        this.firstHero = firstHero;
        this.hardMode = hardMode;
        this.batch = new SpriteBatch();
        TiledMap foregroundMap = new TmxMapLoader().load("light.tmx");
        initResources();
        initHud();
        tileWidth = (Integer) map.getProperties().get("tilewidth");
        tileHeight = (Integer) map.getProperties().get("tileheight");
        mapWidth = (Integer) map.getProperties().get("width") * tileWidth;
        mapHeight = (Integer) map.getProperties().get("height") * tileHeight;
        mapRenderer = new OrthogonalTiledMapRenderer(map);
        stage = new Stage(port);
        Vector2 position = new Vector2(
                (Float) map.getLayers().get("Player").getObjects().get(0).getProperties().get("x") / PIXELS_TO_METERS,
                (Float) map.getLayers().get("Player").getObjects().get(0).getProperties().get("y") / PIXELS_TO_METERS
        );


        playerActor = new PlayerActor(
                new TextureRegion(heroTexture),
                position,
                world,
                new TextureRegion(heroMissileTexture),
                entities
        );
        entities.put(playerActor.getBody(), playerActor);
        playerActor.setMoveAnimation(heroMoveAnimation);
        stage.addActor(playerActor);
        world.setContactListener(new ContactListener() {
            private boolean with(Contact contact, String that) {
                return  that.equals(contact.getFixtureA().getBody().getUserData().toString()) ||
                        that.equals(contact.getFixtureB().getBody().getUserData().toString());
            }

            private Body getBody(Contact contact, String data) {
                Body first = contact.getFixtureA().getBody();
                Body second = contact.getFixtureB().getBody();
                return data.equals(first.getUserData())
                            ? first
                            : data.equals(second.getUserData())
                                ? second
                                : null;
            }

            @Override
            public void beginContact(Contact contact) {
                if (with(contact, "hero"))
                    if (with(contact, "ground")) {
                        if (MathUtils.isEqual(MathUtils.floor(contact.getWorldManifold().getNormal().y), -1))
                            playerActor.setOnTheGround(true);
                    }
                    else if (with(contact, "stork") || with(contact, "bottom")) {
                        if (!hardMode && playerData.getLives() > 0) {
                            playerData.setLives(playerData.getLives() - 1);
                            game.setScreen(new GameScreen(
                                    playerData,
                                    firstHero,
                                    hardMode,
                                    map,
                                    resourceManager,
                                    game
                            ));
                        }
                        playerActor.kill();
                        bodiesToBeRemoved.offer(getBody(contact, "hero"));
                        gameIsOver = true;
                    }
                if (with(contact, "missile")) {
                    if (with(contact, "stork")) {
                        Body first = contact.getFixtureA().getBody();
                        Body second = contact.getFixtureB().getBody();
                        playerData.setScore(playerData.getScore() + 1);
                        scoreLabel.setText("STORKS KILLED: " + playerData.getScore());
                        final Stork deadStork = (Stork) entities.get(getBody(contact, "stork"));
                        if (deadStork.isRespawn()) {
                            stage.addAction(new Action() {
                                float timeToRespawn = 5f;

                                @Override
                                public boolean act(float delta) {
                                    timeToRespawn -= delta;
                                    if (timeToRespawn < 0) {
                                        Stork stork = new Stork(deadStork);
                                        stage.addActor(stork);
                                        entities.put(stork.getBody(), stork);
                                        return true;
                                    }
                                    return false;
                                }
                            });
                        }
                        bodiesToBeRemoved.offer(first);
                        bodiesToBeRemoved.offer(second);
                        entities.get(first).kill();
                        entities.get(second).kill();
                    } else if (with(contact, "ground") || with(contact, "boundary")) {
                        Body body = getBody(contact, "missile");
                        bodiesToBeRemoved.offer(body);
                        entities.get(body).kill();
                    }
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
        addBoundaries();
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
            Vector2 position = new Vector2(
                    (Float) obj.getProperties().get("x") / PIXELS_TO_METERS,
                    (Float) obj.getProperties().get("y") / PIXELS_TO_METERS
            );
            String respawn = (String) obj.getProperties().get("respawn");
            System.out.println(respawn);
            Stork actor = new Stork(
                    new TextureRegion(storkTexture),
                    position,
                    world,
                    (Integer.parseInt((String) obj.getProperties().get("routeDistance"))) * tileWidth / PIXELS_TO_METERS,
                    respawn.equals("true")
            );
            actor.setDeathTexture(new TextureRegion(deathStorkTexture));
            stage.addActor(actor);
            entities.put(actor.getBody(), actor);
        }
    }

    public void addBoundaries() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1 / PIXELS_TO_METERS, mapHeight / PIXELS_TO_METERS);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        body.setUserData("boundary");

        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.StaticBody;
        bodyDef2.position.set(mapWidth / PIXELS_TO_METERS, 0);
        Body body2 = world.createBody(bodyDef2);

        PolygonShape shape2 = new PolygonShape();
        shape2.setAsBox(1 / PIXELS_TO_METERS, mapHeight / PIXELS_TO_METERS);
        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.shape = shape2;
        body2.createFixture(fixtureDef2);
        body2.setUserData("boundary");

        BodyDef bodyDef3 = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0f);
        Body body3 = world.createBody(bodyDef3);

        PolygonShape shape3 = new PolygonShape();
        shape3.setAsBox(mapWidth / PIXELS_TO_METERS, 1 / PIXELS_TO_METERS);
        FixtureDef fixtureDef3 = new FixtureDef();
        fixtureDef3.shape = shape3;
        body3.createFixture(fixtureDef3);
        body3.setUserData("bottom");

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
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            playerActor.fire();
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
        while (!bodiesToBeRemoved.isEmpty()) {
            world.destroyBody(bodiesToBeRemoved.poll());
        }
        if (!gameIsOver) {
            handleInput();
        }
        world.step(delta, 6, 2);
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        mapRenderer.setView(camera);
        mapRenderer.render();
        centerCamera();
        stage.act(delta);
        stage.draw();
        hud.act(delta);
        hud.draw();

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
