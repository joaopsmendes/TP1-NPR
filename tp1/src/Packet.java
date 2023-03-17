import java.io.*;
import java.net.InetAddress;
import java.util.List;

public class Packet implements Serializable{

    private int msgType;//tipo de mensagem a enviar //0
    
    private int custo;
    

    public Packet() {
        this.msgType = 0;
    }

    public Packet(byte[] bytes) {

        try {
            Packet msg = deserialize(bytes);
            this.custo = msg.getCusto();
            this.msgType = msg.getMsgType();
            this.vizinhos = msg.getVizinhos();
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

    public Packet(int msgType, int data, List<InetAddress> vizinhos) {
        this.msgType = msgType;
        this.custo = data;
        this.vizinhos = vizinhos;
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

}
