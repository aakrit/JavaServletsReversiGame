package hw3;

/**
 * Created with IntelliJ IDEA.
 * User: aakritprasad
 * Date: 8/19/13
 * Time: 6:11 PM
 * To change this template use File | Settings | File Templates.
 */
/*
  create a DOM tree in memory, populate a few of the nodes,
  and convert to an proper XML string
*/

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class DOM2XML{

    public static void main(String[] args) throws Exception{
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        //line below creates root of DOM tree in memory from scratch
        org.w3c.dom.Document document = builder.newDocument();
        Element root = document.createElement("move");
        document.appendChild(root);
        Element xc = document.createElement("xc");
        Element yc = document.createElement("yc");
        root.appendChild(xc);
        root.appendChild(yc);
        root.setAttribute("playerId", "1");
        Text text1 = document.createTextNode("2");
        Text text2 = document.createTextNode("1");
        xc.appendChild(text1);
        yc.appendChild(text2);
        //at this point simple DOM is complete and we want
        //to convert it to a String (ie an XML "file")
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        //now that we have transformer, we need a source and a target
        //to feed to the transform method
        DOMSource source = new DOMSource(document);
        StreamResult target = new StreamResult(sw);
        transformer.transform(source,target);
        System.out.println(sw);
    }








}