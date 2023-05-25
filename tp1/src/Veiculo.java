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

    final double xRSU2 = 983.0;
    final double yRSU2 = 457.0;





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

    public List<Packet> warningsFromSv;
    public List<Integer> warningsRecebidos;

    int timeout;

    ReentrantLock lockDB;
    ReentrantLock lockVizinhos;
    ReentrantLock lockWarnings;


    public Veiculo(InetAddress ip) throws SocketException, UnknownHostException {



        this.socketEnviar = new DatagramSocket(4000);
        this.socketReceber = new DatagramSocket(4321);

        this.database = new HashMap<>();

        this.DBlist = new ArrayList<>();

        this.lockDB = new ReentrantLock();
        this.lockVizinhos = new ReentrantLock();
        this.lockWarnings = new ReentrantLock();

        //this.neighborTable = new HashMap<>();
        this.neighborList = new ArrayList<>();

        this.warningsFromSv = new ArrayList<>();
        this.warningsRecebidos = new ArrayList<>();


        this.timeout = 10000;//timeout para atualizar neighbor table!

        new Thread(() -> { // THREAD PARA receber
            try {
                while (true) {
                    Thread.sleep(200);
                    //System.out.println("Veiculo " + ipAddress + " ON!\n");

                    byte[] bufferr = new byte[2048]; // Max size of a UDP packet
                    DatagramPacket arrayRecebido = new DatagramPacket(bufferr, bufferr.length);

                    socketReceber.receive(arrayRecebido);

                    try {

                        byte[] receivedData = Arrays.copyOfRange(arrayRecebido.getData(), arrayRecebido.getOffset(), arrayRecebido.getLength());
                        List<Packet> packetsRecebidos = Packet.extractPackets(receivedData);

                        if(debug) System.out.println(">Bytes recebidos: " + Arrays.toString(receivedData) + " Recebido de: "+ arrayRecebido.getAddress());//debug

                        //if(packetsRecebidos.size()==1) {//info de pos

                        for (Packet p : packetsRecebidos) {

                            if (p.getType().equals(1)) {//pacote com info de posição (tipo 1) packetsRecebidos.get(packetsRecebidos.size()-1).getType().equals(1)

                                if (p.getIp().equals(vehicleNodeNumber)) continue;//check se vem do mesmo!
                                //System.out.println("→ Pacote Recebido: [ "+ p.getType() + "|" + p.getIpaddress()+ "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                                //System.out.println("→ Pacote Recebido: [$ " + p.getIp() + " $|" + p.getVelocidade() + "|" + p.getEstadoPiso() + "|" + p.getCoordX() + "|" + p.getCoordY() + "]");

                                double tempX = p.getCoordX();
                                double tempY = p.getCoordY();

                                lockVizinhos.lock();
                                try{
                                    //ver se o vizinho ja se encontra na tabela!
                                    int flag =0;
                                    //VehicleInfo aux ;
                                    if(!neighborList.isEmpty()){

                                        for(VehicleInfo neighborInfo : neighborList){
                                            if(neighborInfo.getNodeNumber().equals(p.getIp()) && neighborInfo.getX()==p.getCoordX() && neighborInfo.getY()==p.getCoordY()){//se for o msm node com as mesmas coods não adiciona!
                                                flag = 1;//nodo está na msm pos , nao adicionar
                                                break;
                                            }else if(neighborInfo.getNodeNumber().equals(p.getIp())){

                                                neighborList.remove(neighborInfo);
                                            }
                                        }
                                    }

                                    if(flag==0) {

                                        neighborList.add(new VehicleInfo(p.getIp(),tempX , tempY, System.currentTimeMillis(), p.getIpaddress()));
                                        System.out.println("VEICULO: Nodo " + p.getIp() + " adicionado/atualizado na tabela de vizinhos! ✓  → Pacote Recebido: [ "+ p.getType() + "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]\n");
                                    }else{
                                        //System.out.println("VEICULO: Nodo " + p.getIp() + " já se encontra na tabela de vizinhos! ✓  → Pacote Recebido: [ "+ p.getType() + "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]\n");
                                    }


                                }finally {
                                    lockVizinhos.unlock();
                                }
                            } else if (!p.getType().equals(2) && !p.getType().equals(1) && !warningsRecebidos.contains(p.getType())) {//não é tipo 1 nem 2, quer dizer que é warning (numeros sequencial sempre a aumentar)

                                if(p.getIp().equals(vehicleNodeNumber)){//quer dizer que a msg é para este carro!

                                    warningsRecebidos.add( p.getType() );//adcionar numero do warning a tabela

                                    String piso = null;// 0->seco | 1->chuva | 2->neve | 3->gelo
                                    if(p.getEstadoPiso()==0) piso="Seco";
                                    else if(p.getEstadoPiso()==1) piso="Chuva";
                                    else if(p.getEstadoPiso()==2) piso="Neve";
                                    else if (p.getEstadoPiso()==3) piso="Gelo";


                                    //System.out.println("⚠ WARNING " + p.getType() + " recebido ⚠ : Vai em excesso de velocidade! | Velocidade Max: "+ p.getVelocidade() + "| Estado do Piso: "+ piso +"\n");
                                    System.out.println("-------------------------Warning recebido-------------------------------------------------------------------");
                                    System.out.println("⚠ WARNING " + p.getType() + " recebido ⚠ : Vai em excesso de velocidade! | Velocidade Max: "+ p.getVelocidade() + "| Estado do Piso: "+ piso +" → Pacote Recebido: [ "+ p.getType() + "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                                    System.out.println("---------------------------------------------------------------------------------------------------------\n");
                                }else if(Packet.checkDistance(x,y,p.getCoordX(),p.getCoordY())<100){//mensagem sobre outro veiculo!

                                    warningsRecebidos.add( p.getType() );//adcionar numero do warning a tabela

                                    System.out.println("-------------------------Warning recebido-----------------------------------------------------------------");
                                    System.out.println("⚠ WARNING " + p.getType() + " recebido ⚠ : O veiculo "+ p.getIp()+ " vai em excesso de velocidade perto da sua área! → Pacote Recebido: [ "+ p.getType() + "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]" );
                                    System.out.println("----------------------------------------------------------------------------------------------------------\n");
                                }
                                lockWarnings.lock();
                                try{
                                    warningsFromSv.add(p);

                                }finally {
                                    lockWarnings.unlock();
                                }


                            }else if(p.getType().equals(2)){

                                if (p.getIp().equals(vehicleNodeNumber)) continue;//check se vem do mesmo!
                                System.out.println("---------------------CAM recebida-----------------------------------------");
                                System.out.println("VEICULO: msg CAM do nodo "+p.getIp() +" guardada para envio ao RSU! ✓  → Pacote Recebido: [ "+ p.getType() + "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                                System.out.println("-------------------------------------------------------------------------\n");
                                lockDB.lock();
                                try{

                                    this.DBlist.removeIf(pjanalista -> pjanalista.getIp().equals(p.getIp()));
                                    DBlist.add(p);
                                }finally {
                                    lockDB.unlock();
                                }

                            }
                        }
                        //}
                        //System.out.println();
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

                    String fileReadStatus = "";

                    // Read the file with the prefix in the parent directory
                    // Read the coordinates from the file
                    Path filePath = targetPath.getParent().resolve(vehicle_nID + ".xy");
                    try (BufferedReader reader = new BufferedReader(new FileReader(filePath.toFile()))) {
                        String line = reader.readLine();
                        String[] tokens = line.split(" ");

                        x = Double.parseDouble(tokens[0]);
                        y = Double.parseDouble(tokens[1]);

                        //System.out.println(vehicle_nID + ".xy Lido ✓  ("+x+","+y+")\n");
                        fileReadStatus = " " + vehicle_nID + ".xy Lido ✓  ("+x+","+y+")";

                    } catch (IOException e) {
                        //System.out.println("ERRO: ficheiro .xy não foi lido!");
                        fileReadStatus = "ERRO: ficheiro .xy não foi lido!";
                    }

                     // Multicast address
                    //int port = 12345;

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

                    //System.out.print("VEICULO: Posição enviada para o grupo Multicast !\n");
                    for(Packet p : Lpacotes){
                        System.out.println(fileReadStatus + "\nVEICULO (vizinhança): Posição enviada para o grupo Multicast! ← Pacote Enviado: [ "+ p.getType() + "|" +p.getIp()+ "|" +p.getCoordX()+"|"+p.getCoordY()+"]\n");
                    }

                    Thread.sleep(1000);//5s
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();


        new Thread(() -> { // enviar msg info de estado -> direcionada ao RSU (bulk)! GREEDY FORWARDING
            try {
                while (true) {

                    if(!neighborList.isEmpty()){
                        //System.out.println("Thread de info de estado ikoqhbnuqbfuqbfu");
                        double distanceRsu1 = Packet.checkDistance(x, y, xRSU, yRSU);
                        double distanceRsu2 = Packet.checkDistance(x, y, xRSU2, yRSU2);

                        double menorDistance = Math.min(distanceRsu1, distanceRsu2);
                        
                        //double menorDistance = Packet.checkDistance(x,y,xRSU,yRSU);//distancia do proprio ao RSU
                        InetAddress nodoDestinoIP = null;
                        int nodoDestino = 0;
                        int flagD = 0;
                        int rsuDestino = 0;

                        lockVizinhos.lock();
                        try{

                            for(VehicleInfo Vinfo : neighborList) {
                                if (Vinfo.getX() == xRSU && Vinfo.getY() == yRSU) { //rsu na lista
                                    //envia para ele
                                    nodoDestinoIP = Vinfo.getCarIP();
                                    nodoDestino = Vinfo.getNodeNumber();
                                    rsuDestino = 1;
                                    flagD=1;
                                    break;

                                }else if(Vinfo.getX() == xRSU2 && Vinfo.getY() == yRSU2){

                                    nodoDestinoIP = Vinfo.getCarIP();
                                    nodoDestino = Vinfo.getNodeNumber();
                                    rsuDestino = 2;
                                    flagD=1;
                                    break;

                                }else { //rsu nao ta na lista
                                    double distanciaRsu1 = Packet.checkDistance(Vinfo.getX(), Vinfo.getY(), xRSU, yRSU);
                                    double distanciaRsu2 = Packet.checkDistance(Vinfo.getX(), Vinfo.getY(), xRSU2, yRSU2);

                                    double distanciaAux = 0;

                                    if(distanciaRsu1 < distanciaRsu2){
                                        distanciaAux = distanciaRsu1;
                                        rsuDestino = 1;
                                    }else{
                                        distanciaAux = distanciaRsu2;
                                        rsuDestino = 2;
                                    }

                                    if (distanciaAux < menorDistance) {
                                        menorDistance = distanciaAux;
                                        nodoDestinoIP = Vinfo.getCarIP();
                                        nodoDestino = Vinfo.getNodeNumber();

                                        flagD=1;//tem de haver um mais proximo else não envia!

                                    }
                                }
                            }
                        }finally {
                            lockVizinhos.unlock();
                        }

                        if(flagD==1){

                            List<Packet> listaPackets = new ArrayList<>();
                            Packet p = new Packet(2,ip, vehicleNodeNumber ,x, y, Packet.getRandomEstadoPiso(), Packet.getRandomVelocidade());
                            if(!DBlist.isEmpty()){

                                //System.out.println("");

                                lockDB.lock();
                                try{
                                    listaPackets.addAll(DBlist);
                                    //listaPackets = new ArrayList<>(DBlist);//adiconar tudo da data base! para ser encaminhado pelos nodos até ao RSU
                                    DBlist.clear();//limpar a Data Base depois de enviar!
                                    System.out.println("------Database cleared ✓");
                                }finally {
                                    lockDB.unlock();
                                }
                            }
                            listaPackets.add(p);//adicionar o proprio

                            byte[] d = Packet.createPacketArray(listaPackets);
                            DatagramPacket pack = new DatagramPacket(d, d.length, nodoDestinoIP, 4321);
                            socketEnviar.send(pack);
                            System.out.println("----------------------CAM enviada------------------------------------");
                            System.out.println("VEICULO (CAM): Info de ESTADO enviada para nodo " + nodoDestino+ "| RSU destino: " + rsuDestino +"! ← Pacote Enviado: [ "+ p.getType() + "|" +p.getIp()+"|"+p.getVelocidade()+"|"+p.getEstadoPiso()+"|"+p.getCoordX()+"|"+p.getCoordY()+"]");
                            System.out.println("-------------------------------------------------------------------\n");
                        }



                    }else{
                        System.out.println("debug: tabela vizinhos vazia!");
                    }
                    Thread.sleep(10000);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> { // WARNINGS: enviar msg info do servidor -> para veiculos numa area! mensagens individuais
            try {
                while (true) {

                    if(!warningsFromSv.isEmpty()){

                        for(Packet pWarning : warningsFromSv){//percorrer todos os warning a enviar!

                            double menorDistance = 10000000;
                            InetAddress nodoDestinoIP = null;
                            int nodoDestino = 0;

                            int flagE = 0;
                            int flagVizinhoNaAreaDeInteresseJaRecebeu = 0;

                            for(VehicleInfo Vinfo : neighborList) {

                                if((Vinfo.getX()==xRSU && Vinfo.getY()==yRSU) || (Vinfo.getX()==xRSU2 && Vinfo.getY()==yRSU2)) continue;// se for rsu nao enviar

                                if(Packet.checkDistance(Vinfo.getX(),Vinfo.getY(),pWarning.getCoordX(),pWarning.getCoordY())<100){ //na area do veiculo em execesso de velocidade!

                                    List<Packet> listaPacketsInRange = new ArrayList<>();
                                    listaPacketsInRange.add(pWarning);//tratar um de cada vez!
                                    byte[] d = Packet.createPacketArray(listaPacketsInRange);
                                    DatagramPacket pack = new DatagramPacket(d, d.length, Vinfo.getCarIP(), 4321);
                                    socketEnviar.send(pack);
                                    System.out.println("------------------------Warning enviado--------------------------------------");
                                    System.out.println("VEICULO: Warning " + pWarning.getType() + " enviado para " + Vinfo.getNodeNumber()+" (na área de interesse)");
                                    System.out.println("-----------------------------------------------------------------------------\n");

                                    flagVizinhoNaAreaDeInteresseJaRecebeu=1;

                                }else { //rsu nao ta na lista
                                    double distanciaAux = Packet.checkDistance(Vinfo.getX(), Vinfo.getY(), pWarning.getCoordX(), pWarning.getCoordY());
                                    if (distanciaAux < menorDistance) {
                                        menorDistance = distanciaAux;
                                        nodoDestinoIP = Vinfo.getCarIP();
                                        nodoDestino = Vinfo.getNodeNumber();

                                        flagE = 1;

                                    }
                                }
                            }
                            if(flagE==1 && flagVizinhoNaAreaDeInteresseJaRecebeu==0){//caso nenhum vizinho estiver a menos de 100 m enviar para o mais proximo do "nodo em execesso de velocidade"

                                List<Packet> listaPackets = new ArrayList<>();
                                listaPackets.add(pWarning);

                                byte[] d = Packet.createPacketArray(listaPackets);
                                DatagramPacket pack = new DatagramPacket(d, d.length, nodoDestinoIP, 4321);
                                socketEnviar.send(pack);
                                System.out.println("--------------------------Warning enviado--------------------------------------");
                                System.out.println("VEICULO: Warning " + pWarning.getType() + " enviado para " + nodoDestino + " (fora da área)");
                                System.out.println("------------------------------------------------------------------------------\n");
                            }
                        }
                        lockWarnings.lock();
                        try{
                            //warningsFromSv.addAll(DBlist);
                            //listaPackets = new ArrayList<>(DBlist);//adiconar tudo da data base! para ser encaminhado pelos nodos até ao RSU
                            warningsFromSv.clear();//limpar a Data Base depois de enviar!
                            System.out.println("------Warnings Database cleared ✓\n");
                        }finally {
                            lockWarnings.unlock();
                        }
                    }

                    Thread.sleep(100);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        //Atualizar tabela de vizinhos
        new Thread(() -> {
            while (true) {
                long currentTime = System.currentTimeMillis();

                //lockVizinhos.lock();
                //try{

                    neighborList.removeIf(vi -> currentTime - vi.getUpdateTime() > timeout);

                //}finally {
                    //lockVizinhos.unlock();
                //}
                // sleep for some time
                //Thread.sleep(5000); // run every 5 seconds
            }
        }).start();
    }

}