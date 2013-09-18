package xml_validation;

/**
 * Created with IntelliJ IDEA.
 * User: aakritprasad
 * Date: 8/19/13
 * Time: 6:13 PM
 * To change this template use File | Settings | File Templates.
 */
import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import javax.xml.*;
import org.xml.sax.*;

/*
  tests that we can validate an XML string against a schema
  used in GetXMLServlet.java
*/
public class TestValidatingXMLParser{
    public static void main(String[] args) throws Exception{
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.newDocument();
        Element root = document.createElement("move");
        document.appendChild(root);

        root.setAttribute("playerID","100");

        Element locEl   = document.createElement("location");
        Element colorEl = document.createElement("color");
        root.appendChild(locEl);
        root.appendChild(colorEl);

        Text text = document.createTextNode("22");
        locEl.appendChild(text);

        text = document.createTextNode("white");
        colorEl.appendChild(text);

        StringWriter sw = new StringWriter();
        TransformerFactory tFactory =
                TransformerFactory.newInstance();
        Transformer transformer = tFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(sw);
        transformer.transform(source, result);
        String xmlString = sw.toString();
        //System.out.println(xmlString);
        Move move = processInput(xmlString);
        System.out.println(move);


    }

    private static Move processInput(String xmlString) throws Exception{

        int moveLocation;
        String color;
        int playerID;
        //on server we don't create a dom from scratch; rather
        //we parse XML message into DOM
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();


        DocumentBuilder builder = factory.newDocumentBuilder();
        //to parse a String do this
        Document document =
                builder.parse(new org.xml.sax.InputSource(new StringReader(xmlString)));

        SchemaFactory sfactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // load a WXS schema, represented by a Schema instance
        Source schemaFile = new StreamSource(new File("PlayerMove.xsd"));
        Schema schema = sfactory.newSchema(schemaFile);

        // create a Validator instance, which can be used to validate an instance document
        Validator validator = schema.newValidator();

        // validate the DOM tree
        try {
            validator.validate(new DOMSource(document));
        } catch (SAXException e) {
            System.out.println(e);
        }

        //now that we have our org.w3c.Document we start by getting root element
        Element root = document.getDocumentElement();
        playerID = Integer.parseInt(root.getAttribute("playerID"));

        Element locElement = (Element) document.getElementsByTagName("location").item(0);
        Element colorElement = (Element) document.getElementsByTagName("color").item(0);

        moveLocation = Integer.parseInt(locElement.getFirstChild().getNodeValue());
        color = colorElement.getFirstChild().getNodeValue();

        Move move = new Move(playerID,color,moveLocation);
        return move;
    }
}

class Move{
    private int playerID;
    private String color;
    private int location;

    Move(int id, String color, int loc){
        this.playerID = id;
        this.color = color;
        this.location = loc;
    }

    public int getPlayerID(){
        return this.playerID;
    }

    public int getLocation(){
        return this.location;
    }

    public String getColor(){
        return this.color;
    }

    public String toString(){
        String output = playerID + " " + color + " " + location;
        return output;
    }
}