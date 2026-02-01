import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SimpleXO extends JFrame {

    // --- GAME VARIABLES ---
    char[] board = {' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' '};
    char current = 'X';
    JButton[] buttons = new JButton[9];

    // --- PANELS ---
    JPanel menuPanel, gamePanel, aboutPanel;

    public SimpleXO() {
        setTitle("XO Game");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new CardLayout());

        createMenu();
        createGame();
        createAbout();

        add(menuPanel, "Menu");
        add(gamePanel, "Game");
        add(aboutPanel, "About");

        showPanel("Menu"); // start with menu
        setResizable(true);
        setVisible(true);
    }

// --- MENU PANEL ---
private void createMenu() {
    menuPanel = new JPanel(new GridBagLayout());
    menuPanel.setBackground(Color.BLACK); // black background
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 0, 10, 0);
    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.NONE;

    // --- TITLE LABEL ABOVE PLAY BUTTON ---
    JLabel titleLabel = new JLabel("XOXO GAME");
    titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

    JButton playBtn = createMenuButton("Play Game");
    JButton aboutBtn = createMenuButton("About");
    JButton exitBtn = createMenuButton("Exit");

    // Actions
    playBtn.addActionListener(e -> {
        resetGame();
        showPanel("Game");
    });
    aboutBtn.addActionListener(e -> showPanel("About"));
    exitBtn.addActionListener(e -> System.exit(0));

    // Add to panel
    gbc.gridy = 0;
    menuPanel.add(titleLabel, gbc);   // Add title first
    gbc.gridy = 1;
    menuPanel.add(playBtn, gbc);
    gbc.gridy = 2;
    menuPanel.add(aboutBtn, gbc);
    gbc.gridy = 3;
    menuPanel.add(exitBtn, gbc);
}

    // --- ABOUT PANEL ---
    private void createAbout() {
        aboutPanel = new JPanel(new GridBagLayout());
        aboutPanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JLabel aboutLabel = new JLabel("<html><center>"
                + "XO Game<br><br>"
                + "Players take turns placing X and O.<br>"
                + "First to get 3 in a row wins!<br><br>"
                + "Enjoy playing!"
                + "</center></html>");
        aboutLabel.setHorizontalAlignment(SwingConstants.CENTER);
        aboutLabel.setForeground(Color.WHITE); // white text

        JButton backBtn = createMenuButton("Back to Menu");
        backBtn.addActionListener(e -> showPanel("Menu"));

        gbc.gridy = 0;
        aboutPanel.add(aboutLabel, gbc);
        gbc.gridy = 1;
        aboutPanel.add(backBtn, gbc);
    }

    // --- GAME PANEL ---
    private void createGame() {
        gamePanel = new JPanel(new GridBagLayout());
        gamePanel.setBackground(Color.BLACK);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);

        JPanel grid = new JPanel(new GridLayout(3, 3, 5, 5));
        grid.setPreferredSize(new Dimension(300, 300));
        grid.setMaximumSize(new Dimension(300, 300));
        grid.setMinimumSize(new Dimension(300, 300));
        grid.setBackground(Color.BLACK);

        for (int i = 0; i < 9; i++) {
            buttons[i] = new JButton(" ");
            buttons[i].setFont(new Font("Arial", Font.BOLD, 40));
            buttons[i].setPreferredSize(new Dimension(100, 100));
            buttons[i].setMaximumSize(new Dimension(100, 100));
            buttons[i].setMinimumSize(new Dimension(100, 100));
            buttons[i].setBackground(Color.DARK_GRAY); // dark button
            buttons[i].setForeground(Color.WHITE); // white X/O
            final int idx = i;
            buttons[i].addActionListener(e -> makeMove(idx));
            grid.add(buttons[i]);
        }

        JButton backBtn = createMenuButton("Back to Menu");
        backBtn.addActionListener(e -> showPanel("Menu"));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gamePanel.add(grid, gbc);
        gbc.gridy = 1;
        gamePanel.add(backBtn, gbc);
    }

    // --- CREATE BUTTONS (BLACK THEME) ---
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(200, 50));
        btn.setBackground(Color.DARK_GRAY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }

    // --- PANEL SWITCHING ---
    private void showPanel(String name) {
        CardLayout cl = (CardLayout) getContentPane().getLayout();
        cl.show(getContentPane(), name);
    }

    // --- RESET BOARD ---
    private void resetGame() {
        current = 'X';
        for (int i = 0; i < 9; i++) {
            board[i] = ' ';
            buttons[i].setText(" ");
            buttons[i].setEnabled(true);
        }
    }

    // --- MAKE MOVE ---
    private void makeMove(int idx) {
        if (board[idx] == ' ') {
            board[idx] = current;
            buttons[idx].setText(String.valueOf(current));

            if (checkWin(current)) {
                JOptionPane.showMessageDialog(this, "Player " + current + " wins!");
                disableButtons();
                return;
            }

            if (isFull()) {
                JOptionPane.showMessageDialog(this, "Draw!");
                disableButtons();
                return;
            }

            current = (current == 'X') ? 'O' : 'X';
        }
    }

    // --- CHECK WIN CONDITION ---
    private boolean checkWin(char p) {
        int[][] wins = {
                {0,1,2}, {3,4,5}, {6,7,8},
                {0,3,6}, {1,4,7}, {2,5,8},
                {0,4,8}, {2,4,6}
        };
        for (int[] w : wins)
            if (board[w[0]]==p && board[w[1]]==p && board[w[2]]==p)
                return true;
        return false;
    }

    private boolean isFull() {
        for (char c : board) if (c==' ') return false;
        return true;
    }

    private void disableButtons() {
        for (JButton b : buttons) b.setEnabled(false);
    }

    public static void main(String[] args) {
        new SimpleXO();
    }
}
