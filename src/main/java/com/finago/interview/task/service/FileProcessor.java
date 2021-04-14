package com.finago.interview.task.service;

import com.finago.interview.task.modal.Receiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Logger;

public class FileProcessor {
    ArrayList<Receiver> receivers = new ArrayList<>();
    Logger logger = Logger.getLogger(FileProcessor.class.getName());

    public void process(Path path) {
        if (path.toString().toLowerCase().endsWith(".xml")) {
            parseXmlAndUpdateReceivers(path);
            Boolean pdfCheck = receivers.stream()
                    .map(receiver -> checkPdf(receiver.getFile(), receiver.getHash()))
                    .reduce(Boolean.FALSE, Boolean::logicalAnd);
        }
    }

    private Boolean checkPdf(String file, String hash) {
        return false;
    }

    private void parseXmlAndUpdateReceivers(Path path) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(path.toString());
            doc.getDocumentElement().normalize();
            NodeList list = doc.getElementsByTagName("receiver");
            for (int temp = 0; temp < list.getLength(); temp++) {
                Node node = list.item(temp);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String id = element.getElementsByTagName("receiver_id").item(0).getTextContent();
                    String firstname = element.getElementsByTagName("first_name").item(0).getTextContent();
                    String lastname = element.getElementsByTagName("last_name").item(0).getTextContent();
                    String file = element.getElementsByTagName("file").item(0).getTextContent();
                    String hash = element.getElementsByTagName("file_md5").item(0).getTextContent();

                    Receiver receiver = new Receiver(Integer.parseInt(id), firstname, lastname, file, hash);
                    receivers.add(receiver);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            //TODO: Handle exception case.
            e.printStackTrace();
        }
    }
}
