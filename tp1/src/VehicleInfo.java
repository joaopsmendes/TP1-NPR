import java.net.InetAddress;

public class VehicleInfo {
        //private InetAddress address;
        private final Integer nodeNumber;
        private final double x;
        private final double y;
        private final long updateTime;
        private final InetAddress carIP;
        
        public VehicleInfo(int nodeNumber, double x, double y, long updateTime,InetAddress carIP) {
            //this.address = address;
            this.nodeNumber = nodeNumber;
            this.x = x;
            this.y = y;
            this.updateTime = updateTime;
            this.carIP = carIP;
        }

    public Integer getNodeNumber() {
        return nodeNumber;
    }

    public InetAddress getCarIP() {
        return carIP;
    }
    public double getX() {
            return x;
        }
    public double getY() {
            return y;
        }

    public long getUpdateTime() {
            return updateTime;
        }

    @Override
    public String toString() {
        return "VehicleInfo{" +
                "Coordx=" + x +
                ", Coordy=" + y +
                ", updateTime=" + updateTime +
                ", IP=" + carIP +
                '}';
    }
}