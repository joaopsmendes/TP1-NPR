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

        /*new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    System.out.println("RSU ON!");

                    Packet[] pacotesRecebidos = PacketTransmission.receivePackets(socketReceber);

                    System.out.println("Pacotes recebidos!");

                    assert pacotesRecebidos != null;
                    int i=1;
                    for(Packet p : pacotesRecebidos){

                        System.out.println("Pacote" + i + ":"+ p.getIp().toString());
                        i++;

                        if (databaseRSU.containsKey(p.getIp())) {
                            databaseRSU.get(p.getIp()).add(p);
                        } else {
                            ArrayList<Packet> listCarMsgs = new ArrayList<>();
                            listCarMsgs.add(p);
                            databaseRSU.put(p.getIp(), listCarMsgs);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();*/

        new Thread(() -> { // THREAD PARA receber
            try {
                while (!Thread.interrupted()) {
                    System.out.println("RSU ON!\n");

                    byte[] buffer = new byte[65507]; // Max size of a UDP packet
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    // Non-blocking call to receive packet
                    socketReceber.setSoTimeout(1000); // Timeout set to 1 second
                    try {
                        socketReceber.receive(packet);
                    } catch (SocketTimeoutException e) {
                        // No packets received within timeout period, continue loop
                        continue;
                    }

                    //System.out.println("Veiculo " + ipAddress + " recebeu pacote!");

                    // Process received packet
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(buffer);
                    ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                    int msgtype = objectStream.readInt();

                    if (msgtype == 2) { //bulk
                        int numPackets = objectStream.readInt(); // read the number of packets being received
                        Packet[] packets1 = new Packet[numPackets];
                        for (int i = 0; i < numPackets; i++) {
                            packets1[i] = (Packet) objectStream.readObject(); // read each packet from the stream
                        }
                        for (Packet p : packets1) {
                            if (databaseRSU.containsKey(p.getIp())) {
                                databaseRSU.get(p.getIp()).add(p);
                            } else {
                                ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                listCarMsgs.add(p);
                                databaseRSU.put(p.getIp(), listCarMsgs);
                            }
                        }

                        // Handle received packets as needed
                    } else if (msgtype == 1) { //1 packet
                        Packet[] packets1 = new Packet[1];
                        packets1[0] = (Packet) objectStream.readObject();

                        if (databaseRSU.containsKey(packets1[0].getIp())) {
                            databaseRSU.get(packets1[0].getIp()).add(packets1[0]);
                        } else {
                            ArrayList<Packet> listCarMsgs = new ArrayList<>();
                            listCarMsgs.add(packets1[0]);
                            databaseRSU.put(packets1[0].getIp(), listCarMsgs);
                        }
                    } else {//outros tipos.....
                        System.out.println("<<pacote invalido!>>");
                    }

                }
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        /*new Thread(() -> { // THREAD PARA ENVIAR! para o server!
            try {
                while (true) {

                    if(!databaseRSU.isEmpty()){//databse not empty

                        List<Packet> Lpacotes = new ArrayList<>();
                        for (ArrayList<Packet> Plist : databaseRSU.values()){
                            Lpacotes.add(Plist.get(Plist.size()-1));//last added???
                        }

                        Packet[] pacotes = Lpacotes.toArray(new Packet[0]);
                        //tipo 2
                        DatagramPacket data = PacketTransmission.sendPackets(ipserver,4321,pacotes);
                        socketEnviar.send(data);

                        System.out.println("Pacote tipo 2 enviado a para o server!");

                    }else{

                        System.out.println("RSU não tem dados!");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();*/
    }
}