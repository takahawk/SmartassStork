package com.bitbucket.iddqdteam.smartassstork.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Created by takahawk on 08.03.16.
 */
public class PlayerActor extends Actor {
    private static final float VELOCITY = 100f;
    private static final float JUMP_VELOCITY = 200;
    Sprite _sprite ;
    Body _body;

    public PlayerActor(final TextureRegion texture, final Body body) {
        _sprite = new Sprite(texture);
        _body = body;
    }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(
                _sprite.getTexture(),
                _body.getPosition().x - _sprite.getWidth() / 2,
                _body.getPosition().y - _sprite.getHeight() / 2
        );
    }

    @Override
    public float getX() {
        return _body.getPosition().x;
    }

    @Override
    public float getY() {
        return _body.getPosition().y;
    }

    public void moveRight() {
        _body.setLinearVelocity(VELOCITY, 0);
    }

    public void moveLeft() {
        _body.setLinearVelocity(-VELOCITY, 0);
    }

    public void jump() {
        _body.setLinearVelocity(0, -JUMP_VELOCITY);
    }
}
