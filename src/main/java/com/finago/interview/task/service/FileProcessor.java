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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

public class FileProcessor {
    final String INPUT_DIRECTORY = "/opt/batch-processor/data/in/";
    final String OUTPUT_DIRECTORY = "/opt/batch-processor/data/out/";
    final String ERROR_DIRECTORY = "/opt/batch-processor/data/error/";
    CreateXMLFile createXMLFile;
    ArrayList<Receiver> receivers;
    Logger logger;

    public FileProcessor() {
        createXMLFile = new CreateXMLFile();
        receivers = new ArrayList<>();
        logger = Logger.getLogger(FileProcessor.class.getName());
    }

    public void process(Path path) throws IOException {
        try {
            final String filePath = path.toString();
            if (filePath.toLowerCase().endsWith(".xml")) {
                parseXmlAndUpdateReceivers(filePath);
                Boolean pdfCheck = checkAllPDF();
                logger.log(Level.INFO, "files are not corrupt - " + pdfCheck);
                if (pdfCheck) {
                    moveToDirectory();
                    String newPath = filePath.replace("in", "archive");
                    Files.move(Paths.get(filePath), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
                    deleteProcessedFiles();
                }
            }
        } catch (ParseException e) {
            logger.log(Level.WARNING, path.toString() + " file was corrupted");
        }
    }

    private Boolean checkAllPDF() {
        return receivers.stream()
                .map(receiver -> checkPdf(receiver.getFile(), receiver.getHash()))
                .reduce(Boolean.TRUE, Boolean::logicalAnd);
    }

    private void deleteProcessedFiles() {
        receivers.stream()
                .collect(collectingAndThen(toCollection(() -> new TreeSet<>(comparing(Receiver::getFile))), ArrayList::new))
                .forEach(receiver -> {
                    final String fileName = receiver.getFile();
                    final String inputPath = INPUT_DIRECTORY + fileName;
                    try {
                        Files.delete(Paths.get(inputPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    private void moveToDirectory() {
        receivers.forEach(receiver -> {
            String receiverId = receiver.getId();
            logger.log(Level.INFO, "Starting to move files for receiver id " + receiverId);
            int innerDirectory = Integer.parseInt(receiverId);
            int outerDirectory = innerDirectory % 100 / innerDirectory;
            String outputPath = OUTPUT_DIRECTORY + outerDirectory;
            String destinationDirectory = outputPath + '/' + innerDirectory;
            createDirectory(outputPath);
            createDirectory(destinationDirectory);
            final String fileName = receiver.getFile();
            final String inputPath = INPUT_DIRECTORY + fileName;
            try {
                Files.copy(Paths.get(inputPath), Paths.get(destinationDirectory + '/' + fileName), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.log(Level.WARNING, "pdf file " + fileName + " missing");
                outputPath = ERROR_DIRECTORY + outerDirectory;
                destinationDirectory = outputPath + '/' + innerDirectory;
            }
            createXMLFile.createFile(receiver, destinationDirectory);
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
            boolean check = checksum.equals(hash);
            logger.log(Level.INFO, fileName + " checksum did match with hash value: " + check);
            return check;
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void parseXmlAndUpdateReceivers(String path) throws IOException, ParseException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(path);
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
        } catch (ParserConfigurationException | SAXException ex) {
            logger.log(Level.FINE, "file corrupted: " + path);
            String newPath = path.replace("in", "error");
            Files.move(Paths.get(path), Paths.get(newPath), StandardCopyOption.REPLACE_EXISTING);
            throw new ParseException("file corrupted", 0);
        }
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] byteArray = new byte[1024];
        int bytesCount;
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
