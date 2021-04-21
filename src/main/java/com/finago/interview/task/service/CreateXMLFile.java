package com.finago.interview.task.service;

import com.finago.interview.task.modal.Receiver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class CreateXMLFile {
   public void createFile(Receiver receiver, String directoryPath) {
       String xmlFilePath = directoryPath + "/" + receiver.getFile().replace(".pdf", ".xml");
       try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();
            Element root = document.createElement("receivers");
            document.appendChild(root);
            Element receiverNode = document.createElement("receiver");
            root.appendChild(receiverNode);
            addChildNode(document, receiverNode, "receiver_id", receiver.getId());
            addChildNode(document, receiverNode, "first_name", receiver.getFirstname());
            addChildNode(document, receiverNode, "last_name", receiver.getLastname());
            addChildNode(document, receiverNode, "file", receiver.getFile());
            addChildNode(document, receiverNode, "file_md5", receiver.getHash());

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(xmlFilePath));
            transformer.transform(domSource, streamResult);
        } catch (ParserConfigurationException | TransformerException pce) {
            pce.printStackTrace();
        }
   }

    private void addChildNode(Document document, Element receiverNode, String nodeName, String nodeValue) {
        Element receiverId = document.createElement(nodeName);
        receiverId.appendChild(document.createTextNode(nodeValue));
        receiverNode.appendChild(receiverId);
    }
}
