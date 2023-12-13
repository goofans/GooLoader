package me.wy.gooloader.gooloader.util;

import me.wy.gooloader.gooloader.Goomod;
import me.wy.gooloader.gooloader.Main;
import me.wy.gooloader.gooloader.format.BinltlAN;
import me.wy.gooloader.gooloader.format.BinuniAN;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FileUtil {
    public static void deleteFolderContents(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
    }
    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    public static void addGoomod(Goomod goomod) {
        try {
            Files.write(Main.addinsList.toPath(), (goomod.getId() + "|").getBytes(), StandardOpenOption.APPEND);
            Main.enabledAddins = new ArrayList<>();
            Main.enabledAddins.addAll(Arrays.stream(Files.readString(Main.addinsList.toPath()).split("\\|")).toList());
            goomod.setEnabled(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Enabled: " + goomod.getName());
    }

    public static void removeGoomod(Goomod goomod) {
        try {
            String lines = FileUtils.readFileToString(Main.addinsList, Charset.defaultCharset());
            String updatedLines = lines.replace(goomod.getId() + "|", "");
            FileUtils.writeStringToFile(Main.addinsList, updatedLines, Charset.defaultCharset());
            System.out.println(Main.enabledAddins);
            Main.enabledAddins.remove(goomod.getId());
            goomod.setEnabled(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Disabled: " + goomod.getName());
    }
}
