package org.bitbucket.iddqdteam.smartassstork.game;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import org.bitbucket.iddqdteam.smartassstork.screens.GameScreen;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by takahawk on 08.03.16.
 */
public class PlayerActor
        extends Actor
        implements Killable {
    private static final float MOVE_DELAY_TIME = 0.5f;
    private static final float MOVE_FORCE = 6f;
    private static final float JUMP_FORCE = 2f;
    TextureRegion texture;
    Animation moveAnimation;
    TextureRegion missileTexture;
    Body _body;
    private boolean rightOrientation = true;
    private boolean onTheGround = false;
    private float lastMove = 0;
    private float elapsedTime = 0;
    private Map<Body, Killable> entities = new HashMap<Body, Killable>();

    public PlayerActor(
            final TextureRegion texture,
            Vector2 position,
            World world,
            final TextureRegion missileTexture,
            Map<Body, Killable> gameEntities
    ) {
        this.entities = gameEntities;
        this.texture = texture;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(position);
        _body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                texture.getRegionWidth() / 2 / GameScreen.PIXELS_TO_METERS,
                texture.getRegionHeight() / 2 / GameScreen.PIXELS_TO_METERS
        );
        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 0.0f;
        fixture.restitution = 0f;
        _body.createFixture(fixture);
        _body.setUserData("hero");
        shape.dispose();
        this.missileTexture = missileTexture;
    }

    public void setMoveAnimation(Animation moveAnimation) {
        this.moveAnimation = moveAnimation;
    }


    public Body getBody() {
        return _body;
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion frame;

        if ((elapsedTime - lastMove > MOVE_DELAY_TIME) || !onTheGround)
            frame = texture;
        else {
            frame = moveAnimation.getKeyFrame(elapsedTime, true);
        }
        if (!rightOrientation) {
            frame.flip(true, false);
        }
        batch.draw(
                frame,
                getX() - texture.getRegionWidth() / 2,
                getY() - texture.getRegionHeight() / 2
        );

        if (!rightOrientation) {
            frame.flip(true, false);
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        elapsedTime += delta;
    }

    @Override
    public float getX() {
        return _body.isActive()
                    ? _body.getPosition().x * GameScreen.PIXELS_TO_METERS
                    : super.getX();
    }

    @Override
    public float getY() {
        return _body.isActive()
                    ? _body.getPosition().y * GameScreen.PIXELS_TO_METERS
                    : super.getY();
    }

    public void moveRight() {
        rightOrientation = true;
        lastMove = elapsedTime;
        _body.applyForceToCenter(MOVE_FORCE, 0, true);
    }

    public void moveLeft() {
        rightOrientation = false;
        lastMove = elapsedTime;
        _body.applyForceToCenter(-MOVE_FORCE, 0, true);
    }

    public void jump() {
        if (onTheGround) {
            _body.applyLinearImpulse(0, JUMP_FORCE, _body.getPosition().x, _body.getPosition().y, true);
        }

    }

    public void fire() {
        Missile missile = new Missile(
                missileTexture,
                new Vector2(
                        _body.getPosition().x
                                + (rightOrientation ? 1 : -1) *
                                texture.getRegionWidth() / GameScreen.PIXELS_TO_METERS,
                        _body.getPosition().y
                ),
                _body.getWorld(),
                rightOrientation
        );
        getStage().addActor(missile);
        entities.put(missile.getBody(), missile);
    }

    public void setOnTheGround(boolean onTheGround) {
        this.onTheGround = onTheGround;
    }

    @Override
    public void kill() {
        getStage().addActor(new Actor() {
            float timeLeft = 3f;

            {
                setPosition(PlayerActor.this.getX(), PlayerActor.this.getY());
                MoveToAction fall = new MoveToAction();
                fall.setPosition(getX(), getY() -500f);
                fall.setDuration(1f);
                addAction(fall);
            }

            @Override
            public void draw(Batch batch, float parentAlpha) {
                batch.draw(texture, getX(), getY(), texture.getRegionWidth(), texture.getRegionHeight());
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                timeLeft -= delta;
                if (timeLeft < 0)
                    remove();

            }
        });
        remove();
    }
}
