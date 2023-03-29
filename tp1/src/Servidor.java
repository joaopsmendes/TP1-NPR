
import java.net.*;


public class Servidor{

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    private InetAddress ip;

    /*public Servidor(InetAddress ipserver) throws IOException {
        
        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        new Thread(() -> { //receber
            try {
                while (true) {

                    byte[] msg = new byte[1024];
                    DatagramPacket receiveP = new DatagramPacket(msg, msg.length);
                    try {
                        socketReceber.receive(receiveP);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    msg = receiveP.getData();
//                    Packet p = new Packet(msg);

                    InetAddress nodeAdr = receiveP.getAddress();//ip do carro de quem recebeu msg

                }
            }
        }
    }*/
}