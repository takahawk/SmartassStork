package org.bitbucket.iddqdteam.smartassstork.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import org.bitbucket.iddqdteam.smartassstork.screens.GameScreen;

/**
 * Created by takahawk on 09.03.16.
 */
public class Missile
        extends Actor
        implements Killable {
    private static final float VELOCITY = 5f;
    private TextureRegion texture;
    private Body body;
    private boolean rightOrientation;

    public Missile(TextureRegion texture, Vector2 initPos, World world, boolean rightOrientation) {
        this.rightOrientation = rightOrientation;
        this.texture = texture;
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.gravityScale = 0;
        bodyDef.position.set(initPos);
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
        body.setUserData("missile");
        body.setLinearVelocity(rightOrientation ? VELOCITY : -VELOCITY, 0);
    }

    public Body getBody() {
        return body;
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion frame = texture;

        if (!rightOrientation) {
            frame.flip(true, false);
        }
        batch.draw(
                frame,
                body.getPosition().x * GameScreen.PIXELS_TO_METERS - texture.getRegionWidth() / 2,
                body.getPosition().y * GameScreen.PIXELS_TO_METERS - texture.getRegionHeight() / 2
        );

        if (!rightOrientation) {
            frame.flip(true, false);
        }
    }

    @Override
    public void kill() {
        remove();
    }
}
