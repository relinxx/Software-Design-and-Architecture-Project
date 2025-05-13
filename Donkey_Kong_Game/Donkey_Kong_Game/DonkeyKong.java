import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class DonkeyKong extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Mario mario;
    private ArrayList<Barrel> barrels;
    private DonkeyKongCharacter donkeyKong;
    private ArrayList<Platform> platforms;
    private ArrayList<Ladder> ladders;
    private Banana princess;
    private int score;
    private boolean gameOver;
    private boolean gameWon;

    private BufferedImage marioImage, donkeyKongImage, barrelImage, platformImage, ladderImage, princessImage;

    public DonkeyKong() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);
        this.addKeyListener(this);

        loadImages();
        initGame();
    }

    private void loadImages() {
        try {
            marioImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("mario.png")));
            donkeyKongImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("donkey_kong.png")));
            barrelImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("barrel.png")));
            platformImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("platform.png")));
            ladderImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("ladder.png")));
            princessImage = ImageIO.read(Objects.requireNonNull(getClass().getResource("princess.png")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initGame() {
        mario = new Mario(50, 500);
        barrels = new ArrayList<>();
        donkeyKong = new DonkeyKongCharacter(80, 200);
        platforms = new ArrayList<>();
        ladders = new ArrayList<>();
        score = 0;
        gameOver = false;
        gameWon = false;

        // Initialize platforms
        platforms.add(new Platform(0, 500, 800, 20));
        platforms.add(new Platform(100, 400, 600, 20));
        platforms.add(new Platform(200, 300, 400, 20));
        platforms.add(new Platform(300, 200, 200, 20));

        // Add small platform under Donkey Kong
        platforms.add(new Platform(donkeyKong.getX(), donkeyKong.getY() + donkeyKong.getHeight(), donkeyKong.getWidth(), 20));

        // Initialize ladders
        ladders.add(new Ladder(150, 420, 20, 80));
        ladders.add(new Ladder(250, 320, 20, 80));
        ladders.add(new Ladder(350, 220, 20, 80));

        // Initialize princess
        princess = new Banana(350, 175);

        timer = new Timer(20, this);
        timer.start();
    }

    private void resetGame() {
        initGame();
        timer.restart();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver) {
            drawGameOver(g);
        } else if (gameWon) {
            drawGameWon(g);
        } else {
            drawObjects(g);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawObjects(Graphics g) {
        g.drawImage(marioImage, mario.getX(), mario.getY(), this);

        for (Barrel barrel : barrels) {
            g.drawImage(barrelImage, barrel.getX(), barrel.getY(), this);
        }

        g.drawImage(donkeyKongImage, donkeyKong.getX(), donkeyKong.getY(), this);

        // Draw platforms and ladders
        for (Platform platform : platforms) {
            g.drawImage(platformImage, platform.getX(), platform.getY(), platform.getWidth(), platform.getHeight(), this);
        }
        for (Ladder ladder : ladders) {
            g.drawImage(ladderImage, ladder.getX(), ladder.getY(), ladder.getWidth(), ladder.getHeight(), this);
        }

        // Draw Princess
        g.drawImage(princessImage, princess.getX(), princess.getY(), this);

        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20); // Adjusted y-coordinate for better visibility

        // Draw instructions
        g.drawString("Use arrow keys to move. Reach the princess to win!", 10, 40);
    }

    private void drawGameOver(Graphics g) {
        String message = "Game Over You Lost !";
        String scoreMessage = "Score: " + score;
        String retryMessage = "Press Space to Retry";
        Font font = new Font("Helvetica", Font.BOLD, 40);
        FontMetrics fm = getFontMetrics(font);

        g.setColor(Color.WHITE);
        g.setFont(font);
        g.drawString(message, (getWidth() - fm.stringWidth(message)) / 2, getHeight() / 2 - 50);
        g.drawString(scoreMessage, (getWidth() - fm.stringWidth(scoreMessage)) / 2, getHeight() / 2);
        g.drawString(retryMessage, (getWidth() - fm.stringWidth(retryMessage)) / 2, getHeight() / 2 + 50);
    }

    private void drawGameWon(Graphics g) {
        String message = "You Won!\n Saved the Princess!";
        String scoreMessage = "Score: " + score;
        String retryMessage = "Press Space to Restart";
        Font font = new Font("Helvetica", Font.BOLD, 40);
        FontMetrics fm = getFontMetrics(font);

        g.setColor(Color.YELLOW);
        g.setFont(font);
        g.drawString(message, (getWidth() - fm.stringWidth(message)) / 2, getHeight() / 2 - 50);
        g.drawString(scoreMessage, (getWidth() - fm.stringWidth(scoreMessage)) / 2, getHeight() / 2);
        g.drawString(retryMessage, (getWidth() - fm.stringWidth(retryMessage)) / 2, getHeight() / 2 + 50);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !gameWon) {
            mario.move();
            donkeyKong.move();
            for (Barrel barrel : barrels) {
                barrel.move();
            }
            checkCollisions();
            spawnBarrels();
            removeBarrels();
        }

        repaint();
    }

    private void removeBarrels() {
        // Remove barrels that fell off the screen
        barrels.removeIf(barrel -> barrel.getY() > getHeight());
    }

    private void checkCollisions() {
        Rectangle marioBounds = mario.getBounds();

        for (Barrel barrel : barrels) {
            if (marioBounds.intersects(barrel.getBounds())) {
                gameOver = true;
                timer.stop();
            }
        }

        // Check for winning condition
        if (marioBounds.intersects(princess.getBounds())) {
            score += 500; // Increment score when Mario touches the princess
            gameWon = true;
            timer.stop();
        }

        // Handle platform collisions
        boolean onPlatform = false;
        for (Platform platform : platforms) {
            if (marioBounds.intersects(platform.getBounds())) {
                onPlatform = true;
                mario.setY(platform.getY() - mario.getHeight());
            }
        }
        if (!onPlatform && !mario.isJumping() && !mario.isClimbing()) {
            mario.setY(mario.getY() + 2); // Gravity effect
        }

        // Handle ladder collisions for climbing
        boolean wasClimbing = mario.isClimbing();
        for (Ladder ladder : ladders) {
            if (marioBounds.intersects(ladder.getBounds())) {
                if (mario.isClimbing()) {
                    mario.setY(mario.getY() - 2); // Climb up
                } else if (mario.isJumping() && mario.getY() + mario.getHeight() > ladder.getY()) {
                    // Allow Mario to grab the ladder while jumping
                    mario.setClimbing(true);
                }
            }
        }

        if (wasClimbing && onPlatform && mario.isClimbing()) {
            score += 100; // Increment score when Mario climbs up and reaches a platform
        }
    }

    private void spawnBarrels() {
        Random random = new Random();
        for (Platform platform : platforms) {
            if (random.nextDouble() < 0.01) {
                long count = barrels.stream().filter(b -> b.getY() == platform.getY() - 20).count();
                if (count < 5) {
                    int barrelX = platform.getX();
                    int barrelY = platform.getY() - 20;
                    barrels.add(new Barrel(barrelX, barrelY));
                }
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            mario.setDx(-1);
        }

        if (key == KeyEvent.VK_RIGHT) {
            mario.setDx(1);
        }

        if (key == KeyEvent.VK_UP) {
            boolean onLadder = false;
            for (Ladder ladder : ladders) {
                if (mario.getBounds().intersects(ladder.getBounds())) {
                    mario.setClimbing(true);
                    onLadder = true;
                    break;
                }
            }
            if (!onLadder) {
                mario.jump();
            }
        }

        if (key == KeyEvent.VK_SPACE) {
            if (gameOver || gameWon) {
                resetGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            mario.setDx(0);
        }

        if (key == KeyEvent.VK_UP) {
            mario.setClimbing(false);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Donkey Kong");
        DonkeyKong game = new DonkeyKong();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    static class Mario {
        private int x, y;
        private int width, height;
        private int dx;
        private boolean jumping;
        private boolean climbing;
        private int jumpStrength;

        public Mario(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 20;
            this.height = 20;
            this.jumping = false;
            this.climbing = false;
            this.jumpStrength = 0;
        }

        public void move() {
            x += dx;

            if (jumping) {
                y -= jumpStrength;
                jumpStrength--;
                if (jumpStrength < -10) {
                    jumping = false;
                }
            }

            if (y > 540) {
                y = 540;
            }
        }

        public void jump() {
            if (!jumping && !climbing) {
                jumping = true;
                jumpStrength = 10;
            }
        }

        public void setDx(int dx) {
            this.dx = dx;
        }

        public void setClimbing(boolean climbing) {
            this.climbing = climbing;
        }

        public boolean isClimbing() {
            return climbing;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getHeight() {
            return height;
        }

        public boolean isJumping() {
            return jumping;
        }
    }

    class Barrel {
        private int x, y;
        private int width, height;
        private int dy;
        private int dx;

        public Barrel(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 20;
            this.height = 20;
            this.dy = 1;
            this.dx = 1;
        }

        public void move() {
            boolean onPlatform = false;

            // Check if the barrel is on a platform
            for (Platform platform : platforms) {
                if (getBounds().intersects(platform.getBounds())) {
                    onPlatform = true;
                    y = platform.getY() - height;
                    dy = 0; // Stop vertical movement
                    break;
                }
            }

            // If not on a platform, barrel falls
            if (!onPlatform) {
                y += dy; // Fall vertically
                dy = 2; // Apply gravity
            }

            // Move horizontally
            x += dx;

            // Check if the barrel reaches the end of the platform
            if (onPlatform) {
                boolean willFall = true;
                for (Platform platform : platforms) {
                    if (new Rectangle(x + dx, y + dy, width, height).intersects(platform.getBounds())) {
                        willFall = false;
                        break;
                    }
                }
                if (willFall) {
                    dy = 2; // Start falling
                }
            }
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }


    }

    static class DonkeyKongCharacter {
        private int x, y;
        private int width, height;

        public DonkeyKongCharacter(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 40;
            this.height = 40;
        }

        public void move() {
            // Donkey Kong doesn't move in this simple version
        }

        public int getX() {
            return x;
        }

        public int getY() { return y; }

        public int getWidth() { return width; }

        public int getHeight() { return height; }
    }

    static class Platform {
        private int x, y, width, height;

        public Platform(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    static class Ladder {
        private int x, y, width, height;

        public Ladder(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    static class Banana {
        private int x, y;
        private int width, height;

        public Banana(int x, int y) {
            this.x = x;
            this.y = y;
            this.width = 20;
            this.height = 20;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
