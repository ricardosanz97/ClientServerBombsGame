package ClientServerGame;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.StringTokenizer;

public class Servidor {

    private static final int HIDDEN_SQUARE = 0;
    private static final int BOMB_SQUARE = 1;
    private static final int VISITED_SQUARE = 2;
    private static final int NUM_MAPS = 20;
    private static final int NUM_BOMBS = 15;
    private static final int NUM_ROWS = 6;
    private static final int NUM_COLS = 6;
    private static int[][][] internalBoards = new int[NUM_MAPS][NUM_ROWS][NUM_COLS];
    private static char[][][] boardsToShow = new char[NUM_MAPS][NUM_ROWS][NUM_COLS];
    private static int[] bombsCounter = new int[NUM_MAPS];
    private static int[] visitedSquaresPlayer1 = new int[NUM_MAPS];
    private static int[] visitedSquaresPlayer2 = new int[NUM_MAPS];
    private static int mapPlayer1 = 0;
    private static int mapPlayer2 = 0;
    private static int playerWinner = 0;
    private static int turnPlayer = 1;//player1 starts
    private static Random rand = new Random();
    private static BufferedReader readerPlayer1;
    private static BufferedReader readerPlayer2;
    private static PrintWriter writerPlayer1;
    private static PrintWriter writerPlayer2;
    private static Socket socketPlayer1;
    private static Socket socketPlayer2;
    private static String isBombPlayer1;
    private static String isBombPlayer2;
    private static PrintWriter[] writers = new PrintWriter[2];

    public static void main(String[] args) throws IOException, InterruptedException {

            if (args.length < 1) {
                System.out.println("Not enough parameters.");
                return;
            }

            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));

            for (int i = 0; i < NUM_MAPS; i++){
                fillInternalBoard(i);
                fillBoardToWatch(i);
            }

        socketPlayer1 = serverSocket.accept();
        socketPlayer2 = serverSocket.accept();

        readerPlayer1 = new BufferedReader(new InputStreamReader(socketPlayer1.getInputStream()));
        readerPlayer2 = new BufferedReader(new InputStreamReader(socketPlayer2.getInputStream()));

        writerPlayer1 = new PrintWriter(socketPlayer1.getOutputStream(),false);
        writerPlayer2 = new PrintWriter(socketPlayer2.getOutputStream(), false);

        writers[0] = writerPlayer1;
        writers[1] = writerPlayer2;

        asignNewMaps();
        while(mapPlayer1 == mapPlayer2){
            asignNewMaps();
        }

        sendBoardToWatch(mapPlayer1, writerPlayer1);
        sendBoardToWatch(mapPlayer2, writerPlayer1);
        sendBoardToWatch(mapPlayer2, writerPlayer2);
        sendBoardToWatch(mapPlayer1, writerPlayer2);
        sendDataToAllClients("Para mandar las jugadas escriba el numero de fila y el numero de columna separados por un espacio.", writers);

        while (true){

            while (turnPlayer == 1) {

                sendDataToClient(writerPlayer1, "Player1's turn: ");
                String playPlayer1 = waitForClientInput(readerPlayer1);
                processPlayReceived(playPlayer1, 1);
                sendBoardToWatch(mapPlayer1, writerPlayer1);//el mio
                sendBoardToWatch(mapPlayer2, writerPlayer1);//el del otro
                sendDataToClient(writerPlayer1, "waiting");
                checkWinCondition(1);
            }

            while (turnPlayer == 2) {

                sendDataToClient(writerPlayer2, "Player2's turn: ");
                String playPlayer2 = waitForClientInput(readerPlayer2);
                processPlayReceived(playPlayer2, 2);
                sendBoardToWatch(mapPlayer2, writerPlayer2);//el mio
                sendBoardToWatch(mapPlayer1, writerPlayer2);//el del otro
                sendDataToClient(writerPlayer2, "waiting");
                checkWinCondition(2);
            }
            showWinnerInfo();

        }
    }

    private static void showWinnerInfo(){
        if (playerWinner == 1) {
            asignNewMaps();
            sendDataToAllClients("Player 1 wins, restarting match with new maps!", writers);
            resetData();
        } else if (playerWinner == 2) {
            asignNewMaps();
            sendDataToAllClients("Player 2 wins, restarting match with new maps!", writers);
            resetData();
        }
    }

    private static void resetData() {
        visitedSquaresPlayer1 = new int[NUM_MAPS];
        visitedSquaresPlayer2 = new int[NUM_MAPS];
        playerWinner = 0;
        turnPlayer = 1;
        resetMaps();
    }

    private static void resetMaps(){
        for (int i = 0; i < NUM_MAPS; i++){
            for (int j = 0; j < NUM_ROWS; j++){
                for (int k = 0; k < NUM_ROWS; k++){
                    if (internalBoards[i][j][k] == VISITED_SQUARE){
                        internalBoards[i][j][k] = HIDDEN_SQUARE;
                    }
                }
            }
        }
    }

    private static void asignNewMaps() {
        int rand1 = rand.nextInt(NUM_MAPS);
        while (rand1 == mapPlayer1) {
            rand1 = rand.nextInt(NUM_MAPS);
        }
        mapPlayer1 = rand1;

        int rand2 = rand.nextInt(NUM_MAPS);
        while (rand2 == mapPlayer2) {
            rand2 = rand.nextInt(NUM_MAPS);
        }
        mapPlayer2 = rand2;
    }

    private static void sendDataToAllClients(String msg, PrintWriter[] writers) {
        for (PrintWriter writer :
                writers) {
            sendDataToClient(writer, msg);
        }
    }

    private static void processPlayReceived(String jugada, int player) throws IOException {
        StringTokenizer stringTokenizer = new StringTokenizer(jugada);
        int row = Integer.parseInt(stringTokenizer.nextToken());
        int col = Integer.parseInt(stringTokenizer.nextToken());

        if (player == 1) {
            isBombPlayer1 = "null";
            if (internalBoards[mapPlayer1][row][col] == BOMB_SQUARE) {
                boardsToShow[mapPlayer1][row][col] = 'B';
                isBombPlayer1 = "bomba";

            } else if (internalBoards[mapPlayer1][row][col] != BOMB_SQUARE) {

                internalBoards[mapPlayer1][row][col] = VISITED_SQUARE;
                boardsToShow[mapPlayer1][row][col] = 'V';
                visitedSquaresPlayer1[mapPlayer1]++;
            }
            sendDataToClient(writerPlayer1, isBombPlayer1);

        } else if (player == 2){
            isBombPlayer2 = "null";
            if (internalBoards[mapPlayer2][row][col] == BOMB_SQUARE) {
                boardsToShow[mapPlayer2][row][col] = 'B';
                isBombPlayer2 = "bomba";
            } else if (internalBoards[mapPlayer2][row][col] != BOMB_SQUARE) {
                internalBoards[mapPlayer2][row][col] = VISITED_SQUARE;
                boardsToShow[mapPlayer2][row][col] = 'V';
                visitedSquaresPlayer2[mapPlayer2]++;

            }
            sendDataToClient(writerPlayer2, isBombPlayer2);
        }
    }

    private static void checkWinCondition(int player) {

        if (player == 1){
            if (isBombPlayer1 == "bomba"){
                turnPlayer = 2;
                isBombPlayer1 = "null";
            }
            else if (visitedSquaresPlayer1[mapPlayer1] >= (internalBoards[0].length * internalBoards[0][0].length) - bombsCounter[mapPlayer1]){
                playerWinner = 1;
                turnPlayer = 0;
            }
        }

        else if (player == 2){
            if (isBombPlayer2 == "bomba"){
                turnPlayer = 1;
                isBombPlayer2 = "null";
            }
            else if (visitedSquaresPlayer2[mapPlayer2] >= (internalBoards[0].length * internalBoards[0][0].length) - bombsCounter[mapPlayer2]){
                playerWinner = 1;
                turnPlayer = 0;
            }
        }
    }

    private static void sendDataToClient(PrintWriter writer1, String s) {
        writer1.println(s);
        writer1.flush();
    }

    private static String waitForClientInput(BufferedReader reader) throws IOException {
        String response = "";
        while(response.equals("")){
            if(reader.ready()) {
                response = reader.readLine();
            }
        }
        return response;
    }

    private static void fillInternalBoard(int whichBoard) {
        Random rnd = new Random();
        int bombs = 0;
        int[][] m = internalBoards[whichBoard];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                if (rnd.nextInt() % 2 == 0 && bombs < NUM_BOMBS) {
                    m[i][j] = BOMB_SQUARE;
                    bombs++;
                }else{
                    m[i][j] = HIDDEN_SQUARE;
                }
            }
        }
        bombsCounter[whichBoard] = bombs;
    }

    private static void fillBoardToWatch(int matrixIndex){
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_ROWS; j++) {
                boardsToShow[matrixIndex][i][j] = 'X';
            }
        }
    }

    private static void sendBoardToWatch(int whichBoard, PrintWriter writer) {
        for (int i = 0; i < boardsToShow[0].length; i++)
            for (int j = 0; j < boardsToShow[0][0].length; j++)
                writer.write(boardsToShow[whichBoard][i][j]);
    }

}

