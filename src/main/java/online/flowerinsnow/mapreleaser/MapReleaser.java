package online.flowerinsnow.mapreleaser;

import online.flowerinsnow.mapreleaser.shaded.online.flowerinsnow.saussureautils.collection.ListUtils;
import online.flowerinsnow.mapreleaser.shaded.online.flowerinsnow.saussureautils.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MapReleaser extends JavaPlugin {
    private static MapReleaser instance;

    @Override
    public void onLoad() {
        instance = this;

        saveDefaultConfig();
        reloadConfig();

        if (!getConfig().getBoolean("enabled")) {
            getLogger().info("由于未在配置文件中将enabled设为true，所以插件暂未启用");
            setEnabled(false);
            return;
        }

        File worldContainer = Bukkit.getWorldContainer();
        List<String> rmdir = getConfig().getStringList("rmdir");
        rmdir.forEach(dir -> {
            File f = new File(worldContainer, dir);
            if (!f.exists()) {
                if (isConfigTrue("warning")) {
                    getLogger().info(dir + " 不是文件或文件夹，已跳过");
                }
                return;
            }
            IOUtils.delete(new File(worldContainer, dir));
            if (isConfigTrue("info")) {
                getLogger().info("已移除文件 " + f.getName());
            }
        });

        List<String> files = getConfig().getStringList("files");
        files.forEach(fn -> {
            File file = new File(fn);
            if (!file.isFile() || !file.canRead()) {
                if (isConfigTrue("warning")) {
                    getLogger().warning(fn + " 不是合法的可读文件，已跳过");
                }
                return;
            }
            ArrayList<AutoCloseable> resources = new ArrayList<>();
            try {
                InputStream fileIn = Files.newInputStream(file.toPath());
                resources.add(fileIn);
                ZipInputStream zin = new ZipInputStream(fileIn);
                resources.add(zin);

                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    File targetFile = new File(worldContainer, entry.getName());
                    if (entry.isDirectory()) {
                        //noinspection ResultOfMethodCallIgnored
                        targetFile.mkdirs();
                    } else {
                        IOUtils.copy(zin, targetFile);
                    }
                }
                if (isConfigTrue("info")) {
                    getLogger().info("已解压文件 " + file.getName());
                }
            } catch (IOException e) {
                getLogger().severe(fn + " 解压失败");
                e.printStackTrace();
            } finally {
                resources = ListUtils.reserve(resources);
                resources.forEach(IOUtils::closeQuietly);
            }
        });
    }

    public static MapReleaser getInstance() {
        return instance;
    }

    private boolean isConfigTrue(String path) {
        return getConfig().getBoolean(path);
    }
}
