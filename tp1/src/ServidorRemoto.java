
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
public class ServidorRemoto {

    private DatagramSocket socketReceber;

    public List<Packet> SVdatabase;



    public ServidorRemoto() throws IOException{

        this.socketReceber = new DatagramSocket(4321);
        this.SVdatabase = new ArrayList<>();

        System.out.println("Remote Server ON ✓ \n");

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    Thread.sleep(200);

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket arrayRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(arrayRecebido);

                    System.out.println("recebi coisas");

                    try {
                        byte[] receivedData = Arrays.copyOfRange(arrayRecebido.getData(), arrayRecebido.getOffset(), arrayRecebido.getLength());
                        List<Packet> packetsRecebidos = Packet.extractPackets(receivedData);

                        //if(debug) System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());

                        for (Packet p : packetsRecebidos) {

                            SVdatabase.add(p);

                            System.out.println("SV Remoto: Pacote adicionado à db: [ "+ p.getType() + "|" + p.getIpaddress()+ "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");


                        }
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
