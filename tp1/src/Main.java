import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        //String ip = "2001:13::7";

        //Veiculo v = new Veiculo(InetAddress.getByName(ip));
        //System.out.println("syntax: java Main V/RSU");

        if (args.length < 2) {
            System.out.println("syntax error!");
            return;
        }

        String ip = args[1];

        switch (args[0]) {
            case "V" -> {
                Veiculo v = new Veiculo(InetAddress.getByName(ip));
            }
            //Veiculo v = new Veiculo();

            case "RSU" -> {
                RSU rsu = new RSU(InetAddress.getByName(ip));
            }
            case "SV" -> {
                Servidor sv = new Servidor();
            }
        }
    }
}
