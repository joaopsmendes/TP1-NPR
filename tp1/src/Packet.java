import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Packet implements Serializable {

    //private Integer msgType;//1- info normal | 2- info bulk (segundo int nº pacotes) | 3-....
    //int -> nº pacotes
    private Integer ip;
    private double coordX;
    private double coordY;
    private Integer estadoPiso;// 0->seco | 1->chuva | 2->neve | 3->gelo
    private Integer velocidade;
    //private Velocidade velocidade;
    //private EstadoPiso estadoPiso;


    //private static final Random rand = new Random();

    public static int getRandomVelocidade() {
        int[] values = {50, 90, 120, 150};
        Random random = new Random();
        int index = random.nextInt(values.length);
        return values[index];
    }
    public static int getRandomEstadoPiso() {
        int[] values = {0, 1, 2, 3};// 0->seco | 1->chuva | 2->neve | 3->gelo
        Random random = new Random();
        int index = random.nextInt(values.length);
        return values[index];
    }

    /*public static Velocidade getRandomVelocidade() {
        Velocidade[] allVelocidades = Velocidade.values();
        int randomIndex = rand.nextInt(allVelocidades.length);
        return allVelocidades[randomIndex];
    }

    public static EstadoPiso getRandomEstadoPiso() {
        EstadoPiso[] allEstados = EstadoPiso.values();
        int randomIndex = rand.nextInt(allEstados.length);
        return allEstados[randomIndex];
    }*/

    //hoe to create enum
    enum EstadoPiso {
        SECO, CHUVA, NEVE, GELO
    }


    public enum Velocidade {
        TRINTA(30),
        CINQUENTA(50),
        NOVENTA(90),
        CENTO_VINTE(120);

        private final int valor;

        Velocidade(int valor) {
            this.valor = valor;
        }

        public int getValor() {
            return valor;
        }
    }

    public Packet(Integer ip, double coordX, double coordY, int estadoPiso, int velocidade) {
        //this.msgType = type;
        this.ip = ip;
        this.coordX = coordX;
        this.coordY = coordY;
        this.estadoPiso = estadoPiso;
        this.velocidade = velocidade;
    }

    public Packet( Packet p){
        //this.msgType = p.getMsgType();
        this.ip = p.getIp();
        this.coordY = p.getCoordY();
        this.coordX = p.getCoordX();
        this.estadoPiso = p.getEstadoPiso();
        this.velocidade = p.getVelocidade();
    }
    public String toString() {
        return "Packet [ip=" + ip + ", coordX=" + coordX + ", coordY=" + coordY +
                ", estadoPiso=" + estadoPiso + ", velocidade=" + velocidade + "]";
    }

    public static byte[] createPacketArray(List<Packet> packets) {//varios pacotes a enviar!
        int numPackets = packets.size();
        int packetSize = Integer.BYTES + 2 * Double.BYTES + 2 * Integer.BYTES;;
        int totalSize = Integer.BYTES + (numPackets * packetSize); // 4 bytes for the packet count

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putInt(numPackets);

        for (Packet packet : packets) {

            buffer.putInt(packet.getIp());
            buffer.putDouble(packet.getCoordX());
            buffer.putDouble(packet.getCoordY());
            buffer.putInt(packet.getEstadoPiso());
            buffer.putInt(packet.getVelocidade());
        }

        return buffer.array();
    }
    public static List<Packet> extractPackets(byte[] packetArray) {
        ByteBuffer buffer = ByteBuffer.wrap(packetArray);
        int numPackets = buffer.getInt();
        //int packetSize = (packetArray.length - 4) / numPackets;
        //int packetSize = Integer.BYTES + 2 * Double.BYTES + 2 * Integer.BYTES;

        List<Packet> packets = new ArrayList<>();
        /*for (int i = 0; i < numPackets; i++) {
            byte[] packetData = new byte[packetSize];
            buffer.get(packetData);
            packets.add(new Packet(packetData));
        }*/
        for (int i = 0; i < numPackets; i++) {
            int ip = buffer.getInt();
            double coordX = buffer.getDouble();
            double coordY = buffer.getDouble();
            int estadoPiso = buffer.getInt();
            int velocidade = buffer.getInt();

            packets.add(new Packet(ip, coordX, coordY, estadoPiso, velocidade));
        }

        return packets;
    }

    public Integer getIp() {
        return ip;
    }

    public void setIp(Integer ip) {
        this.ip = ip;
    }

    public double getCoordX() {
        return coordX;
    }

    public void setCoordX(double coordX) {
        this.coordX = coordX;
    }

    public double getCoordY() {
        return coordY;
    }

    public void setCoordY(double coordY) {
        this.coordY = coordY;
    }

    //public EstadoPiso getEstadoPiso() {return estadoPiso;}

    //public void setEstadoPiso(EstadoPiso estadoPiso) {this.estadoPiso = estadoPiso;}

    //public Velocidade getVelocidade() {return velocidade;}

    //public void setVelocidade(Velocidade velocidade) {this.velocidade = velocidade;}


    public Integer getEstadoPiso() {
        return estadoPiso;
    }

    public void setEstadoPiso(Integer estadoPiso) {
        this.estadoPiso = estadoPiso;
    }

    public Integer getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(Integer velocidade) {
        this.velocidade = velocidade;
    }

    byte[] serialize() throws IOException {

        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream);
        oo.writeObject(this);
        oo.close();
        return bStream.toByteArray();
    }

    public Packet deserialize(byte[] recBytes) throws IOException, ClassNotFoundException {
        ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(recBytes));
        Packet messageClass = (Packet) iStream.readObject();
        iStream.close();

        return messageClass;
    }



}
