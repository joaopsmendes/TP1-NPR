import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Packet implements Serializable {
    //private Integer msgType;//1- info pos | 2- info esatdo (segundo int nº pacotes) | 3- warning do SV
    //int -> nº pacotes
    private Integer type;//1- info normal | 2- info bulk
    private InetAddress ipaddress;
    private Integer ip;//número do nodo CORE
    private double coordX;
    private double coordY;
    private Integer estadoPiso;// 0->seco | 1->chuva | 2->neve | 3->gelo
    private Integer velocidade;
    //private Velocidade velocidade;
    //private EstadoPiso estadoPiso;

//identificador de 0 ou 1 para distinguir se é info normal ou vulk

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

    public Packet(Integer type,InetAddress ipaddress,Integer ip, double coordX, double coordY, int estadoPiso, int velocidade) {
        this.type = type;
        this.ipaddress = ipaddress;
        this.ip = ip;
        this.coordX = coordX;
        this.coordY = coordY;
        this.estadoPiso = estadoPiso;
        this.velocidade = velocidade;
    }

    public Packet( Packet p){
        this.type = p.getType();
        this.ipaddress = p.getIpaddress();
        this.ip = p.getIp();
        this.coordY = p.getCoordY();
        this.coordX = p.getCoordX();
        this.estadoPiso = p.getEstadoPiso();
        this.velocidade = p.getVelocidade();
    }
    public String toString() {
        return "Packet [PktType =" + type  + "|id=" + ip + "|Coordenadas= (" + coordX + "," + coordY +
                ")|EstadoPiso=" + estadoPiso + "|Velocidade=" + velocidade + "]";
    }

    public static byte[] createPacketArray(List<Packet> packets) {//varios pacotes a enviar!
        int numPackets = packets.size();
        int packetSize = 4 * Integer.BYTES + 2 * Double.BYTES + 16;
        int totalSize = Integer.BYTES + (numPackets * packetSize); // 4 bytes for the packet count

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putInt(numPackets);

        for (Packet packet : packets) {

            buffer.putInt(packet.getType());

            //byte[] addressBytes = packet.getIpaddress().getAddress(); // Get the byte array representation of InetAddress

            buffer.put(packet.getIpaddress().getAddress());//???????

            buffer.putInt(packet.getIp());
            buffer.putDouble(packet.getCoordX());
            buffer.putDouble(packet.getCoordY());
            buffer.putInt(packet.getEstadoPiso());
            buffer.putInt(packet.getVelocidade());
        }

        return buffer.array();
    }
    public static List<Packet> extractPackets(byte[] packetArray) throws UnknownHostException {
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
            int type = buffer.getInt();

            byte[] addressBytes = new byte[16];
            buffer.get(addressBytes);
            InetAddress ipaddress = InetAddress.getByAddress(addressBytes);
            //InetAddress ipaddress = buffer.get()
            //InetAddress ipaddress = buffer.get();
            int ip = buffer.getInt();
            double coordX = buffer.getDouble();
            double coordY = buffer.getDouble();
            int estadoPiso = buffer.getInt();
            int velocidade = buffer.getInt();

            packets.add(new Packet(type,ipaddress,ip, coordX, coordY, estadoPiso, velocidade));
        }

        return packets;
    }

    public InetAddress getIpaddress() {
        return ipaddress;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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

    public static double checkDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }



}
