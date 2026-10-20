import java.io.*;
import java.net.*;

public class server {
    public static void main(String[] args) {

        try{
            ServerSocket serverocket = new ServerSocket(5000);
            System.out.println("Server is waiting for client request...");

            Socket socket = serverocket.accept();

            System.out.println("Client connected");

            BufferedReader Input= new BufferedReader(new InputStreamReader(socket.getInputStream()));

            PrintWriter Output = new PrintWriter(socket.getOutputStream(), true);
            
            String message = Input.readLine();
            
            System.out.println("Received from client: " + message);
            
            Output.println("Message received by server");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server is running...");
    }
}