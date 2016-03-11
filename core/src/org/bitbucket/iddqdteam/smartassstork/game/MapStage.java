package org.bitbucket.iddqdteam.smartassstork.game;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Created by takahawk on 11.03.16.
 */
public class MapStage extends Stage {
    TiledMap map;
    TiledMapRenderer renderer;
    OrthographicCamera camera;
    private int mapWidth, mapHeight;
    private int tileWidth, tileHeight;
    private Actor mapActor;
    private Actor centerActor;

    public MapStage(Viewport port, TiledMap map, final OrthographicCamera camera) {
        super(port);
        this.map = map;
        renderer = new OrthogonalTiledMapRenderer(map);
        this.camera = camera;
        tileWidth = (Integer) map.getProperties().get("tilewidth");
        tileHeight = (Integer) map.getProperties().get("tileheight");
        mapWidth = (Integer) map.getProperties().get("width") * tileWidth;
        mapHeight = (Integer) map.getProperties().get("height") * tileHeight;
        mapActor = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
            }
        };
        mapActor.setPosition(0, 0);
        mapActor.setBounds(0, 0, mapWidth, mapHeight);
        addActor(mapActor);
    }

    public void centerCamera() {
        camera.position.set(centerActor.getX(), centerActor.getY(), 0);
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

    public void setCenterActor(Actor centerActor) {
        this.centerActor = centerActor;
    }

    public Actor getMapActor() {
        return mapActor;
    }
    public int getMapWidth() {
        return mapWidth;
    }

    public int getMapHeight() {
        return mapHeight;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    @Override
    public void draw() {
        renderer.setView(camera);
        renderer.render();
        centerCamera();
        super.draw();
    }


}
