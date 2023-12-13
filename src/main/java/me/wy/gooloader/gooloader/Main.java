package me.wy.gooloader.gooloader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import me.wy.gooloader.gooloader.util.FileUtil;
import me.wy.gooloader.gooloader.util.RGBButton;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Main extends Application {

    public static List<Goomod> goomods;

    public static File ogGameDir;

    public static File modGameDir;

    public static File addinDir;

    private static Font font;

    public static Properties properties;

    public static File config;

    public static File addinsList;

    public static boolean steammode;

    public static boolean makeAddinsList;

    public static List<String> enabledAddins;

    public static ProgressBar progressBar;

    public static Label progressLabel;

    private static Button saveButton;

    private static Button saveAndPlayButton;

    @Override
    public void start(Stage stage) throws IOException {
        enabledAddins = new ArrayList<>();
        loadConfig();
        if (addinDir != null) {
            if (addinDir.exists()) {
                File[] addinList = addinDir.listFiles();
                if (addinList != null) {
                    for (File file : addinList) {
                        try {
                            goomods.add(new Goomod(file));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 660, 620);
        stage.setTitle("GooLoader");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(windowEvent -> {
            ((Stage)stage.getScene().getWindow()).close();
            Platform.exit();
        });
        font = Font.font("SansSerif", FontWeight.SEMI_BOLD, 20);
        TableView tableView = (TableView) scene.lookup("#addinsList");

        TableColumn<Goomod, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Goomod, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Goomod, String> versionColumn = new TableColumn<>("Version");
        versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        TableColumn<Goomod, String> authorColumn = new TableColumn<>("Author");
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        TableColumn<Goomod, CheckBox> enabledColumn = new TableColumn<>("Enabled");
        enabledColumn.setCellValueFactory(new PropertyValueFactory<>("enabled"));
        enabledColumn.setCellValueFactory(goomodCheckBoxCellDataFeatures -> {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(goomodCheckBoxCellDataFeatures.getValue().isEnabled());
            checkBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                if (t1) {
                    FileUtil.addGoomod(goomodCheckBoxCellDataFeatures.getValue());
                } else {
                    FileUtil.removeGoomod(goomodCheckBoxCellDataFeatures.getValue());
                }
            });
            return new SimpleObjectProperty<>(checkBox);
        });

        tableView.getColumns().addAll(nameColumn, typeColumn, versionColumn, authorColumn, enabledColumn);

        progressBar = (ProgressBar) scene.lookup("#progressBar");
        progressLabel = (Label) scene.lookup("#progressLabel");

        //tableView.setCellFactory((Callback<ListView, ListCell>) listView1 -> new GoomodListCell());
        saveButton = (Button) scene.lookup("#save");
        saveButton.setOnAction(actionEvent -> {
            /*
            for (Object object : tableView.getItems()) {
                if (object instanceof GoomodCell goomodListCell) {
                    Goomod goomod = getGoomodByName(goomodListCell.g.getText());
                    if (goomod != null) {
                        if (goomodListCell.enabled.isSelected()) {
                            FileUtil.addGoomod(goomod);
                        } else {
                            FileUtil.removeGoomod(goomod);
                        }
                    }
                }
            }*/
            new Thread(() -> {
                try {
                    save(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });
        saveAndPlayButton = (Button) scene.lookup("#saveAndPlay");
        saveAndPlayButton.setOnAction(actionEvent -> {
            /*
            for (Object object : tableView.getItems()) {
                if (object instanceof GoomodCell goomodListCell) {
                    Goomod goomod = getGoomodByName(goomodListCell.title.getText());
                    if (goomod != null) {
                        if (goomodListCell.enabled.isSelected()) {
                            FileUtil.addGoomod(goomod);
                        } else {
                            FileUtil.removeGoomod(goomod);
                        }
                    }
                }
            }*/
            new Thread(() -> {
                try {
                    save(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });
        Label desclabel = (Label) scene.lookup("#desclabel");
        tableView.setRowFactory(tv -> {
            TableRow<Goomod> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                if (!row.isEmpty()) {
                    Goomod goomod = row.getItem();
                    desclabel.setText(goomod.getName() + " | " + goomod.getAuthor() + " | " + goomod.getVersion() + System.lineSeparator() + goomod.getType() + System.lineSeparator() + System.lineSeparator() + goomod.getDesc());
                }
            });
            return row;
        });
        /*
        Button binuniButton = (Button) scene.lookup("#binuniButton");
        binuniButton.setDisable(true);
        binuniButton.setOnAction(actionEvent -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Binuni anim.");
            fileChooser.getExtensionFilters().addAll(
            );
            File binuni = fileChooser.showOpenDialog(new Stage());
            if (binuni != null) {
                try {
                    BinltlAN binltlAN = new BinltlAN(binuni);
                    BinuniAN binuniAN = new BinuniAN(binltlAN);
                    File file = new File(System.getProperty("user.dir") + "\\.gooloader\\test.anim.binuni");
                    file.createNewFile();
                    FileUtils.writeByteArrayToFile(file, binuniAN.encode());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/

        MenuButton helpMenu = (MenuButton) scene.lookup("#helpMenu");
        MenuItem aboutButton = helpMenu.getItems().get(0);
        aboutButton.setOnAction(actionEvent -> {
            Parent root;
            try {
                root = FXMLLoader.load(getClass().getResource("/me/wy/gooloader/gooloader/about.fxml"));
                Stage aboutStage = new Stage();
                aboutStage.setTitle("About GooLoader");
                aboutStage.setScene(new Scene(root, 450, 450));
                aboutStage.setResizable(false);
                Image image = new Image("http://goofans.com/sites/default/files/pictures/picture-28142.png");
                ImageView imageView = new ImageView();
                imageView.setImage(image);
                RGBButton goofansButton = (RGBButton) root.lookup("#goofansButton");
                goofansButton.setGraphic(imageView);
                goofansButton.setOnMouseClicked((mouseEvent -> {
                    getHostServices().showDocument("http://goofans.com/user/28142");
                }));
                aboutStage.setOnCloseRequest((c) -> goofansButton.stop = true);
                aboutStage.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        for (Goomod goomod : goomods) {
            /*
            GoomodListCell goomodListCell = new GoomodListCell(goomod);
            goomodListCell.setText(goomod.getName());
            tableView.getItems().add(goomodListCell);
            */
            tableView.getItems().add(goomod);
            if (makeAddinsList) {
                FileUtil.addGoomod(goomod);
            }
        }
        /*
        if (tableView != null) {
            for (Goomod goomod : goomods) {
                goomod.getCell(tableView);
            }
        } else {
            System.out.println("null");
        }*/
    }


    public static void main(String[] args) {
        goomods = new ArrayList<>();
        launch();
    }

    public static Goomod getGoomodByName(String name) {
        for (Goomod goomod : goomods) {
            if (goomod.getName().equalsIgnoreCase(name)) {
                return goomod;
            }
        }
        return null;
    }

    public static Goomod getGoomodById(String id) {
        for (Goomod goomod : goomods) {
            if (goomod.getId().equalsIgnoreCase(id)) {
                return goomod;
            }
        }
        return null;
    }

    public static void loadConfig() {
        properties = new Properties();
        if (System.getProperty("os.name").startsWith("Windows")) {
            config = new File(System.getProperty("user.dir") + "\\.gooloader\\gooloader.config");
            if (config.exists()) {
                try {
                    FileInputStream fileInputStream = new FileInputStream(config);
                    properties.load(fileInputStream);
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        addinDir = new File(System.getenv("APPDATA") + "\\GooTool\\addins");
                    }
                    if (addinDir.exists() && Objects.equals(properties.getProperty("addins"), "")) {
                        properties.setProperty("addins", addinDir.getAbsolutePath());
                        saveConfig();
                    }
                    if (Objects.equals(properties.getProperty("originalgame"), "") || Objects.equals(properties.getProperty("moddedgame"), "") || Objects.equals(properties.getProperty("addins"), "")) {
                        setupConfig();
                    } else {
                        ogGameDir = new File(properties.getProperty("originalgame"));
                        modGameDir = new File(properties.getProperty("moddedgame"));
                        addinDir = new File(properties.getProperty("addins"));
                        steammode = Boolean.parseBoolean(properties.getProperty("steammode"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    InputStream is = Main.class.getResourceAsStream("/gooloader.config");
                    assert is != null;
                    config.mkdirs();
                    Files.copy(is, config.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Created config file!");
                    properties = new Properties();
                    FileInputStream fileInputStream = new FileInputStream(config);
                    properties.load(fileInputStream);
                    setupConfig();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            addinsList = new File(System.getProperty("user.dir") + "\\.gooloader\\addins.txt");
            if (addinsList.exists()) {

            } else {
                try {
                    makeAddinsList = true;
                    addinsList.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void saveConfig() {
        try {
            if (properties != null) {
                properties.store(new FileOutputStream(config), null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setupConfig() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Welcome to GooLoader, this is an experimental addin loader for the 2019 World Of Goo update. This is not a replacement for GooTool, before we can begin you need to select your original game and where you want your modded game to be.");
        alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Select your WorldOfGoo.exe file from your original game.");
                    fileChooser.getExtensionFilters().addAll(
                            new FileChooser.ExtensionFilter("WorldOfGoo.exe", "WorldOfGoo.exe")
                    );
                    File wogEXE = fileChooser.showOpenDialog(new Stage());
                    if (wogEXE != null) {
                        ogGameDir = wogEXE;
                        properties.setProperty("originalgame", wogEXE.getAbsolutePath().replace("WorldOfGoo.exe", ""));
                        saveConfig();
                    }
                });
        Alert alert2 = new Alert(Alert.AlertType.INFORMATION, "Now we will select where your modded folder will be. WARNING THIS FOLDER WILL BE WIPED.");
        alert2.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .ifPresent(response -> {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Select your modded folder.");
                    File moddedDir = directoryChooser.showDialog(new Stage());
                    if (moddedDir != null) {
                        modGameDir = moddedDir;
                        properties.setProperty("moddedgame", modGameDir.getAbsolutePath());
                        saveConfig();
                    }
                    FileUtil.deleteFolderContents(modGameDir);
                    if (System.getProperty("os.name").startsWith("Windows")) {
                        addinDir = new File(System.getenv("APPDATA") + "\\GooTool\\addins");
                        properties.setProperty("addins", addinDir.getAbsolutePath());
                        saveConfig();
                    }
                });
        Alert alert3 = new Alert(Alert.AlertType.INFORMATION, "GooLoader is all setup now, have fun!");
        alert3.showAndWait();
    }

    public static void save(boolean play) throws IOException {
        Platform.runLater(() -> saveButton.setDisable(true));
        Platform.runLater(() -> saveAndPlayButton.setDisable(true));
        System.out.println(ogGameDir);
        System.out.println(modGameDir);
        Platform.runLater(() -> Main.progressLabel.setText("Copying game files to custom directory."));
        Platform.runLater(() -> progressBar.setProgress(-1));
        if (!ogGameDir.isDirectory()) {
            ogGameDir = new File(ogGameDir.getAbsolutePath().replace("WorldOfGoo.exe", ""));
        }
        FileUtils.copyDirectory(ogGameDir, modGameDir);

        Platform.runLater(() -> Main.progressLabel.setText("Supplying old directories."));

        FileUtils.copyDirectory(new File(modGameDir + "/game/res/balls/generic"), new File(modGameDir + "/game/res/balls/_generic"));
        FileUtils.copyDirectory(new File(modGameDir + "/game/res/movie/generic"), new File(modGameDir + "/game/res/movie/_generic"));

        System.out.println("Finished");
        List<Goomod> finalGoomodsList = goomods.stream().filter((goomod1) -> goomod1.isEnabled() && !goomod1.isMalformed()).toList();
        float i = 0;
        for (Goomod goomod : finalGoomodsList) {
            if (goomod.isEnabled() && !goomod.isMalformed()) {
                InstallAddin.install(goomod);
            }
            i++;
            float finalI = i;
            Platform.runLater(() -> progressBar.setProgress(finalI / finalGoomodsList.size()));
        }
        Platform.runLater(() -> Main.progressLabel.setText("Done!"));
        Platform.runLater(() -> progressBar.setProgress(0));
        if (System.getProperty("os.name").startsWith("Windows")) {
            File tada = new File("C:/Windows/Media/tada.wav");
            if (tada.exists()) {
                new AudioClip("file:///C:/Windows/Media/tada.wav").play();
            }
        }
        if (play) {
            Runtime.getRuntime().exec(modGameDir.getAbsolutePath() + "/WorldOfGoo.exe", null , modGameDir);
        }
        Platform.runLater(() -> saveButton.setDisable(false));
        Platform.runLater(() -> saveAndPlayButton.setDisable(false));
    }
}