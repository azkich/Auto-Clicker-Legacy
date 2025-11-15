package pro.mikey.autoclicker;

public class Config {

    private final LeftMouseConfig leftClick;
    private final RightMouseConfig rightClick;
    private final JumpConfig jump;
    private final HudConfig hudConfig;

    public Config(LeftMouseConfig leftClick, RightMouseConfig rightClick, JumpConfig jump, HudConfig hudConfig) {
        this.leftClick = leftClick;
        this.rightClick = rightClick;
        this.jump = jump;
        this.hudConfig = hudConfig;
    }

    public LeftMouseConfig getLeftClick() {
        return this.leftClick;
    }

    public RightMouseConfig getRightClick() {
        return this.rightClick;
    }

    public JumpConfig getJump() {
        return this.jump;
    }

    public HudConfig getHudConfig(){return this.hudConfig;}

    @Override
    public String toString() {
        return "Config{" +
                "leftClick=" + this.leftClick +
                ", rightClick=" + this.rightClick +
                ", jump=" + this.jump +
                '}';
    }

    public static class HudConfig {
        private boolean enabled;
        private String location;
        private boolean disableAfterDeath;
        private boolean disableAfterDisconnect;
        private boolean disableAfterReloadScreen;

        public HudConfig(Boolean enabled, String location, Boolean disableAfterDeath, Boolean disableAfterDisconnect, Boolean disableAfterReloadScreen){
            this.enabled = enabled;
            this.location = location;
            this.disableAfterDeath = disableAfterDeath != null ? disableAfterDeath : false;
            this.disableAfterDisconnect = disableAfterDisconnect != null ? disableAfterDisconnect : false;
            this.disableAfterReloadScreen = disableAfterReloadScreen != null ? disableAfterReloadScreen : false;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getLocation(){
            return this.location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public boolean isDisableAfterDeath() {
            return this.disableAfterDeath;
        }

        public void setDisableAfterDeath(boolean disableAfterDeath) {
            this.disableAfterDeath = disableAfterDeath;
        }

        public boolean isDisableAfterDisconnect() {
            return this.disableAfterDisconnect;
        }

        public void setDisableAfterDisconnect(boolean disableAfterDisconnect) {
            this.disableAfterDisconnect = disableAfterDisconnect;
        }

        public boolean isDisableAfterReloadScreen() {
            return this.disableAfterReloadScreen;
        }

        public void setDisableAfterReloadScreen(boolean disableAfterReloadScreen) {
            this.disableAfterReloadScreen = disableAfterReloadScreen;
        }

        public String toString(){
            return "Config{" +
                    "hudEnabled=" + this.enabled +
                    ", hudLocation=" + this.location +
                    ", disableAfterDeath=" + this.disableAfterDeath +
                    ", disableAfterDisconnect=" + this.disableAfterDisconnect +
                    ", disableAfterReloadScreen=" + this.disableAfterReloadScreen +
                    '}';
        }
    }

    public static class LeftMouseConfig extends SharedConfig {
        public LeftMouseConfig(boolean active, boolean spamming, int cpt) {
            super(active, spamming, cpt);
        }
    }

    public static class RightMouseConfig extends SharedConfig {
        public RightMouseConfig(boolean active, boolean spamming, int cpt) {
            super(active, spamming, cpt);
        }
    }

    public static class JumpConfig extends SharedConfig {
        public JumpConfig(boolean active, boolean spamming, int cpt) {
            super(active, spamming, cpt);
        }
    }

    public static class SharedConfig {
        private boolean active;
        private boolean spamming;
        private int cpt;

        public SharedConfig(boolean active, boolean spamming, int cpt) {
            this.active = active;
            this.spamming = spamming;
            this.cpt = cpt;
        }

        public boolean isActive() {
            return this.active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public boolean isSpamming() {
            return this.spamming;
        }

        public void setSpamming(boolean spamming) {
            this.spamming = spamming;
        }

        public int getCpt() {
            return this.cpt;
        }

        public void setCpt(int cpt) {
            this.cpt = cpt;
        }

        @Override
        public String toString() {
            return "SharedConfig{" +
                    "active=" + this.active +
                    ", spamming=" + this.spamming +
                    ", cpt=" + this.cpt +
                    '}';
        }
    }
}
