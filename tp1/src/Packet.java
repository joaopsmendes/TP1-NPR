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
    private String ip;
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

    public Packet(String ip, double coordX, double coordY, int estadoPiso, int velocidade) {
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

    /*public Packet(byte[] arr) {

        int offset = 0;
        byte[] ipBytes = Arrays.copyOfRange(arr, offset, offset + 16);
        this.ip = new String(ipBytes).trim(); // IP address - string 16 bytes
        offset += 16;
        this.coordX = ByteBuffer.wrap(Arrays.copyOfRange(arr, offset, offset + 8)).getDouble();
        offset += 8;
        this.coordY = ByteBuffer.wrap(Arrays.copyOfRange(arr, offset, offset + 8)).getDouble();
        offset += 8;
        this.estadoPiso = EstadoPiso.valueOf(new String(Arrays.copyOfRange(arr, offset, offset + 5)).trim()); // estadoPiso  string
        offset += 5;
        int velocidadeValue = ByteBuffer.wrap(Arrays.copyOfRange(arr, offset, offset + 4)).getInt(); // velocidade  int
        offset += 4;
        for (Velocidade v : Velocidade.values()) {
            if (v.getValor() == velocidadeValue) {
                this.velocidade = v;
                break;
            }
        }
    }*/
    public Packet(byte[] data) {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte[] ipBytes = new byte[16];
        buffer.get(ipBytes);
        this.ip = new String(ipBytes).trim();
        this.coordX = buffer.getDouble();
        this.coordY = buffer.getDouble();
        this.estadoPiso = buffer.getInt();
        this.velocidade = buffer.getInt();
    }
    public static byte[] createByteArray(Packet packet) {
        return packet.packetToByteArray();
    }
    //how to do!

    /*
    List<Packet> packetList = ...; // list of packets
    byte[][] packets = new byte[packetList.size()][];
    for (int i = 0; i < packetList.size(); i++) {
    packets[i] = packetList.get(i).toByteArray();
    }
    */
    public static byte[] createPacketArray(byte[][] packets) {//varios pacotes a enviar!
        int numPackets = packets.length;
        int packetSize = packets[0].length;
        int totalSize = 4 + (numPackets * packetSize); // 4 bytes for the packet count

        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        buffer.putInt(numPackets);

        for (byte[] packet : packets) {
            buffer.put(packet);
        }

        return buffer.array();
    }
    public static List<Packet> extractPackets(byte[] packetArray) {
        ByteBuffer buffer = ByteBuffer.wrap(packetArray);
        int numPackets = buffer.getInt();
        int packetSize = (packetArray.length - 4) / numPackets;

        List<Packet> packets = new ArrayList<>();
        for (int i = 0; i < numPackets; i++) {
            byte[] packetData = new byte[packetSize];
            buffer.get(packetData);
            packets.add(new Packet(packetData));
        }

        return packets;
    }

    /*ublic static List<Packet> extractPackets(byte[] packetArray) {//definir tamanho do pacote!
        // Extract the number of packets from the first four bytes
        ByteBuffer buffer = ByteBuffer.wrap(packetArray);
        int numPackets = buffer.getInt();
        int packetSize = 1024;

        // Create a 2D byte array to hold the packets
        //byte[][] packets = new byte[numPackets][packetSize];

        List<Packet> allPacketsReceived = new ArrayList<>();

        // Loop over the remaining bytes in the array and extract the packets
        int offset = 4; // Skip the first four bytes
        for (int i = 0; i < numPackets; i++) {
            byte[] packet = new byte[packetSize];
            System.arraycopy(packetArray, offset, packet, 0, packetSize);
            allPacketsReceived.add(new Packet(packet)); //construtor byte[]
            offset += packetSize;
        }

        return allPacketsReceived;
    }*/
    public byte[] packetToByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(28);
        buffer.put(this.ip.getBytes());
        buffer.putDouble(this.coordX);
        buffer.putDouble(this.coordY);
        buffer.putInt(this.estadoPiso);
        buffer.putInt(this.velocidade);

        return buffer.array();
    }

    /*public byte[] packetToByteArray() {
        // Calculate the total length of the byte array
        int totalLength = 16 + 8 + 8 + 5 + 4;

        byte[] ipBytes = Arrays.copyOf(this.ip.getBytes(), 16);
        byte[] coordXBytes = ByteBuffer.allocate(8).putDouble(this.coordX).array();
        byte[] coordYBytes = ByteBuffer.allocate(8).putDouble(this.coordY).array();
        byte[] estadoPisoBytes = Arrays.copyOf(this.estadoPiso.toString().getBytes(), 5);
        byte[] velocidadeBytes = ByteBuffer.allocate(4).putInt(this.velocidade.getValor()).array();

        byte[] result = new byte[totalLength];
        int offset = 0;
        System.arraycopy(ipBytes, 0, result, offset, ipBytes.length);
        offset += ipBytes.length;
        System.arraycopy(coordXBytes, 0, result, offset, coordXBytes.length);
        offset += coordXBytes.length;
        System.arraycopy(coordYBytes, 0, result, offset, coordYBytes.length);
        offset += coordYBytes.length;
        System.arraycopy(estadoPisoBytes, 0, result, offset, estadoPisoBytes.length);
        offset += estadoPisoBytes.length;
        System.arraycopy(velocidadeBytes, 0, result, offset, velocidadeBytes.length);

        return result;
    }*/

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
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
