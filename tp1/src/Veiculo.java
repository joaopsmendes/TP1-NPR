import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

public class Veiculo {

    //o rui vai em excesso de velocidade e esta na pos xy :
    //msg bullk, (typo msg, numero de entradas)

    public double x;
    public double y;

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public Map<Integer, ArrayList<Packet>> database; //no array get("list".size()-1) para ultimo added! (numero do nodo?)



    public Veiculo() throws SocketException, UnknownHostException {



        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.database = new HashMap<>();

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    //System.out.println("Veiculo " + ipAddress + " ON!\n");

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket arrayRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(arrayRecebido);



                    try {
                        //List<Packet> packetsRecebidos = Packet.extractPackets(Arrays.copyOfRange(bufferr, 0, arrayRecebido.getLength()));
                        //List<Packet> packetsRecebidos = Packet.extractPackets(bufferr);

                        byte[] receivedData = Arrays.copyOfRange(arrayRecebido.getData(), arrayRecebido.getOffset(), arrayRecebido.getLength());
                        List<Packet> packetsRecebidos = Packet.extractPackets(receivedData);

                        System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());


                        int i=1;//print packet
                        for (Packet p : packetsRecebidos) {

                            if (database.containsKey(p.getIp())) {
                                database.get(p.getIp()).add(p);
                            } else {
                                ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                listCarMsgs.add(p);
                                database.put(p.getIp(), listCarMsgs);
                            }
                            //System.out.println("Packet "+ i++ + " : ["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"] adicionado à Database!");
                            //System.out.println(p.toString());
                            System.out.println("Received Packet: ip=" + p.getIp() + ", coordX=" + p.getCoordX() + ", coordY=" + p.getCoordY() + ", estadoPiso=" + p.getEstadoPiso() + ", velocidade=" + p.getVelocidade());
                        }
                    }catch (Exception e) {
                        //System.out.println("depois de ");
                        e.printStackTrace();
                    }

                    //ByteArrayInputStream byteStream = new ByteArrayInputStream(bufferr); //será que se pode enviar apenas isto?? e tirar as variaveis com objectStream.read() ??
                    //ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                    //int msgtype = objectStream.readInt();

                    //System.out.println("Veiculo " + ipAddress + " recebeu pacote!");

                    /*
                    if (msgtype == 2) { //bulk
                        int numPackets = objectStream.readInt(); // read the number of packets being received
                        Packet[] packets1 = new Packet[numPackets];
                        for (int i = 0; i < numPackets; i++) {
                            packets1[i] = (Packet) objectStream.readObject(); // read each packet from the stream
                        }
                        int i=1;//print packet
                        for (Packet p : packets1) {

                            if (database.containsKey(p.getIp())) {
                                database.get(p.getIp()).add(p);
                            } else {
                                ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                listCarMsgs.add(p);
                                database.put(p.getIp(), listCarMsgs);
                            }
                            System.out.println("Packet "+ i++ + " : ["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"] adicionado à Database!");
                        }

                        // Handle received packets as needed
                    } else if (msgtype == 1) { //1 packet -> NÃO SE USA?
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
                    }*/
                }
            } catch (Exception e) {
                //System.out.println("depois de ");
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> { // enviar msg period -> broadcast
            try {
                while(true) {

                    //ler ficheiro e tirar coords
                    //ler primeiros 3 chars da diretoria atual
                    Path currentPath = Paths.get("").toAbsolutePath();
                    Path targetPath = currentPath.getParent().getParent().getParent();
                    String directoryName = targetPath.getFileName().toString();
                    String vehicleID = directoryName.substring(0, Math.min(directoryName.length(), 3));

                    String vehicleIDint = directoryName.substring(1, Math.min(directoryName.length(), 3));
                    int vIDint = Integer.parseInt(vehicleIDint);

                    // Read the file with the prefix in the parent directory
                    // Read the coordinates from the file
                    Path filePath = targetPath.getParent().resolve(vehicleID + ".xy");
                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                        String line = reader.readLine();
                        String[] tokens = line.split(" ");

                        x = Double.parseDouble(tokens[0]);
                        y = Double.parseDouble(tokens[1]);

                        System.out.println("A ler " + vehicleID + ".xy ... OK! ["+x+","+y+"]");

                    } catch (IOException e) {
                        System.out.println("ERRO: ficheiro .xy não foi lido!");
                    }

                    // create the broadcast address
                    socketEnviar.setBroadcast(true);
                    InetAddress broadcastAddr = InetAddress.getByName("ff02::1");//multicast addr

                    //if (database.isEmpty()) {//databse not empty

                        List<Packet> Lpacotes = new ArrayList<>();

                        /*if(!database.isEmpty()) {
                            for (ArrayList<Packet> Plist : database.values()) {
                                Lpacotes.add(Plist.get(Plist.size() - 1));//last added???
                            }
                        }*/

                        //adicionar o proprio
                        Lpacotes.add(new Packet(vIDint, x, y, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade()));

                        for(Packet p : Lpacotes){
                            System.out.println(">["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]\n");
                        }

                        //Packet[] pacotes = Lpacotes.toArray(new Packet[0]);

                        //byte[][] packets = new byte[Lpacotes.size()][];
                        /*for (int i = 0; i < Lpacotes.size(); i++) {
                            packets[i] = Lpacotes.get(i).packetToByteArray();
                        }*/
                    System.out.println("antes de createPaacketArray!");
                        byte[] datab = Packet.createPacketArray(Lpacotes);
                    System.out.println("depois de createPaacketArray!");

                        //System.out.println("Packet "+ i++ + " : ["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]

                        //tipo 2
                        DatagramPacket requestb = new DatagramPacket(datab,datab.length,broadcastAddr,4321);

                        System.out.println(">Bytes a enviar: " + Arrays.toString(datab));

                        socketEnviar.send(requestb);

                        System.out.println("Pacote tipo 2 enviado a para o grupo!");

                    /*} else {
                        //tipo 1 (info normnal)
                        Packet p = new Packet(vehicleID, x, y, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade());
                        //DatagramPacket request = Utils.sendPacket(broadcastAddr, 4321, p);

                        byte[] data = Packet.createByteArray(p);

                        //enviar sempre bulk info? enviar sempre Packet.createPacketArray()!
                        DatagramPacket request = new DatagramPacket(data, data.length,broadcastAddr,4321);

                        socketEnviar.send(request);

                        System.out.println("Pacote tipo 1 (Novo) enviado a para broadcast!");

                    }*/
                    Thread.sleep(5000);//5s
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

}