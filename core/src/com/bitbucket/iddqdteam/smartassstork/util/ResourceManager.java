package com.bitbucket.iddqdteam.smartassstork.util;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by takahawk on 09.03.16.
 */
public class ResourceManager {
    private AssetManager assetManager = new AssetManager();

    public <T> T get(String filename, Class<T> type) {
        if (!assetManager.isLoaded(filename)) {
            assetManager.load(filename, type);
            assetManager.finishLoading();
        }
        return assetManager.get(filename, type);
    }

    public void dispose() {
        assetManager.dispose();
    }
}
