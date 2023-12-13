package me.wy.gooloader.gooloader;

import me.wy.gooloader.gooloader.util.ZippedFileInputStream;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Goomod {

    private File file;
    private String id;
    private String name;
    private String type;
    private String version;
    private String author;
    private String desc;
    private boolean enabled;
    private boolean malformed;
    private Document levels;

    public Goomod(File file) throws IOException {
        try {
            this.file = file;
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(file));
            Document document = null;
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = df.newDocumentBuilder();

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                ZippedFileInputStream fileInputStream = new ZippedFileInputStream(zipInputStream);
                if (entry.getName().equals("addin.xml")) {
                    document = builder.parse(fileInputStream);
                }
                fileInputStream.close();
            }
            zipInputStream.close();

            if (document == null) {
                this.malformed = true;
                return;
            }

            id = "??";
            name = "??";
            type = "??";
            version = "??";
            author = "??";
            desc = "??";

            id = document.getElementsByTagName("id").item(0).getTextContent();
            name = document.getElementsByTagName("name").item(0).getTextContent();
            type = document.getElementsByTagName("type").item(0).getTextContent();
            version = document.getElementsByTagName("version").item(0).getTextContent();
            desc = document.getElementsByTagName("description").item(0).getTextContent();
            author = document.getElementsByTagName("author").item(0).getTextContent();
            if (document.getElementsByTagName("level").item(0) != null) {
                levels = document.getElementsByTagName("level").item(0).getOwnerDocument();
            }

            System.out.println(id);
            System.out.println(name);
            System.out.println(type);
            System.out.println(version);
            System.out.println(desc);
            System.out.println(author);

            if (Main.addinsList.exists()) {
                if (Files.readString(Main.addinsList.toPath()).contains(id)) {
                    this.enabled = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public String getAuthor() {
        return author;
    }

    public File getFile() {
        return file;
    }

    public String getId() {
        return id;
    }

    public Document getLevels() {
        return levels;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isMalformed() {
        return malformed;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLevels(Document levels) {
        this.levels = levels;
    }

    public void setMalformed(boolean malformed) {
        this.malformed = malformed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    /*{
    public void getCell(ListView listView) {
        listView.setCellFactory((Callback<ListView<Goomod>, GoomodListCell>) listView1 -> {
            return new GoomodListCell();
                private Label name = new Label(Goomod.this.name);
                private Label type = new Label(Goomod.this.type);
                private Label version = new Label(Goomod.this.version);
                private Label author = new Label(Goomod.this.author);
                private FlowPane bp = new FlowPane(name, type, version, author);

                @Override
                protected void updateItem(Goomod goomod, boolean b) {
                    super.updateItem(goomod, b);
                    setText(Goomod.this.name);
                }
            }
        })
    };*/
}
