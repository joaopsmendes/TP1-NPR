import java.io.*;
import java.net.InetAddress;
import java.util.List;

public class Packet implements Serializable {
    private InetAddress ip;
    private double coordX;
    private double coordY;
    private EstadoPiso estadoPiso;
    private Velocidade velocidade;




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

    public Packet(InetAddress ip, double coordX, double coordY, EstadoPiso estadoPiso, Velocidade velocidade) {
        this.ip = ip;
        this.coordX = coordX;
        this.coordY = coordY;
        this.estadoPiso = estadoPiso;
        this.velocidade = velocidade;
    }

    public Packet( Packet p){
        this.ip = p.getIp();
        this.coordY = p.getCoordY();
        this.coordX = p.getCoordX();
        this.estadoPiso = p.getEstadoPiso();
        this.velocidade = p.getVelocidade();
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
