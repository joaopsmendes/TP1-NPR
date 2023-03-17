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

public class Veiculo {

    Timer cTimer; //timer used to receive data from the UDP socket
    byte[] cBuf; //buffer used to store data received from the server

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public veiculo(InetAddress ipRSU) throws SocketException {

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        new Thread(() -> { // 
            try {

                Packet p = new Packet();
                DatagramPacket request = new DatagramPacket(p.serialize(), p.serialize().length, ipRSU, 4321);
                socketEnviar.send(request);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}