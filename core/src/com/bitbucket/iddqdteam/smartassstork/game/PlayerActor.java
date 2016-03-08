package com.bitbucket.iddqdteam.smartassstork.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.bitbucket.iddqdteam.smartassstork.screens.GameScreen;

/**
 * Created by takahawk on 08.03.16.
 */
public class PlayerActor extends Actor {
    private static final float MOVE_FORCE = 2f;
    private static final float JUMP_FORCE = 1f;
    Sprite _sprite;
    Body _body;
    private boolean onTheGround = false;

    public PlayerActor(final TextureRegion texture, final Body body) {
        _sprite = new Sprite(texture);
        _body = body;
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(
                _sprite.getTexture(),
                _body.getPosition().x * GameScreen.PIXELS_TO_METERS - _sprite.getWidth() / 2,
                _body.getPosition().y * GameScreen.PIXELS_TO_METERS - _sprite.getHeight() / 2
        );
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
        _body.applyForceToCenter(MOVE_FORCE, 0, true);
        System.out.println(_body.getLinearVelocity());
    }

    public void moveLeft() {
        _body.applyForceToCenter(-MOVE_FORCE, 0, true);
    }

    public void jump() {
        if (onTheGround) {
            _body.applyLinearImpulse(0, JUMP_FORCE, _body.getPosition().x, _body.getPosition().y, true);
            System.out.println(_body.getLinearVelocity());
        }

    }

    public void setOnTheGround(boolean onTheGround) {
        this.onTheGround = onTheGround;
    }

}
