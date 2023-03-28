import java.io.*;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;

public class Packet implements Serializable {

    private Integer msgType;//1- info normal | 2- info bulk (segundo int nº pacotes) | 3-....
    //int -> nº pacotes
    private InetAddress ip;
    private double coordX;
    private double coordY;
    private EstadoPiso estadoPiso;
    private Velocidade velocidade;


    private static final Random rand = new Random();

    public static Velocidade getRandomVelocidade() {
        Velocidade[] allVelocidades = Velocidade.values();
        int randomIndex = rand.nextInt(allVelocidades.length);
        return allVelocidades[randomIndex];
    }

    public static EstadoPiso getRandomEstadoPiso() {
        EstadoPiso[] allEstados = EstadoPiso.values();
        int randomIndex = rand.nextInt(allEstados.length);
        return allEstados[randomIndex];
    }

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

    public Packet(Integer type, InetAddress ip, double coordX, double coordY, EstadoPiso estadoPiso, Velocidade velocidade) {
        this.msgType = type;
        this.ip = ip;
        this.coordX = coordX;
        this.coordY = coordY;
        this.estadoPiso = estadoPiso;
        this.velocidade = velocidade;
    }

    public Packet( Packet p){
        this.msgType = p.getMsgType();
        this.ip = p.getIp();
        this.coordY = p.getCoordY();
        this.coordX = p.getCoordX();
        this.estadoPiso = p.getEstadoPiso();
        this.velocidade = p.getVelocidade();
    }


    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
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

    public EstadoPiso getEstadoPiso() {
        return estadoPiso;
    }

    public void setEstadoPiso(EstadoPiso estadoPiso) {
        this.estadoPiso = estadoPiso;
    }

    public Velocidade getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(Velocidade velocidade) {
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
