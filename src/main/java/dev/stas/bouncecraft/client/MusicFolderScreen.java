package dev.stas.bouncecraft.client;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class MusicFolderScreen extends Screen {
    private final Path folder;
    private FileList list;
    private Path selected;

    public MusicFolderScreen(Path folder) {
        super(Component.translatable("screen.bouncecraft.music"));
        this.folder = folder;
    }

    @Override
    protected void init() {
        list = new FileList(minecraft, width, height - 64, 24, 20);
        addRenderableWidget(list);
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                .bounds(width / 2 - 154, height - 28, 150, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.bouncecraft.play"), b -> playSelected())
                .bounds(width / 2 + 4, height - 28, 150, 20).build());
    }

    private void playSelected() {
        if (selected == null) {
            return;
        }
        onClose();
        UploadClient.startFile(selected);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(font, folder.toAbsolutePath().toString(), width / 2, 18, 0xA0A0A0);
    }

    private final class FileList extends ObjectSelectionList<FileList.Entry> {
        private FileList(net.minecraft.client.Minecraft mc, int width, int height, int y, int itemHeight) {
            super(mc, width, height, y, itemHeight);
            List<Path> files = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(folder)) {
                for (Path path : stream) {
                    String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                    if (name.endsWith(".ogg") || name.endsWith(".mp3") || name.endsWith(".wav")) {
                        files.add(path);
                    }
                }
            } catch (IOException ignored) {
            }
            files.sort(Comparator.comparing(p -> p.getFileName().toString().toLowerCase(Locale.ROOT)));
            for (Path file : files) {
                addEntry(new Entry(file));
            }
        }

        @Override
        public void setSelected(Entry entry) {
            super.setSelected(entry);
            selected = entry == null ? null : entry.file;
        }

        @Override
        public int getRowWidth() {
            return width - 40;
        }

        private final class Entry extends ObjectSelectionList.Entry<Entry> {
            private final Path file;

            private Entry(Path file) {
                this.file = file;
            }

            @Override
            public void render(
                    GuiGraphics graphics,
                    int index,
                    int top,
                    int left,
                    int width,
                    int height,
                    int mouseX,
                    int mouseY,
                    boolean hovering,
                    float partialTick
            ) {
                graphics.drawString(font, file.getFileName().toString(), left + 4, top + 4, hovering ? 0xFFFFA0 : 0xFFFFFF);
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                FileList.this.setSelected(this);
                return true;
            }

            @Override
            public Component getNarration() {
                return Component.literal(file.getFileName().toString());
            }
        }
    }
}
