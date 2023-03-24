import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
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

    //o rui vai em excesso de velocidade e esta na pos xy :
    //msg bullk, (typo msg, numero de entradas)

    public double x;
    public double y;

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public Map<InetAddress, ArrayList<Packet>> database; //no array get("list".size()-1) para ultimo added!

    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }

    public void veiculo(InetAddress ipRSU) throws SocketException, UnknownHostException {

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.database = new HashMap<>();

        InetAddress localHost = InetAddress.getLocalHost();
        InetAddress ipAddress = InetAddress.getByName(localHost.getHostAddress());

        new Thread(() -> { // THREAD PARA receber 
            try {
                while (true) {

                    byte[] msg = new byte[1024];
                    DatagramPacket receiveP = new DatagramPacket(msg, msg.length);
                    socketReceber.receive(receiveP);

                    msg = receiveP.getData();
                    Packet p = new Packet(msg);

                    InetAddress ipCarro = receiveP.getAddress();

                    //DATABASE - adicionar novo carro / nova msg!

                    if (database.containsKey(ipCarro)) {
                        database.get(ipCarro).add(p);
                    } else {
                        ArrayList<Packet> listCarMsgs = new ArrayList<>();
                        listCarMsgs.add(p);
                        database.put(ipCarro, listCarMsgs);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> { // enviar msg period -> broadcast
            try {

                //ler ficheiro e tirar coords//////////////////////////////////////////////////////////////////////////////////////
                //ler primeiros 3 chars da diretoria atual
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

                for ()

                Packet p = new Packet(ipAddress,x,y, Packet.EstadoPiso.SECO, Packet.Velocidade.CINQUENTA);
                DatagramPacket request = new DatagramPacket(p.serialize(), p.serialize().length, broadcastAddr, 4321);
                socketEnviar.send(request);

                System.out.println("Pacote enviado a para broadcast!");

                //if(euclideanDistance(x,y,850.0,220.0)>200){//200 metros do RSU , RSU na pos (850,220)??
                    
                //}

                //sleep(10000);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}