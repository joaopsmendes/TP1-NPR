import java.util.ArrayList;
import java.util.List;

public class MultiPacket {

    private List<Packet> packets;

    public MultiPacket() {
        packets = new ArrayList<>();
    }

    public void addPacket(Packet packet) {
        packets.add(packet);
    }

    public List<Packet> getPackets() {
        return packets;
    }
}
