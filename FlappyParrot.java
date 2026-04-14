import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class FlappyParrot {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Flappy Parrot: Legacy Edition");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setResizable(true);
            
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);

            frame.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.getKeyCode() == KeyEvent.VK_F11) {
                        if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
                            frame.dispose();
                            frame.setUndecorated(false);
                            frame.setExtendedState(JFrame.NORMAL);
                            frame.setVisible(true);
                        } else {
                            frame.dispose();
                            frame.setUndecorated(true);
                            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                            frame.setVisible(true);
                        }
                        gamePanel.requestFocusInWindow();
                    }
                }
            });

            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
        });
    }
}

class GamePanel extends JPanel {
    private enum State { MENU, LOADING, PLAYING, GAMEOVER, LEADERBOARD }
    private State currentState = State.MENU;

    private int score = 0;
    private int highscore = 0;
    private int loadProgress = 0;
    private double gameSpeed = 4.0;
    private String pilotName = "PILOT";
    private final String DATA_FILE = "scores.dat";

    private Bird bird;
    private ArrayList<Pipe> pipes;
    private ArrayList<Cloud> clouds;
    private ArrayList<Star> stars;
    private List<ScoreEntry> leaderboard = new ArrayList<>();
    
    private Timer gameTimer;
    private final Random rand = new Random();

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);
        loadHighScores();
        setupEnvironment();
        setupInput();
    }

    private void setupEnvironment() {
        clouds = new ArrayList<>();
        pipes = new ArrayList<>();
        stars = new ArrayList<>();
        
        for (int i = 0; i < 50; i++) stars.add(new Star(rand.nextInt(2000), rand.nextInt(1000)));
        for (int i = 0; i < 6; i++) clouds.add(new Cloud(rand.nextInt(2000), rand.nextInt(300)));
    }

    private void setupInput() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (currentState == State.MENU) {
                    if (code == KeyEvent.VK_SPACE) startLoading();
                    if (code == KeyEvent.VK_L) currentState = State.LEADERBOARD;
                } else if (currentState == State.PLAYING) {
                    if (code == KeyEvent.VK_SPACE) bird.jump();
                } else if (currentState == State.GAMEOVER) {
                    if (code == KeyEvent.VK_SPACE) goToMenu();
                } else if (currentState == State.LEADERBOARD) {
                    if (code == KeyEvent.VK_ESCAPE || code == KeyEvent.VK_SPACE) currentState = State.MENU;
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int mx = e.getX(), my = e.getY();
                int cx = getWidth() / 2;
                if (currentState == State.MENU) {
                    if (checkBounds(mx, my, cx - 100, 280, 200, 50)) startLoading();
                    else if (checkBounds(mx, my, cx - 100, 340, 200, 50)) updateName();
                    else if (checkBounds(mx, my, cx - 100, 400, 200, 50)) currentState = State.LEADERBOARD;
                    else if (checkBounds(mx, my, cx - 100, 460, 200, 50)) System.exit(0);
                } else if (currentState == State.PLAYING) {
                    bird.jump();
                } else if (currentState == State.GAMEOVER || currentState == State.LEADERBOARD) {
                    goToMenu();
                }
            }
        });
    }

    private boolean checkBounds(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private void updateName() {
        String n = JOptionPane.showInputDialog(this, "Identify Pilot:", pilotName);
        if (n != null && !n.trim().isEmpty()) {
            pilotName = n.trim().toUpperCase();
            if (pilotName.length() > 10) pilotName = pilotName.substring(0, 10);
        }
    }

    private void startLoading() {
        currentState = State.LOADING;
        loadProgress = 0;
        if (gameTimer != null) gameTimer.stop();
        gameTimer = new Timer(16, e -> update());
        gameTimer.start();
    }

    private void startGame() {
        currentState = State.PLAYING;
        score = 0;
        gameSpeed = 4.0;
        bird = new Bird(150, getHeight() / 2);
        pipes.clear();
        for (int i = 0; i < 6; i++) {
            pipes.add(new Pipe(getWidth() + (i * 300)));
        }
    }

    private void goToMenu() {
        currentState = State.MENU;
        if (gameTimer != null) gameTimer.stop();
        repaint();
    }

    private void update() {
        for (Star s : stars) s.update(gameSpeed * 0.1, getWidth());
        for (Cloud c : clouds) c.update(gameSpeed * 0.2, getWidth());

        if (currentState == State.LOADING) {
            loadProgress += 2;
            if (loadProgress >= 100) startGame();
        } else if (currentState == State.PLAYING) {
            bird.update();
            gameSpeed = 4.0 + (Math.floor(score / 5.0) * 0.8);
            bird.setGravity(0.5 + (Math.floor(score / 5.0) * 0.05));

            for (Pipe p : pipes) {
                p.update(gameSpeed);
                if (bird.getBounds().intersects(p.getTopBounds()) || 
                    bird.getBounds().intersects(p.getBottomBounds(getHeight()))) {
                    endGame();
                }
                if (!p.isScored() && p.getX() < bird.getX()) {
                    p.setScored(true);
                    score++;
                    if (score > highscore) highscore = score;
                }
            }

            if (pipes.get(0).getX() < -100) {
                pipes.remove(0);
                pipes.add(new Pipe(pipes.get(pipes.size() - 1).getX() + 300));
            }

            if (bird.getY() > getHeight() - 50 || bird.getY() < -50) endGame();
        }
        repaint();
    }

    private void endGame() {
        currentState = State.GAMEOVER;
        saveScore(pilotName, score);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawSkyBackground(g2);
        for (Star s : stars) s.draw(g2);
        drawCelestialBodies(g2);
        for (Cloud c : clouds) c.draw(g2);
        drawGround(g2);

        switch (currentState) {
            case MENU -> drawMenu(g2);
            case LOADING -> drawLoading(g2);
            case PLAYING -> {
                for (Pipe p : pipes) p.draw(g2, getHeight());
                bird.draw(g2);
                drawHUD(g2);
            }
            case GAMEOVER -> {
                for (Pipe p : pipes) p.draw(g2, getHeight());
                bird.draw(g2);
                drawGameOverOverlay(g2);
            }
            case LEADERBOARD -> drawLeaderboard(g2);
        }
    }

    private void drawSkyBackground(Graphics2D g) {
        GradientPaint gp = new GradientPaint(0, 0, new Color(15, 32, 67), 0, getHeight(), new Color(44, 83, 100));
        g.setPaint(gp);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawCelestialBodies(Graphics2D g) {
        int sunX = getWidth() - 150;
        g.setColor(new Color(255, 255, 200, 180));
        g.fillOval(sunX, 50, 80, 80);
        g.setColor(new Color(255, 255, 255, 40));
        for(int i = 1; i < 4; i++) {
            g.fillOval(sunX - (i*10), 50 - (i*10), 80 + (i*20), 80 + (i*20));
        }
    }

    private void drawGround(Graphics2D g) {
        g.setColor(new Color(20, 60, 20));
        g.fillRect(0, getHeight() - 50, getWidth(), 50);
        g.setColor(new Color(40, 120, 40));
        g.fillRect(0, getHeight() - 50, getWidth(), 5);
    }

    private void drawMenu(Graphics2D g) {
        int cx = getWidth() / 2;
        drawOverlay(g, 0.6f);
        drawShadowText(g, "FLAPPY PARROT", cx, 150, 80, Color.WHITE);
        drawShadowText(g, "ELITE PILOT: " + pilotName, cx, 210, 22, Color.YELLOW);
        drawModernButton(g, cx - 100, 280, 200, 50, "LAUNCH", new Color(39, 174, 96));
        drawModernButton(g, cx - 100, 340, 200, 50, "PILOT I.D.", new Color(41, 128, 185));
        drawModernButton(g, cx - 100, 400, 200, 50, "RECORDS", new Color(142, 68, 173));
        drawModernButton(g, cx - 100, 460, 200, 50, "TERMINATE", new Color(192, 57, 43));
        drawShadowText(g, "HI-SCORE: " + highscore, cx, getHeight() - 40, 18, Color.LIGHT_GRAY);
    }

    private void drawLoading(Graphics2D g) {
        int cx = getWidth() / 2;
        drawShadowText(g, "CALIBRATING ENGINES...", cx, 280, 25, Color.WHITE);
        g.setColor(new Color(255, 255, 255, 50));
        g.fillRoundRect(cx - 150, 310, 300, 15, 10, 10);
        g.setColor(new Color(46, 204, 113));
        g.fillRoundRect(cx - 150, 310, (int)(3.0 * loadProgress), 15, 10, 10);
    }

    private void drawHUD(Graphics2D g) {
        drawShadowText(g, "SCORE: " + score, 100, 60, 35, Color.WHITE);
        drawShadowText(g, "SPD: " + String.format("%.1f", gameSpeed), getWidth() - 100, 60, 20, Color.CYAN);
    }

    private void drawGameOverOverlay(Graphics2D g) {
        int cx = getWidth() / 2;
        drawOverlay(g, 0.8f);
        drawShadowText(g, "MISSION FAILED", cx, 240, 70, new Color(231, 76, 60));
        drawShadowText(g, "FINAL DATA RECOVERED: " + score, cx, 310, 28, Color.WHITE);
        drawShadowText(g, "PRESS SPACE TO RETURN TO BASE", cx, 400, 20, Color.LIGHT_GRAY);
    }

    private void drawLeaderboard(Graphics2D g) {
        int cx = getWidth() / 2;
        drawOverlay(g, 0.9f);
        drawShadowText(g, "GLOBAL RECORDS", cx, 100, 50, Color.YELLOW);
        g.setColor(new Color(255, 255, 255, 30));
        g.fillRoundRect(cx - 250, 140, 500, 320, 20, 20);
        for (int i = 0; i < Math.min(leaderboard.size(), 8); i++) {
            ScoreEntry se = leaderboard.get(i);
            Color rankColor = i == 0 ? new Color(255, 215, 0) : Color.WHITE;
            drawShadowText(g, (i + 1) + ". " + se.name, cx - 100, 180 + (i * 35), 24, rankColor);
            drawShadowText(g, String.valueOf(se.score), cx + 150, 180 + (i * 35), 24, rankColor);
        }
        drawShadowText(g, "PRESS ESC TO EXIT", cx, 520, 18, Color.GRAY);
    }

    private void drawOverlay(Graphics2D g, float alpha) {
        g.setColor(new Color(0, 0, 0, (int)(255 * alpha)));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawShadowText(Graphics2D g, String t, int x, int y, int s, Color c) {
        g.setFont(new Font("SansSerif", Font.BOLD, s));
        FontMetrics fm = g.getFontMetrics();
        int tx = x - fm.stringWidth(t) / 2;
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(t, tx + 3, y + 3);
        g.setColor(c);
        g.drawString(t, tx, y);
    }

    private void drawModernButton(Graphics2D g, int x, int y, int w, int h, String t, Color c) {
        g.setColor(c.darker().darker());
        g.fillRoundRect(x, y + 4, w, h, 12, 12);
        g.setColor(c);
        g.fillRoundRect(x, y, w, h, 12, 12);
        g.setColor(new Color(255, 255, 255, 100));
        g.drawRoundRect(x, y, w, h, 12, 12);
        drawShadowText(g, t, x + w / 2, y + h / 2 + 8, 18, Color.WHITE);
    }

    private void loadHighScores() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            leaderboard = (List<ScoreEntry>) ois.readObject();
            if (!leaderboard.isEmpty()) highscore = leaderboard.get(0).score;
        } catch (Exception e) {
            leaderboard = new ArrayList<>();
        }
    }

    private void saveScore(String name, int s) {
        leaderboard.add(new ScoreEntry(name, s));
        Collections.sort(leaderboard);
        if (leaderboard.size() > 10) leaderboard = leaderboard.subList(0, 10);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(leaderboard);
        } catch (Exception ignored) {}
    }
}

class Bird {
    private double x, y, vel;
    private double gravity = 0.5;
    private final double jumpPower = -9.0;
    private final int size = 36;
    private int animTicks = 0;

    public Bird(double x, double y) { this.x = x; this.y = y; }
    public void update() { vel += gravity; y += vel; animTicks++; }
    public void jump() { vel = jumpPower; }
    public void setGravity(double g) { this.gravity = g; }
    public void draw(Graphics2D g) {
        int ix = (int) x, iy = (int) y;
        double angle = Math.max(-0.5, Math.min(0.5, vel * 0.08));
        g.rotate(angle, ix, iy);
        g.setColor(new Color(150, 40, 40));
        int[] tx = {ix - 18, ix - 35, ix - 35, ix - 18};
        int[] ty = {iy, iy - 10, iy + 10, iy};
        g.fillPolygon(tx, ty, 4);
        g.setColor(new Color(231, 76, 60));
        g.fillOval(ix - 18, iy - 18, size, size - 4);
        g.setColor(new Color(52, 152, 219));
        int flap = (int)(Math.sin(animTicks * 0.4) * 10);
        g.fillOval(ix - 15, iy - 5 + flap, 22, 12);
        g.setColor(Color.WHITE);
        g.fillOval(ix + 6, iy - 12, 12, 12);
        g.setColor(Color.BLACK);
        g.fillOval(ix + 12, iy - 9, 5, 5);
        g.setColor(new Color(241, 196, 15));
        int[] bx = {ix + 15, ix + 30, ix + 15};
        int[] by = {iy - 4, iy + 6, iy + 14};
        g.fillPolygon(bx, by, 3);
        g.rotate(-angle, ix, iy);
    }
    public Rectangle getBounds() { return new Rectangle((int) x - 14, (int) y - 14, 28, 28); }
    public double getX() { return x; }
    public double getY() { return y; }
}

class Pipe {
    private int x, topHeight;
    private final int width = 85;
    private final int gap = 165;
    private boolean scored = false;
    public Pipe(int x) { this.x = x; this.topHeight = 80 + new Random().nextInt(240); }
    public void update(double speed) { x -= (int)speed; }
    public void draw(Graphics2D g, int h) {
        renderPipeSegment(g, x, 0, width, topHeight, true);
        renderPipeSegment(g, x, topHeight + gap, width, h - (topHeight + gap) - 50, false);
    }
    private void renderPipeSegment(Graphics2D g, int px, int py, int w, int ph, boolean isTop) {
        GradientPaint grad = new GradientPaint(px, 0, new Color(34, 139, 34), px + w, 0, new Color(144, 238, 144));
        g.setPaint(grad);
        g.fillRect(px, py, w, ph);
        g.setColor(new Color(0, 50, 0));
        g.setStroke(new BasicStroke(3));
        g.drawRect(px, py, w, ph);
        int lipY = isTop ? py + ph - 30 : py;
        g.setPaint(new GradientPaint(px - 5, 0, new Color(34, 139, 34), px + w + 5, 0, new Color(144, 238, 144)));
        g.fillRect(px - 5, lipY, w + 10, 30);
        g.setColor(new Color(0, 50, 0));
        g.drawRect(px - 5, lipY, w + 10, 30);
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(px + 15, py, 10, ph);
    }
    public Rectangle getTopBounds() { return new Rectangle(x, 0, width, topHeight); }
    public Rectangle getBottomBounds(int h) { return new Rectangle(x, topHeight + gap, width, h); }
    public int getX() { return x; }
    public boolean isScored() { return scored; }
    public void setScored(boolean b) { scored = b; }
}

class Cloud {
    private double x, y, speed;
    public Cloud(int x, int y) { this.x = x; this.y = y; this.speed = 0.4 + new Random().nextDouble(); }
    public void update(double s, int w) { x -= s * speed; if (x < -200) x = w + 100; }
    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 180));
        g.fillOval((int)x, (int)y, 80, 40);
        g.fillOval((int)x + 20, (int)y - 15, 50, 40);
    }
}

class Star {
    private double x, y;
    public Star(int x, int y) { this.x = x; this.y = y; }
    public void update(double s, int w) { x -= s; if (x < -10) x = w + 10; }
    public void draw(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.fillRect((int)x, (int)y, 2, 2);
    }
}

class ScoreEntry implements Serializable, Comparable<ScoreEntry> {
    private static final long serialVersionUID = 1L;
    String name; int score;
    public ScoreEntry(String n, int s) { this.name = n; this.score = s; }
    @Override public int compareTo(ScoreEntry o) { return Integer.compare(o.score, this.score); }
}