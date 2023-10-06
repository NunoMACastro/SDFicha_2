import java.net.*;
import java.io.*;
import java.util.*;

public class UDPServer {

    public static Integer getLast(LinkedHashMap<Integer, String> lhm) {
        int count = 1;

        for (Map.Entry<Integer, String> it : lhm.entrySet()) {
            if (count == lhm.size()) {
                return it.getKey();
            }
            count++;
        }
        return 0;
    }

    public static void main(String[] args) {
        DatagramSocket aSocket = null;

        try {
            aSocket = new DatagramSocket(6789);
            byte[] buffer = new byte[1000];

            LinkedHashMap<Integer, String> listaEmOrdem = new LinkedHashMap<Integer, String>();
            LinkedHashMap<Integer, String> listaTemp = new LinkedHashMap<Integer, String>();
            ArrayList<String> mensagensTemporarias = new ArrayList<>();
            Integer proximaMensagemEsperada = 1;
            Integer lastKey = 0;

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                String msgRecebida = new String(request.getData()).trim();

                Integer numero = Integer.valueOf(msgRecebida.split(",")[0]);
                String msg = msgRecebida.split(",")[1];


                if(numero.equals(proximaMensagemEsperada)) {
                    String reply = "";
                    System.out.println("Mensagem em ordem!");
                    listaEmOrdem.put(numero, msg);
                    proximaMensagemEsperada++;

                    if(mensagensTemporarias.isEmpty()){
                        System.out.println("última mensagem processada");
                        reply = "última mensagem processada";
                        DatagramPacket reply_F = new DatagramPacket(reply.getBytes(), reply.length(), request.getAddress(), request.getPort());
                        aSocket.send(reply_F);
                    }
                    else {

                        while (mensagensTemporarias.contains(proximaMensagemEsperada + ",")) {
                            int index = mensagensTemporarias.indexOf(proximaMensagemEsperada + ",");
                            String mensagemTemporaria = mensagensTemporarias.remove(index);
                            String[] partesTemporarias = mensagemTemporaria.split(",");
                            System.out.println("Processando mensagem temporária " + proximaMensagemEsperada + ": " + partesTemporarias[1]);
                            listaEmOrdem.put(proximaMensagemEsperada, partesTemporarias[1]);
                            proximaMensagemEsperada++;
                        }
                    }
                }
                else {
                    System.out.println("Recebida mensagem fora de ordem " + numero + ": " + msg);
                    mensagensTemporarias.add(msgRecebida);

                    // Notificar o cliente sobre a mensagem que está em falta
                    String reply = "waitingfor," + proximaMensagemEsperada;
                    DatagramPacket reply_F = new DatagramPacket(reply.getBytes(), reply.length(), request.getAddress(), request.getPort());
                    aSocket.send(reply_F);
                }

            }
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null) {
                aSocket.close();
            }
        }
    }
}

