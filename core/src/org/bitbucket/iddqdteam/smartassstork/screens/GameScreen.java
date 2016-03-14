package org.bitbucket.iddqdteam.smartassstork.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.MapLayer;
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
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import org.bitbucket.iddqdteam.smartassstork.SmartassStorkGame;
import org.bitbucket.iddqdteam.smartassstork.util.ResourceManager;
import org.bitbucket.iddqdteam.smartassstork.util.TiledMapParser;
import org.bitbucket.iddqdteam.smartassstork.game.*;

import java.util.*;

/**
 * Created by takahawk on 08.03.16.
 */
public class GameScreen
        implements Screen {
    private static final int LEVEL_COUNT = 2;
    public static final float PIXELS_TO_METERS = 100;
    private static final float GRAVITY = -5f;

    private int level;
    private int storksToKill;
    private SmartassStorkGame game;
    private boolean firstHero;
    private boolean hardMode;
    private PlayerData playerData;
    private ResourceManager resourceManager;
    private OrthographicCamera camera = initCamera();
    private OrthographicCamera camera2 = new OrthographicCamera();
    private Viewport port = new ScalingViewport(Scaling.fit, game.SCREEN_WIDTH, game.SCREEN_HEIGHT, camera);
    private World world = new World(new Vector2(0, GRAVITY), true);
    private TiledMapParser mapParser = new TiledMapParser();
    private Texture heroTexture;
    private Texture heroMissileTexture;
    private Texture hero1deathTexture, hero2deathTexture;
    private Texture moonTexture;
    private Texture storkTexture;
    private Texture deathStorkTexture;
    private Texture ded;
    private Texture gameOverTexture;
    private Texture gameCompletedTexture;
    private Animation heroMoveAnimation;
    private PlayerActor playerActor;
    private MapStage mapStage;
    private Stage hud;
    private Stage controls;
    private ImageButton rightButton, leftButton, jumpButton, fireButton;
    private Texture rightTexture, leftTexture, jumpTexture, fireTexture;
    private Skin hudSkin;
    private SpriteBatch batch;
    private Label scoreLabel, livesLabel;
    private boolean mapTouched = false;
    private Vector2 lastMapTouch = new Vector2();

    private Map<Body, Killable> entities = new HashMap<Body, Killable>();
    private Queue<Body> bodiesToBeRemoved = new ArrayDeque<Body>();

    private boolean gameIsOver = false;
    private boolean gameCompleted = false;

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
        heroMissileTexture = resourceManager.get(firstHero ? "molotok.png" : "ultr.png", Texture.class);
        deathStorkTexture = resourceManager.get("stork.png", Texture.class);
        hero1deathTexture = resourceManager.get("hero1dead.png", Texture.class);
        hero2deathTexture = resourceManager.get("hero2dead.png", Texture.class);
        hudSkin = resourceManager.get("uiskin.json", Skin.class);
        rightTexture = resourceManager.get("right.png", Texture.class);
        leftTexture = resourceManager.get("left.png", Texture.class);
        jumpTexture = resourceManager.get("up.png", Texture.class);
        fireTexture = resourceManager.get("attack.png", Texture.class);
        ded = resourceManager.get("ded.png", Texture.class);
        gameOverTexture = resourceManager.get("gameover.png", Texture.class);
        gameCompletedTexture = resourceManager.get("congrats.png", Texture.class);
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

    private void initMapStage(TiledMap map) {
        mapStage = new MapStage(port, map, (OrthographicCamera) port.getCamera());
        mapStage.getMapActor().addListener(new ClickListener() {

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                mapTouched = true;
                System.out.println("down");
                lastMapTouch.set(x, y);
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                System.out.println("up");
                mapTouched = false;
            }
        });

    }

    private void initControls() {
        rightButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(rightTexture)));
        leftButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(leftTexture)));
        jumpButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(jumpTexture)));
        fireButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(fireTexture)));
        controls = new Stage();
        Table table = new Table();
        table.setFillParent(true);
        table.top();
        table.add(jumpButton).padTop(50f);
        table.add().padTop(50f);
        table.add(fireButton).padTop(50f);
        table.row();
        table.add().expandY();
        table.row();
        table.add(leftButton).align(Align.bottom).padBottom(50f);
        table.add().expandX();
        table.add(rightButton).align(Align.bottom).padBottom(50f);
        table.row();
        controls.addActor(table);
    }

    public GameScreen(
            final PlayerData playerData,
            final boolean firstHero,
            final boolean hardMode,
            final ResourceManager resourceManager,
            final SmartassStorkGame game,
            final int level
        ) {
        this.game = game;
        this.level = level;
        storksToKill = level * 10;
        final TiledMap map = new TmxMapLoader().load("map" + level + ".tmx");
        this.playerData = playerData;
        this.resourceManager = resourceManager;
        this.firstHero = firstHero;
        this.hardMode = hardMode;
        this.batch = new SpriteBatch();
        TiledMap foregroundMap = new TmxMapLoader().load("light.tmx");
        initResources();
        initHud();
        initMapStage(map);
        initControls();
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
        playerActor.setDeathTexture(new TextureRegion(firstHero ? hero1deathTexture : hero2deathTexture));
        mapStage.addActor(playerActor);
        mapStage.setCenterActor(playerActor);
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
                                    resourceManager,
                                    game,
                                    level
                            ));
                        } else {
                            gameIsOver = true;
                        }
                        playerActor.kill();
                        bodiesToBeRemoved.offer(getBody(contact, "hero"));
                    } else if (with(contact, "exit")) {
                        if (level < LEVEL_COUNT) {
                            if (storksToKill <= playerData.getScore()) {
                                game.setScreen(new GameScreen(
                                        playerData,
                                        firstHero,
                                        hardMode,
                                        resourceManager,
                                        game,
                                        level + 1
                                ));
                            }
                        } else {
                            gameCompleted = true;
                        }
                    }
                if (with(contact, "missile")) {
                    if (with(contact, "stork")) {
                        Body first = contact.getFixtureA().getBody();
                        Body second = contact.getFixtureB().getBody();
                        playerData.setScore(playerData.getScore() + 1);
                        scoreLabel.setText("STORKS KILLED: " + playerData.getScore() + " of " + storksToKill);
                        final Stork deadStork = (Stork) entities.get(getBody(contact, "stork"));
                        if (deadStork.isRespawn()) {
                            mapStage.addAction(new Action() {
                                float timeToRespawn = 5f;

                                @Override
                                public boolean act(float delta) {
                                    timeToRespawn -= delta;
                                    if (timeToRespawn < 0) {
                                        Stork stork = new Stork(deadStork);
                                        mapStage.addActor(stork);
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
        addExit(map);
        addNPC(map);
        Gdx.input.setInputProcessor(controls);
    }

    private void addGround(TiledMap map) {
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

    private void addStorks(TiledMap map) {
        for (MapObject obj : map.getLayers().get("Enemies").getObjects()) {
            Vector2 position = new Vector2(
                    (Float) obj.getProperties().get("x") / PIXELS_TO_METERS,
                    (Float) obj.getProperties().get("y") / PIXELS_TO_METERS
            );
            String respawn = (String) obj.getProperties().get("respawn");
            Stork actor = new Stork(
                    new TextureRegion(storkTexture),
                    position,
                    world,
                    (Integer.parseInt((String) obj.getProperties().get("routeDistance"))) * mapStage.getTileWidth() / PIXELS_TO_METERS,
                    respawn.equals("true")
            );
            actor.addListener(new InputListener() {

                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    playerActor.fire();
                    return true;
                }
            });
            actor.setDeathTexture(new TextureRegion(deathStorkTexture));
            mapStage.addActor(actor);
            entities.put(actor.getBody(), actor);
        }
    }

    private void addBoundaries() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(1 / PIXELS_TO_METERS, mapStage.getMapHeight() / PIXELS_TO_METERS);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        body.createFixture(fixtureDef);
        body.setUserData("boundary");

        BodyDef bodyDef2 = new BodyDef();
        bodyDef2.type = BodyDef.BodyType.StaticBody;
        bodyDef2.position.set(mapStage.getMapWidth() / PIXELS_TO_METERS, 0);
        Body body2 = world.createBody(bodyDef2);

        PolygonShape shape2 = new PolygonShape();
        shape2.setAsBox(1 / PIXELS_TO_METERS, mapStage.getMapHeight() / PIXELS_TO_METERS);
        FixtureDef fixtureDef2 = new FixtureDef();
        fixtureDef2.shape = shape2;
        body2.createFixture(fixtureDef2);
        body2.setUserData("boundary");

        BodyDef bodyDef3 = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0f);
        Body body3 = world.createBody(bodyDef3);

        PolygonShape shape3 = new PolygonShape();
        shape3.setAsBox(mapStage.getMapWidth() / PIXELS_TO_METERS, 1 / PIXELS_TO_METERS);
        FixtureDef fixtureDef3 = new FixtureDef();
        fixtureDef3.shape = shape3;
        body3.createFixture(fixtureDef3);
        body3.setUserData("bottom");

    }

    private void addExit(TiledMap map) {
        for (MapObject obj : map.getLayers().get("Exit").getObjects()) {
            if (obj instanceof PolygonMapObject) {
                Shape shape = mapParser.getPolygon((PolygonMapObject) obj, 1 / PIXELS_TO_METERS);
                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyDef.BodyType.StaticBody;
                Body body = world.createBody(bodyDef);
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                body.createFixture(fixtureDef);
                body.setUserData("exit");
            }
        }
    }

    private void addNPC(TiledMap map) {
        MapLayer layer = map.getLayers().get("NPC");
        BitmapFont font = new BitmapFont();
        if (layer != null) {
            for (MapObject obj : layer.getObjects()) {
                final float x = (Float) obj.getProperties().get("x");
                final float y = (Float) obj.getProperties().get("y");
                final String text = (String) obj.getProperties().get("text");
                Actor actor = new Actor() {
                    @Override
                    public void draw(Batch batch, float parentAlpha) {
                        batch.draw(ded, x, y, ded.getWidth(), ded.getHeight());
                    }
                };
                Label textLabel = new Label(text, hudSkin);
                textLabel.setWrap(true);
                textLabel.setWidth(300);
                textLabel.setPosition(x, y + 100f);
                mapStage.addActor(actor);
                mapStage.addActor(textLabel);
                actor.toBack();
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
        if ((gameIsOver || gameCompleted) && Gdx.input.isTouched()) {
            game.setScreen(new MenuScreen(
                    game,
                    resourceManager
            ));
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerActor.moveRight();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerActor.moveLeft();
        } else if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            playerActor.jump();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            playerActor.fire();
        }

        if (rightButton.isPressed()) {
            playerActor.moveRight();
        }
        if (leftButton.isPressed()) {
            playerActor.moveLeft();
        }
        if (fireButton.isPressed()) {
            playerActor.fire();
        }
        if (jumpButton.isPressed()) {
            playerActor.jump();
        }
    }


    @Override
    public void show() {

    }

    public void update(float delta) {
        if (mapTouched) {
            if (lastMapTouch.y - playerActor.getY() > 100f)
                playerActor.jump();
            else
                if (playerActor.getX() < lastMapTouch.x)
                    playerActor.moveRight();
                else
                    playerActor.moveLeft();
        }
        while (!bodiesToBeRemoved.isEmpty()) {
            world.destroyBody(bodiesToBeRemoved.poll());
        }

        world.step(delta, 6, 2);
    }

    @Override
    public void render(float delta) {
        if (!gameIsOver && !gameCompleted) {
            update(delta);
        }
        handleInput();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapStage.act(delta);
        mapStage.draw();
        hud.act(delta);
        hud.draw();

        controls.act(delta);
        controls.draw();

        Matrix4 debugMatrix = camera.combined.cpy().scale(PIXELS_TO_METERS, PIXELS_TO_METERS, 0);
        // debugRenderer.render(world, debugMatrix);
        if (gameIsOver) {
            batch.begin();
            batch.draw(gameOverTexture, 0, 0, gameOverTexture.getWidth(), gameOverTexture.getHeight());
            batch.end();
        }
        if (gameCompleted) {
            batch.begin();
            batch.draw(gameCompletedTexture, 0, 0, gameCompletedTexture.getWidth(), gameCompletedTexture.getHeight());
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
//        camera.setToOrtho(false, width, height);
//        camera.update();
        port.update(width, height, true);
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
