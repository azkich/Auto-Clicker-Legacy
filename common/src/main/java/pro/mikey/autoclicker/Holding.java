package pro.mikey.autoclicker;

import net.minecraft.client.KeyMapping;

public class Holding {
    private final KeyMapping key;
    Config.SharedConfig config;
    private int timeout;

    public Holding(KeyMapping key, Config.SharedConfig config) {
        this.config = config;
        this.key = key;
        this.timeout = config.getCpt();
    }

    public KeyMapping getKey() {
        return this.key;
    }

    public boolean isActive() {
        return this.config.isActive();
    }

    public void setActive(boolean active) {
        this.config.setActive(active);
    }

    public boolean isSpamming() {
        return this.config.isSpamming();
    }

    public void setSpamming(boolean spamming) {
        this.config.setSpamming(spamming);
    }

    public int getSpeed() {
        return this.config.getCpt();
    }

    public void setSpeed(int speed) {
        this.config.setCpt(speed);
        // Reset timeout when speed changes
        this.timeout = speed;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void resetTimeout() {
        this.timeout = this.config.getCpt();
    }

    public void decreaseTimeout() {
        if (this.timeout > 0) {
            this.timeout -= 1;
        }
    }

    public static class AttackHolding extends Holding {

        public AttackHolding(KeyMapping key, Config.LeftMouseConfig config) {
            super(key, config);
        }
    }
}
