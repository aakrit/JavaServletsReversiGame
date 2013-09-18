package hw3;

/**
 * Created with IntelliJ IDEA.
 * User: aakritprasad
 * Date: 8/19/13
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

import org.w3c.dom.Text;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

//a proof-of-concept servlet to play the role of reversi server
//based on XML tunneled over http
public class Servlet extends HttpServlet {
    // this method is called once when the servlet is first loaded
//    private ArrayList<ReversiGame> games = new ArrayList<ReversiGame>(10);
    private static ReversiGame game;


    public void init() {
        // initialize 10 boards
//        for(ReversiGame r: games){
//            r = new ReversiGame();
//      }
        game = new ReversiGame(); //create a board

    }
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        // first goal is to be able to receive and process
        // a well formatted XML move piggybacked on a POST
        BufferedReader br = new BufferedReader( new InputStreamReader( req.getInputStream(), "UTF-8" ) );
        String line = null;
        StringBuffer input = new StringBuffer();

        while( (line = br.readLine()) != null )
        {
            input.append(line);
        }
        System.out.println("Server POST REQ INPUT: \n" + input);
//
        String inputAsString = input.toString();
        // parse the XML and marshal the java Move object
        String move = null;
        try {
            move = marshalJavaStringFromXMLForDoPostResponse(processPostInput(inputAsString));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // convert move into xml and return

        PrintWriter out = res.getWriter();

        if(move.equals(null))
            out.print("Invalid move");
        else   {
            out.println(move);
        }
        out.flush();
        out.close();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException
    {
        //process input to tell which player is sending the get request
//        System.out.println("Server GET REQ INPUT: \n");
        String move = null;
//        BufferedReader br = new BufferedReader( new InputStreamReader( req.getInputStream(), "UTF-8" ) );
//        String line = null;
//        StringBuffer input = new StringBuffer();
//
//        while( (line = br.readLine()) != null ) {
//            input.append(line);
//        }
//        System.out.print(input);
////
//        String inputAsString = input.toString(); //use client id to find out who they are
//        try {
//            move = processGetReqInput(inputAsString);//determine what to send to the client based on who they are
//
//        } catch (Exception e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
        PrintWriter out = res.getWriter();
        try {
            move = processGetReqInput();
            String responseBack = null;
            if(move.equalsIgnoreCase("gameRunning"))
            {
                //send the player id and the board
                responseBack = createBoardDisplayInXML();
//                System.out.println("Sending GET REP to client: " + responseBack);
                out.print(responseBack);
            }
            else
            {
                responseBack = move;
//                System.out.println("Sending GET REP to client: " + responseBack);
//                System.out.println(marshalJavaStringFromXMLForDoGetResponse(responseBack));
                out.print(marshalJavaStringFromXMLForDoGetResponse(responseBack));
            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
//        out.print(move);
        out.flush();
        out.close();
    }
    private static String marshalJavaStringFromXMLForDoPostResponse(String s) throws Exception   //return an xmlString
    {
        //create a java dom tree with the java game info
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("server_response");
        document.appendChild(root);
        Text text = document.createTextNode(s);
        root.appendChild(text);

        //convert the java dom tree to an xml string for the server,
        StringWriter sw = new StringWriter();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();
    }
    private static String marshalJavaStringFromXMLForDoGetResponse(String s) throws Exception   //return an xmlString
    {
        //create a java dom tree with the java game info
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("server_get_response");
        document.appendChild(root);
        Text text = document.createTextNode(s);
        root.appendChild(text);

        //convert the java dom tree to an xml string for the server,
        StringWriter sw = new StringWriter();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        return sw.toString();
    }

    private static String processGetReqInput() throws Exception
    {
        //convert string to DOM to get id of player to detmine if they're a player or obsever
        int playerID;
        String gameResult;
        //check to see if the game has started
        if(!game.gameRunning)//game not started yet
        {
            return "gameNotstarted";
        }
        else if(gameOver())          //if the game is over
        {
            gameResult = game.winner();
            game.resetPlayers();
            game.resetBoard();
            if(gameResult.equals("TIE"))
                return "TIE";
            else
                return (gameResult);
        }
        else  //game is being played
        {
            return "gameRunning";
            //return the id and color of the player whose turn it is as well as the board

        }
//        try
//        {
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//factory.setIgnoringElementContentWhitespace(true);
////            factory.setValidating(true);
//            DocumentBuilder builder = factory.newDocumentBuilder();
//            Document document = null;
//            Element root = null;
//            try{
//                document = builder.parse(new InputSource(new StringReader(xmlString)));
//                root = document.getDocumentElement();
//
//            } catch (Exception e)
//            {
//                e.printStackTrace(); System.out.print("String reader exception");
//            }
//            if(root.equals(null))
//                throw new RuntimeException("Error building parser");
//            //find out which players turn it is
//
//            if(root.getTagName().equals("user_info"))      //new user is connecting
//            {
//                playerID = Integer.parseInt(root.getAttribute("id"));
//                if((playerID == game.playerBlackId && game.currentPlayer == 0) ||
//                        (playerID == game.playerWhiteId && game.currentPlayer == 1))  //if its the player whose turn it is return 'move'
//                {
//                    return marshalJavaStringFromXMLForDoGetResponse("move");
//                }
//                else if((playerID == game.playerBlackId && game.currentPlayer == 1) ||
//                        (playerID == game.playerWhiteId && game.currentPlayer == 0))  //if its the player waiting return 'wait'
//                {
//                    return marshalJavaStringFromXMLForDoGetResponse("wait");
//                }
//                else  //if its an observer
//                {
//                    return marshalJavaStringFromXMLForDoGetResponse(game.showBorad());
////                    return marshalBoardDisplayToXML();
//                }
//            }  //else if (some other xml sent
//        }catch (Exception e)
//        {
//            System.out.print(e);
//            System.out.println("DOM PARSING ERROR: Returning NULL2");
//            return null;
//        }
//        return null;
    }

    private static String processPostInput(String xmlString) throws Exception
    {
        int moveLocation;
        String color = null;
        int playerID;
        String gameResult = null;
        if(gameOver())
        {
            gameResult = game.winner();
            game.resetPlayers();
            game.resetBoard();
            if(gameResult.equals("TIE"))
                return "The Game is over, it was a TIE Game";
            else
                return "The Game is over and the winner was Player: "+gameResult;
        }
        //build a DOM from XML manage code
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();//factory.setIgnoringElementContentWhitespace(true);
//            factory.setValidating(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = null;
            Element root = null;
            try{
                document = builder.parse(new InputSource(new StringReader(xmlString)));
                root = document.getDocumentElement();

            } catch (Exception e)
            {
                e.printStackTrace(); System.out.print("String reader exception");
            }
            //TODO: need to add XML Validator per schema
//            if(!xmlValidation(xmlString));
//                System.out.print("XML VALIDATION ISSUE");

            if(root.equals(null))
                throw new RuntimeException("Error building parser");

            if(root.getTagName().equalsIgnoreCase("name"))      //new user is connecting
            {
                playerID = Integer.parseInt(root.getAttribute("id"));
                Element nameElement = (Element) document.getElementsByTagName("name").item(0);
                String  name = (String) nameElement.getTextContent();
                if(game.getPlayersCount() == 0)  //no players
                {
                    game.addPlayerToBoard(playerID, name, 0);
                    return "Welcome to the Game " + game.player[0].getName()+"! You are player 1 and your color is: " + "Black. " +
                            "Waiting on Player 2 (WHITE) to Join";
                }
                if(game.getPlayersCount() == 1)  //1 player waiting
                {
                    if(playerID == game.player[0].getId())  //preventing some hack
                    {
                        return "You have already joined the Game";
                    }
                    game.addPlayerToBoard(playerID, name, 1);
                    game.gameRunning = true;  //game is ready to be played
                    return "Welcome to the Game " + game.player[1].getName()+"! You are player 2 and your color is: " + "White. "+
                            "Starting Game";
                }
                else  //observer
                {
                    return ("this board is full but you can now be an observer");
                }

            }
            else if(root.getTagName().equalsIgnoreCase("move"))
            {
                //check to make sure a valid game player is providing move
                //black moves 1st
                if(!game.gameRunning)
                    return ("Need Two Players to start the Game!");
                playerID = Integer.parseInt(root.getAttribute("id"));
                Element locElement = (Element) document.getElementsByTagName("location").item(0);
                moveLocation = Integer.parseInt(locElement.getFirstChild().getNodeValue());
                //check if either player
                if(playerID == game.playerBlackId)
                {
                    if(moveLocation == 65)
                    {
                       //player wants to quit
                        String retrep = playerQuiting(0);
                        //game is over, reset board and players
                        game.resetPlayers();
                        game.resetBoard();
                        return retrep;
                    }
                    if(game.currentPlayer == 0)
                    {
                        color = "Black";
                        Move move = new Move(playerID,color,moveLocation);
                        System.out.print("Player 1 moving");
                        return (makeMove(moveLocation));
                    }
                    else
                        return ("It is not your turn yet!");
                }
                else if(playerID == game.playerWhiteId)
                {
                    if(moveLocation == 65)
                    {
                        return (playerQuiting(1));
                    }
                    if(game.currentPlayer == 1)
                    {
                    color = "White";
                    Move move = new Move(playerID,color,moveLocation);
                    System.out.print("Player 2 moving");
                    return (makeMove(moveLocation));
                    }
                    else
                        return ("It is not your turn yet!");
                }
                else
                {
                    System.out.print("Blackplayerid: "+game.playerBlackId+" Whiteplayeid: "+game.playerWhiteId);
                    return ("You are not a player in this Game, NICE TRY!");
                }
            }
            else{
                playerID = Integer.parseInt(root.getAttribute("id"));
                if(playerID == game.player[0].getId())  //preventing some hack
                {
                    return "You must wait for Player 2 to join";
                }
                else{
                System.out.print("Returning NULL1");
                return null;
                }
            }
        }
        catch (Exception e) {
            System.out.print(e);
            System.out.print("Returning NULL2");

            return null;
        }
    }
    private static String playerQuiting(int exitingPlayer)
    {
        String winner, loser;
        game.quittingPlayer = (exitingPlayer == 0) ? "Player 1" : "Player 2";
        game.winningPlayer = (exitingPlayer == 0) ? "Player 2" : "Player 1";
        game.gameRunning = false;//stop game
        if(game.quittingPlayer.equalsIgnoreCase("Player 1"))
        {
            winner = game.player[1].getName();
            loser = game.player[0].getName();
        }
        else
        {
            winner = game.player[0].getName();
            loser = game.player[1].getName();
        }
        return "This Game is now over, since "+game.quittingPlayer+" ("+loser+") has quit, "+
                "hence the winner is "+game.winningPlayer+" ("+winner+ "), thank you for playing!";
    }
    private static String playerQuittingGet()   //getRepCheck
    {
        String winner, loser;
        if(!game.gameRunning)
        {
            if(game.quittingPlayer.equalsIgnoreCase("Player 1"))
            {
                winner = game.player[1].getName();
                loser = game.player[0].getName();
            }
            else
            {
                winner = game.player[0].getName();
                loser = game.player[1].getName();
            }
            return "This Game is now over, since "+game.quittingPlayer+" ("+loser+") has quit, "+
                    "hence the winner is "+game.winningPlayer+" ("+winner+ "), thank you for playing!";
        }
        else
            return null;
    }

    private static String convertBoardToXML()
    {

        return null;

    }
    private static boolean gameOver()
    {
        if(!game.checkIfGameIsNotDone())  //check if all squares are full or not
        {
            //if game is over then repond back

            return true;
        }
        return false;
    }

    private static void convertMoveToRowCol(int move)
    {
        //row is move/64  col is 64 mod move
        game.nextRow = (int) Math.floor(move/8);
        game.nextCol = (int) Math.floor(move%8);
        System.out.print(" to location row "+game.nextRow+" col "+game.nextCol);
    }
    private static String makeMove(int move)
    {
        if(move == 66)    //player passing their turn
        {
            game.togglePlayers();
            return "You have passed your turn, waiting on the other player to move\n";// +game.showBorad();
        }
        convertMoveToRowCol(move);
        if((game.markBoard(game.nextRow, game.nextCol, game.currentPlayer)) == 0)
        {
            game.togglePlayers();
            return "ValidMove was placed!\n you have "+game.countPlayerPoints(game.currentPlayer)
                    +" points. \n\n"+ "\nWaiting for Other Player to move";

        } else{
            return "Cannot move there since its either out of bounds," +
                    " an occupied spot, or a position where no points can be gained!\r\n" +
                    "Please select another location";
        }
    }
    private static boolean xmlValidation(String xmlString) throws SAXException, MalformedURLException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);   //W3C_XML_SCHEMA_INSTANCE_NS_URI

        Source schemaFile = new StreamSource(new File("ServerValidation.xsd"));
        Schema schema = schemaFactory.newSchema(schemaFile);

        Source xmlFile = new StreamSource(xmlString);

        Validator validator = schema.newValidator();
        try {
            validator.validate(xmlFile);
            System.out.print(xmlFile.getSystemId() + "is Valid");
            return true;
        }catch (Exception e){
            System.out.print(xmlFile.getSystemId() + "is NOT Valid");
            System.out.print(" because "+e.getLocalizedMessage());
            return false;
        }
    }
    private static String createBoardDisplayInXML() throws Exception   //return an xmlString
    {
        int currentPlayerID = (game.currentPlayer == 0) ? game.playerBlackId : game.playerWhiteId;


        /*                  -1:empty; 0:Black; 1:White
        <board id="game1" player_ture_id="currentPlayerID" player_turn_color="">
            <row0></row0>
            <row2></row2>
            <row3></row3>
            <row7> </row7>
        </board>
         */
        //create a java dom tree with the java game info

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("board");
        root.setAttribute("id", "BOARD_1");
        root.setAttribute("player_turn_id", Integer.toString(currentPlayerID));
        root.setAttribute("player_turn_color", game.getPlayerColor(game.currentPlayer));

        document.appendChild(root);

        Element row0 = document.createElement("row0");
        root.appendChild(row0);
        Text row0Text = document.createTextNode(getRowValues(0));
        row0.appendChild(row0Text);

        Element row1 = document.createElement("row1");
        root.appendChild(row1);
        Text row1Text = document.createTextNode(getRowValues(1));
        row1.appendChild(row1Text);

        Element row2 = document.createElement("row2");
        root.appendChild(row2);
        Text row2Text = document.createTextNode(getRowValues(2));
        row2.appendChild(row2Text);

        Element row3 = document.createElement("row3");
        root.appendChild(row3);
        Text row3Text = document.createTextNode(getRowValues(3));
        row3.appendChild(row3Text);

        Element row4 = document.createElement("row4");
        root.appendChild(row4);
        Text row4Text = document.createTextNode(getRowValues(4));
        row4.appendChild(row4Text);

        Element row5 = document.createElement("row5");
        root.appendChild(row5);
        Text row5Text = document.createTextNode(getRowValues(5));
        row5.appendChild(row5Text);

        Element row6 = document.createElement("row6");
        root.appendChild(row6);
        Text row6Text = document.createTextNode(getRowValues(6));
        row6.appendChild(row6Text);

        Element row7 = document.createElement("row7");
        root.appendChild(row7);
        Text row7Text = document.createTextNode(getRowValues(7));
        row7.appendChild(row7Text);

        //convert the DOM to XML string format to send over HTTP
        StringWriter sw = new StringWriter();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        //    <move id="id" pass="pass"><location> [0-66] </location></move>
        return sw.toString();

//        return (Integer.toString(currentPlayerID)+"="+game.getPlayerColor(game.currentPlayer)+"="
//                +game.showBorad());
    }
    private static String getRowValues(int rowNumber)
    {
        StringBuffer rowValues = new StringBuffer();
        rowValues.append(game.getBoardValueAtPosition(rowNumber,0));
        for(int i = 1; i < 8; i++)
        {
            rowValues.append(",");
            rowValues.append(game.getBoardValueAtPosition(rowNumber,i));
        }
        System.out.println("Creating row"+rowNumber+"with values "+rowValues);

        return rowValues.toString();
    }
}

//board display steps:
//server
    //create board in xml using the game state with DOM
    //transform xml to string to send over http
