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


public class Servidor{

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    private InetAddress ip;




    public Servidor(InetAddress ipserver) throws IOException {
        
        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        new Thread(() -> { 
            try {
                while (true) {

                    byte[] msg = new byte[1024];
                    DatagramPacket receiveP = new DatagramPacket(msg, msg.length);
                    socketReceber.receive(receiveP);

                    msg = receiveP.getData();
                    Packet p = new Packet(msg);

                    InetAddress nodeAdr = receiveP.getAddress();//ip do carro de quem recebeu msg

                }
            }
        }
    }
}