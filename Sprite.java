import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import java.util.Scanner;

// Parent class for all Sprites. Each subclass will have Provides update(), draw(), and handleCollision() methods.
public class Sprite{
    
    public String ClientName = "Client 1";
    public static InetAddress host;
    public int PORT = BoxFighter.PORT;
    public Socket socket = BoxFighter.socket;
    public PrintWriter networkOutput = BoxFighter.networkOutput;
    public Scanner networkInput;
    public int packetInterruptions = 0;
    //public long timeSinceLastPacket = System.nanoTime();
    
    // x- and y-coordinates of upper-left corner of the sprite. Use float for sub-pixel precision.
    protected float x, y;
    protected int width;
    protected int height;
    protected Color color;
    protected String player;
    protected float xvector;
    protected float yvector;
    protected Hitbox hurtbox;
    protected int hp = 100;
    protected int stunnedFrames = 0;
    protected int blockStun = 0;
    protected boolean facingRight = true;
    // rectangular region on canvas specifying where the sprite can be hit
    
    
    protected Hitbox attackHitbox;
    // extended hitbox drawn during moves
    protected Hitbox hurtboxExtension;
    
    protected String attackName = "Standing Attack";
    protected int attackIdentifier = 0;
    protected int startupFrames;
    protected int attackDamage;
    protected int activeFrames;
    protected int recoveryFrames;
    protected int stunFrames;
    protected int whiffFrames;
    protected String attackProperty;
    
    protected int hitOffsetX;
    protected int hitOffsetY;
    protected int hitWidth;
    protected int hitHeight;
    protected int extendOffsetX;
    protected int extendOffsetY;
    protected int extendWidth;
    protected int extendHeight;
    
    
    protected boolean attackKeyPressed = false;
    // used to generate random numbers
    protected boolean holdingLeft = false;
    protected boolean holdingRight = false;
    protected boolean holdingUp = false;
    protected boolean holdingDown = false;
    protected int recoveryLeft = 0;
    protected int moveBuffer = 0;
    protected double gravity = 30;
    protected double upwardsMomentum = 0;
    protected boolean airbourne = false;
    protected boolean movingRight = false;
    protected boolean movingLeft = false;
    protected boolean crouching = false;
    protected boolean inAction = false;
    protected boolean hitboxActive = false;
    protected int keyHoldLockout = 0;
    protected boolean whiffState = false;
    protected boolean hitState = false;
    
    
    
    protected String inputString = "0 0 0 0 0 0 0 0 0 0 0 0";
    protected String receivedString = "0 0 0 0 0 0 0 0 0 0 0 0";
    
    
    // FIGHTER SPRITE CONSTRUCTOR
    public Sprite(String player, float x, float y, int width, int height, Color color, Hitbox hurtbox) {
        this.player = player;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.hurtbox = hurtbox;
        this.xvector = 0;
        this.yvector = 0;

        try{
        this.networkInput = new Scanner(socket.getInputStream());
        }
        catch(Exception e){
            System.out.println("constructor error");
        }
        //this.attackHitbox = new Hitbox(1, 1, 1, 1);
        //this.hurtboxExtension = hurtbox;
    }
    
    public String getAttackProperty(){
        return this.attackProperty;
    }
    
    public float getX(){
        return this.x;
    }
    public void setX(float x){
        this.x = x;
    }
    public float getY(){
        return this.y;
    }
    public void setY(float x){
        this.y = y;
    }
    
    public int getAttackIdentifier(){
        return this.attackIdentifier;
    }
        
        
    public void updateInputs(){
            int strIndex = 0;
            int strIndexEnd = 0;
            //System.out.println(socket);
            //Set up stream for keyboard entry…
            try{  
                if (networkInput.hasNextLine()){
                    receivedString = "";
                    receivedString = networkInput.nextLine();
                    packetInterruptions = 100;
                }
                else
                    packetInterruptions++;
                }
                catch (Exception e){
                    System.out.println("updateInputs error");
                }
            if (!(receivedString.charAt(0)=='0')){
                strIndexEnd = receivedString.indexOf(" ");
                this.holdingRight = Boolean.valueOf(receivedString.substring(strIndex, strIndexEnd));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                
                //inputFromServer = networkInput.nextBoolean();
                this.holdingLeft = Boolean.valueOf(receivedString.substring(strIndex, strIndexEnd));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                //inputFromServer = networkInput.nextBoolean();
                this.holdingUp = Boolean.valueOf(receivedString.substring(strIndex, strIndexEnd));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                //inputFromServer = networkInput.nextBoolean();
                this.holdingDown = Boolean.valueOf(receivedString.substring(strIndex, strIndexEnd));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                //inputFromServer = networkInput.nextBoolean();
                this.hitboxActive = Boolean.valueOf(receivedString.substring(strIndex, strIndexEnd));;
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                setMoveBuffer(Integer.valueOf(receivedString.substring(strIndex, strIndexEnd)));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                
                if (packetInterruptions >= 100)
                    this.x = Float.valueOf(receivedString.substring(strIndex, strIndexEnd));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                if (packetInterruptions >= 100)
                    this.y = Float.valueOf(receivedString.substring(strIndex, strIndexEnd));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);

                this.hp = Integer.valueOf(receivedString.substring(strIndex, strIndexEnd));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                doAttack(Integer.valueOf(receivedString.substring(strIndex, strIndexEnd)));
                this.xvector = Float.valueOf(receivedString.substring(strIndex, strIndexEnd));
                strIndexEnd++;
                strIndex = strIndexEnd;
                strIndexEnd = receivedString.indexOf(" ", strIndex);
                this.yvector = Float.valueOf(receivedString.substring(strIndex, strIndexEnd));
            }
    }
    public void sendKeyState() throws IOException{
        //Scanner networkInput = new Scanner(socket.getInputStream());
        //while (networkInput.hasNext())
        //System.out.print(networkInput.next());
        try{
        inputString = (String.valueOf(holdingRight) + " " +
                        String.valueOf(holdingLeft) + " " +
                        String.valueOf(holdingUp) + " " +
                        String.valueOf(holdingDown) + " " +
                        String.valueOf(hitboxActive) + " " +
                        String.valueOf(moveBuffer) + " " +
                        String.valueOf(x) + " " +
                        String.valueOf(y) + " " +
                        String.valueOf(hp) + " " +
                        String.valueOf(getAttackIdentifier()) + " " +
                        String.valueOf(xvector) + " " +
                        String.valueOf(yvector)
                        );
        networkOutput.println(inputString);
            //networkOutput.println(holdingRight);
    //networkOutput.println(holdingLeft);
        //networkOutput.println(holdingUp);
        //networkOutput.println(holdingDown);
        //networkOutput.println(hitboxActive);
        //networkOutput.println(moveBuffer);
        
        //networkOutput.println(x);
        //networkOutput.println(y);
        //networkOutput.println(hp);
        //networkOutput.println(getAttackIdentifier());
    
        networkOutput.flush();
    }
    catch (Exception e){
        System.out.println("sendKeyState error");
    }
        
        
	}
    public void handleKeyPressed(KeyEvent e)throws IOException{
            if (e.getKeyCode() == KeyEvent.VK_D) {  // handle right input
                holdingRight = true;  // move to the right
            } 
            if (e.getKeyCode() == KeyEvent.VK_A) {  // handle left input
                holdingLeft = true;  // move to the left
            }
            if (e.getKeyCode() == KeyEvent.VK_W) {  // handle up input
                holdingUp = true;  // move up (y decreases!)
            } 
            if (e.getKeyCode() == KeyEvent.VK_S) {  // handle down input
                holdingDown = true;  // move down (y increases!)
            } 
            if (e.getKeyCode() == KeyEvent.VK_J) {   //handle attack input J
                if (attackKeyPressed == false){
                    attackKeyPressed = true;
                    setMoveBuffer(6);
                }
            }
            //try{
            //sendKeyState();
            //}
            //catch(IOException ioEx)
            {
                //System.out.println(
                //"eh?Unable to disconnect!");
                //System.exit(1);
            }
    }
    
    public void handleKeyReleased(KeyEvent e) throws IOException{
        if (e.getKeyCode() == KeyEvent.VK_D) {
            holdingRight = false;  // user stopped pressing left/right arrow key. stop horizontal movement
        } 
        if (e.getKeyCode() == KeyEvent.VK_A) {
            holdingLeft = false;
        } 
        if (e.getKeyCode() == KeyEvent.VK_W)   {
            holdingUp = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            holdingDown = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_J) {
            attackKeyPressed = false;
        }
        
    }
    
    

    
    
    public void setMoveBuffer(int moveBuffer){
        this.moveBuffer = moveBuffer;
    }
    public int getHP(){
        return this.hp;
    }
    
    public int getStartupFrames(){
        return this.startupFrames;
    }
    public int getAttackDamage(){
        return this.attackDamage;
    }
    public int getActiveFrames(){
        return this.activeFrames;
    }
    public int getRecoveryFrames(){
        return this.recoveryFrames;
    }
    public int getStunFrames(){
        return this.stunFrames;
    }
    public int getWhiffFrames(){
        return this.whiffFrames;
    }
    public int getHitOffsetX(){
        return this.hitOffsetX;
    }
    public int getHitOffsetY(){
        return this.hitOffsetY;
    }
    public int getHitWidth(){
        return this.hitWidth;
    }
    public int getHitHeight(){
        return this.hitHeight;
    }
    public int getExtendOffsetX(){
        return this.extendOffsetX;
    }
    public int getExtendOffsetY(){
        return this.extendOffsetY;
    }
    public int getExtendWidth(){
        return this.extendWidth;
    }
    public int getExtendHeight(){
        return this.extendHeight;
    }
    
    public void doAttack(String attackName){
        if (attackName.equals("Standing Attack"))
            attackMid();
        if (attackName.equals("Crouching Attack"))
            attackLow();
        if (attackName.equals("Air Attack"))
            attackAir();
    }
    
    public void doAttack(int recievedIdentifier){
        if (recievedIdentifier == 0)
            attackMid();
        if (recievedIdentifier == 1)
            attackLow();
        if (recievedIdentifier == 2)
            attackAir();
    }
    
    private void attackMid() {
        this.attackName = "Standing Attack";
        this.attackIdentifier = 0;
        this.attackProperty = "Mid";
        this.startupFrames = 2;
        this.attackDamage = 10;
        this.activeFrames = 3;
        this.recoveryFrames = 15;
        this.whiffFrames = 3;
        this.stunFrames = 1+ recoveryFrames - startupFrames;
        
        if (facingRight){
            this.hitOffsetX = 70;
            this.hitOffsetY = 50;
            this.hitWidth = 100;
            this.hitHeight = 10;
            this.extendOffsetX = 50;
            this.extendOffsetY = 50;
            this.extendWidth = 20;
            this.extendHeight = 10;
        }
        else{
            this.hitOffsetX = -120;
            this.hitOffsetY = 50;
            this.hitWidth = 100;
            this.hitHeight = 10;
            this.extendOffsetX = -20;
            this.extendOffsetY = 50;
            this.extendWidth = 20;
            this.extendHeight = 10;
        }
    }
    
    private void attackLow() {
        this.attackName = "Crouching Attack";
        this.attackIdentifier = 1;
        this.attackProperty = "Low";
        this.startupFrames = 5;
        this.attackDamage = 7;
        this.activeFrames = 2;
        this.recoveryFrames = 18;
        this.whiffFrames = 6;
        this.stunFrames = 1 + recoveryFrames - startupFrames;
        
        if (facingRight){
            this.hitOffsetX = 95;
            this.hitOffsetY = 190;
            this.hitWidth = 65;
            this.hitHeight = 10;
            this.extendOffsetX = 75;
            this.extendOffsetY = 190;
            this.extendWidth = 20;
            this.extendHeight = 10;
        }
        else
            {
            this.hitOffsetX = -110;
            this.hitOffsetY = 190;
            this.hitWidth = 65;
            this.hitHeight = 10;
            this.extendOffsetX = -45;
            this.extendOffsetY = 190;
            this.extendWidth = 20;
            this.extendHeight = 10;
        }
    }
    
    private void attackAir() {
        this.attackName = "Air Attack";
        this.attackIdentifier = 2;
        this.attackProperty = "High";
        this.startupFrames = 4;
        this.attackDamage = 10;
        this.activeFrames = 10;
        this.recoveryFrames = 28;
        this.whiffFrames = 10;
        this.stunFrames = 20;
        
        if (facingRight){
            this.hitOffsetX = 20;
            this.hitOffsetY = 201;
            this.hitWidth = 100;
            this.hitHeight = 10;
            this.extendOffsetX = 0;
            this.extendOffsetY = 200;
            this.extendWidth = 20;
            this.extendHeight = 20;
        }
        else{
            this.hitOffsetX = -70;
            this.hitOffsetY = 201;
            this.hitWidth = 100;
            this.hitHeight = 10;
            this.extendOffsetX = 30;
            this.extendOffsetY = 200;
            this.extendWidth = 20;
            this.extendHeight = 20;
        }

    }
    
    public boolean checkInAction(){
        return inAction;
    }
    
    public boolean checkHitState(){
        return hitState;
    }
    
    public boolean checkHitboxActive(){
        return hitboxActive;
    }
    
    public void setHitState(boolean hitState){
        this.hitState = hitState;
    }
    
    public void handleEverything(float speedX, int screenW, int screenH, float opponentX)   {
        
        
    
        
        // increment x-coordinate and y-coordinate according to speed
        if (inAction == false && airbourne == false){
            if (holdingRight == true && holdingLeft == false){
                movingRight = true;
                movingLeft = false;
            }
            else if (holdingRight == false && holdingLeft == true){
                movingLeft = true;
                movingRight = false;
            }
            else{
                movingRight = false;
                movingLeft = false;
            }
        }
        else if (inAction == true && airbourne == false){
                movingRight = false;
                movingLeft = false;
        }
        
        if (holdingDown == true && holdingUp == false && inAction == false && airbourne == false ){  //ADD AIRBOURNE CHECK IF YOU FINISH
            crouching = true;
            movingRight = false;
            movingLeft = false;
        }
        else if (inAction == false && holdingDown == false)
            crouching = false;
            
            
        if (moveBuffer > 0 && inAction == false){
            if (airbourne == true){
                doAttack("Air Attack");
            }
            else if (crouching == true){
                doAttack("Crouching Attack");
            }
            else{
                doAttack("Standing Attack");
            }
            recoveryLeft = getRecoveryFrames();
        }
        else if (holdingUp == true && airbourne == false && inAction == false)
            upwardsMomentum = 65;
        
        if (y >= 400.0){
            airbourne = false;
        }
        else{
            airbourne = true;
            if (upwardsMomentum > 0)
                upwardsMomentum -= (2.5);
        }
                    
        if (stunnedFrames > 0)
            if (facingRight == true)
                movingLeft = true;
            else if (facingRight == false)
                movingRight = true;
                
                
        
        if (movingRight == true)
            xvector = speedX;
        else if (movingLeft == true)
            xvector = -speedX;
        else
            xvector = 0;
        x += xvector;
            
            
            
        yvector = ((float)gravity - (float)upwardsMomentum);
        y += yvector;

        // bounds checking: check if we've gone off left or right screen edge. if so, move sprite back in bounds
        if (x + width > screenW) {  // right edge check
            x = screenW - width;
        } else if (x < 30) {  // left edge check
            x = 30;
        }
        // bounds checking: check if we've gone off top or bottom screen edge. if so, move sprite back in bounds.
        // do not combine this if/else with the one above, because the sprite could go off a vertical *and* horizontal
        // edge simultaneously, and that would not be caught.
        if (y + height > screenH) {  // bottom edge check
            y = screenH - height;
        } else if (y < 0) {  // top edge check
            y = 0;
        }
        
        
        if (getRecoveryFrames() - recoveryLeft > getStartupFrames() 
                && getRecoveryFrames() - recoveryLeft <= getStartupFrames() + getActiveFrames())
            hitboxActive = true;
        else 
            hitboxActive = false;
            
        if (getRecoveryFrames() - recoveryLeft > getStartupFrames() 
                && getRecoveryFrames() - recoveryLeft <= getStartupFrames() + getActiveFrames() + getWhiffFrames())
            whiffState = true;
        else 
            whiffState = false;
        

        if (recoveryLeft > 0)
            recoveryLeft--;
        if (moveBuffer > 0)
            moveBuffer--;
        if (stunnedFrames > 0)
            stunnedFrames--;
        if (blockStun > 0)
            stunnedFrames--;
        
        if (y > 350 && hitboxActive == true && airbourne)
            recoveryLeft = 4;
        
            
        if (airbourne == false && inAction == false)
                    faceEachOther(opponentX);
                    
                if (recoveryLeft > 0  || stunnedFrames > 0)
                    inAction = true;
                else 
                    inAction = false;
            
            
        // update hitbox coordinates to match sprite
        if (inAction == false)
            hurtboxExtension.set(x, y, width, height);
            
        // update hurtbox coordinates to match sprite
        if (crouching == false){
            hurtbox.set(x, y, width, height);
            if (inAction == false)
                hurtboxExtension.set(x, y, width, height);
        }
        else{
            hurtbox.set((int) x-25, (int) y+50, width+50, height-50);
            if (inAction == false)
                hurtboxExtension.set((int) x-25, (int) y+50, width+50, height-50);
        }    
            
        if (hitboxActive == true)
            attackHitbox.set((int) x+getHitOffsetX(), (int)y+getHitOffsetY() , getHitWidth(), getHitHeight());
        else
            attackHitbox.set(1, 1, 1, 1);
            
        if (whiffState == true)
            hurtboxExtension.set((int)x+getExtendOffsetX(), (int)y+getExtendOffsetY() , getExtendWidth(), getExtendHeight());
        
    }
    
    public void faceEachOther(float opponentX){
        if (opponentX > this.x)
            facingRight = true;
        else
            facingRight = false;

    }
    
    public void gotHit(int damage, int stunFrames, String property){
        ControllableBox.drawClock = -1;
        this.setHitState(true);
        if ((inAction == true || airbourne == true)
            || (stunnedFrames > 0 && blockStun == 0)
            || (facingRight == true && holdingLeft == false)
            || (facingRight == false && holdingRight == false)
            ||  (property.equals("Low") && crouching == false)
            ||  (property.equals("High") && crouching == true)){
            this.hp -= damage;
            if (airbourne == true){
                if (facingRight == true)
                    movingRight = true;
                else
                    movingLeft = true;
                upwardsMomentum = 50;
                stunnedFrames = 10;
            }
        }
        else{
            blockStun = stunFrames;
            this.hp -= 1;
        }
        this.stunnedFrames = stunFrames;
    }
    
    public void drawHitboxes(Graphics2D g){
        if (crouching == false){
            g.setColor(color);
            g.fillRect((int) x, (int) y, width, height);  // draw square with correct coordinate and width/height
        }
        else {
            g.setColor(color);
            g.fillRect((int) x-25, (int) y+50, width+50, height-50);
        }
        if (whiffState == true){
            g.setColor(color);
            g.fillRect((int) x+getExtendOffsetX(), (int)y+getExtendOffsetY() , getExtendWidth(), getExtendHeight());  // draw square with correct coordinate and width/height
        }
        if (hitboxActive == true){
            g.setColor(Color.RED);             //p1 mid    lots of things undecided
            g.fillRect((int) x+getHitOffsetX(), (int)y+getHitOffsetY() , getHitWidth(), getHitHeight());
            if (y > 350 && airbourne == true)
                ControllableBox.drawClock = -1;
        }
    }
    
    public void drawHealthBar(Graphics2D g){
        if (this.player.equals("Player 1")){
            g.setColor(Color.GREEN);             //p1 mid    lots of things undecided
            g.fillRect((int) 30, (int)60 , 400-(400-(this.getHP()*4)), 50);
            g.setColor(Color.RED);             //p1 mid    lots of things undecided
            g.fillRect((int) 430-(400-hp*4), (int)60 , (400-hp*4), 50);
        }
        if (this.player.equals("Player 2")){
            g.setColor(Color.GREEN);             //p1 mid    lots of things undecided
            g.fillRect((int) 800+400-(this.getHP()*4), (int)60 , 400-(400-(this.getHP()*4)), 50);
            g.setColor(Color.RED);             //p1 mid    lots of things undecided
            g.fillRect((int) 800, (int)60 , 400-(hp*4), 50);
        }
    }
    
    // updates the state of the sprite (implemented in subclass)
    //public void update();

    // draws the sprite to the screen/graphics object (implemented in subclass)
    //public void draw(Graphics2D g);

    // executes logic when a collision is detected with given Sprite s
    //public void handleCollision(Sprite s);

    // returns whether this Sprite collides with the given Sprite, i.e. whether their Hitboxes intersect.
    public boolean collides(Sprite s) {
        return hurtbox.intersects(s.hurtbox);
    }
}
