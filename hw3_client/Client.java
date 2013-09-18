package hw3_client;
/**
 * Created with IntelliJ IDEA.
 * User: aakritprasad
 * Date: 8/19/13
 * Time: 9:49 PM
 * To change this template use File | Settings | File Templates.
 */
import java.net.*;
import java.io.*;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.StringWriter;

//java HWClientExample

public class Client{
    private static FileOutputStream outputFile;
    private static Random random = new Random();
    private static int id = random.nextInt(1000000);//Player1
    private static int pass = random.nextInt(100000000);
    private static double fileNameSerial = random.nextInt(1000000000);

    private static URL url;
    private static HttpURLConnection conn;

    private static boolean poolingFlag = false;
    private static String name;
    private static boolean boardWrite = false;
    private static String validMoveServerXMLResponse = null;

    private static int currentPlayerID;
    private static String currentPlayerColor = null;
    private static String gameNumber = null;

    private static int[][] board;
    private static final int totalrow = 8; //num of rows on the game board
    private static final int totalcol = 8; //num of cols on the game board
    public static final int B = 0; //black color team
    public static final int W = 1; //white color team
    public static final int EMPTY = -1;  //unmoved location

    public static void main(String[] args) throws Exception
    {
        initializeBoardDisplay();
        Scanner keyInput = new Scanner(System.in);
        boolean gameOver = false;
        System.out.println("Welcome to the Reversi Multiplayer Java Servlet Game!");
        System.out.println(showBorad());
        //ask user for name to pass to server for identification
        System.out.println("Please enter your name:");
        System.out.print(">>");
        name = keyInput.next();
        String serverRep = postToServlet(marshalNameToXML(name));
//        System.out.println("Server OutPut:");
        System.out.println(serverRep);
        //server returns 'welcome to game'
        //start the game
        //send a req to get the board
        poolingFlag = true;
        int movePosition = 0;
        while (!gameOver)
        {
//
            while(poolingFlag) //while true and ok to keep polling every second
            {
                waitForPolling();
                try {
                    String playerID = Integer.toString(id);

                    String serverReponse1 = doGetReqFromServlet(marshalGetInfoToXML(name)); //send getReq to server
                    //server will return either 'notStarted', 'turnPlayerID', 'TIE','BLACK','WHITE' or the board
                    String[] repBreakdown = serverReponse1.split(",");
                    String serverRepo = repBreakdown[0];
                    String currentPlayerColor = null, currentGameBoard = null;
                    if(repBreakdown.length > 1)
                    {
                        currentPlayerColor = repBreakdown[1];
                        currentGameBoard = repBreakdown[2];
                    }

                    if(serverRepo.equalsIgnoreCase("gameNotStarted"))
                    {
                        System.out.println("Game has not started, waiting for another player");
                        waitForPolling();
                        continue;
                    }
                    else if(serverRepo.equalsIgnoreCase("TIE"))
                    {
                        System.out.println("The Game is over, there was a TIE");
                        poolingFlag = false;
                    }
                    else if(serverRepo.equalsIgnoreCase("BLACK"))
                    {
                        System.out.println("The Game is over, Player Black won");
                        poolingFlag = false;
                    }
                    else if(serverRepo.equalsIgnoreCase("WHITE"))
                    {
                        System.out.println("The Game is over, Player WHITE won");
                        poolingFlag = false;
                    }


                    else if(serverRepo.equalsIgnoreCase(playerID))
                    {
                        System.out.print("Its your turn to move:");
                        System.out.print(">>");
                        poolingFlag = false;

                    }
                    else
                    {
                        //need to send the board back from server here
//                        System.out.print(serverRepo);
                        System.out.println("Waiting for Player " + currentPlayerColor + " to move...");
//                        System.out.println(currentGameBoard+"\n"+showBorad()+"\n");

                        waitForPolling();
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    //force exit when server is down
                    System.out.print("THE SERVER IS NO LONGER RUNNING!\nPLEASE TRY AGAIN LATER\n");
                    System.exit(0);
                }
            }
            //check to make sure player provides a valid move
            String move = keyInput.next();

            String serverReply = "No Reply from Server";
            if(move.equalsIgnoreCase("exit"))
            {
                //let server the player has quit
                movePosition = 65;
                gameOver = true;
                serverReply = postToServlet(marshalMoveToXML(movePosition));
//                System.out.println("output returned from server:");
                System.out.println(serverReply);
                System.out.println("Thanks for playing");
                poolingFlag = true;
                continue;
            }
            if(move.equalsIgnoreCase("pass"))
            {
                //let server know to pass move ->
                movePosition = 66;
                serverReply = postToServlet(marshalMoveToXML(movePosition));
//                System.out.println("output returned from server:");
                //convert the output xml into readable string-unmarshal
                System.out.println(serverReply);
                poolingFlag = true;
                continue;
            }
            try{
                movePosition = Integer.parseInt(move);
            }catch (Exception e)
            {
                System.out.println("INVALID INPUT FORMAT, please try again");
                continue;
            }
            if(Integer.parseInt(move) > 64 || Integer.parseInt(move) < 0)  {
                System.out.println("INVALID MOVE VALUE, please enter a value between 0 and 64");
                continue;
            }
            else if(Integer.parseInt(move) <= 64 || Integer.parseInt(move) >= 0)
            {
                movePosition = Integer.parseInt(move);
                System.out.println("sending input XML to server:");
                serverReply = postToServlet(marshalMoveToXML(movePosition));
                String[] repy = serverReply.split(" ");
                String validRep = "ValidMove";
                if(repy[0].equalsIgnoreCase(validRep))
                    writeValidatedMoveOutputInXMLToFile(movePosition);

                System.out.println("output returned from server:");
                System.out.println(serverReply);
                poolingFlag = true;
                continue;
            }
            else
            {
                System.out.println("INVALID INPUT FORMAT, please try again");
                continue;
            }

        }
    }

    private static void waitForPolling()
    {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    private static String marshalNameToXML(String s) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("name");
        document.appendChild(root);
        root.setAttribute("id",Integer.toString(id));
        root.setAttribute("pass", Integer.toString(pass));
        Text text = document.createTextNode(s);
        root.appendChild(text);

        StringWriter sw = new StringWriter();
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        //      <name id="id" pass="pass">name</name>
        return sw.toString();

    }
    private static String marshalGetInfoToXML(String s) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("user_info");
        document.appendChild(root);
        root.setAttribute("id",Integer.toString(id));
        Text text = document.createTextNode(s);
        root.appendChild(text);

        StringWriter sw = new StringWriter();
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        //      <name id="id">name</name>
        return sw.toString();

    }

    private static String marshalMoveToXML(int loc) throws Exception{
        //obviously this first part should be done once per game, not for each move
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("move");
        document.appendChild(root);
        root.setAttribute("id",Integer.toString(id));
        root.setAttribute("pass", Integer.toString(pass));
        Element locEl   = document.createElement("location");
        root.appendChild(locEl);
        Text text = document.createTextNode(Integer.toString(loc));
        locEl.appendChild(text);

        StringWriter sw = new StringWriter();
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        //    <move id="id" pass="pass"><location> [0-66] </location></move>
        return sw.toString();
    }

    private static String unMarshalServerXMLPostResponse() throws Exception
    {
        BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream(), "UTF-8" ) );
        String line = null;
        StringBuffer output = new StringBuffer();
        while((line = br.readLine()) != null ) {
            output.append(line);
        }
//        System.out.println("Server Output in XML: \n" + output);
        String outputAsString = output.toString();
        validMoveServerXMLResponse = outputAsString;
        String rep = null;
        try {
            rep = processServerDoPostResponseFromXMLToString(outputAsString);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        disConnect();
        //need to convert the xml returned into string
        return (rep);
    }
    private static String unMarshalServerXMLGetResponse() throws Exception
    {
        BufferedReader br = new BufferedReader( new InputStreamReader( conn.getInputStream(), "UTF-8" ) );
        String line = null;
        StringBuffer output = new StringBuffer();
        while((line = br.readLine()) != null ) {
            output.append(line);
        }
        System.out.println("Server GET Response Output in XML: \n");
        String outputAsString = output.toString();
        String rep = null;
        try {
            rep = processServerDoGetResponseFromXMLToString(outputAsString);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        disConnect();
        //need to convert the xml returned into string
        return (rep);
    }
    private static String processServerDoPostResponseFromXMLToString(String xmlString) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            factory.setIgnoringElementContentWhitespace(true);
//            factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = null;
        Element root = null;
        try
        {
            document = builder.parse(new InputSource(new StringReader(xmlString)));
            root = document.getDocumentElement();

        } catch (Exception e)
        {
            e.printStackTrace(); System.out.print("String reader exception");
        }
//        if(!xmlValidation(xmlString));
//            System.out.print("XML VALIDATION ISSUE\n");        //TODO: need to add XML Validator per schema

        if(root.equals(null))
            throw new RuntimeException("Error building parser");

        else if(root.getTagName().equals("server_response"))      //new user is connecting
        {
            Element nameElement = (Element) document.getElementsByTagName("server_response").item(0);
            return nameElement.getTextContent();
        }
        else
            return null;
    }
    private static String processServerDoGetResponseFromXMLToString(String s) throws Exception
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            factory.setIgnoringElementContentWhitespace(true);
//            factory.setValidating(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = null;
        Element root = null;
        try{
            document = builder.parse(new InputSource(new StringReader(s)));
            root = document.getDocumentElement();

        } catch (Exception e)
        {
            e.printStackTrace(); System.out.print("String reader exception");
        }

//        if(!xmlValidation(s));
//            System.out.print("XML VALIDATION ISSUE\n");        //TODO: need to add XML Validator per schema

        if(root.equals(null))
            throw new RuntimeException("Error building parser");

        else if(root.getTagName().equals("server_get_response"))//new user is connecting
        {
            Element nameElement = (Element) document.getElementsByTagName("server_get_response").item(0);
            return nameElement.getTextContent();
        }
        else if(root.getTagName().equals("board"))//once the game has started the board is returned
        {
            //need to convert the playerID and board into a string with string buffer and return it
            currentPlayerID = Integer.parseInt(root.getAttribute("player_turn_id"));
            gameNumber = root.getAttribute("id");
            currentPlayerColor = root.getAttribute("player_turn_color");
            System.out.println("This game is on "+gameNumber);

            for(int i = 0; i < 7; i++)
            {
                Element rowElement = (Element) document.getElementsByTagName("row"+Integer.toString(i)).item(0);
                String row = rowElement.getTextContent();
                // save to the board state
                for(int j = 0; j < 7; j++)
                {
                    String[] col = row.split(",");
                    int boardSquare = Integer.parseInt(col[j]);
                    if(boardSquare == -1)
                        board[i][j] = EMPTY;
                    else if(boardSquare == 0)
                        board[i][j] = B;
                    else if(boardSquare == 1)
                        board[i][j] = W;
                    else
                        System.out.print("INCORRECT BOARD VALUE RETURNED... SERVER ERROR!");
                }
            }
            System.out.println(showBorad());
            return Integer.toString(currentPlayerID)+","+currentPlayerColor+","+gameNumber;


        }
        else
            return null;
    }
    private static String postToServlet(String xmlMove) throws Exception
    {
        establishPostConnection();
        Writer pw = new PrintWriter(conn.getOutputStream());
//        System.out.println(xmlMove);

        pw.write(xmlMove);
        pw.close();
        return unMarshalServerXMLPostResponse();
    }

    private static String doGetReqFromServlet(String xmlUserInfo) throws Exception{
        establishGetConnection();
        //send user info to server so it knows who you are
//        Writer pw = new PrintWriter(conn.getOutputStream());
////        System.out.println(xmlUserInfo);
//
//        pw.write(xmlUserInfo);
//        pw.close();
        //listen for get Response
        return unMarshalServerXMLGetResponse();
    }

    private static void establishPostConnection() throws Exception{
        url = null; conn = null;
        url = new URL("http://localhost:8080/hw3");
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml");
        //conn.setRequestProperty("Content-Length", "" +  8);
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setDoInput(true);
        conn.setDoOutput(true);
    }
    private static void establishGetConnection() throws Exception{
        url = null; conn = null;
        url = new URL("http://localhost:8080/hw3");
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "text/xml");
        //conn.setRequestProperty("Content-Length", "" +  8);
        conn.setRequestProperty("Content-Language", "en-US");
        conn.setDoInput(true);
        conn.setDoOutput(true);
    }
    private static void disConnect() throws Exception{
        conn.disconnect();

    }

    private static boolean xmlValidation(String xmlString) throws SAXException, MalformedURLException {
        try{
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
//        System.out.println("\nreached here 1 \n\n");
        Source schemaFile = new StreamSource(new File("ClientValidation.xsd"));

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
        }catch (Exception exception){
                System.out.print(exception.getLocalizedMessage());
            return false;
        }

    }

    private static void writeValidatedMoveOutputInXMLToFile(int move) throws Exception
    {
        String fileName = "GameOutPutFile_"+fileNameSerial;
        File file = new File(fileName);
        outputFile = new FileOutputStream(file, true);
//        if(!file.exists())
//        {
//            file.createNewFile();
//        }
        DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date todayDate = new Date();
        String date = df.format(todayDate);
        if (!boardWrite)
            writeToFile("YOUR REVERSI GAME MOVES on DATE: "+date+"\n");

        writeToFile(marshalMoveToXML(move));
        boardWrite = true;
//
        System.out.println("Valid Move Written to File: "+fileName);
        outputFile.close();
    }
    private static void writeToFile(String s) throws IOException
    {
        byte[] input = s.getBytes();
        outputFile.write(input);
        outputFile.flush();
    }

    private static void initializeBoardDisplay()
    {
        board = new int[totalrow][totalcol];
        for(int i = 0; i < totalrow; ++i)
            for(int j = 0; j < totalcol; ++j)
                board[i][j] = EMPTY;
        initialBoardSet();
    }
    private static void initialBoardSet()
    {
        board[3][3] = B;
        board[3][4] = W;
        board[4][3] = W;
        board[4][4] = B;
    }
    public static String showBorad() //trying to make it look decent
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
}