
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


public class Servidor{

    //pos: 106,438

    final double xServer = 106.0;
    final double yServer = 438.0;


    public List<Integer> veiculosEmRange;

    public List<Packet> SVdatabase;

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;



    private InetAddress ip;

    ReentrantLock lockDB;

    public Servidor(InetAddress ipRsu) throws IOException{

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        //this.databaseSV = new HashMap<>();

        this.SVdatabase = new ArrayList<>();

        this.veiculosEmRange = new ArrayList<>();

        this.lockDB = new ReentrantLock();

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    Thread.sleep(1000);

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket arrayRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(arrayRecebido);

                    try {
                        byte[] receivedData = Arrays.copyOfRange(arrayRecebido.getData(), arrayRecebido.getOffset(), arrayRecebido.getLength());
                        List<Packet> packetsRecebidos = Packet.extractPackets(receivedData);

                        //if(debug) System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());

                        for (Packet p : packetsRecebidos) {

                            if(Packet.checkDistance(p.getCoordX(),p.getCoordY(),xServer, yServer)<500){//distancia ao servidor! 500m

                                lockDB.lock();
                                try{
                                    SVdatabase.add(p);
                                }finally {
                                    lockDB.unlock();
                                }

                                if(!veiculosEmRange.contains(p.getIp())){

                                    veiculosEmRange.add(p.getIp());
                                }

                                System.out.println("SV: Pacote adicionado à db: [ "+ p.getType() + "|" + p.getIpaddress()+ "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                                //System.out.println("Received Packet: ip=" + p.getIp() + ", coordX=" + p.getCoordX() + ", coordY=" + p.getCoordY() + ", estadoPiso=" + p.getEstadoPiso() + ", velocidade=" + p.getVelocidade());
                            }else{

                                System.out.println("SV: Veiculo ["+p.getIp() + "] fora da area de interesse!\n");

                                veiculosEmRange.remove(p.getIp());//se tiver remove!
                            }
                        }
                    }catch (Exception e) {
                        //System.out.println("depois de ");
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                //System.out.println("depois de ");
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> { // enviar msg ao RSU e depois encaminhar até à area especifica!
            try {
                while(true) {
                    //pacote:
                    //msg Type-> 3 !
                    //inetAddress -> null
                    //ip-> nodo que vai em execesso de velocidade
                    //coordX -> raio à volta do carro em causa, enviar msg para veiculos nessa area
                    //coordY -> tipo de warning |0-  |1- excesso velocidade  |2-   |3-
                    //estadoPiso -> avisar estado do piso! // 0->seco | 1->chuva | 2->neve | 3->gelo
                    //velocidade -> velocidade max na area

                    if(!SVdatabase.isEmpty()){

                        for(Packet p : SVdatabase){
                            ArrayList<Packet> Lpacotes= new ArrayList<>();
                            int veloMax=0;
                            //double warningType = 1;
                            //double raioDeAviso = 100;
                            int nodoSujeito = p.getIp();
                            int piso = p.getEstadoPiso();// 0->seco | 1->chuva | 2->neve | 3->gelo



                            if(p.getCoordX()<220){//velocidade max: 50km/h
                                veloMax = 50;

                            }else if (p.getCoordX()<310 && p.getCoordX()>220){//velocidade max: 90km/h
                                veloMax = 90;

                            }else {//velocidade max: 120km/h
                                veloMax = 120;

                            }

                            if(p.getVelocidade()>veloMax){

                                Packet Psend = new Packet(3, null,nodoSujeito,p.getCoordX(),p.getCoordY(),piso,veloMax);
                                byte[] datab = Packet.createPacketArray(Lpacotes);
                                DatagramPacket requestb = new DatagramPacket(datab,datab.length,ipRsu,4321);
                                socketEnviar.send(requestb);

                                System.out.println("SV: Mensagem de Warning encaminhada para o RSU !");

                            }
                        }
                        lockDB.lock();
                        try{
                            //Lpacotes.addAll(SVdatabase);
                            SVdatabase.clear();
                        }finally {
                            lockDB.unlock();
                        }
                    }

                    //Thread.sleep(10000);//10s
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            while(true) {

                try {
                    Thread.sleep(15000);//15s

                        //System.out.println("$" + entry.getKey() + " : Recent Info: " + entry.getValue().get(databaseRSU.size() - 1).toString());
                        System.out.println("SV: Número de veiculos na região de interesse: " + veiculosEmRange.size());
                        //System.out.println("Velocidade Recomendada: 100 Km/h");

                    //System.out.println("RSU ON!");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

}