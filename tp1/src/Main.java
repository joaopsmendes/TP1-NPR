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


        if (args[0].equals("V")) {

            Veiculo v = new Veiculo(InetAddress.getByName(ip));
            //Veiculo v = new Veiculo();

        }else if(args[0].equals("RSU")){
            String ip2 = args[2];
            RSU rsu = new RSU(InetAddress.getByName(ip), InetAddress.getByName(ip2));

        }else if(args[0].equals("SV")) {
            Servidor sv = new Servidor();
        }
    }
}
