import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Bird {
    static final int DIAMETER_PX = 100;
    private static final int BULLET_BODY_WIDTH = 25;
    private static final int BULLET_BODY_HEIGHT = 15;
    private static final int BULLET_NOSE_LENGTH = 10;

    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    static {
        preloadImages();
    }

    public static void preloadImages() {
        for (PhyConfig.BulletConfig config : PhyConfig.BulletConfig.values()) {
            String imageName = config.name();
            try (InputStream is = Bird.class.getResourceAsStream("/" + imageName + ".png")) {
                if (is != null) {
                    BufferedImage img = ImageIO.read(is);
                    imageCache.put(imageName, img);
                }
            } catch (IOException e) {
                System.err.println("Could not load image: " + imageName);
            }
        }
    }

    // Чтобы замедлить симуляции и было понятно как летит, а не когда по настоящему скорости
    public static double slowTimeBy = 1;
    double lastTimeSec = 0;
    boolean hitBoundary = false;
    boolean isLaunched = false;
    Physics physics = new Physics();
    double currentTime = 0;
    double lastDumpTime = 0;
    Vector velocity0 = new Vector(1, 0);
    ArrayList<BirdState> history = new ArrayList<>();
    private int x;
    private int y;

    public Bird(PhyConfig config) {
        physics.config = config;
        reset();
    }

    public void reset() {
        isLaunched = false;
        hitBoundary = false;
        history.clear();
        physics.setup(physics.config);
        updatePositionFromPhysics();
    }

    public void paint(Graphics g) {
        if (hitBoundary) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.translate(x, y);

        double angle = isLaunched ? -physics.velocity.angleRad() : -velocity0.angleRad();
        g2d.rotate(angle);

        BufferedImage currentBulletImage = null;
        if (physics.config.selectedConfig != null) {
            currentBulletImage = imageCache.get(physics.config.selectedConfig.name());
        }

        if (currentBulletImage != null) {
            int iw = currentBulletImage.getWidth();
            int ih = currentBulletImage.getHeight();

            // Scale to DIAMETER_PX width while maintaining aspect ratio
            double scale = (double) DIAMETER_PX / iw;
            int drawW = DIAMETER_PX;
            int drawH = (int) (ih * scale);

            g2d.drawImage(currentBulletImage, -drawW / 2, -drawH / 2, drawW, drawH, null);
        } else {
            Rectangle2D.Double body = new Rectangle2D.Double(
                    -BULLET_BODY_WIDTH,
                    -BULLET_BODY_HEIGHT / 2.0,
                    BULLET_BODY_WIDTH,
                    BULLET_BODY_HEIGHT
            );
            g2d.setColor(Color.DARK_GRAY);
            g2d.fill(body);
            g2d.setColor(Color.BLACK);
            g2d.draw(body);

            int[] noseX = {0, BULLET_NOSE_LENGTH, 0};
            int[] noseY = {-BULLET_BODY_HEIGHT / 2, 0, BULLET_BODY_HEIGHT / 2};

            g2d.setColor(Color.BLACK);
            g2d.fillPolygon(noseX, noseY, 3);
            g2d.drawPolygon(noseX, noseY, 3);
        }

        g2d.dispose();

        if (isLaunched) {
            ForceVisualizer.drawForceVectors(g, physics.forces, physics.velocity, x, y);
        }
    }

    public void preLanuch(PhyConfig config) {
        physics.setup(config);
        updatePositionFromPhysics();
    }

    public void launch(PhyConfig config) {
        preLanuch(config);
        isLaunched = true;
        hitBoundary = false;

        lastTimeSec = Time.seconds();
        currentTime = 0;
        lastDumpTime = 0;
    }

    public void update() {
        if (!isLaunched || hitBoundary) //  позиция на земле
            return;

        double time = Time.seconds();
        double delta = (time - lastTimeSec) / slowTimeBy;
        lastTimeSec = time;
        if (delta > 0.1) return;

        dump();

        currentTime += delta;
        physics.update(delta);

        updatePositionFromPhysics();
    }

    void updatePositionFromPhysics() {
        Point nextPoint = physics.getPosition();
        x = nextPoint.toScreenX();
        y = nextPoint.toScreenY();

        checkHitBoundary();
    }

    public void checkHitBoundary() {
        if (!this.isLaunched) return;
        boolean hit = physics.position.y < 0 || physics.position.y > MyPanel.getStaticMaxHeightMeters();
        if (hit) {
            hitBoundary = true;
            isLaunched = false;
        }
    }

    public void dump() {
        if ((currentTime - lastDumpTime) > 0.05) {
            history.add(new BirdState(
                    currentTime,
                    physics.position,
                    physics.velocity
            ));
            lastDumpTime = currentTime;
        }
    }

    public void resetToTime(double time) {
        if (history.isEmpty()) return;
        BirdState nearestState = history.getFirst();
        double best = Double.MAX_VALUE;
        for (BirdState state : history) {
            double dt = Math.abs(state.time() - time);
            if (dt < best) {
                best = dt;
                nearestState = state;
            }
        }
        physics.position = nearestState.position();
        physics.velocity = nearestState.velocity();
        currentTime = nearestState.time();
        lastDumpTime = currentTime;
        updatePositionFromPhysics();
    }

    void dropFutureHistory() {
        ArrayList<BirdState> ok = new ArrayList<>();
        for (BirdState state : history) {
            if (state.time() < currentTime) ok.add(state);
        }
        history.retainAll(ok);
    }
}
