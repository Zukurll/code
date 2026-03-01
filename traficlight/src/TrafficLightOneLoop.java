import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TrafficLightOneLoop extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 400;
    private static final int ROAD_Y = 250;
    private static final int ROAD_HEIGHT = 100;
    private static final int ROAD_BOTTOM = ROAD_Y + ROAD_HEIGHT;
    private static final int CROSSWALK_WIDTH = 100;
    private static final int CROSSWALK_START_X = 370;
    private static final int CROSSWALK_END_X = 470;
    private static final int CROSSWALK_CENTER = (CROSSWALK_START_X + CROSSWALK_END_X) / 2;
    private static final int STOP_LINE_X = 340;
    private static final int LIGHT_X = 310;
    private static final int LIGHT_Y = ROAD_Y - 100;
    private static final int PED_X = CROSSWALK_CENTER;
    private static final int PED_START_Y = ROAD_BOTTOM + 30;
    private static final int PED_END_Y = ROAD_Y - 30;
    private static final int PED_SPEED = 2;
    private static final int CAR_WIDTH = 70;
    private static final int CAR_HEIGHT = 35;
    private static final int CAR_SPEED = 4;

    private enum LightState { GREEN, RED }
    private LightState lightState = LightState.GREEN;
    private int carX = -100;
    private boolean carStopped = false;
    private boolean cycleComplete = false;
    private int pedX = PED_X;
    private int pedY = PED_START_Y;
    private boolean isCrossing = false;
    private boolean finishedCrossing = false;
    private Timer animTimer;
    private Timer lightTimer;

    public TrafficLightOneLoop() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);
        animTimer = new Timer(33, e -> { update(); repaint(); });
        animTimer.start();
        lightTimer = new Timer(4000, e -> changeLight());
        lightTimer.start();
    }

    private void changeLight() {
        if (lightState == LightState.GREEN && !cycleComplete) {
            lightState = LightState.RED;
            isCrossing = true;
            carStopped = false;
        } else if (lightState == LightState.RED && !cycleComplete) {
            lightState = LightState.GREEN;
            cycleComplete = true;
            isCrossing = false;
        }
    }

    private void update() {
        if (lightState == LightState.GREEN) {
            carX += CAR_SPEED;
            carStopped = false;
            if (carX > WIDTH) carX = -100;
        } else {
            int carFront = carX + CAR_WIDTH;
            if (carFront < STOP_LINE_X) carX += CAR_SPEED;
            else { carX = STOP_LINE_X - CAR_WIDTH; carStopped = true; }
        }
        if (isCrossing && pedY > PED_END_Y) pedY -= PED_SPEED;
        if (pedY <= PED_END_Y && isCrossing) finishedCrossing = true;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(135, 206, 250));
        g2.fillRect(0, 0, WIDTH, ROAD_Y);

        g2.setColor(new Color(46, 139, 87));
        g2.fillRect(0, 0, WIDTH, ROAD_Y);
        g2.fillRect(0, ROAD_BOTTOM, WIDTH, HEIGHT - ROAD_BOTTOM);

        for (int i = 50; i < WIDTH; i += 150) {
            g2.setColor(new Color(139, 69, 19));
            g2.fillRect(i, ROAD_Y - 60, 10, 40);
            g2.setColor(new Color(34, 139, 34));
            g2.fillOval(i - 15, ROAD_Y - 80, 40, 40);
        }

        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(0, ROAD_Y, WIDTH, ROAD_HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0,
                                     new float[]{15, 15}, 0));
        g2.drawLine(0, ROAD_Y + (ROAD_HEIGHT / 2), WIDTH, ROAD_Y + (ROAD_HEIGHT / 2));
        g2.setStroke(new BasicStroke(1));

        g2.setColor(Color.WHITE);
        int stripeHeight = 10;
        int stripeSpacing = 10;
        for (int y = ROAD_Y + 5; y < ROAD_Y + ROAD_HEIGHT; y += stripeHeight + stripeSpacing) {
            g2.fillRect(CROSSWALK_START_X, y, CROSSWALK_WIDTH, stripeHeight);
        }

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(5));
        g2.drawLine(STOP_LINE_X, ROAD_Y + 10, STOP_LINE_X, ROAD_BOTTOM - 10);
        g2.setStroke(new BasicStroke(1));

        g2.setColor(new Color(50, 50, 50));
        g2.fillRect(LIGHT_X - 6, LIGHT_Y, 12, ROAD_Y - LIGHT_Y);

        g2.setColor(Color.BLACK);
        g2.fillRoundRect(LIGHT_X - 18, LIGHT_Y - 15, 36, 90, 12, 12);

        g2.setColor(lightState == LightState.RED ? Color.RED : new Color(60, 0, 0));
        g2.fillOval(LIGHT_X - 12, LIGHT_Y - 8, 24, 24);
        g2.setColor(Color.WHITE);
        g2.drawOval(LIGHT_X - 12, LIGHT_Y - 8, 24, 24);

        g2.setColor(lightState == LightState.GREEN ? Color.GREEN : new Color(0, 60, 0));
        g2.fillOval(LIGHT_X - 12, LIGHT_Y + 50, 24, 24);
        g2.setColor(Color.WHITE);
        g2.drawOval(LIGHT_X - 12, LIGHT_Y + 50, 24, 24);

        g2.setColor(new Color(25, 118, 210));
        g2.fillRoundRect(carX, ROAD_Y + 20, CAR_WIDTH, CAR_HEIGHT, 10, 10);
        g2.setColor(new Color(200, 230, 255));
        g2.fillRect(carX + 12, ROAD_Y + 25, 22, 16);
        g2.fillRect(carX + 40, ROAD_Y + 25, 18, 16);
        g2.setColor(Color.BLACK);
        g2.fillOval(carX + 8, ROAD_Y + 48, 16, 16);
        g2.fillOval(carX + 48, ROAD_Y + 48, 16, 16);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));

        if (pedY >= PED_END_Y - 10) {
            int headX = pedX - 9;
            int headY = pedY - 22;
            g2.fillOval(headX, headY, 18, 18);
            g2.drawLine(headX + 9, headY + 18, headX + 9, headY + 42);
            g2.drawLine(headX + 2, headY + 25, headX + 16, headY + 25);
            if (isCrossing && pedY > PED_END_Y) {
                int offset = (int)(Math.sin(System.currentTimeMillis() / 80.0) * 6);
                g2.drawLine(headX + 9, headY + 42, headX + 2, headY + 58 + offset);
                g2.drawLine(headX + 9, headY + 42, headX + 16, headY + 58 - offset);
            } else {
                g2.drawLine(headX + 9, headY + 42, headX + 3, headY + 58);
                g2.drawLine(headX + 9, headY + 42, headX + 15, headY + 58);
            }
        }

        g2.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("🚦 Traffic Light - Pedestrian Crossing with Trees");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new TrafficLightOneLoop());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            frame.setVisible(true);
        });
    }
}