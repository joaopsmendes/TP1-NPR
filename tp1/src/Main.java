import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.out.println("syntax: java Main ...");
            return;
        }

        String ipRSU = args[1];

        if (args[0].equals("S")) {

            Servidor s = new Servidor(InetAddress.getByName(ipRSU));

        } else if (args[0].equals("V")) {

            Veiculo v = new Cliente(InetAddress.getByName(ipRSU));

        }
    }
}
