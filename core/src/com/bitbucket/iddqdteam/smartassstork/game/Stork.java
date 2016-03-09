package com.bitbucket.iddqdteam.smartassstork.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.bitbucket.iddqdteam.smartassstork.screens.GameScreen;

/**
 * Created by takahawk on 09.03.16.
 */
public class Stork extends Actor {
    private static final float VELOCITY = 3f;
    TextureRegion texture;
    Body body;
    float beginX;
    float endX;
    private boolean rightOrientation = true;


    public Stork(final TextureRegion texture, final Body body, float routeDistance) {

        this.texture = texture;
        this.body = body;
        beginX = body.getPosition().x;
        endX = body.getPosition().x + routeDistance;
        body.setLinearVelocity(VELOCITY, 0);
        System.out.println(beginX);
        System.out.println(endX);
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
}
