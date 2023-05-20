import java.io.IOException;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Veiculo {

    //o rui vai em excesso de velocidade e esta na pos xy :
    //msg bullk, (typo msg, numero de entradas)

    final double xRSU = 277.0;
    final double yRSU = 428.0;

    public double x;
    public double y;

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public Map<Integer, ArrayList<Packet>> database; //no array get("list".size()-1) para ultimo added! (numero do nodo?)

    private final Boolean debug = false;

    public Integer vehicleNodeNumber;

    public List<Packet> DBlist;

    //public Map<InetAddress,VehicleInfo> neighborTable;
    public List<VehicleInfo> neighborList;

    int timeout;

    ReentrantLock lockDB;
    ReentrantLock lockVizinhos;


    public Veiculo(InetAddress ip) throws SocketException, UnknownHostException {



        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.database = new HashMap<>();

        this.DBlist = new ArrayList<>();

        this.lockDB = new ReentrantLock();
        this.lockVizinhos = new ReentrantLock();

        //this.neighborTable = new HashMap<>();
        this.neighborList = new ArrayList<>();

        this.timeout = 1000;//timeout para atualizar neighbor table!

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    Thread.sleep(1000);
                    //System.out.println("Veiculo " + ipAddress + " ON!\n");

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket arrayRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(arrayRecebido);

                    try {

                        byte[] receivedData = Arrays.copyOfRange(arrayRecebido.getData(), arrayRecebido.getOffset(), arrayRecebido.getLength());
                        List<Packet> packetsRecebidos = Packet.extractPackets(receivedData);

                        if(debug) System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());//debug

                        if(packetsRecebidos.size()==1) {//info de pos

                            for (Packet p : packetsRecebidos) {

                                if (p.getIp().equals(vehicleNodeNumber)) continue;//check se vem do mesmo!
                                System.out.println("<- Pacote Recebido: [$ " + p.getIp() + " $|" + p.getVelocidade() + "|" + p.getEstadoPiso() + "|" + p.getCoordX() + "|" + p.getCoordY() + "]");


                                if (p.getType().equals(1)) {//pacote com info de posição (tipo 1) packetsRecebidos.get(packetsRecebidos.size()-1).getType().equals(1)

                                    double tempX = p.getCoordX();
                                    double tempY = p.getCoordY();

                                    lockVizinhos.lock();
                                    try{
                                        //ver se o vizinho ja se encontra na tabela!
                                        int flag =0;
                                        for(VehicleInfo neighborInfo : neighborList){
                                            if(neighborInfo.getNodeNumber().equals(p.getIp())){
                                                flag = 1;
                                                break;
                                            }
                                        }

                                        if(flag==0) neighborList.add(new VehicleInfo(p.getIp(),tempX , tempY, System.currentTimeMillis(), p.getIpaddress()));
                                        System.out.println("ip adicionado: "+ p.getIpaddress());
                                        System.out.println("Nodo " + p.getIp() + "adicionado à tabela de vizinhos!\n");

                                    }finally {
                                        lockVizinhos.unlock();
                                    }
                                }
                            }
                        }else {

                            for (Packet p : packetsRecebidos) {

                                System.out.println("<- Pacote Recebido: [$ " + p.getIp() + " $|" + p.getVelocidade() + "|" + p.getEstadoPiso() + "|" + p.getCoordX() + "|" + p.getCoordY() + "]");
                                if (p.getIp().equals(vehicleNodeNumber)) continue;//check se vem do mesmo!

                                if (p.getType().equals(2)) {//pacote com info de estado (tipo 2)

                                    lockDB.lock();
                                    try{
                                        DBlist.add(p);
                                    }finally {
                                        lockDB.unlock();
                                    }
                                }
                            }
                        }
                        System.out.println();
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

        new Thread(() -> { // enviar msg period -> broadcast
            try {
                while(true) {

                    //ler ficheiro e tirar coords
                    //ler primeiros 3 chars da diretoria atual
                    Path currentPath = Paths.get("").toAbsolutePath();
                    Path targetPath = currentPath.getParent().getParent().getParent();
                    String directoryName = targetPath.getFileName().toString();
                    String vehicle_nID = directoryName.substring(0, Math.min(directoryName.length(), 3));

                    String vehicleIDint = directoryName.substring(1, Math.min(directoryName.length(), 3));
                    vehicleNodeNumber = Integer.parseInt(vehicleIDint);

                    // Read the file with the prefix in the parent directory
                    // Read the coordinates from the file
                    Path filePath = targetPath.getParent().resolve(vehicle_nID + ".xy");
                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                        String line = reader.readLine();
                        String[] tokens = line.split(" ");

                        x = Double.parseDouble(tokens[0]);
                        y = Double.parseDouble(tokens[1]);

                        System.out.println("A ler " + vehicle_nID + ".xy ... OK! ["+x+","+y+"]");

                    } catch (IOException e) {
                        System.out.println("ERRO: ficheiro .xy não foi lido!");
                    }

                    // create the broadcast address
                    socketEnviar.setBroadcast(true);
                    InetAddress broadcastAddr = InetAddress.getByName("ff02::1");//multicast addr

                    //enviar apenas a info propria !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!| INFO PROPRIA |!!!!!!!!!!!!!
                    List<Packet> Lpacotes = new ArrayList<>();

                    //adicionar o proprio  type = 1 (info do veiculo)
                    Lpacotes.add(new Packet(1,ip,vehicleNodeNumber, x, y, 0, 0));

                    byte[] datab = Packet.createPacketArray(Lpacotes);

                    //tipo 2
                    DatagramPacket requestb = new DatagramPacket(datab,datab.length,broadcastAddr,4321);

                    if(debug) System.out.println(">Bytes a enviar: " + Arrays.toString(datab));

                    socketEnviar.send(requestb);

                    System.out.println("! Posição enviada para o grupo Multicast !");
                    for(Packet p : Lpacotes){
                        System.out.println("-> Pacote Enviado: [ "+ p.getType() + "|" + p.getIpaddress()+ "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                    }

                    Thread.sleep(5000);//5s
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();


        new Thread(() -> { // enviar msg info de estado -> direcionada!
            try {
                while (true) {

                    if(!neighborList.isEmpty()){

                        double menorDistance = 10000000;
                        InetAddress nodoDestinoIP = null;
                        int nodoDestino = 0;

                        for(VehicleInfo Vinfo : neighborList) {
                            if (Vinfo.getX() == xRSU && Vinfo.getY() == yRSU) { //rsu na lista
                                //envia para ele
                                nodoDestinoIP = Vinfo.getCarIP();
                                nodoDestino = Vinfo.getNodeNumber();
                                break;

                            } else { //rsu nao ta na lista
                                double distanciaAux = Packet.checkDistance(Vinfo.getX(), Vinfo.getY(), xRSU, yRSU);
                                if (distanciaAux < menorDistance) {
                                    menorDistance = distanciaAux;
                                    nodoDestinoIP = Vinfo.getCarIP();
                                    nodoDestino = Vinfo.getNodeNumber();

                                }
                            }
                        }

                        List<Packet> listaPackets = new ArrayList<>();

                        Packet p = new Packet(2,ip, vehicleNodeNumber ,0, 0, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade());

                        if(!DBlist.isEmpty()){

                            lockDB.lock();
                            try{
                                listaPackets.addAll(DBlist);
                                //listaPackets = new ArrayList<>(DBlist);//adiconar tudo da data base! para ser encaminhado pelos nodos até ao RSU
                                DBlist.clear();//limpar a Data Base depois de enviar!
                                System.out.println("-> Database cleared <-");
                            }finally {
                                lockDB.unlock();
                            }
                        }
                        listaPackets.add(p);//adicionar o proprio

                        byte[] d = Packet.createPacketArray(listaPackets);
                        DatagramPacket pack = new DatagramPacket(d, d.length, nodoDestinoIP, 4321);

                        System.out.println("Nodo destino: "+ nodoDestino + "IP: " + nodoDestinoIP);//????

                        socketEnviar.send(pack);

                        System.out.println("Info de ESTADO enviada para " + nodoDestino);

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        //Atualizar tabela de vizinhos
        new Thread(() -> {
            while (true) {
                long currentTime = System.currentTimeMillis();

                lockVizinhos.lock();
                try{

                    neighborList.removeIf(vi -> currentTime - vi.getUpdateTime() > timeout);

                }finally {
                    lockVizinhos.unlock();
                }
                // sleep for some time
                try {
                    Thread.sleep(5000); // run every 5 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}