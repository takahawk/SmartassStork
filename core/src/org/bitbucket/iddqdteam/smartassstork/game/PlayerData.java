package org.bitbucket.iddqdteam.smartassstork.game;

/**
 * Created by takahawk on 08.03.16.
 */
public class PlayerData {
    private int score = 0;
    private int lives = 5;

    public int getLives() {
        return lives;
    }

    public int getScore() {
        return score;
    }

    public void setLives(int lives) {
        this.lives = lives;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
