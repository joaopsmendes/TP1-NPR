import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Veiculo {

    public double x;
    public double y;


    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public Map<InetAddress,Packet> info = new HashMap<>();

    public void veiculo(InetAddress ipRSU) throws SocketException {

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        new Thread(() -> { // THREAD PARA receber 
            try {

                byte[] msg = new byte[1024];
                DatagramPacket receiveP = new DatagramPacket(msg, msg.length);
                socketReceber.receive(receiveP);

                msg = receiveP.getData();
                Packet p = new Packet(msg);

                InetAddress ipCarro = receiveP.getAddress();

                info.put(ipCarro,p);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> { // enviar msg period -> broadcast
            try {


//ler ficheiro e tirar coords//////////////////////////////////////////////////////////////////////////////////////
                String currentDirectory = Paths.get("").toAbsolutePath().toString();
                String directoryPrefix = currentDirectory.substring(0, Math.min(currentDirectory.length(), 3));

                // Read the file with the prefix in the parent directory
                // Read the coordinates from the file
                try (BufferedReader reader = new BufferedReader(new FileReader("../" + directoryPrefix + ".xy"))) {

                    String line = reader.readLine();
                    String[] tokens = line.split(" ");

                    x = Double.parseDouble(tokens[0]);
                    y = Double.parseDouble(tokens[1]);

                    //System.out.println("x = " + x);
                    //System.out.println("y = " + y);

                } catch (IOException e) {
                    e.printStackTrace();
                }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                socketEnviar.setBroadcast(true);

                // create the broadcast address
                InetAddress broadcastAddr = InetAddress.getByName("ff02::1");

                Packet p = new Packet(0,0,0,0,);
                DatagramPacket request = new DatagramPacket(p.serialize(), p.serialize().length, broadcastAddr, 4321);
                socketEnviar.send(request);

                println("Pacote enviado a para broadcast!")

                //sleep(10000);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> { //ler do ficheiro .xy constantemente , sleep?

            try {

                readFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        /*new Thread(() -> {
            try {

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();*/
    }

    

    
}