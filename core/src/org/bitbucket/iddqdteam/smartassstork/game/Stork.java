package org.bitbucket.iddqdteam.smartassstork.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import org.bitbucket.iddqdteam.smartassstork.screens.GameScreen;

/**
 * Created by takahawk on 09.03.16.
 */
public class Stork
        extends Actor
        implements Killable {
    private static final float VELOCITY = 3f;
    TextureRegion texture;
    TextureRegion deathTexture;
    Body body;
    Vector2 initPos;
    float beginX;
    float endX;
    private boolean rightOrientation = true;
    private boolean respawn;
    World world;

    public Stork(Stork that) {
        this(
                that.texture,
                new Vector2(that.initPos),
                that.world,
                that.endX - that.beginX,
                that.respawn
                );
        setDeathTexture(that.deathTexture);
    }

    public Stork(
            final TextureRegion texture,
            Vector2 position,
            World world,
            float routeDistance,
            boolean respawn
    ) {
        this.initPos = position;
        this.texture = texture;
        this.respawn = respawn;
        this.world = world;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.gravityScale = 0;
        bodyDef.position.set(position);
        body = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
                texture.getRegionWidth() / 2 / GameScreen.PIXELS_TO_METERS,
                texture.getRegionHeight() / 2 / GameScreen.PIXELS_TO_METERS
        );
        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 0.0f;
        fixture.restitution = 0f;
        body.createFixture(fixture);
        shape.dispose();
        body.setUserData("stork");
        beginX = body.getPosition().x;
        endX = body.getPosition().x + routeDistance;
        body.setLinearVelocity(VELOCITY, 0);
        setWidth(texture.getRegionWidth() * 2);
        setHeight(texture.getRegionHeight() * 2);
        setBounds(getX(),getY(),getWidth(),getHeight());
    }

    public void setDeathTexture(TextureRegion deathTexture) {
        this.deathTexture = deathTexture;
    }

    public boolean isRespawn() {
        return respawn;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion frame = texture;
        if (!rightOrientation)
            frame.flip(true, false);
        batch.draw(
                frame,
                body.getPosition().x * GameScreen.PIXELS_TO_METERS - texture.getRegionWidth() / 2,
                body.getPosition().y * GameScreen.PIXELS_TO_METERS - texture.getRegionHeight() / 2
        );
        if (!rightOrientation)
            frame.flip(true, false);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (body.getPosition().x > endX) {
            body.setLinearVelocity(-VELOCITY, 0);
            rightOrientation = false;
        }
        else if (body.getPosition().x < beginX) {
            body.setLinearVelocity(VELOCITY, 0);
            rightOrientation = true;
        }

    }

    @Override
    public float getX() {
        return body.getPosition().x * GameScreen.PIXELS_TO_METERS;
    }

    @Override
    public float getY() {
        return body.getPosition().y * GameScreen.PIXELS_TO_METERS;
    }



    public Body getBody() {
        return body;
    }

    @Override
    public void kill() {
        getStage().addActor(new Actor() {
            float timeLeft = 3f;

            {
                setPosition(Stork.this.getX(), Stork.this.getY());
                MoveToAction fall = new MoveToAction();
                fall.setPosition(getX(), getY() -500f);
                fall.setDuration(1f);
                addAction(fall);
            }

            @Override
            public void draw(Batch batch, float parentAlpha) {
                deathTexture.flip(false, true);
                batch.draw(
                        deathTexture,
                        getX(),
                        getY(),
                        deathTexture.getRegionWidth(),
                        deathTexture.getRegionHeight()
                );
                deathTexture.flip(false, true);
            }

            @Override
            public void act(float delta) {
                super.act(delta);
                timeLeft -= delta;
                if (timeLeft < 0) {
                    remove();
                }

            }
        });
        remove();
    }
}
