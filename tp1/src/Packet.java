import java.io.*;
import java.net.InetAddress;
import java.util.List;

public class Packet implements Serializable{

    private int msgType;//tipo de mensagem a enviar //0
    private int custo;
    private String matricula;
    private double velocidade;
    private String tipo;
    private EstadoPiso estadoPiso;

    //hoe to create enum
    public enum EstadoPiso {
        SECO, CHUVA, NEVE, GELO
    }

    public Packet(int msgType, int custo, String matricula, double velocidade, String tipo, EstadoPiso estadoPiso) {
        this.msgType = msgType;
        this.custo = custo;
        this.matricula = matricula;
        this.velocidade = velocidade;
        this.tipo = tipo;
        this.estadoPiso = estadoPiso;
    }
    

    public Packet() {
        this.msgType = 0;
        this.custo = 0;
        this.matricula = "";
        this.velocidade = 0;
        this.tipo = "";
        this.estadoPiso = EstadoPiso.SECO;
    }

    public Packet(Packet p){
        this.msgType = p.getMsgType();
        this.custo = p.getCusto();
        this.matricula = p.getMatricula();
        this.velocidade = p.getVelocidade();
        this.tipo = p.getTipo();
        this.estadoPiso = p.getEstadoPiso();
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

    

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getCusto() {
        return custo;
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

}
