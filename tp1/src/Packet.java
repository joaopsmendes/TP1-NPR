import java.io.*;
import java.net.InetAddress;
import java.util.List;

public class Packet implements Serializable{

    private int msgType;//tipo de mensagem a enviar //0
    private int custo;
    private String matricula;
    private double velocidade;
    private String tipo;
    private double coordX;
    private double coordY;
    private EstadoPiso estadoPiso;


    //hoe to create enum
    enum EstadoPiso {
        SECO, CHUVA, NEVE, GELO
    }

    public Packet(int msgType, int custo, String matricula, double velocidade, String tipo,double coordX, double coordY, estadoPiso) {
        this.msgType = msgType;
        this.custo = custo;
        this.matricula = matricula;
        this.velocidade = velocidade;
        this.tipo = tipo;
        this.coordX = coordX;
        this.coordY = coordY;
        this.estadoPiso = estadoPiso;
    }
    

    public Packet(byte[] bytes) {

        try {
            Packet msg = deserialize(bytes);
            this.custo = msg.getCusto();
            this.msgType = msg.getMsgType();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getCusto() {
        return custo;
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

    

    public EstadoPiso getEstadoPiso() {
        return estadoPiso;
    }

    public void setEstadoPiso(EstadoPiso estadoPiso) {
        this.estadoPiso = estadoPiso;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public double getVelocidade() {
        return velocidade;
    }

    public void setVelocidade(double velocidade) {
        this.velocidade = velocidade;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setCusto(int custo) {
        this.custo = custo;
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
}
