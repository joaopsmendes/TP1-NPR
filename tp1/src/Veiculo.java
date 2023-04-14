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

    private final Boolean debug = false;

    public Integer vehicleNodeNumber;

    public List<Packet> DBlist;


    public Veiculo(InetAddress ip) throws SocketException, UnknownHostException {



        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.database = new HashMap<>();

        this.DBlist = new ArrayList<>();

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    Thread.sleep(1000);
                    //System.out.println("Veiculo " + ipAddress + " ON!\n");

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket arrayRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(arrayRecebido);

                    try {

                        byte[] receivedData = Arrays.copyOfRange(arrayRecebido.getData(), arrayRecebido.getOffset(), arrayRecebido.getLength());
                        List<Packet> packetsRecebidos = Packet.extractPackets(receivedData);

                        if(debug) System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());


                        //DBlist.clear();
                        //int i=1;//print packet

                        /*for(Packet p : packetsRecebidos){
                            if(p.getIp().equals(vehicleNodeNumber)) continue;//check se vem do mesmo!

                            DBlist.add(p);

                            System.out.println("-> Pacote Recebido: [$ "+p.getIp()+" $|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                        }*/
                        //System.out.println();

                                /*socketEnviar.setBroadcast(true);
                                InetAddress broadcastAddr = InetAddress.getByName("ff02::1");//multicast addr

                                List<Packet> Lpacotes = new ArrayList<>(DBlist);

                                byte[] datab = Packet.createPacketArray(Lpacotes);
                                //tipo x
                                DatagramPacket requestb = new DatagramPacket(datab,datab.length,broadcastAddr,4321);

                                //if(debug) System.out.println(">Bytes a enviar: " + Arrays.toString(datab));

                                socketEnviar.send(requestb);

                                for (Packet p : DBlist) {

                                    System.out.println("-> Pacote Enviado: [$ "+p.getIp()+" $|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                                }*/



                        for (Packet p : packetsRecebidos) {

                            if(p.getIp().equals(vehicleNodeNumber)) continue;//check se vem do mesmo!

                            if (database.containsKey(p.getIp())) {
                                database.get(p.getIp()).add(p);
                            } else {
                                ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                listCarMsgs.add(p);
                                database.put(p.getIp(), listCarMsgs);
                            }
                            System.out.println("<- Pacote Recebido: [$ "+p.getIp()+" $|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                            //System.out.println("Received Packet: ip=" + p.getIp() + ", coordX=" + p.getCoordX() + ", coordY=" + p.getCoordY() + ", estadoPiso=" + p.getEstadoPiso() + ", velocidade=" + p.getVelocidade());
                        }

                        System.out.println();
                    }catch (Exception e) {
                        //System.out.println("depois de ");
                        e.printStackTrace();
                    }
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
                    String vehicle_nID = directoryName.substring(0, Math.min(directoryName.length(), 3));

                    String vehicleIDint = directoryName.substring(1, Math.min(directoryName.length(), 3));
                    vehicleNodeNumber = Integer.parseInt(vehicleIDint);

                    // Read the file with the prefix in the parent directory
                    // Read the coordinates from the file
                    Path filePath = targetPath.getParent().resolve(vehicle_nID + ".xy");
                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                        String line = reader.readLine();
                        String[] tokens = line.split(" ");

                        x = Double.parseDouble(tokens[0]);
                        y = Double.parseDouble(tokens[1]);

                        System.out.println("A ler " + vehicle_nID + ".xy ... OK! ["+x+","+y+"]");

                    } catch (IOException e) {
                        System.out.println("ERRO: ficheiro .xy não foi lido!");
                    }

                    // create the broadcast address
                    socketEnviar.setBroadcast(true);
                    InetAddress broadcastAddr = InetAddress.getByName("ff02::1");//multicast addr

                    //enviar apenas a info propria !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!| INFO PROPRIA |!!!!!!!!!!!!!

                    List<Packet> Lpacotes = new ArrayList<>();

                    if(!database.isEmpty()) {
                        for (ArrayList<Packet> Plist : database.values()) {
                            Lpacotes.add(Plist.get(Plist.size() - 1));//last added???
                        }
                    }

                    /*if(!DBlist.isEmpty()) {
                        //last added???
                        Lpacotes.addAll(DBlist);
                    }*/

                    //adicionar o proprio
                    Lpacotes.add(new Packet(vehicleNodeNumber, x, y, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade()));

                    byte[] datab = Packet.createPacketArray(Lpacotes);

                    //tipo 2
                    DatagramPacket requestb = new DatagramPacket(datab,datab.length,broadcastAddr,4321);

                    if(debug) System.out.println(">Bytes a enviar: " + Arrays.toString(datab));

                    socketEnviar.send(requestb);

                    System.out.println("! Info de Estado enviada via Multicast !");
                    for(Packet p : Lpacotes){
                        System.out.println("-> Pacote Enviado: [$ "+p.getIp()+" $|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                    }
                    //System.out.println();

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