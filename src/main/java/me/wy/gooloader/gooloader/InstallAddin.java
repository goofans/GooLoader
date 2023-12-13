package me.wy.gooloader.gooloader;

import javafx.application.Platform;
import me.wy.gooloader.gooloader.util.ZippedFileInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InstallAddin {

    public static void install(Goomod goomod) throws IOException {
        //System.out.println("Installing " + goomod.getId());
        Platform.runLater(() -> Main.progressLabel.setText("Merging: " + goomod.getId()));
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(goomod.getFile()));
        ZipEntry zipEntry;
        List<String> levels = new ArrayList<>();
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            //System.out.println(zipEntry.getName());
            ZipEntry finalZipEntry = zipEntry;
            Platform.runLater(() -> Main.progressLabel.setText("Merging: " + goomod.getId() + " | " + finalZipEntry.getName()));
            String startsWith;
            if (zipEntry.getName().startsWith("override")) {
                startsWith = "override";
            } else if (zipEntry.getName().startsWith("compile")) {
                startsWith = "compile";
            } else if (zipEntry.getName().startsWith("merge")) {
                startsWith = "merge";
            } else {
                startsWith = "";
            }
            switch (startsWith) {
                case "override":
                    ZippedFileInputStream fileInputStream = new ZippedFileInputStream(zipInputStream);
                    override(zipEntry, fileInputStream, true);
                    fileInputStream.close();
                    break;
                case "compile":
                    if (zipEntry.getName().endsWith(".level.xml")) {
                        levels.add(zipEntry.getName().replace("compile/res/levels/", "").split("/")[0].replace("/",""));
                    }
                    // We don't need to compile anything with the new update lol
                    ZippedFileInputStream fileInputStream2 = new ZippedFileInputStream(zipInputStream);
                    override(zipEntry, fileInputStream2, false);
                    fileInputStream2.close();
                    break;
                case "merge":
                    ZippedFileInputStream fileInputStream3 = new ZippedFileInputStream(zipInputStream);
                    merge(zipEntry, fileInputStream3);
                    fileInputStream3.close();
                    break;
                default:
                    break;
            }
            if (zipEntry.getName().equalsIgnoreCase("text.xml")) {
                ZippedFileInputStream zippedFileInputStream = new ZippedFileInputStream(zipInputStream);
                text(zippedFileInputStream);
                zippedFileInputStream.close();
            }
        }
        zipInputStream.closeEntry();
        zipInputStream.close();
        if (goomod.getType().equalsIgnoreCase("level")) {
            for (String level : levels) {
                installLevel(goomod, level);
            }
        }
        System.out.println("Finished Zip stuff");
    }

    private static void override(ZipEntry zipEntry, ZippedFileInputStream zipInputStream, boolean override) throws IOException {
        if (override) {
            String path = (Main.modGameDir + "/game" + (zipEntry.getName().replaceFirst("override", "")).replaceFirst(".xml", ""));
            String fileName = StringUtils.reverse(StringUtils.reverse(path).split("/")[0]);
            File file = new File(path);
            File path2 = new File(path.replace(fileName, ""));
            path2.mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            if (!file.isDirectory()) {
                FileUtils.copyInputStreamToFile(zipInputStream, file);
            }
        } else {
            String path = (Main.modGameDir + "/game" + (zipEntry.getName().replaceFirst("compile", "")).replaceFirst(".xml", ""));
            String fileName = StringUtils.reverse(StringUtils.reverse(path).split("/")[0]);
            File file = new File(path);
            File path2 = new File(path.replace(fileName, ""));
            System.out.println(file.getPath());
            System.out.println(path2.getPath());
            path2.mkdirs();
            if (!file.exists()) {
                file.createNewFile();
            }
            if (!file.isDirectory()) {
                FileUtils.copyInputStreamToFile(zipInputStream, file);
            }
        }
    }
    private static void merge(ZipEntry zipEntry, ZippedFileInputStream zipInputStream) {
        if (zipEntry.getName().endsWith(".xsl")) {
            String path = (Main.modGameDir + "/game" + (zipEntry.getName().replaceFirst("merge", "")).replaceFirst(".xsl", ""));
            String fileName = StringUtils.reverse(StringUtils.reverse(path).split("/")[0]);
            File file = new File(path);
            File path2 = new File(path.replace(fileName, ""));
            path2.mkdirs();
            if (file.exists()) {
                Reader transform = new InputStreamReader(zipInputStream, StandardCharsets.UTF_8);
                try {
                    Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(transform));
                    Source src = new StreamSource(file);
                    StringWriter writer = new StringWriter();
                    Result res = new StreamResult(writer);
                    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                    transformer.transform(src, res);
                    FileUtils.writeStringToFile(file, writer.toString(), Charset.defaultCharset());
                } catch (TransformerException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void text(ZippedFileInputStream zipInputStream) {
        try {
            System.out.println("zip");
            File file = new File(Main.modGameDir + "/game/properties/text.xml");
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            df.setNamespaceAware(true);
            DocumentBuilder builder = df.newDocumentBuilder();
            Document xmlFile = builder.parse(file);
            Document document = builder.parse(zipInputStream);
            NodeList nodeList = document.getElementsByTagName("string");
            Node root = xmlFile.getElementsByTagName("strings").item(0);
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                root.appendChild(xmlFile.importNode(node, true));
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(xmlFile);
            FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8);
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            writer.close();
        } catch (IOException | ParserConfigurationException | SAXException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void text(InputStreamReader inputStreamReader, String levelNameId, String levelTextId, String levelNameText, String levelTextText) {
        try {
            System.out.println("is");
            File file = new File(Main.modGameDir + "/game/properties/text.xml");
            byte[] bytes = Files.readAllBytes(file.toPath());
            InputSource inputSource = new InputSource();
            inputSource.setCharacterStream(new StringReader(Arrays.toString(bytes).trim()));
            Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(inputStreamReader));
            transformer.setParameter("level_name_string", levelNameId);
            transformer.setParameter("level_text_string", levelTextId);
            transformer.setParameter("level_name_text", levelNameText);
            transformer.setParameter("level_text_text", levelTextText);
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            Source src = new StreamSource(file);
            StringWriter writer = new StringWriter();
            Result res = new StreamResult(writer);
            transformer.transform(src, res);
            FileUtils.writeStringToFile(file, writer.toString(), StandardCharsets.UTF_8);
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    private static void installLevel(Goomod goomod, String level) throws IOException {
        String levelNameId = "LEVEL_NAME_" + level.toUpperCase();
        String levelTextId = "LEVEL_TEXT_" + level.toUpperCase();
        String levelNameText = goomod.getLevels().getElementsByTagName("name").item(0).getTextContent();
        String levelTextText = goomod.getLevels().getElementsByTagName("subtitle").item(0).getAttributes().item(0).getTextContent();

        InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/level-text.xsl"), "UTF-8"));
        text(inputStreamReader, levelNameId, levelTextId, levelNameText, levelTextText);
        inputStreamReader.close();
        InputStreamReader inputStreamReader2 = new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/level-island.xsl"), "UTF-8"));
        try {
            File file = new File(Main.modGameDir + "/game/res/islands/island1.xml");
            Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(inputStreamReader2));
            transformer.setParameter("level_id", level);//TODO Fill these in
            transformer.setParameter("level_name_id", levelNameId);
            transformer.setParameter("level_text_id", levelTextId);
            try {
                String ocd = goomod.getLevels().getElementsByTagName("ocd").item(0).getNodeValue();
                if (ocd != null) {
                    transformer.setParameter("level_ocd", ocd);
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            //transformer.setParameter("level_ocd", "");
            transformer.setParameter("level_cutscene", "");
            transformer.setParameter("level_skipeolsequence", "");
            Source src = new StreamSource(file);
            StringWriter writer = new StringWriter();
            Result res = new StreamResult(writer);
            transformer.transform(src, res);
            FileUtils.writeStringToFile(file, writer.toString(), Charset.defaultCharset());
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
        inputStreamReader2.close();
        InputStreamReader inputStreamReader3 = new InputStreamReader(Objects.requireNonNull(Main.class.getResourceAsStream("/level-island-scene.xsl"), "UTF-8"));
        try {
            File file = new File(Main.modGameDir + "/game/res/levels/island1/island1.scene");
            File path = new File(Main.modGameDir + "/game/res/levels/island1/");
            Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(inputStreamReader3));
            transformer.setParameter("level_id", level);
            transformer.setParameter("level_name_id", levelNameId);
            Source src = new StreamSource(file);
            StringWriter writer = new StringWriter();
            Result res = new StreamResult(writer);
            transformer.transform(src, res);
            FileUtils.writeStringToFile(file, writer.toString(), Charset.defaultCharset());
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }
        inputStreamReader3.close();
    }

}
