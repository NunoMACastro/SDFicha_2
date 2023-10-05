import java.net.*;
import java.io.*;


public class UDPClient{

    public static void main(String args[]){
        DatagramSocket aSocket = null;

        try {
            aSocket = new DatagramSocket();
            int i = 0;
            while (i < 10) {
                if(i == 4 || i == 7) { i++; }
                String temp = i + "," + "vou enviar esta mensagem ao servidor";
                byte[] m = temp.getBytes();
                InetAddress aHost = InetAddress.getByName("localhost");
                int serverPort = 6789;

                DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);

                aSocket.send(request);
                i++;


                byte[] buffer = new byte[1000];

                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

                aSocket.receive(reply);

                System.out.println("Reply: " + new String(reply.getData()));
            }

        }catch (SocketException e){System.out.println("Socket: " + e.getMessage());
        }catch (IOException e){System.out.println("IO: " + e.getMessage());
        }finally {if(aSocket != null) aSocket.close();}
    }
}
