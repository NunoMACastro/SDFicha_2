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

    public static void main(String args[]) {
        DatagramSocket aSocket = null;

        try {
            aSocket = new DatagramSocket(6789);
            byte[] buffer = new byte[1000];

            LinkedHashMap<Integer, String> listaEmOrdem = new LinkedHashMap<Integer, String>();
            LinkedHashMap<Integer, String> listaTemp = new LinkedHashMap<Integer, String>();
            Integer quantidade = 0;
            Integer lastKey = 0;

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);

                String msgRecebida = new String(request.getData()).trim();

                Integer numero = Integer.valueOf(msgRecebida.split(",")[0]);
                String msg = msgRecebida.split(",")[1];

                lastKey = getLast(listaEmOrdem);

                if (quantidade != numero) {
                    System.out.println("Desfazado: Mensagem vai para lista Temporária!");
                    listaTemp.put(numero, msg);

                    // Notificar o cliente da mensagem seguinte à última mensagem recebida em ordem
                    int nextExpectedMessage = lastKey + 1;
                    String notificationMsg = "waitingfor," + nextExpectedMessage;
                    DatagramPacket notification = new DatagramPacket(notificationMsg.getBytes(), notificationMsg.length(), request.getAddress(), request.getPort());
                    aSocket.send(notification);
                } else {
                    System.out.println("Mensagem em ordem!");
                    listaEmOrdem.put(numero, msg);

                    // Entregar todas as mensagens em ordem presentes na estrutura temporária
                    while (listaTemp.containsKey(lastKey + 1)) {
                        lastKey++;
                        String nextMsg = listaTemp.get(lastKey);
                        listaEmOrdem.put(lastKey, nextMsg);
                        listaTemp.remove(lastKey);
                    }

                    // Enviar uma resposta para o cliente para indicar que a mensagem foi processada
                    String replyMsg;
                    if (listaTemp.isEmpty()) {
                        replyMsg = "última mensagem processada";
                    } else {
                        int nextExpectedMessage = lastKey + 1;
                        replyMsg = "waitingfor," + nextExpectedMessage;
                    }
                    DatagramPacket reply = new DatagramPacket(replyMsg.getBytes(), replyMsg.length(), request.getAddress(), request.getPort());
                    aSocket.send(reply);
                }

                quantidade = listaEmOrdem.size();
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
