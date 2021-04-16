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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileProcessor {
    final String INPUT_DIRECTORY = "/opt/batch-processor/data/in/";
    final String OUTPUT_DIRECTORY = "/opt/batch-processor/data/out/";
    final String ERROR_DIRECTORY = "/opt/batch-processor/data/out/";
    final String ARCHIVE_DIRECTORY = "/opt/batch-processor/data/archive/";
    ArrayList<Receiver> receivers = new ArrayList<>();
    Logger logger = Logger.getLogger(FileProcessor.class.getName());

    public void process(Path path) throws IOException {
        final String filePath = path.toString();
        if (filePath.toLowerCase().endsWith(".xml")) {
            parseXmlAndUpdateReceivers(path);
            Boolean pdfCheck = receivers.stream()
                    .map(receiver -> checkPdf(receiver.getFile(), receiver.getHash()))
                    .reduce(Boolean.TRUE, Boolean::logicalAnd);
            logger.log(Level.INFO, "files are not corrupt - " + pdfCheck);
            if (pdfCheck) {
                moveToDirectory(OUTPUT_DIRECTORY);
                String newPath = filePath.replace("in", "archive");
                Files.move(Paths.get(filePath), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void moveToDirectory(String outDirectory) {
        receivers.forEach(receiver -> {
            Integer innerDirectory = receiver.getId();
            int outerDirectory = innerDirectory % 100 / innerDirectory;
            final String outputPath = outDirectory + outerDirectory;
            final String destinationDirectory = outputPath + '/' + innerDirectory;
            createDirectory(outputPath);
            createDirectory(destinationDirectory);
            final String fileName = receiver.getFile();
            final String inputPath = INPUT_DIRECTORY + fileName;
            try {
                Files.copy(Paths.get(inputPath), Paths.get(destinationDirectory + '/' + fileName), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(Paths.get(inputPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean createDirectory(String path) {
        File file = new File(path);
        return file.mkdir();
    }

    private Boolean checkPdf(String fileName, String hash) {
        try {
            File file = new File(INPUT_DIRECTORY + fileName);
            MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            String checksum = getFileChecksum(md5Digest, file);
            System.out.println(checksum + " - " + hash + " : " + checksum.equals(hash));
            return checksum.equals(hash);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
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

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };
        fis.close();
        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
