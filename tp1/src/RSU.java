
import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class RSU{

    //manter o RSU fixo??
    //receber a pos doi RSU? (850.0 , 220.0)
    //ip RSU1 : 2001:11::7

    private DatagramSocket socketEnviar;
    private DatagramSocket socketReceber;

    public Map<Integer, ArrayList<Packet>> databaseRSU;

    public List<VehicleInfo> neighborsList;
    public List<Packet> listDatabaseRSU;

    public List<Packet> waringsFromSV;

    public Integer nodeNumber;
    public Double x;
    public Double y;

    final double xRSU = 277.0;
    final double yRSU = 428.0;

    int timeout;

    ReentrantLock lockDB;
    ReentrantLock lockVizinhos;

    ReentrantLock lockwarnings;


    public RSU(InetAddress ipserver,InetAddress iprsu) throws IOException {

        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.databaseRSU = new HashMap<>();

        this.neighborsList = new ArrayList<>();

        this.listDatabaseRSU = new ArrayList<>();

        this.timeout = 1000;

        this.lockDB = new ReentrantLock();
        this.lockVizinhos = new ReentrantLock();

        this.lockwarnings = new ReentrantLock();



        /*new Thread(() -> {//por while
            while(true) {

                try {
                    Thread.sleep(10000);//10s
                    for (Map.Entry<Integer, ArrayList<Packet>> entry : databaseRSU.entrySet()) {
                        System.out.println("$" + entry.getKey() + " : Recent Info: " + entry.getValue().get(databaseRSU.size() - 1).toString());
                    }
                    //System.out.println("RSU ON!");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();*/

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

                        //if(debug) System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());//debug

                        if(packetsRecebidos.size()==1) {//info de pos

                            for (Packet p : packetsRecebidos) {

                                if (p.getIp().equals(nodeNumber)) continue;//check se vem do mesmo!
                                System.out.println("→ Pacote Recebido: [" + p.getType()+  "|" + p.getIp() + "|" + p.getIpaddress()+ "|" + p.getVelocidade() + "|" + p.getEstadoPiso() + "|" + p.getCoordX() + "|" + p.getCoordY() + "]");

                                if (p.getType().equals(1)) {//pacote com info de posição (tipo 1) packetsRecebidos.get(packetsRecebidos.size()-1).getType().equals(1)

                                    double tempX = p.getCoordX();
                                    double tempY = p.getCoordY();

                                    lockVizinhos.lock();
                                    try{

                                        int flag =0;//se 1 ja existe na tabela!
                                        for(VehicleInfo neighborInfo : neighborsList){
                                            if(neighborInfo.getNodeNumber().equals(p.getIp())){
                                                flag = 1;
                                                break;
                                            }
                                        }
                                        if(flag==0) neighborsList.add(new VehicleInfo(p.getIp(),tempX , tempY, System.currentTimeMillis(), p.getIpaddress()));
                                        System.out.println("RSU: Nodo " + p.getIp() + "adicionado à tabela de vizinhos!\n");
                                    }finally {
                                        lockVizinhos.unlock();
                                    }
                                }else if (p.getType().equals(3)) {//warning para os carros vindo do SV!

                                        lockwarnings.lock();
                                        try{
                                            waringsFromSV.add(p);

                                        }finally {
                                            lockwarnings.unlock();
                                        }
                                    }
                                }

                        }else {

                            for (Packet p : packetsRecebidos) {

                                System.out.println("→ Pacote Recebido: [" + p.getType()+  "|" + p.getIp() + "|" + p.getIpaddress()+ "|" + p.getVelocidade() + "|" + p.getEstadoPiso() + "|" + p.getCoordX() + "|" + p.getCoordY() + "]");
                                if (p.getIp().equals(nodeNumber)) continue;//check se vem do mesmo!

                                if (p.getType().equals(2)) {//pacote com info de estado (tipo 2)

                                    lockDB.lock();
                                    try{
                                        listDatabaseRSU.add(p);
                                    }finally {
                                        lockDB.unlock();
                                    }

                                    //mudar aqui, ver se o pacote n é repetido!! numero de sequncia? id de pacote unico?
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

        new Thread(() -> { // enviar msg ao SERVIDOR!
            try {
                while(true) {

                    if(!listDatabaseRSU.isEmpty()){

                        ArrayList<Packet> Lpacotes= new ArrayList<>();

                        lockDB.lock();
                        try{
                            Lpacotes.addAll(listDatabaseRSU);

                            listDatabaseRSU.clear();
                        }finally {
                            lockDB.unlock();
                        }

                        byte[] datab = Packet.createPacketArray(Lpacotes);
                        DatagramPacket requestb = new DatagramPacket(datab,datab.length,ipserver,4321);
                        //if(debug) System.out.println(">Bytes a enviar: " + Arrays.toString(datab));
                        socketEnviar.send(requestb);

                        for (Packet Psend : Lpacotes) {

                            System.out.println("$ Recent Info: " + Psend.toString());
                        }
                        System.out.println("RSU: Dados enviados ao servidor !");

                    }

                    Thread.sleep(10000);//10s
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> { // enviar msg pos -> broadcast (RSU envia posição para preencher tabelas de vizinhança!)
            try {
                while(true) {
                    //ler ficheiro e tirar coords
                    //ler primeiros 3 chars da diretoria atual
                    Path currentPath = Paths.get("").toAbsolutePath();
                    Path targetPath = currentPath.getParent().getParent().getParent();
                    String directoryName = targetPath.getFileName().toString();
                    String vehicle_nID = directoryName.substring(0, Math.min(directoryName.length(), 3));

                    String vehicleIDint = directoryName.substring(1, Math.min(directoryName.length(), 3));
                    nodeNumber = Integer.parseInt(vehicleIDint);

                    // Read the file with the prefix in the parent directory
                    // Read the coordinates from the file
                    Path filePath = targetPath.getParent().resolve(vehicle_nID + ".xy");
                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                        String line = reader.readLine();
                        String[] tokens = line.split(" ");

                        x = Double.parseDouble(tokens[0]);
                        y = Double.parseDouble(tokens[1]);

                        System.out.println(vehicle_nID + ".xy lido ✓ ("+x+","+y+")");

                    } catch (IOException e) {
                        System.out.println("ERRO: ficheiro .xy não foi lido!");
                    }

                    // create the broadcast address
                    socketEnviar.setBroadcast(true);
                    InetAddress broadcastAddr = InetAddress.getByName("ff02::1");//multicast addr

                    //enviar apenas a info propria !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!| INFO PROPRIA |!!!!!!!!!!!!!
                    List<Packet> Lpacotes = new ArrayList<>();

                    Lpacotes.add(new Packet(1,iprsu,nodeNumber, xRSU, yRSU, 0, 0));

                    byte[] datab = Packet.createPacketArray(Lpacotes);

                    //tipo 2
                    DatagramPacket requestb = new DatagramPacket(datab,datab.length,broadcastAddr,4321);
                    socketEnviar.send(requestb);

                    System.out.println("RSU: Posição enviada para o grupo Multicast !");
                    for(Packet p : Lpacotes){
                        System.out.println("← Pacote Enviado: [$ "+p.getIp()+" $|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                    }

                    Thread.sleep(5000);//5s
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> { // WARNINGS: enviar msg info do servidor -> para veiculos numa area!
            try {
                while (true) {

                    if(!waringsFromSV.isEmpty()){

                        for(Packet pWarning : waringsFromSV){//percorrer todos os warning a enviar!

                            double menorDistance = 10000000;
                            InetAddress nodoDestinoIP = null;
                            int nodoDestino = 0;

                            int flagE = 0;

                            for(VehicleInfo Vinfo : neighborsList) {
                                if(Packet.checkDistance(Vinfo.getX(),Vinfo.getY(),pWarning.getCoordX(),pWarning.getCoordY())<100){ //na area do veiculo em execesso de velocidade!

                                    List<Packet> listaPacketsInRange = new ArrayList<>();
                                    listaPacketsInRange.add(pWarning);//tratar um de cada vez!
                                    byte[] d = Packet.createPacketArray(listaPacketsInRange);
                                    DatagramPacket pack = new DatagramPacket(d, d.length, Vinfo.getCarIP(), 4321);
                                    socketEnviar.send(pack);
                                    System.out.println("RSU: Warning enviado para " + Vinfo.getNodeNumber());

                                    flagE=1;


                                } else { //rsu nao ta na lista
                                    double distanciaAux = Packet.checkDistance(Vinfo.getX(), Vinfo.getY(), pWarning.getCoordX(), pWarning.getCoordY());
                                    if (distanciaAux < menorDistance) {
                                        menorDistance = distanciaAux;
                                        nodoDestinoIP = Vinfo.getCarIP();
                                        nodoDestino = Vinfo.getNodeNumber();

                                    }
                                }
                            }
                            if(flagE!=1){//caso nenhum vizinho estiver a menos de 100 m enviar para o mais proximo do "nodo em execesso de velocidade"

                                List<Packet> listaPackets = new ArrayList<>();
                                listaPackets.add(pWarning);

                                byte[] d = Packet.createPacketArray(listaPackets);
                                DatagramPacket pack = new DatagramPacket(d, d.length, nodoDestinoIP, 4321);
                                socketEnviar.send(pack);
                                System.out.println("RSU: warning enviado para " + nodoDestino);

                            }
                        }
                        lockwarnings.lock();
                        try{
                            //warningsFromSv.addAll(DBlist);
                            //listaPackets = new ArrayList<>(DBlist);//adiconar tudo da data base! para ser encaminhado pelos nodos até ao RSU
                            waringsFromSV.clear();//limpar a Data Base depois de enviar!
                            System.out.println("Warnings Database cleared ✓");
                        }finally {
                            lockwarnings.unlock();
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();



        new Thread(() -> {
            while (true) {
                long currentTime = System.currentTimeMillis();

                lockVizinhos.lock();
                try{
                    neighborsList.removeIf(vi -> currentTime - vi.getUpdateTime() > timeout);

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