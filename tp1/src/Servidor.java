
import java.io.IOException;
import java.net.*;
import java.util.*;


public class Servidor{

    //pos: 106,438

    public Map<Integer, ArrayList<Packet>> databaseSV;

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    private InetAddress ip;

    public Servidor() throws IOException{
        
        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        databaseSV = new HashMap<>();

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    Thread.sleep(1000);

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket arrayRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(arrayRecebido);

                    try {
                        byte[] receivedData = Arrays.copyOfRange(arrayRecebido.getData(), arrayRecebido.getOffset(), arrayRecebido.getLength());
                        List<Packet> packetsRecebidos = Packet.extractPackets(receivedData);

                        //if(debug) System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());
                        
                        for (Packet p : packetsRecebidos) {

                            if(checkDistance(p.getCoordX(),p.getCoordY())<500){//distancia ao servidor! 500m

                                if (databaseSV.containsKey(p.getIp())) {
                                    databaseSV.get(p.getIp()).add(p);
                                } else {
                                    ArrayList<Packet> listCarMsgs = new ArrayList<>();
                                    listCarMsgs.add(p);
                                    databaseSV.put(p.getIp(), listCarMsgs);
                                }
                                System.out.println("Pacote adicionado à db: ["+p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]\n");
                                //System.out.println("Received Packet: ip=" + p.getIp() + ", coordX=" + p.getCoordX() + ", coordY=" + p.getCoordY() + ", estadoPiso=" + p.getEstadoPiso() + ", velocidade=" + p.getVelocidade());
                            }else{

                                System.out.println("Veiculo ["+p.getIp() + "] fora da area de interesse!");

                            }
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
    public static double checkDistance(double x1, double y1) {
        double dx = 106 - x1;
        double dy = 438 - y1;
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
}