import java.io.*;
import java.net.*;
import java.util.Scanner;

public class BoxFightServer
{
    private static ServerSocket serverSocket;
    private static final int PORT = 26189;
    public static void main(String[] args)
    throws IOException
    {
        try
        {
            
            serverSocket = new ServerSocket(PORT);
            //serverSocket.bind
            //System.out.println(serverSocket.(getInetAddress));
        }
        catch (IOException ioEx)
        {
            System.out.println("\nUnable to set up port!");
            System.exit(1);
        }
        Socket socket;
        do
        {
            //Wait for client…
            Socket client1 = serverSocket.accept();
            System.out.println("\nNew client accepted.\n");
            Socket client2 = serverSocket.accept();
            System.out.println("\nNew client accepted.\n");
            //Create a thread to handle communication with
            //this client and pass the constructor for this
            //thread a reference to the relevant socket…
            
            PrintWriter playerNamer = new PrintWriter(client1.getOutputStream(),true);
            playerNamer.println("Player 1");
            ClientHandler Client1 =
            new ClientHandler(client1, client2);
            Client1.start();//As usual, method calls run.
            
            playerNamer = new PrintWriter(client2.getOutputStream(),true);
            playerNamer.println("Player 2");
            ClientHandler Client2 =
            new ClientHandler(client2, client1);
            Client2.start();//As usual, method calls run.
            
        }while (true);
    }
}
class ClientHandler extends Thread
{
    private Socket client1;
    private Socket client2;
    private Scanner input;
    private PrintWriter output;
    public ClientHandler(Socket socket, Socket socket2)
    {
        //Set up reference to associated socket…
        client1 = socket;
        client2 = socket2;
        try
        {
            input = new Scanner(client1.getInputStream());
            output = new PrintWriter(
            client2.getOutputStream(),true);
        }
        catch(IOException ioEx)
        {
            ioEx.printStackTrace();
        }
    }

    public void run()
    {   
        String received = "E";
        do
        {    
            
            try{
                output.println(input.nextLine());
                
                //output.println(input.nextBoolean());
                //output.println(input.nextBoolean());
                //output.println(input.nextBoolean());
                //output.println(input.nextBoolean());
                //output.println(input.nextBoolean());
                //output.println(input.nextInt());
                //output.println(input.nextFloat());
                //output.println(input.nextFloat());
                //output.println(input.nextInt());
                //output.println(input.nextInt());
            }
            catch(Exception e){
            }
            output.flush();
            //Repeat above until 'QUIT' sent by client…
        }while (!received.equals("QUIT"));
        try
        {
            if (client1!=null)
            {
                System.out.println(
                "Closing down connection…");
                client1.close();
            }
        }
        catch(IOException ioEx)
        {
            System.out.println("Unable to disconnect!");
        }
    }
}
