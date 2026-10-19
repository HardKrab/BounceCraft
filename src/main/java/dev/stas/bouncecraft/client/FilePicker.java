package dev.stas.bouncecraft.client;

import dev.stas.bouncecraft.BounceCraft;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public final class FilePicker {
    private FilePicker() {
    }

    public static Path musicFolder() {
        Path dir = Minecraft.getInstance().gameDirectory.toPath().resolve("bouncecraft").resolve("music");
        try {
            Files.createDirectories(dir);
        } catch (Exception e) {
            BounceCraft.LOGGER.warn("Could not create music folder {}", dir, e);
        }
        return dir;
    }

    public static void open() {
        Minecraft mc = Minecraft.getInstance();
        String selected = nativeDialog();
        if (selected != null && !selected.isBlank()) {
            UploadClient.startFile(Path.of(selected));
            return;
        }
        mc.setScreen(new MusicFolderScreen(musicFolder()));
    }

    private static String nativeDialog() {
        Path folder = musicFolder();
        String defaultPath = folder.toAbsolutePath() + java.io.File.separator;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer filters = stack.mallocPointer(3);
            filters.put(stack.UTF8("*.ogg")).put(stack.UTF8("*.mp3")).put(stack.UTF8("*.wav")).flip();
            return TinyFileDialogs.tinyfd_openFileDialog(
                    "BounceCraft",
                    defaultPath,
                    filters,
                    "Audio (OGG/MP3/WAV)",
                    false
            );
        } catch (Throwable t) {
            BounceCraft.LOGGER.info("TinyFileDialogs unavailable ({}); using folder screen", t.toString());
            return null;
        }
    }
}
