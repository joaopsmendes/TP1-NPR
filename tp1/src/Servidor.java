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

    private List<InetAddress> carros;


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

                    if (p.getMsgType() == 2) {//msg de pedir vizinhos ao RSU

                        //System.out.println("sv: Nodo [ " + nodeAdr + " ] lido!");

                        try {
                            lockNodosRede.lock();
                            nodosRede.add(nodeAdr);
                        } finally {
                            lockNodosRede.unlock();
                        }

                        List<InetAddress> listVizinhos = database.getNeighbours(nodeAdr);

                        Packet send = new Packet(4,0, listVizinhos); //MSG tipo 4 -> Sv envia vizinhos

                        DatagramPacket pResponse = new DatagramPacket(send.serialize(), send.serialize().length, nodeAdr, 4321);
                        socketEnviar.send(pResponse);
                        System.out.println("sv: Enviei pacote tipo 4 (vizinhos) ao nodo [ " + nodeAdr + " ]");

                    } else if
                }
            }
        }