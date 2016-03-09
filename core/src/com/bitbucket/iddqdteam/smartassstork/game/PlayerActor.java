package com.bitbucket.iddqdteam.smartassstork.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.bitbucket.iddqdteam.smartassstork.screens.GameScreen;

/**
 * Created by takahawk on 08.03.16.
 */
public class PlayerActor extends Actor {
    private static final float MOVE_DELAY_TIME = 0.5f;
    private static final float MOVE_FORCE = 4f;
    private static final float JUMP_FORCE = 2f;
    TextureRegion texture;
    Animation moveAnimation;
    Body _body;
    private boolean rightOrientation = true;
    private boolean onTheGround = false;
    private float lastMove = 0;
    private float elapsedTime = 0;

    public PlayerActor(final TextureRegion texture, final Body body) {
        this.texture = texture;
        _body = body;
    }

    public void setMoveAnimation(Animation moveAnimation) {
        this.moveAnimation = moveAnimation;
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
                _body.getPosition().x * GameScreen.PIXELS_TO_METERS - texture.getRegionWidth() / 2,
                _body.getPosition().y * GameScreen.PIXELS_TO_METERS - texture.getRegionHeight() / 2
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
        return _body.getPosition().x * GameScreen.PIXELS_TO_METERS;
    }

    @Override
    public float getY() {
        return _body.getPosition().y * GameScreen.PIXELS_TO_METERS;
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

    public void setOnTheGround(boolean onTheGround) {
        this.onTheGround = onTheGround;
    }

}
