import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
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
import java.util.List;

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

    public Veiculo(InetAddress ipAddress) throws SocketException, UnknownHostException {

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.database = new HashMap<>();



        /*new Thread(() -> { // THREAD PARA receber
            try {

                while (!Thread.interrupted()) {

                    System.out.println("Veiculo " + ipAddress + " ON!\n");

                    if (socketReceber.isClosed()) {
                        break;
                    }
                    Packet[] pacotesRecebidos = PacketTransmission.receivePackets(socketReceber);

                    System.out.println("Veiculo " + ipAddress + " recebeu pacotes!");

                    if (pacotesRecebidos != null) {
                        for(Packet p : pacotesRecebidos){

                            if (database.containsKey(p.getIp())) {
                                database.get(p.getIp()).add(p);
                            } else {
                                ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                listCarMsgs.add(p);
                                database.put(p.getIp(), listCarMsgs);
                            }
                        }
                    } else {
                        System.out.println("<<pacote invalido!>>");

                        //Thread.sleep(100);
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
                    System.out.println("Veiculo " + ipAddress + " ON!\n");

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

                    System.out.println("Veiculo " + ipAddress + " recebeu pacote!");

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
                            if (database.containsKey(p.getIp())) {
                                database.get(p.getIp()).add(p);
                            } else {
                                ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                listCarMsgs.add(p);
                                database.put(p.getIp(), listCarMsgs);
                            }
                        }

                        // Handle received packets as needed
                    } else if (msgtype == 1) { //1 packet
                        Packet[] packets1 = new Packet[1];
                        packets1[0] = (Packet) objectStream.readObject();

                        if (database.containsKey(packets1[0].getIp())) {
                            database.get(packets1[0].getIp()).add(packets1[0]);
                        } else {
                            ArrayList<Packet> listCarMsgs = new ArrayList<>();
                            listCarMsgs.add(packets1[0]);
                            database.put(packets1[0].getIp(), listCarMsgs);
                        }
                    } else {//outros tipos.....
                        System.out.println("<<pacote invalido!>>");
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> { // enviar msg period -> broadcast
            try {

                //ler ficheiro e tirar coords
                //ler primeiros 3 chars da diretoria atual
                Path currentPath = Paths.get("").toAbsolutePath();
                Path targetPath = currentPath.getParent().getParent().getParent();
                String directoryName = targetPath.getFileName().toString();
                String directoryPrefix = directoryName.substring(0, Math.min(directoryName.length(), 3));

                // Read the file with the prefix in the parent directory
                // Read the coordinates from the file
                Path filePath = targetPath.getParent().resolve(directoryPrefix + ".xy");
                try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                    String line = reader.readLine();
                    String[] tokens = line.split(" ");

                    x = Double.parseDouble(tokens[0]);
                    y = Double.parseDouble(tokens[1]);

                    System.out.println("Ficheiro " + directoryPrefix + "Lido! ("+x+" , "+y+")");

                } catch (IOException e) {
                    // Handle the exception
                }


                // create the broadcast address
                socketEnviar.setBroadcast(true);
                InetAddress broadcastAddr = InetAddress.getByName("ff02::1");


                if(!database.isEmpty()){//databse not empty

                    List<Packet> Lpacotes = new ArrayList<>();
                    for (ArrayList<Packet> Plist : database.values()){
                        Lpacotes.add(Plist.get(Plist.size()-1));//last added???
                    }

                    //adicionar o proprio
                    Lpacotes.add(new Packet(1,ipAddress,x,y, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade()));
                    Packet[] pacotes = Lpacotes.toArray(new Packet[0]);
                    //tipo 2
                    DatagramPacket data = sendPackets(broadcastAddr,4321,pacotes);
                    socketEnviar.send(data);

                    System.out.println("Pacote tipo 2 enviado a para broadcast!");

                }else{
                    //tipo 1 (info normnal)
                    Packet p = new Packet(1,ipAddress,x,y, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade());
                    DatagramPacket request = new DatagramPacket(p.serialize(), p.serialize().length, broadcastAddr, 4321);
                    socketEnviar.send(request);

                    System.out.println("Pacote tipo 1 enviado a para broadcast!");

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static DatagramPacket sendPackets(InetAddress address, int port, Packet[] packets) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeInt(2);//info bulk
        objectStream.writeInt(packets.length); // write the number of packets being sent as the second int
        for (Packet packet : packets) {
            objectStream.writeObject(packet); // write each packet to the stream
        }
        objectStream.flush();
        byte[] data = byteStream.toByteArray();
        //socket.send(packet);
        return new DatagramPacket(data, data.length, address, port);
    }
}