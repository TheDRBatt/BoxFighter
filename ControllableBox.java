import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.EnumMap;
import java.util.Map;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.*;
import javax.swing.*;

// A square with variable size and fill color that can be controlled by arrow keys via
// handleKeyPressed() and handleKeyReleased(). Cannot go past window bounds defined by
// screenW and screenH. Changes to random color when colliding with another sprite.
public class ControllableBox    implements Runnable{

    private Thread outputThread, inputThread;

    public boolean[] otherPlayerInput= {false, false, false, false, false};
    // height (px)
    private int height;
    // width (px)
    private int width;
    // color to fill square
    private Color color;
    // width and height of the window the Square is on (pixels). Square cannot go outside this
    private int screenW, screenH;
    // pixels per frame the Square should move along the x-axis and y-axis, respectively
    private float speedX = 12.0f;
    // whether or not the attack key is pressed
    private float swap;
    private int lagCompensation = 0;
    private int updateTicks = 1;
    private long startTime;
    private long estimatedTime;
    public PrintWriter networkOutput = BoxFighter.networkOutput;
    public JFrame frame;
    public JLabel textLbl;
    public Panel panel;
    public float playerEx;
    public float opponentEx;
    public static int drawClock = -2;
    public boolean colorFlip = true;
    //private long startTime = System.nanoTime();
    //private long estimatedTime;
    
    Sprite ControlledPlayer = new Sprite("unassigned", 1, 1, 1, 1, Color.CYAN, new Hitbox(1, 1, 1, 1));
    Sprite Opponent = new Sprite("unassigned", 1, 1, 1, 1, Color.GREEN, new Hitbox(1, 1, 1, 1));

    public ControllableBox(float p1spawn, float y, int width, int height, Color p1color, Color p2color ,int screenW, int screenH, String playerName, String opponentName, float p2spawn) {
        //super(x, y, new Hitbox(x, y, width, height));
        this.height = height;
        this.width = width;
        this.color = color;
        this.screenW = screenW;
        this.screenH = screenH;
        
        if (playerName.equals("Player 2")){
            ControlledPlayer = new Sprite(playerName, p2spawn, y, width, height, p2color, new Hitbox(p2spawn, y, width, height));
            Opponent = new Sprite(opponentName, p1spawn, y, width, height, p1color, new Hitbox((float)p1spawn, y, width, height));
            ControlledPlayer.attackHitbox = new Hitbox(1, 1, 1, 1);
            ControlledPlayer.hurtboxExtension = new Hitbox(p2spawn, y, width, height);        //Created duplication of hitbox that is reassigned 
                                                                    //during an attack            
            Opponent.attackHitbox = new Hitbox(3, 1, 1, 1);
            Opponent.hurtboxExtension = new Hitbox((float)p1spawn, y, width, height); 
        }
        else if (playerName.equals("Player 1")){
            //Hitbox hurtbox = new Hitbox(x, y, width, height);  // initialize Hitbox with same dimensions
            ControlledPlayer = new Sprite(playerName, p1spawn, y, width, height, p1color, new Hitbox(p1spawn, y, width, height));
            Opponent = new Sprite(opponentName, p2spawn, y, width, height, p2color, new Hitbox((float)p2spawn, y, width, height));
            ControlledPlayer.attackHitbox = new Hitbox(1, 1, 1, 1);
            ControlledPlayer.hurtboxExtension = new Hitbox(p1spawn, y, width, height);        //Created duplication of hitbox that is reassigned 
                                                                        //during an attack            
            Opponent.attackHitbox = new Hitbox(3, 1, 1, 1);
            Opponent.hurtboxExtension = new Hitbox((float)p2spawn, y, width, height);
        }
        outputThread = new Thread(this);
        inputThread = new Thread(this);
        outputThread.start();
        inputThread.start();
    }

    // handles a KeyEvent for a key pressed. sets speedX and speedY accordingly
    public void handleKeyPressed(KeyEvent e) throws IOException{
        ControlledPlayer.handleKeyPressed(e);
    }

    // handles a KeyEvent for a key released. sets speedX and speedY accordingly
    public void handleKeyReleased(KeyEvent e) throws IOException{
        ControlledPlayer.handleKeyReleased(e);
    }

    public void run(){          //Networking, baby!
        long sendKeyStartTime = System.nanoTime();
        long updateInputStartTime = System.nanoTime();
        if (Thread.currentThread().getName().equals("Thread-0"))
            while (true){
                if ((System.nanoTime() - sendKeyStartTime) > 1000000){
                    sendKeyStartTime = System.nanoTime();
                    try {
                        ControlledPlayer.sendKeyState();
                    }
                    catch(IOException ioe){
                        System.out.println("outPutThread ioError");
                        };
                    }
                }
        else
            while (true) {
                if ((System.nanoTime() - updateInputStartTime) > 1000000){
                    updateInputStartTime = System.nanoTime();
                    Opponent.updateInputs();
                }
            }
    }

    //@Override
    void update() {     //this is the game logic done every frame
        //System.out.println("1: " + (System.nanoTime() - startTime));
        startTime = System.nanoTime();
        enforceOneHit();

        startTime = System.nanoTime();
        
        playerEx = ControlledPlayer.getX();
        opponentEx = Opponent.getX();
        
                
        ControlledPlayer.handleEverything(speedX, screenW, screenH, Opponent.getX());
        Opponent.handleEverything(speedX, screenW, screenH, ControlledPlayer.getX());
        
        hitOpponentCheck();
        }

    
    void enforceOneHit(){
        if (ControlledPlayer.checkHitState() == true)
            if (Opponent.checkInAction() == false)
                ControlledPlayer.setHitState(false);
        if (Opponent.checkHitState() == true)
            if (ControlledPlayer.checkInAction() == false)
                Opponent.setHitState(false);
    }
    
    void hitOpponentCheck(){
        if ((ControlledPlayer.attackHitbox.intersects(Opponent.hurtbox) || ControlledPlayer.attackHitbox.intersects(Opponent.hurtboxExtension)) && ControlledPlayer.hitboxActive == true && Opponent.checkHitState() == false){
            Opponent.gotHit(ControlledPlayer.getAttackDamage(), ControlledPlayer.getStunFrames(), ControlledPlayer.getAttackProperty());
        }
        if ((Opponent.attackHitbox.intersects(ControlledPlayer.hurtbox) || Opponent.attackHitbox.intersects(ControlledPlayer.hurtboxExtension)) && Opponent.hitboxActive == true && ControlledPlayer.checkHitState() == false){
            ControlledPlayer.gotHit(Opponent.getAttackDamage(), Opponent.getStunFrames(), Opponent.getAttackProperty());
        }
    }
    
    
    void healthBar(Graphics2D g){
        ControlledPlayer.drawHealthBar(g);
        Opponent.drawHealthBar(g);
    }
    
    //@Override
    //void handleCollision(Sprite s) {
        // set a new color with random RGB
    //    color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    //}

    //@Override
    void draw(Graphics2D g) {
        
        //if (drawClock == -2){
            g.setColor(Color.getHSBColor((float)0.0, (float)0.30, (float)0.75));
            g.fillRect(30, 60, screenW-30, screenH -60);
        //}
        ControlledPlayer.drawHitboxes(g);
        Opponent.drawHitboxes(g);
        
            healthBar(g);
        if (drawClock == -1)
            colorFlip = !colorFlip;
            if (colorFlip == true){
                for (int i = 0 ; i < (30+screenW)/2 ; i++){
                        if ((i % 8) > 5)
                        {
                            g.setColor(Color.getHSBColor((float)0.44, (float)0.07, (float)0.10));     //floor
                            g.fillRect(i*2, screenH, 2, 30);
                            g.fillRect(i*2, 0, 2, 60);
                        }
                        else {
                            g.setColor(Color.getHSBColor(0, (float)0, (float)0.18));     //floor
                            g.fillRect(i*2, screenH, 2, 30);
                            g.fillRect(i*2, 0, 2, 60);
                        }
                }
            }
            else
            {
                for (int i = 0 ; i < (30+screenW)/2 ; i++){
                        if ((i % 8) > 5)
                        {
                            g.setColor(Color.getHSBColor(0, (float)0, (float)0.18));     //floor
                            g.fillRect(i*2, screenH, 2, 30);
                            g.fillRect(i*2, 0, 2, 60);
                        }
                        else {
                            g.setColor(Color.getHSBColor((float)0.44, (float)0.07, (float)0.10));     //floor
                            g.fillRect(i*2, screenH, 2, 30);
                            g.fillRect(i*2, 0, 2, 60);
                        }
                    }
                }
            
            
            healthBar(g);
            
            g.setColor(Color.getHSBColor(0, (float)0, (float)0.39));         //left side
            g.fillRect(0, 60, 30, screenH-60);
        
            g.fillRect(screenW, 60, 30, screenH-60);    //right side
            

            g.setColor(Color.getHSBColor((float)0.0, (float)0.30, (float)0.75));
            g.fillRect(30, 150, screenW-30, screenH -150);


            drawClock++;
        
        if (drawClock == 50)
            drawClock = -1;
            
        
        ControlledPlayer.drawHitboxes(g);
        Opponent.drawHitboxes(g);
        
        
        //winning state
        try{
            if (ControlledPlayer.getHP() <= 0 || Opponent.getHP() <= 0){
  
                for (int j = 0 ; j < 10 ; j++){
                    for (int i = 0 ; i < (30+screenW)/2 ; i++){
                        if ((i % 8) > 5)
                        {
                            g.setColor(Color.getHSBColor(0, (float)0, (float)0.18));     //floor
                            g.fillRect(i*2, screenH, 2, 30);
                            g.fillRect(i*2, 0, 2, 60);
                        }
                        else {
                            g.setColor(Color.getHSBColor((float)0.44, (float)0.07, (float)0.10));     //floor
                            g.fillRect(i*2, screenH, 2, 30);
                            g.fillRect(i*2, 0, 2, 60);
                        }
                    }
                    Thread.sleep(300);
                    for (int i = 0 ; i < (30+screenW)/2 ; i++){
                        if ((i % 8) > 5)
                        {
                            g.setColor(Color.getHSBColor((float)0.44, (float)0.07, (float)0.10));     //floor
                            g.fillRect(i*2, screenH, 2, 30);
                            g.fillRect(i*2, 0, 2, 60);
                        }
                        else {
                            g.setColor(Color.getHSBColor(0, (float)0, (float)0.18));     //floor
                            g.fillRect(i*2, screenH, 2, 30);
                            g.fillRect(i*2, 0, 2, 60);
                        }
                    }
                    Thread.sleep(300);
                }
                System.exit(0);
            }
        }
        catch(Exception e){}
        //updateTicks--;

    
    }
}
