import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    static List<PrintWriter> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {


        int port = 9009;

        ServerSocket serverSocket = new ServerSocket(port);
        ExecutorService pool = Executors.newCachedThreadPool();

        System.out.println(InetAddress.getLocalHost().getHostAddress() + " | " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New Connection" + clientSocket.getInetAddress());

            //Ask for and store clients name
            BufferedReader nameIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            BufferedReader rUReadyIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter rUReadyOut = new PrintWriter(clientSocket.getOutputStream(), true);
            PrintWriter nameOut = new PrintWriter(clientSocket.getOutputStream(), true);
            PrintWriter begoneTextOut = new PrintWriter(clientSocket.getOutputStream(), true);

            rUReadyOut.println("You Have To Chose a Username...\n" +
                    "It Will Be Your Username FOREVER...\n" +
                    "--------------------------\n" +
                    "Are You Ready? [n/y]\n" +
                    "--------------------------");
            String isClientReady = rUReadyIn.readLine();
            if (isClientReady.equals("n")) {
                begoneTextOut.println("Begone Then...");

            } else if (isClientReady.equals("y")) {
                nameOut.println("Please enter your name: ");
                String clientName = nameIn.readLine();

                for (PrintWriter client : clients) {
                    client.println(clientName + " Joined");
                }


                Handler clientHandler = new Handler(clientSocket, clientName);
                pool.submit(clientHandler);

                //Opening message
                PrintWriter openingMessage = new PrintWriter(clientSocket.getOutputStream(), true);
                synchronized (clients) {
                    clients.add(openingMessage);
                    openingMessage.println("=-=-=-=-=-=-=-=-=-=-=-=-\nWELCOOOMEEE " + clientName + "\nTo exit type: /quit\n=-=-=-=-=-=-=-=-=-=-=-=-");
                }
            }




        }
    }

    static class Handler implements Runnable {
        private Socket clientSocket;
        private String clientName;

        public Handler(Socket clientSocket, String clientName) {
            this.clientSocket = clientSocket;
            this.clientName = clientName;
        }


        @Override
        public void run() {
            //Client input Stream
            try {
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


                //Keep stream open while client keeps sending stuff
                String clientText = "";

                System.out.println(clientText);

                //receive and print text from client
                while (clientText != null) {

                    if (clientText.equals("/quit")) {
                        System.out.println(clientName + " Disconnected");
                        for (PrintWriter client : clients) {
                            client.println(clientName + " Disconnected");
                        }
                        break;
                    }

                    //Prints clients text to the console
                    clientText = input.readLine();
                    System.out.println(clientName + " --> " + clientText);

                    //Print a client message to all clients
                    synchronized (clients) {
                        for (PrintWriter client : clients) {
                            if (clientText.equals("/quit")) {
                                break;
                            } else {
                                client.println(clientName + " => " + clientText);
                                System.out.println();
                            }
                        }
                    }


                }
                input.close();
                clientSocket.close();


            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}

