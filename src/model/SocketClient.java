/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author italo oliveira
 */
public class SocketClient {

    static Socket clientSocket;
    DataOutputStream outToServer;
    DataInputStream inFromServer;
   

    public boolean abrirConexao(String ipConexao, int portaTCP) {

        try {
            clientSocket = new Socket(ipConexao, portaTCP);

            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new DataInputStream(clientSocket.getInputStream());
            System.out.println("Conexao TCP aberta ");

        
            
            if (clientSocket.isConnected()) {

                return true;
            }

        } catch (IOException ex) {
          
            System.out.println("Não foi possvivel abrir conexao TCP ");
        }

        return false;
    }

    public boolean abrirConexaoComModem(String imei) throws InterruptedException {
        int tamanhoBuffer;
        byte[] buffer = new byte[10000];
        byte[] entradaBytes = null;
        int BUFFERSIZE = 10000;
        int i;
        byte[] resposta = null;
        try {

            String ativacao = "ATIVAR;" + imei + ";";
            outToServer.write(ativacao.getBytes());

            Thread.sleep(2000);

            if (inFromServer.available() > -1) {

                tamanhoBuffer = inFromServer.read(buffer);
                entradaBytes = new byte[BUFFERSIZE]; // criando um array
                entradaBytes = buffer;

                i = tamanhoBuffer;

                if (i > 0) {
                    resposta = new byte[i];

                    System.arraycopy(entradaBytes, 0, resposta, 0, i);
                }
                String str = new String(resposta, StandardCharsets.UTF_8);

                System.out.println("String: " + str);

                if (str.contains("OK")) {

                    return true;
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(SocketClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;

    }

    public byte [] enviarComando(String comando, int delay) throws InterruptedException, IOException {

        int tamanhoBuffer;
        byte[] buffer = new byte[10000];
        byte[] entradaBytes = null;
        int BUFFERSIZE = 10000;
        int i;
        byte[] resposta = null;

        int time = delay;
        outToServer.write(hexStringToByteArray(comando));

        Thread.sleep(1000);

        while (time > 0) {

            if (inFromServer.available() > 2) {
                //System.out.println("Chegou TCP");
                tamanhoBuffer = inFromServer.read(buffer);
                entradaBytes = new byte[BUFFERSIZE]; // criando um array
                entradaBytes = buffer;
                i = tamanhoBuffer;

                if (i > 0) {
                    resposta = new byte[i];
                    System.arraycopy(entradaBytes, 0, resposta, 0, i);
                }
                String respost = bytesToHex(resposta);
                System.out.println(respost);

                
                if (resposta.length > 0) {
                    
                   return resposta;
                }
            } else {
                Thread.sleep(1000);
                time--;
                
                System.out.println("Time: " + time);
            }

            if (time == 0) {
                System.out.println("[Enviarcomando]: Timeout comando");
            }// comando para atualizacao 
            
        }
        return null;
    }

    public boolean fecharConexao (){
        
       try {
           clientSocket.close();
           return true;
       } catch (IOException ex) {
           System.out.println("Nao foi possivel fechar o socket TCP");
       }
        return false;
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();

        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
