import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class RSU{

    //manter o RSU fixo??
    //receber a pos doi RSU? (850.0 , 220.0)
    //ip RSU1 : 2001:11::7

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public Map<InetAddress, ArrayList<Packet>> databaseRSU;


    public RSU(InetAddress ipserver) throws IOException {

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.databaseRSU = new HashMap<>();

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

                    if (databaseRSU.containsKey(ipCarro)) {
                        databaseRSU.get(ipCarro).add(p);
                    } else {
                        ArrayList<Packet> listCarMsgs = new ArrayList<>();
                        listCarMsgs.add(p);
                        databaseRSU.put(ipCarro, listCarMsgs);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}