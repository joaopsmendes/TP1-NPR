import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Utils {

    public static DatagramPacket sendPackets(InetAddress address, int port, Packet[] packets) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeInt(2);//info bulk
        objectStream.writeInt(packets.length); // write the number of packets being sent as the second int
        for (Packet packet : packets) {
            objectStream.writeObject(packet); // write each packet to the stream
        }
        objectStream.flush();
        byte[] data = byteStream.toByteArray();
        //socket.send(packet);
        return new DatagramPacket(data, data.length, address, port);
    }
    public static DatagramPacket sendPacket(InetAddress address, int port, Packet packet) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeInt(2);//info bulk
        objectStream.writeInt(1); // write the number of packets being sent as the second int

        objectStream.writeObject(packet); // write each packet to the stream

        objectStream.flush();
        byte[] data = byteStream.toByteArray();
        //socket.send(packet);
        return new DatagramPacket(data, data.length, address, port);
    }

    public static double euclideanDistance(double x1, double y1, double x2, double y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
    }
}
