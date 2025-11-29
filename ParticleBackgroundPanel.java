
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

/**
 * Dynamic starfield + floating dots background panel for premium UI.
 */
public class ParticleBackgroundPanel extends JPanel implements ActionListener {

    private final int starCount;
    private final float[] starPx, starPy, starPz; // star positions
    private final float[] starR0, starR1; // star randomness
    private final float[] starCr, starCg, starCb; // star colors

    private final int dotCount = 30; // floating dots
    private final float[] dotX, dotY; // dot positions
    private final float[] dotVx, dotVy; // dot velocities
    private final float[] dotSize; // dot sizes
    private final float[] dotAlpha; // dot alpha

    private final float spread;
    private final float baseSize;
    private final float sizeRandomness;
    private final float cameraDistance;

    private final Random rnd = new Random();
    private final String[] palette = {"#FFD700", "#FFDF00", "#FFD59A", "#FFF9E6", "#FFFFFF"};

    private final Timer timer;
    private long startTime;

    public ParticleBackgroundPanel() {
        // Starfield tuned params
        this.starCount = 150;
        this.spread = 12f;
        this.baseSize = 2f;
        this.sizeRandomness = 0.45f;
        this.cameraDistance = 28f;

        starPx = new float[starCount];
        starPy = new float[starCount];
        starPz = new float[starCount];
        starR0 = new float[starCount];
        starR1 = new float[starCount];
        starCr = new float[starCount];
        starCg = new float[starCount];
        starCb = new float[starCount];

        dotX = new float[dotCount];
        dotY = new float[dotCount];
        dotVx = new float[dotCount];
        dotVy = new float[dotCount];
        dotSize = new float[dotCount];
        dotAlpha = new float[dotCount];

        initParticles();
        initDots();
        setOpaque(true);

        startTime = System.currentTimeMillis();
        timer = new Timer(30, this); // ~33 FPS for smooth animation
        timer.start();
    }

    private void initDots() {
        for (int i = 0; i < dotCount; i++) {
            dotX[i] = rnd.nextFloat();
            dotY[i] = rnd.nextFloat();
            dotVx[i] = (rnd.nextFloat() - 0.5f) * 0.0003f;
            dotVy[i] = (rnd.nextFloat() - 0.5f) * 0.0003f;
            dotSize[i] = 3f + rnd.nextFloat() * 8f;
            dotAlpha[i] = 0.15f + rnd.nextFloat() * 0.25f;
        }
    }

    private void initParticles() {
        for (int i = 0; i < starCount; i++) {
            // Use normalized screen-space coordinates so stars are evenly dispersed
            starPx[i] = rnd.nextFloat(); // 0..1 across width
            starPy[i] = rnd.nextFloat(); // 0..1 across height
            starPz[i] = rnd.nextFloat(); // depth factor 0..1 for size variation

            starR0[i] = rnd.nextFloat(); // size variation
            starR1[i] = rnd.nextFloat(); // brightness variation

            float[] col = hexToRgb(palette[rnd.nextInt(palette.length)]);
            starCr[i] = col[0];
            starCg[i] = col[1];
            starCb[i] = col[2];
        }
    }

    private float[] hexToRgb(String hex) {
        hex = hex.replaceFirst("^#", "");
        if (hex.length() == 3) {
            char[] cs = hex.toCharArray();
            hex = "" + cs[0] + cs[0] + cs[1] + cs[1] + cs[2] + cs[2];
        }
        int val = Integer.parseInt(hex, 16);
        float r = ((val >> 16) & 255) / 255f;
        float g = ((val >> 8) & 255) / 255f;
        float b = (val & 255) / 255f;
        return new float[]{r, g, b};
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        int w = getWidth();
        int h = getHeight();
        // solid black background
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, w, h);

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw stars
        for (int i = 0; i < starCount; i++) {
            float sx = starPx[i] * w;
            float sy = starPy[i] * h;
            float depthScale = 0.6f + 0.8f * starPz[i];
            float size = baseSize * (1f + sizeRandomness * (starR0[i] - 0.5f)) * depthScale;
            size = Math.max(1f, Math.min(6f, size));
            float brightness = 0.6f + 0.4f * starR1[i];
            int si = Math.max(1, Math.round(size));

            Color outer = new Color(starCr[i], starCg[i], starCb[i], clamp(0.12f * brightness, 0f, 1f));
            g2.setColor(outer);
            int outerSize = si * 3;
            g2.fillOval((int) (sx - outerSize / 2f), (int) (sy - outerSize / 2f), outerSize, outerSize);

            Color main = new Color(starCr[i], starCg[i], starCb[i], clamp(0.9f * brightness, 0f, 1f));
            g2.setColor(main);
            g2.fillOval((int) (sx - si / 2f), (int) (sy - si / 2f), si, si);

            int inner = Math.max(1, si / 2);
            g2.setColor(new Color(1f, 0.96f, 0.6f, clamp(1f * brightness, 0f, 1f)));
            g2.fillOval((int) (sx - inner / 2f), (int) (sy - inner / 2f), inner, inner);
        }

        // Draw floating dots with subtle glow
        for (int i = 0; i < dotCount; i++) {
            float dx = dotX[i] * w;
            float dy = dotY[i] * h;
            int ds = Math.round(dotSize[i]);

            // Outer soft glow
            g2.setColor(new Color(255, 215, 0, (int) (dotAlpha[i] * 40)));
            g2.fillOval((int) (dx - ds * 1.5f), (int) (dy - ds * 1.5f), ds * 3, ds * 3);

            // Main dot
            g2.setColor(new Color(255, 215, 0, (int) (dotAlpha[i] * 255)));
            g2.fillOval((int) (dx - ds / 2f), (int) (dy - ds / 2f), ds, ds);
        }

        g2.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Update floating dots positions
        for (int i = 0; i < dotCount; i++) {
            dotX[i] += dotVx[i];
            dotY[i] += dotVy[i];

            // Wrap around screen
            if (dotX[i] < 0) {
                dotX[i] = 1f;
            }
            if (dotX[i] > 1) {
                dotX[i] = 0f;
            }
            if (dotY[i] < 0) {
                dotY[i] = 1f;
            }
            if (dotY[i] > 1) {
                dotY[i] = 0f;
            }
        }
        repaint();
    }

    private static float clamp(float v, float a, float b) {
        return Math.max(a, Math.min(b, v));
    }
}
