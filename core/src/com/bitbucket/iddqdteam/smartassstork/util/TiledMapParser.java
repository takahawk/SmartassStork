package com.bitbucket.iddqdteam.smartassstork.util;

import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.physics.box2d.PolygonShape;

/**
 * Created by takahawk on 08.03.16.
 */
public class TiledMapParser {

    public PolygonShape getPolygon(PolygonMapObject object, float ppt) {
        PolygonShape shape = new PolygonShape();
        float[] vertices = object.getPolygon().getTransformedVertices();
        float[] worldVertices = new float[vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            System.out.println(vertices[i]);
            worldVertices[i] = vertices[i];
        }

        shape.set(worldVertices);
        return shape;
    }
}
