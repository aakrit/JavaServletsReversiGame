package hw3;

/**
 * Created with IntelliJ IDEA.
 * User: aakritprasad
 * Date: 8/19/13
 * Time: 6:09 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Scanner;

import java.util.Scanner;

// pesudo Code
// Create game in Main

public class ReversiGame {

    //game fields
    public int[][] board;

    private static final int totalrow = 8; //num of rows on the game board
    private static final int totalcol = 8; //num of cols on the game board
    public static final int B = 0; //black color team
    public static final int W = 1; //white color team
    public static final int EMPTY = -1;
    public static final int valid_move = 0;
    public static final int invalid_move = -1;
    public static final String white = "WHITE";
    public static final String black = "BLACK";
    public static final String tie = "TIE";

    public int nextRow = 0;
    public int nextCol = 0;
    public boolean gameRunning = false;
    public String quittingPlayer = null, winningPlayer = null;

//	public static final int winning_move = 1;


    public int playersCount = 0;
    public int observersCount = 0;
    public int playerBlackId = 0;
    public int playerWhiteId = 0;
    public int players = 0;//default start with two players and one observer
    public Player player[] = new Player[2];
    public int currentPlayer = 0;//used to loop for the currentplayer output

    public ReversiGame()
    {
        board = new int[totalrow][totalcol]; //create board
        //initial board with empty values
        for(int i = 0; i < totalrow; ++i)
            for(int j = 0; j < totalcol; ++j)
                board[i][j] = EMPTY;
        initialBoardSet();
    }
    public int getPlayersCount()
    {
        return playersCount;
    }
    public String getPlayerColor(int i)
    {
        //use the players id to get their color
        if(i == 0)
            return "BLACK";//black
        else
            return "WHITE";//white
    }
    public void addPlayerToBoard(int id, String name, int i){
        if(i == 0)
        {
            int color = 0;     //black
            playerBlackId = id;
            player[i] = new Player(id, name, color);
        }
        else if(i == 1)
        {
            int color = 1;//white
            playerWhiteId = id;
            player[i] = new Player(id, name, color);
        }
        else
        {
            observersCount++;
        }
        playersCount++;
    }
    public void resetPlayers()
    {
        player[0] = null;
        player[1] = null;
        playersCount = 0;
    }
    public int[][] getBoard() {
        return board;
    }
    public int getBoardValueAtPosition(int row, int col)
    {
        return (board[row][col]);
    }

    public void togglePlayers()
    {
        if(currentPlayer == 0)
        {
            currentPlayer = 1;
        }
        else
            currentPlayer = 0;
    }
    private void initialBoardSet()
    {
        board[3][3] = B;
        board[3][4] = W;
        board[4][3] = W;
        board[4][4] = B;
    }
    //method to reset the board
    public void resetBoard()
    {
        for(int i = 0; i < totalrow; ++i)
            for(int j = 0; j < totalcol; ++j)
                board[i][j] = EMPTY;
        initialBoardSet();
    }
    public int countPlayerPoints(int player)
    {
        int points = 0;
        for (int[] row : board)
        {
            for (int value : row)
            {
                if(value == W && player == W)
                {
                    points++;
                }
                else if(value == B && player == B)
                {
                    points++;
                }
            }
        }
        return points;
    }
    //return true if game is over, false otherwise
    public boolean checkIfGameIsNotDone()
    {
        for(int i = 0; i < totalrow; ++i)
        {
            for(int j = 0; j < totalcol; ++j)
            {
                if(board[i][j] == EMPTY)
                {
                    return true;
                }
            }
        }
        return false;//only return false if game is over
    }
    //method to show board current layout
    public String showBorad() //trying to make it look decent
    {
        StringBuffer s = new StringBuffer();
        int j = 0;
//        String white = "\u25CB", black = "\u25CF", empty = "\u2212";
        String white = "W", black = "B", empty = "-";

        s.append("\n");
        s.append("   |     |     |     |     |     |     |     |     |   \n");
        //i was using \r\n after each row but over HTTP in mac the \r prints as a &#13 so removed it here
        for(int i = -1; i < 8; i++)
        {
            if(i == -1)
            {
                s.append("   |  ");
            }
            else
            {
                s.append(i+"  |  ");
            }
        }
        s.append("\n");
        s.append("___|_____|_____|_____|_____|_____|_____|_____|_____|___\n");
        s.append("   |     |     |     |     |     |     |     |     |   \n");
        for (int[] row : board)
        {
            s.append(j+"  |  ");
            for (int value : row)
            {
                if(value == B)
                {
                    s.append(black+"  |  ");
                }
                else if (value == W)
                {
                    s.append(white+"  |  ");
                }
                else
                    s.append("   |  ");
            }
            s.append("\n");
            s.append("___|_____|_____|_____|_____|_____|_____|_____|_____|___\n");
            s.append("   |     |     |     |     |     |     |     |     |   \n");
            j++;
        }

        return s.toString();
    }
    //method for player to place a marker on the board
    //if returns -1, move is invalid, else move is valid
    public int markBoard(int row, int col, int markercolor)
    {
        if(markercolor != B && markercolor != W)
        {
            System.out.println("Incorrect Marker Color, Server Error!");
            return -1;
        }
        int input = isPlacementOk(row, col, markercolor);
        if (input == invalid_move)
        {
            return invalid_move; //return -1
        }
        else
        {
//			board[row][col] = markercolor;
            return valid_move; //return 0
        }
    }

    //method to check whether marker placement is allowed
    private int isPlacementOk(int row, int col, int markercolor)
    {
        //check if location is within board
        if ((row >= totalrow || row < 0) || (col >= totalcol || col < 0))
            return invalid_move;
            //check if location is on an empty square
        else if (board[row][col] != EMPTY)
            return invalid_move;
            //check if location is next to an actual value and points
        else if (check(row, col, markercolor) == false)
        {
            return invalid_move;
        }
        else
            return valid_move;
    }
    //method to check whether the same color connects two rows, cols,
    //or diagnols are along same line
    private boolean check(int row, int col, int color)
    {
        board[row][col] = color;
        int points = 0;
        boolean somepoints = false;
        //check each direction
        for (int i = -1; i < 2; ++i)//for i=row from -1 to 0 to 1
        {
            for (int j = -1; j < 2; ++j)//for j=col from -1 to 0 to 1
            {
                points = getPoints(row + i, col + j, color, i, j);
                if (points > 0)//can get points in that direction
                {
                    somepoints = true;
                    setPoints(row + i, col + j, color, i, j);
                    points = 0;
                }
            }
        }
        if(somepoints)//user can get points by placing the marker at this location
            return true;
        else
        {
            board[row][col] = EMPTY;
            return false;
        }
    }
    private int getPoints(int row, int col, int color, int rowchange, int colchange)
    {
        int i = row, j = col, points = 0;
        while (true)
        {
            if (i < 0 || j < 0 || i > 7 || j > 7 || board[i][j] == EMPTY)
                return 0;
            else if (board[i][j] == color)
                return points;
            else
                points++;

            i += rowchange;
            j += colchange;
        }
    }
    private void setPoints(int row, int col, int color, int rowchange, int colchange)
    {
        int i = row, j = col;
        while (true)
        {
            if (i < 0 || j < 0 || i > 7 || j > 7 || board[i][j] == EMPTY)
            {
                return;
            }
            else if (board[i][j] == color)
                return;
            else
            {
//		    	  System.out.println(i + " " + j );
                board[i][j] = color;//change the color of that piece
            }
            i += rowchange;
            j += colchange;
        }
    }
    //method to check who the winner is
    //count up all the Blacks and Whites
    public String winner()
    {
        int blackcount = 0;
        int whitecount = 0;

        for (int[] row : board)
        {
            for (int value : row)
            {
                if(value == B)
                    blackcount++;
                else if(value == W)
                    whitecount++;
            }
        }
        if (blackcount > whitecount)
            return black;
        else if(blackcount < whitecount)
            return white;
        else //(blackcount == whitecount)
            return tie;
    }

}