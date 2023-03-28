import java.io.IOException;
import java.net.InetAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        //String ip = "2001:13::7";

        //Veiculo v = new Veiculo(InetAddress.getByName(ip));

        if (args.length < 2) {
            System.out.println("syntax: javac Main ...");
            return;
        }

        String ip = args[1];

        if (args[0].equals("V")) {

            Veiculo v = new Veiculo(InetAddress.getByName(ip));


        }else if(args[0].equals("RSU")){
            RSU rsu = new RSU(InetAddress.getByName(ip));

        }
    }
}
