import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.net.*;
import java.io.*;
import java.util.Scanner;

// 400x400 window with a simple ControllableSquareSprite that can be moved by the user via arrow keys
public class BoxFighter extends JFrame implements KeyListener {
    public static InetAddress host;
    public static int PORT = 26189;
    public static Socket socket = null;
    public static PrintWriter networkOutput;
    
    private long startTime = System.nanoTime();
    private long estimatedTime;
    
                                    //WHY IS GAMESPEED SYSTEM.DEPENDENT???
    private static final double DELAY = 0;        // number of ms delay between frames
    private ControllableBox playerSprite;  // sprite controlled by the player/user. Receives key input

    public BoxFighter() {
        //Scanner userInput = new Scanner(System.in);
        
        //System.out.println("Please enter port number");
        //PORT = Integer.parseInt(userInput.nextLine());
        //Connection Setup
        
        try
        {
            host = InetAddress.getLocalHost();
            System.out.println(host.toString());
        }
        catch(UnknownHostException uhEx)
        {
            System.out.println("\nHost ID not found!\n");
            System.exit(1);
        }
        
        String playerName = "Player 1";
        String opponentName = "Player 2";
        
        try
        {   
            socket = new Socket(host,PORT);
            networkOutput = new PrintWriter(socket.getOutputStream(),true);
            Scanner networkInput = new Scanner(socket.getInputStream());
            playerName = networkInput.nextLine();
        }
        catch(Exception e)
            {
                System.out.println(
                "Unable to disconnect!");
                System.exit(1);
            }
            
        //  initialize spawnpoints
        float player1Spawn = 175;
        float player2Spawn = 1025;
        
        if (playerName.equals("Player 2")){
            opponentName = "Player 1";
        }
        
        System.out.println(playerName + " and " + opponentName);
        
        // window set-up
        setSize(new Dimension(1230, 630));
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("At least it's better than Street Fighter 1");
        setVisible(true);

        // IMPORTANT!! register a listener to this JFrame window
        addKeyListener(this);
        
        // init ControllableSquareSprite at (175, 175) with height 200, width 50 and color Cyan. Set screen bounds to 400px * 400px
        playerSprite = new ControllableBox(player1Spawn, 400, 50, 200, Color.CYAN, Color.GREEN, 1200, 600, playerName, opponentName, player2Spawn);
        

        // set up an ActionListener to invalidate the window every DELAY milliseconds

        while (true){
            estimatedTime = System.nanoTime() - startTime;
            if (estimatedTime >= 16700000){
                playerSprite.update();  // have player-controlled sprite update itself
                startTime = System.nanoTime();
                repaint();  // repaint the screen
                startTime = System.nanoTime();
            }
        }
    }

    // refreshes the screen by painting over it, then draws the playerSprite
    @Override
    public void paint(Graphics g) {
        //g.setColor(Color.WHITE);
        //g.fillRect(0, 0, 800, 800);
        // have the playerSprite draw itself to the window
        try{
            playerSprite.draw((Graphics2D) g);
        }
        catch(Exception e){}
    }

    @Override  // handle keyTyped action: we can ignore it, but still need it for the interface
    public void keyTyped(KeyEvent e) {

    }

    @Override  // handle keyPressed action: send to playerSprite
    public void keyPressed(KeyEvent e) {
        //System.out.println("Key Pressed");
        try{
        playerSprite.handleKeyPressed(e);
        }
        catch(IOException ioEx)
            {
                System.out.println(
                "Unable to disconnect!");
                System.exit(1);
            }
    }

    @Override  // handle keyReleased action: send to playerSprite
    public void keyReleased(KeyEvent e) {
        //System.out.println("Key Released");
        try{
        playerSprite.handleKeyReleased(e);
        }
        catch(IOException ioEx)
            {
                System.out.println(
                "Unable to disconnect!");
                System.exit(1);
            }
    }
}
