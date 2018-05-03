package ClientServerGame;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class Cliente {

    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 6;
    private static boolean playing = true;
    private static char[][] boardToShowPlayer1 = new char[NUM_ROWS][NUM_COLS];
    private static char[][] boardToShowPlayer2 = new char[NUM_ROWS][NUM_COLS];

    public static void main(String[] args){

        if (args.length < 2) {
            System.out.println("Specify an address and a port to connect. ");
            return;
        }

        String address;
        int port;
        address = args[0];
        port = Integer.parseInt(args[1]);

        try {
            Socket socketPort = new Socket(address, port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socketPort.getInputStream()));
            PrintWriter writer = new PrintWriter(socketPort.getOutputStream(),true);
            Scanner scanner = new Scanner(System.in);

            receiveBoardsToShow(reader);//TODO: no se si esta bien
            printBoardsToShow();
            String entrance = waitForServerOutput(reader);
            System.out.println(entrance);

            while (playing) {

                entrance = waitForServerOutput(reader);
                System.out.println(entrance);

                if(entrance.contains("win")) {
                    entrance = waitForServerOutput(reader);
                    System.out.println(entrance);
                }

                System.out.println("Say the coordinates.");
                String jugada = scanner.nextLine();
                while (!validPlay(jugada)){
                    System.out.println("Escribe la jugada. ");
                    jugada = scanner.nextLine();
                }

                sendServerData(writer,jugada);//ENVIAR JUGADA
                String bomba = waitForServerOutput(reader);//ESPERAR RESPUESTA

                if (bomba.equals("null")){
                    System.out.println("Carry on! You discovered a new square without bomb!");
                }
                else if (bomba.equals("bomba")){
                    System.out.println("No! Soldier! You've stepped on a bomb! You lose your turn!");
                }

                receiveBoardsToShow(reader);
                printBoardsToShow();
                waitForServerOutput(reader);

            }

        } catch (UnknownHostException exp) {
            System.err.print(exp);

        } catch (IOException e) {
            System.err.print(e);
        }
    }

    private static void printBoardsToShow() {
        System.out.println("---------------------MY BOARD-------------------------------------");
        drawVisitedBoard(boardToShowPlayer1);
        System.out.println("---------------------OPPONENT BOARD--------------------------------");
        drawVisitedBoard(boardToShowPlayer2);
        System.out.println("------------------------------------------------------------------");
    }

    private static void receiveBoardsToShow(BufferedReader bufReader) throws IOException {
        receiveVisitedBoard(bufReader, true);
        receiveVisitedBoard(bufReader, false);
    }

    private static void sendServerData(PrintWriter writer, String s) {
        writer.println(s);
        writer.flush();
    }

    private static String waitForServerOutput(BufferedReader bufferedReader) throws IOException {

        String entrada = "";
        while((entrada.equals(""))){
            if(bufferedReader.ready())
                entrada = bufferedReader.readLine();
        }
        return entrada;
    }

    public static void receiveVisitedBoard(BufferedReader stream, boolean isMyBoard) throws IOException{

        if(isMyBoard){
            while (!stream.ready()) {
            }

            for (int i = 0; i < NUM_COLS; i++)
                for(int j = 0; j < NUM_COLS; j++)
                    boardToShowPlayer1[i][j] = (char)stream.read();
        }
        else{
            while (!stream.ready()) {
            }
            for (int i = 0; i < NUM_COLS; i++)
                for(int j = 0; j < NUM_COLS; j++)
                    boardToShowPlayer2[i][j] = (char)stream.read();
        }
    }

    private static void drawVisitedBoard(char[][]m){
        try {
            int rows = m.length;
            int columns = m[0].length;
            String str = "|\t";
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    str += m[i][j] + "\t";
                }
                System.out.println(str + "|");
                str = "|\t";
            }
        } catch (Exception e) {
            System.out.println("Matrix is empty!!");
        }
    }

    private static boolean validPlay(String play){

        StringTokenizer st = new StringTokenizer(play);
        if (st.countTokens() != 2){
            return false;
        }
        else{
            int coord1 = Integer.parseInt(st.nextToken());
            int coord2 = Integer.parseInt(st.nextToken());
            if ((coord1 < 0 || coord1 > NUM_ROWS - 1) || (coord2 < 0 || coord2 > NUM_COLS - 1)){
                return false;
            }
        }
        return true;
    }
}
