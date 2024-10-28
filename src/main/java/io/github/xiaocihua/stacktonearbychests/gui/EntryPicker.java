package io.github.xiaocihua.stacktonearbychests.gui;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.Texture;
import juuxel.libninepatch.NinePatch;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static io.github.xiaocihua.stacktonearbychests.ModOptions.MOD_ID;
import static io.github.xiaocihua.stacktonearbychests.gui.ModOptionsGui.TEXT_COLOR;

public abstract class EntryPicker extends WBox {

    protected static final String PREFIX = ModOptionsGui.PREFIX + "entryPicker.";

    private final WLabel title;
    private final WTextField searchByName;
    private final WTextField searchByID;
    protected SelectableEntryList<Identifier> entryList;
    private final WBoxCustom bottomBar;

    private Optional<Runnable> onClose = Optional.empty();

    public EntryPicker(Consumer<List<Identifier>> consumer) {
        super(Axis.VERTICAL);
        insets = Insets.ROOT_PANEL;
        setSize(250, 0);

        title = new WLabel(getTitle(), TEXT_COLOR);
        add(title);

        searchByName = new WTextField(Text.translatable(PREFIX + "searchByName"))
                .setChangedListener(searchStr -> entryList.setData(searchByName(searchStr)));
        add(searchByName);

        searchByID = new WTextField(Text.translatable(PREFIX + "searchByID"))
                .setChangedListener(searchStr -> entryList.setData(searchByID(searchStr)));
        add(searchByID);

        entryList = getEntryList();
        add(entryList);

        bottomBar = new WBoxCustom(Axis.HORIZONTAL);
        bottomBar.setHorizontalAlignment(HorizontalAlignment.RIGHT);

        var addButton = new FlatColorButton(Text.translatable(PREFIX + "add")).setBorder()
                .setOnClick(() -> {
                    consumer.accept(entryList.getSelectedData());
                    close();
                });
        bottomBar.add(addButton, 50);

        var cancelButton = new FlatColorButton(Text.translatable(PREFIX + "cancel")).setBorder()
                .setOnClick(this::close);
        bottomBar.add(cancelButton, 50);

        add(bottomBar);

        layout();
    }

    @Override
    public void layout() {
        int width = this.width - insets.left() - insets.right();
        title.setSize(width, 12);
        searchByName.setSize(width, 20);
        searchByID.setSize(width, 20);
        entryList.setSize(width, 140);
        bottomBar.setSize(width, 20);
        super.layout();
    }

    public abstract Text getTitle();

    public abstract List<Identifier> searchByName(String searchStr);

    public abstract List<Identifier> searchByID(String searchStr);

    public abstract SelectableEntryList<Identifier> getEntryList();

    public void setOnClose(Runnable onClose) {
        this.onClose = Optional.ofNullable(onClose);
    }

    public void close() {
        onClose.ifPresent(Runnable::run);
    }

    @Override
    public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
        getBackgroundPainter().paintBackground(context, x, y, this);
        super.paint(context, x, y, mouseX, mouseY);
    }

    @Override
    public BackgroundPainter getBackgroundPainter() {
        return BackgroundPainter.createNinePatch(new Texture(Identifier.of(MOD_ID, "textures/background_dark_bordered.png")),
                builder -> builder.mode(NinePatch.Mode.STRETCHING).cornerSize(4).cornerUv(0.25f));
    }

    @Override
    public boolean canResize() {
        return false;
    }

    public static class ItemPicker extends EntryPicker {

        public ItemPicker(Consumer<List<Identifier>> consumer) {
            super(consumer);
            entryList.setData(searchByName(""));
        }

        @Override
        public Text getTitle() {
            return Text.translatable(PREFIX + "addItemsToList");
        }

        @Override
        public List<Identifier> searchByName(String searchStr) {
            return Registries.ITEM.stream()
                    .filter(item -> StringUtils.containsIgnoreCase(item.getName().getString(), searchStr))
                    .map(Registries.ITEM::getId)
                    .toList();
        }

        @Override
        public List<Identifier> searchByID(String searchStr) {
            return Registries.ITEM.getIds().stream()
                    .filter(identifier -> StringUtils.containsIgnoreCase(identifier.toString(), searchStr))
                    .toList();
        }

        @Override
        public SelectableEntryList<Identifier> getEntryList() {
            return new SelectableEntryList<>(ItemEntry::new);
        }
    }

    public static class BlockContainerPicker extends EntryPicker {

        public BlockContainerPicker(Consumer<List<Identifier>> consumer) {
            super(consumer);
            entryList.setData(searchByName(""));
        }

        @Override
        public Text getTitle() {
            return Text.translatable(PREFIX + "addContainersToList");
        }

        @Override
        public List<Identifier> searchByName(String searchStr) {
            return Registries.BLOCK.stream()
                    .filter(block -> block instanceof BlockEntityProvider)
                    .filter(block -> StringUtils.containsIgnoreCase(block.getName().toString(), searchStr))
                    .map(Registries.BLOCK::getId)
                    .toList();
        }

        @Override
        public List<Identifier> searchByID(String searchStr) {
            return Registries.BLOCK.stream()
                    .filter(block -> block instanceof BlockEntityProvider)
                    .map(Registries.BLOCK::getId)
                    .filter(identifier -> StringUtils.containsIgnoreCase(identifier.toString(), searchStr))
                    .toList();
        }

        @Override
        public SelectableEntryList<Identifier> getEntryList() {
            return new SelectableEntryList<>(BlockContainerEntry::new);
        }
    }

    public static class EntityContainerPicker extends EntryPicker {
        public EntityContainerPicker(Consumer<List<Identifier>> consumer) {
            super(consumer);
            entryList.setData(searchByName(""));
        }

        @Override
        public Text getTitle() {
            return Text.translatable(PREFIX + "addContainersToList");
        }

        @Override
        public List<Identifier> searchByName(String searchStr) {
            return Registries.ENTITY_TYPE.stream()
                    .filter(entityType -> StringUtils.containsIgnoreCase(entityType.getName().getString(), searchStr))
                    .map(Registries.ENTITY_TYPE::getId)
                    .toList();
        }

        @Override
        public List<Identifier> searchByID(String searchStr) {
            return Registries.ENTITY_TYPE.getIds().stream()
                    .filter(identifier -> StringUtils.containsIgnoreCase(identifier.toString(), searchStr))
                    .toList();
        }

        @Override
        public SelectableEntryList<Identifier> getEntryList() {
            return new SelectableEntryList<>(EntityContainerEntry::new);
        }
    }
}

