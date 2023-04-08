
import java.io.*;
import java.net.*;
import java.util.*;

public class RSU{

    //manter o RSU fixo??
    //receber a pos doi RSU? (850.0 , 220.0)
    //ip RSU1 : 2001:11::7

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public Map<String, ArrayList<Packet>> databaseRSU;


    public RSU(InetAddress ipserver) throws IOException {

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.databaseRSU = new HashMap<>();

        new Thread(() -> {//por while
            while(true) {

                try {
                    Thread.sleep(10000);//10s
                    for (Map.Entry<String, ArrayList<Packet>> entry : databaseRSU.entrySet()) {
                        System.out.println(">" + entry.getKey() + " : Recent Info: " + packetToString(entry.getValue().get(databaseRSU.size() - 1)));
                    }
                    //System.out.println("RSU ON!");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    //System.out.println("Veiculo " + ipAddress + " ON!\n");

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket packetRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(packetRecebido);

                    System.out.println("Packet recebido de: "+ packetRecebido.getAddress());

                    ByteArrayInputStream byteStream = new ByteArrayInputStream(bufferr);
                    ObjectInputStream objectStream = new ObjectInputStream(byteStream);
                    int msgtype = objectStream.readInt();

                    //System.out.println("Veiculo " + ipAddress + " recebeu pacote!");


                    if (msgtype == 2) { //bulk
                        int numPackets = objectStream.readInt(); // read the number of packets being received
                        Packet[] packets1 = new Packet[numPackets];
                        for (int i = 0; i < numPackets; i++) {
                            packets1[i] = (Packet) objectStream.readObject(); // read each packet from the stream
                        }
                        int i=1;//print packet
                        for (Packet p : packets1) {

                            if (databaseRSU.containsKey(p.getIp())) {
                                databaseRSU.get(p.getIp()).add(p);
                            } else {
                                ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                listCarMsgs.add(p);
                                databaseRSU.put(p.getIp(), listCarMsgs);
                            }
                            System.out.println("Packet "+ i++ + " : ["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"] adicionado à DatabaseRSU!");
                        }
                    } else {//outros tipos.....
                        System.out.println("<<pacote invalido!>>");
                    }
                }
            } catch (Exception e) {
                //System.out.println("depois de ");
                e.printStackTrace();
            }
        }).start();

        /*new Thread(() -> { // enviar msg period -> broadcast
            try {
                while(true) {

                    // create the broadcast address
                    socketEnviar.setBroadcast(true);
                    InetAddress broadcastAddr = InetAddress.getByName("ff02::1");

                    if (!databaseRSU.isEmpty()) {//databse not empty

                        List<Packet> Lpacotes = new ArrayList<>();
                        for (ArrayList<Packet> Plist : databaseRSU.values()) {
                            Lpacotes.add(Plist.get(Plist.size() - 1));//last added???
                        }

                        //adicionar o proprio
                        Lpacotes.add(new Packet(vehicleID, x, y, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade()));
                        Packet[] pacotes = Lpacotes.toArray(new Packet[0]);
                        //tipo 2
                        DatagramPacket data = Packet.sendPackets(broadcastAddr, 4321, pacotes);
                        socketEnviar.send(data);

                        System.out.println("Pacote tipo 2 enviado a para broadcast!");

                    } else {
                        //tipo 1 (info normnal)
                        Packet p = new Packet(vehicleID, x, y, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade());
                        DatagramPacket request = Packet.sendPacket(broadcastAddr, 4321, p);
                        socketEnviar.send(request);

                        System.out.println("Pacote tipo 1 enviado a para broadcast!");

                    }
                    Thread.sleep(1000);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();*/
    }
    public String packetToString(Packet p){
        return "Packet: ["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]";
    }
}