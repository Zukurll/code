import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class FlappyBird {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Flappy Bird");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setResizable(false);
            
            GamePanel gamePanel = new GamePanel();
            frame.add(gamePanel);
            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
        });
    }
}

class GamePanel extends JPanel {
    private Bird bird;
    private ArrayList<Pipe> pipes;
    private int score = 0;
    private boolean gameOver = false;
    private boolean gameStarted = false;
    private boolean inMenu = true;
    private int pipeSpacing = 300;
    private int pipeGap = 150;
    private ArrayList<Integer> cloudXs = new ArrayList<>();
    private int cloudSpeed = 1;
    
    private Timer gameTimer;
    
    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(135, 206, 235));
        setFocusable(true);
        
        bird = new Bird(100, 250);
        pipes = new ArrayList<>();
        
        for (int i = 0; i < 4; i++) {
            pipes.add(new Pipe(800 + i * pipeSpacing, pipeGap));
        }
        
        cloudXs = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cloudXs.add(i * 200 - 100);
        }
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (inMenu) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        startGame();
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                } else if (gameStarted) {
                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                        if (gameOver) {
                            goToMenu();
                        } else {
                            bird.jump();
                        }
                    } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        goToMenu();
                    }
                }
            }
        });
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (inMenu) {
                    int mx = e.getX();
                    int my = e.getY();
                    if (mx >= getWidth()/2 - 100 && mx <= getWidth()/2 + 100 && my >= getHeight()/2 + 50 && my <= getHeight()/2 + 100) {
                        startGame();
                    } else if (mx >= getWidth()/2 - 100 && mx <= getWidth()/2 + 100 && my >= getHeight()/2 + 120 && my <= getHeight()/2 + 170) {
                        System.exit(0);
                    }
                } else if (gameStarted) {
                    if (gameOver) {
                        goToMenu();
                    } else {
                        bird.jump();
                    }
                }
            }
        });
    }
    
    private void startGame() {
        inMenu = false;
        gameStarted = true;
        gameOver = false;
        score = 0;
        bird = new Bird(100, 250);
        pipes.clear();
        
        for (int i = 0; i < 4; i++) {
            pipes.add(new Pipe(800 + i * pipeSpacing, pipeGap));
        }
        
        gameTimer = new Timer(16, e -> {
            if (!gameOver) {
                update();
            }
            repaint();
        });
        gameTimer.start();
    }
    
    private void goToMenu() {
        gameStarted = false;
        gameOver = false;
        inMenu = true;
        if (gameTimer != null) {
            gameTimer.stop();
        }
        repaint();
    }
    
    private void update() {
        // Update clouds
        for (int i = 0; i < cloudXs.size(); i++) {
            cloudXs.set(i, cloudXs.get(i) - cloudSpeed);
        }
        if (cloudXs.get(0) < -200) {
            cloudXs.remove(0);
            cloudXs.add(getWidth() + 100);
        }
        
        bird.update();
        
        for (Pipe pipe : pipes) {
            pipe.update();
        }
        
        for (Pipe pipe : pipes) {
            if (bird.collidesWith(pipe, getHeight())) {
                gameOver = true;
                gameTimer.stop();
            }
        }
        
        if (bird.getY() > getHeight() - 50 || bird.getY() < 0) {
            gameOver = true;
            gameTimer.stop();
        }
        
        for (Pipe pipe : pipes) {
            if (!pipe.isScored() && pipe.getX() + pipe.getWidth() < bird.getX()) {
                pipe.setScored(true);
                score++;
            }
        }
        
        if (pipes.get(pipes.size() - 1).getX() < getWidth() - pipeSpacing) {
            pipes.add(new Pipe(getWidth() + 100, pipeGap));
        }
        
        pipes.removeIf(pipe -> pipe.getX() < -100);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        GradientPaint skyPaint = new GradientPaint(0, 0, new Color(135, 206, 235), 0, getHeight(), new Color(180, 225, 255));
        g2d.setPaint(skyPaint);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw clouds
        g2d.setColor(new Color(255, 255, 255, 180));
        for (int cx : cloudXs) {
            g2d.fillOval(cx, 60, 80, 40);
            g2d.fillOval(cx + 20, 50, 60, 50);
            g2d.fillOval(cx + 40, 55, 70, 45);
        }

        int sunSize = 100;
        int sunX = getWidth() - sunSize - 40;
        int sunY = 40;
        g2d.setColor(new Color(255, 235, 120));
        g2d.fillOval(sunX, sunY, sunSize, sunSize);
        g2d.setColor(new Color(255, 255, 200, 100));
        g2d.fillOval(sunX - 20, sunY - 20, sunSize + 40, sunSize + 40);
        // Sun rays
        g2d.setColor(new Color(255, 255, 150, 150));
        for (int i = 0; i < 12; i++) {
            double angle = Math.PI * 2 * i / 12;
            int rx = sunX + sunSize/2 + (int)(Math.cos(angle) * (sunSize/2 + 20));
            int ry = sunY + sunSize/2 + (int)(Math.sin(angle) * (sunSize/2 + 20));
            g2d.drawLine(sunX + sunSize/2, sunY + sunSize/2, rx, ry);
        }

        // Triangular mountains with layering
        // Back mountains (lighter)
        Polygon backMountain1 = new Polygon(new int[] { -50, 150, 350 }, new int[] { getHeight() - 50, 250, getHeight() - 50 }, 3);
        g2d.setColor(new Color(180, 200, 220));
        g2d.fillPolygon(backMountain1);
        Polygon backMountain2 = new Polygon(new int[] { 550, 750, 950 }, new int[] { getHeight() - 50, 280, getHeight() - 50 }, 3);
        g2d.setColor(new Color(190, 210, 230));
        g2d.fillPolygon(backMountain2);
        // Front mountains (darker)
        Polygon mountain1 = new Polygon(new int[] { 100, 300, 500 }, new int[] { getHeight() - 50, 200, getHeight() - 50 }, 3);
        g2d.setColor(new Color(120, 140, 160));
        g2d.fillPolygon(mountain1);
        Polygon mountain2 = new Polygon(new int[] { 700, 900, 1100 }, new int[] { getHeight() - 50, 220, getHeight() - 50 }, 3);
        g2d.setColor(new Color(140, 160, 180));
        g2d.fillPolygon(mountain2);
        // Small mountain in front
        Polygon smallMountain = new Polygon(new int[] { 400, 450, 500 }, new int[] { getHeight() - 50, 300, getHeight() - 50 }, 3);
        g2d.setColor(new Color(100, 120, 140));
        g2d.fillPolygon(smallMountain);

        // Pine trees
        // Tree 1
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(420, getHeight() - 100, 14, 50);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 400, 427, 454 }, new int[] { getHeight() - 100, getHeight() - 130, getHeight() - 100 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 405, 427, 449 }, new int[] { getHeight() - 110, getHeight() - 140, getHeight() - 110 }, 3);
        g2d.setColor(new Color(60, 160, 60));
        g2d.fillPolygon(new int[] { 410, 427, 444 }, new int[] { getHeight() - 120, getHeight() - 150, getHeight() - 120 }, 3);
        // Tree 2
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(680, getHeight() - 110, 16, 55);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 660, 688, 716 }, new int[] { getHeight() - 110, getHeight() - 140, getHeight() - 110 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 665, 688, 711 }, new int[] { getHeight() - 120, getHeight() - 150, getHeight() - 120 }, 3);
        g2d.setColor(new Color(60, 160, 60));
        g2d.fillPolygon(new int[] { 670, 688, 706 }, new int[] { getHeight() - 130, getHeight() - 160, getHeight() - 130 }, 3);
        // Additional trees
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(120, getHeight() - 90, 12, 40);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 100, 126, 152 }, new int[] { getHeight() - 90, getHeight() - 110, getHeight() - 90 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 105, 126, 147 }, new int[] { getHeight() - 100, getHeight() - 120, getHeight() - 100 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(550, getHeight() - 95, 15, 45);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 530, 558, 585 }, new int[] { getHeight() - 95, getHeight() - 115, getHeight() - 95 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 535, 558, 580 }, new int[] { getHeight() - 105, getHeight() - 125, getHeight() - 105 }, 3);
        // More trees
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(50, getHeight() - 85, 10, 35);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 35, 55, 75 }, new int[] { getHeight() - 85, getHeight() - 105, getHeight() - 85 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 40, 55, 70 }, new int[] { getHeight() - 95, getHeight() - 115, getHeight() - 95 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(300, getHeight() - 100, 14, 50);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 280, 307, 334 }, new int[] { getHeight() - 100, getHeight() - 130, getHeight() - 100 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 285, 307, 329 }, new int[] { getHeight() - 110, getHeight() - 140, getHeight() - 110 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(750, getHeight() - 105, 16, 55);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 730, 758, 786 }, new int[] { getHeight() - 105, getHeight() - 135, getHeight() - 105 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 735, 758, 781 }, new int[] { getHeight() - 115, getHeight() - 145, getHeight() - 115 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(950, getHeight() - 90, 12, 40);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 930, 956, 982 }, new int[] { getHeight() - 90, getHeight() - 110, getHeight() - 90 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 935, 956, 977 }, new int[] { getHeight() - 100, getHeight() - 120, getHeight() - 100 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(1100, getHeight() - 95, 15, 45);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 1080, 1108, 1135 }, new int[] { getHeight() - 95, getHeight() - 115, getHeight() - 95 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 1085, 1108, 1130 }, new int[] { getHeight() - 105, getHeight() - 125, getHeight() - 105 }, 3);
        // Even more trees
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(0, getHeight() - 80, 10, 30);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { -15, 5, 25 }, new int[] { getHeight() - 80, getHeight() - 100, getHeight() - 80 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { -10, 5, 20 }, new int[] { getHeight() - 90, getHeight() - 110, getHeight() - 90 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(200, getHeight() - 85, 12, 35);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 180, 206, 232 }, new int[] { getHeight() - 85, getHeight() - 105, getHeight() - 85 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 185, 206, 227 }, new int[] { getHeight() - 95, getHeight() - 115, getHeight() - 95 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(600, getHeight() - 100, 14, 50);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 580, 607, 634 }, new int[] { getHeight() - 100, getHeight() - 130, getHeight() - 100 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 585, 607, 629 }, new int[] { getHeight() - 110, getHeight() - 140, getHeight() - 110 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(800, getHeight() - 90, 13, 40);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 780, 807, 834 }, new int[] { getHeight() - 90, getHeight() - 110, getHeight() - 90 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 785, 807, 829 }, new int[] { getHeight() - 100, getHeight() - 120, getHeight() - 100 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(1000, getHeight() - 95, 15, 45);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 980, 1008, 1035 }, new int[] { getHeight() - 95, getHeight() - 115, getHeight() - 95 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 985, 1008, 1030 }, new int[] { getHeight() - 105, getHeight() - 125, getHeight() - 105 }, 3);
        g2d.setColor(new Color(110, 70, 40));
        g2d.fillRect(1200, getHeight() - 85, 12, 35);
        g2d.setColor(new Color(20, 100, 20));
        g2d.fillPolygon(new int[] { 1180, 1206, 1232 }, new int[] { getHeight() - 85, getHeight() - 105, getHeight() - 85 }, 3);
        g2d.setColor(new Color(40, 140, 40));
        g2d.fillPolygon(new int[] { 1185, 1206, 1227 }, new int[] { getHeight() - 95, getHeight() - 115, getHeight() - 95 }, 3);

        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(0, getHeight() - 50, getWidth(), 50);
        g2d.setColor(new Color(0, 0, 0));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, getHeight() - 50, getWidth(), getHeight() - 50);
        
        for (Pipe pipe : pipes) {
            pipe.draw(g2d, getHeight());
        }
        
        bird.draw(g2d);
        
        g2d.setColor(new Color(0, 0, 0));
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        g2d.drawString("Score: " + score, 20, 50);
        
        if (inMenu) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setColor(new Color(255, 255, 255));
            g2d.setFont(new Font("Arial", Font.BOLD, 80));
            String titleText = "FLAPPY BIRD";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(titleText)) / 2;
            g2d.drawString(titleText, x, getHeight() / 2 - 100);
            
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            String playText = "PLAY";
            fm = g2d.getFontMetrics();
            x = (getWidth() - fm.stringWidth(playText)) / 2;
            g2d.setColor(new Color(0, 200, 0));
            g2d.fillRect(x - 30, getHeight() / 2 + 20, fm.stringWidth(playText) + 60, 60);
            g2d.setColor(new Color(0, 0, 0));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRect(x - 30, getHeight() / 2 + 20, fm.stringWidth(playText) + 60, 60);
            g2d.setColor(new Color(255, 255, 255));
            g2d.drawString(playText, x, getHeight() / 2 + 70);
            
            String quitText = "QUIT";
            fm = g2d.getFontMetrics();
            x = (getWidth() - fm.stringWidth(quitText)) / 2;
            g2d.setColor(new Color(200, 0, 0));
            g2d.fillRect(x - 30, getHeight() / 2 + 100, fm.stringWidth(quitText) + 60, 60);
            g2d.setColor(new Color(0, 0, 0));
            g2d.drawRect(x - 30, getHeight() / 2 + 100, fm.stringWidth(quitText) + 60, 60);
            g2d.setColor(new Color(255, 255, 255));
            g2d.drawString(quitText, x, getHeight() / 2 + 150);
        }
        
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setColor(new Color(255, 0, 0));
            g2d.setFont(new Font("Arial", Font.BOLD, 60));
            String gameOverText = "GAME OVER";
            FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(gameOverText)) / 2;
            g2d.drawString(gameOverText, x, getHeight() / 2 - 80);
            
            g2d.setColor(new Color(255, 255, 255));
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            String scoreText = "Final Score: " + score;
            fm = g2d.getFontMetrics();
            x = (getWidth() - fm.stringWidth(scoreText)) / 2;
            g2d.drawString(scoreText, x, getHeight() / 2 + 30);
            
            g2d.setFont(new Font("Arial", Font.BOLD, 25));
            String restartText = "Click or Press SPACE to Menu";
            fm = g2d.getFontMetrics();
            x = (getWidth() - fm.stringWidth(restartText)) / 2;
            g2d.drawString(restartText, x, getHeight() / 2 + 100);
        }
    }
}

class Bird {
    private double x, y;
    private double velocity = 0;
    private double gravity = 0.6;
    private double jumpPower = -8;
    private int size = 30;
    private int flapCounter = 0;
    
    public Bird(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void update() {
        velocity += gravity;
        y += velocity;
        
        if (velocity > 12) {
            velocity = 12;
        }
        
        flapCounter++;
    }
    
    public void jump() {
        velocity = jumpPower;
        flapCounter = 0;
    }
    
    public boolean collidesWith(Pipe pipe, int panelHeight) {
        Rectangle birdRect = new Rectangle((int)x - size/2, (int)y - size/2, size, size);
        
        Rectangle topPipeRect = new Rectangle(pipe.getX(), 0, pipe.getWidth(), pipe.getTopHeight());
        if (birdRect.intersects(topPipeRect)) {
            return true;
        }
        
        Rectangle bottomPipeRect = new Rectangle(pipe.getX(), pipe.getBottomY(), pipe.getWidth(), panelHeight - pipe.getBottomY());
        if (birdRect.intersects(bottomPipeRect)) {
            return true;
        }
        
        return false;
    }
    
    public void draw(Graphics2D g2d) {
        int bx = (int) x;
        int by = (int) y;
        g2d.setStroke(new BasicStroke(2));
        
        // Body with gradient
        g2d.setColor(new Color(255, 210, 0));
        g2d.fillOval(bx - size/2, by - size/2, size, size);
        g2d.setColor(new Color(255, 170, 0));
        g2d.fillOval(bx - size/2 + 4, by - size/2 + 4, size - 8, size - 8);
        g2d.setColor(new Color(255, 130, 0));
        g2d.fillOval(bx - size/2 + 8, by - size/2 + 8, size - 16, size - 16);
        
        // Head
        g2d.setColor(new Color(255, 190, 40));
        g2d.fillOval(bx - 8, by - 6, 20, 16);
        g2d.setColor(new Color(255, 150, 20));
        g2d.fillOval(bx - 6, by - 4, 16, 12);
        
        // Beak
        g2d.setColor(new Color(255, 145, 0));
        int[] beakX = { bx + size/2, bx + size/2 + 12, bx + size/2 };
        int[] beakY = { by - 4, by, by + 4 };
        g2d.fillPolygon(beakX, beakY, 3);
        g2d.setColor(new Color(255, 100, 0));
        g2d.drawPolygon(beakX, beakY, 3);
        
        // Tail
        g2d.setColor(new Color(255, 170, 0));
        int[] tailX = { bx - size/2, bx - size/2 - 12, bx - size/2 - 6 };
        int[] tailY = { by, by - 6, by + 8 };
        g2d.fillPolygon(tailX, tailY, 3);
        g2d.setColor(new Color(255, 130, 0));
        g2d.drawPolygon(tailX, tailY, 3);
        
        // Eye
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillOval(bx + 4, by - 10, 8, 8);
        g2d.setColor(new Color(255, 255, 255));
        g2d.fillOval(bx + 7, by - 8, 4, 4);
        g2d.setColor(new Color(0, 0, 0));
        g2d.fillOval(bx + 8, by - 7, 2, 2);
        
        // Wings with feathers
        g2d.setColor(new Color(220, 140, 0));
        if (flapCounter % 10 < 5) {
            g2d.fillArc(bx - 20, by - 10, 18, 18, 0, 180);
            // Feather lines
            g2d.setColor(new Color(200, 120, 0));
            g2d.drawLine(bx - 11, by - 1, bx - 20, by - 10);
            g2d.drawLine(bx - 11, by - 1, bx - 15, by - 15);
        } else {
            g2d.fillArc(bx - 20, by - 8, 18, 12, 180, 180);
            // Feather lines
            g2d.setColor(new Color(200, 120, 0));
            g2d.drawLine(bx - 11, by - 2, bx - 20, by - 8);
            g2d.drawLine(bx - 11, by - 2, bx - 15, by - 12);
        }
    }
    
    public double getY() {
        return y;
    }
    
    public double getX() {
        return x;
    }
}

class Pipe {
    private int x;
    private int topHeight;
    private int width = 80;
    private int gapSize;
    private boolean scored = false;
    private int speed = 5;
    
    public Pipe(int x, int gapSize) {
        this.x = x;
        this.gapSize = gapSize;
        this.topHeight = 100 + new Random().nextInt(150);
    }
    
    public void update() {
        x -= speed;
    }
    
    public void draw(Graphics2D g2d, int panelHeight) {
        int bottomY = topHeight + gapSize;

        // Top pipe body
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(x, 0, width, topHeight);
        g2d.setColor(new Color(0, 100, 0));
        g2d.setStroke(new BasicStroke(4));
        g2d.drawRect(x, 0, width, topHeight);
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillRect(x + 10, 10, width / 4, Math.max(1, topHeight - 20));
        g2d.fillRect(x + width - width/4 - 10, 10, width / 4, Math.max(1, topHeight - 20));
        g2d.setColor(new Color(0, 80, 0));
        for (int y = 20; y < topHeight - 10; y += 20) {
            g2d.drawLine(x + 10, y, x + width - 10, y);
        }
        g2d.setColor(new Color(40, 110, 40));
        g2d.fillRoundRect(x - 4, topHeight - 14, width + 8, 24, 12, 12);
        g2d.setColor(new Color(0, 80, 0));
        g2d.drawRoundRect(x - 4, topHeight - 14, width + 8, 24, 12, 12);
        // Additional shadow
        g2d.setColor(new Color(20, 80, 20, 100));
        g2d.fillRect(x + width - 20, 10, 10, topHeight - 20);

        // Bottom pipe body
        g2d.setColor(new Color(34, 139, 34));
        g2d.fillRect(x, bottomY, width, panelHeight - bottomY);
        g2d.setColor(new Color(0, 100, 0));
        g2d.drawRect(x, bottomY, width, panelHeight - bottomY);
        g2d.setColor(new Color(255, 255, 255, 80));
        g2d.fillRect(x + 10, bottomY + 10, width / 4, Math.max(1, panelHeight - bottomY - 20));
        g2d.fillRect(x + width - width/4 - 10, bottomY + 10, width / 4, Math.max(1, panelHeight - bottomY - 20));
        g2d.setColor(new Color(0, 80, 0));
        for (int y = bottomY + 20; y < panelHeight - 10; y += 20) {
            g2d.drawLine(x + 10, y, x + width - 10, y);
        }
        g2d.setColor(new Color(40, 110, 40));
        g2d.fillRoundRect(x - 4, bottomY - 14, width + 8, 24, 12, 12);
        g2d.setColor(new Color(0, 80, 0));
        g2d.drawRoundRect(x - 4, bottomY - 14, width + 8, 24, 12, 12);
        // Additional shadow
        g2d.setColor(new Color(20, 80, 20, 100));
        g2d.fillRect(x + width - 20, bottomY + 10, 10, panelHeight - bottomY - 20);
    }
    
    public int getX() {
        return x;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getTopHeight() {
        return topHeight;
    }
    
    public int getBottomY() {
        return topHeight + gapSize;
    }
    
    public boolean isScored() {
        return scored;
    }
    
    public void setScored(boolean scored) {
        this.scored = scored;
    }
}