
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class RSU{

    //manter o RSU fixo??
    //receber a pos doi RSU? (850.0 , 220.0)
    //ip RSU1 : 2001:11::7

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public Map<Integer, ArrayList<Packet>> databaseRSU;

    public List<Packet> DBlistRSU;


    public RSU(InetAddress ipserver) throws IOException {

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        //this.databaseRSU = new HashMap<>();

        this.DBlistRSU = new ArrayList<>();

        /*new Thread(() -> {//por while
            while(true) {

                try {
                    Thread.sleep(10000);//10s
                    for (Map.Entry<Integer, ArrayList<Packet>> entry : databaseRSU.entrySet()) {
                        System.out.println("$" + entry.getKey() + " : Recent Info: " + entry.getValue().get(databaseRSU.size() - 1).toString());
                    }
                    //System.out.println("RSU ON!");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();*/

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    Thread.sleep(1000);
                    //System.out.println("Veiculo " + ipAddress + " ON!\n");

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket arrayRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(arrayRecebido);

                    //InetAddress localHost = InetAddress.getLocalHost();
                    //if (arrayRecebido.getAddress().equals(ip)) continue;
                    //if(arrayRecebido.getAddress().equals(ip)) continue;

                    try {
                        //List<Packet> packetsRecebidos = Packet.extractPackets(Arrays.copyOfRange(bufferr, 0, arrayRecebido.getLength()));
                        //List<Packet> packetsRecebidos = Packet.extractPackets(bufferr);

                        byte[] receivedData = Arrays.copyOfRange(arrayRecebido.getData(), arrayRecebido.getOffset(), arrayRecebido.getLength());
                        List<Packet> packetsRecebidos = Packet.extractPackets(receivedData);

                        //if(debug) System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());


                        /*for (Packet p : packetsRecebidos) {

                            if (databaseRSU.containsKey(p.getIp())) {
                                databaseRSU.get(p.getIp()).add(p);
                            } else {
                                ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                listCarMsgs.add(p);
                                databaseRSU.put(p.getIp(), listCarMsgs);
                            }
                            System.out.println("-> Pacote Recebido: ["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                            //System.out.println("Received Packet: ip=" + p.getIp() + ", coordX=" + p.getCoordX() + ", coordY=" + p.getCoordY() + ", estadoPiso=" + p.getEstadoPiso() + ", velocidade=" + p.getVelocidade());
                        }*/

                        DBlistRSU.clear();
                        for (Packet p : packetsRecebidos) {

                            DBlistRSU.add(p);

                            System.out.println("-> Pacote Recebido: ["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                        }

                        new Thread(() -> { // enviar msg
                            try {
                                //while(true) {

                                    List<Packet> Lpacotes = new ArrayList<>(DBlistRSU);

                                    byte[] datab = Packet.createPacketArray(Lpacotes);
                                    //tipo x
                                    DatagramPacket requestb = new DatagramPacket(datab,datab.length,ipserver,4321);

                                    //if(debug) System.out.println(">Bytes a enviar: " + Arrays.toString(datab));

                                    socketEnviar.send(requestb);

                                    for (Packet Psend : DBlistRSU) {

                                        System.out.println("$ Recent Info: " + Psend.toString());
                                    }

                                    System.out.println("! Dados enviados ao servidor !");

                                    Thread.sleep(10000);//10s
                                //}

                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }).start();

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
    }
}