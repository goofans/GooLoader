package me.wy.gooloader.gooloader.util;

import javafx.scene.control.CheckBox;
import javafx.scene.control.TableCell;
import me.wy.gooloader.gooloader.Goomod;

public class GoomodCell extends TableCell<Goomod, Boolean> {
    private final CheckBox checkBox;
    public Goomod goomod;

    public GoomodCell() {
        checkBox = new CheckBox();
        goomod = getTableView().getItems().get(getIndex());
        checkBox.setSelected(goomod.isEnabled());
        checkBox.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            if (goomod != null) {
                if (t1 != goomod.isEnabled()) {
                    if (t1) {
                        FileUtil.addGoomod(goomod);
                    } else {
                        FileUtil.removeGoomod(goomod);
                    }
                }
            }
        });
    }

    @Override
    protected void updateItem(Boolean item, boolean b) {
        super.updateItem(item, b);
        setText(null);
        if(!b){
            checkBox.setSelected(item);
            setGraphic(checkBox);
        }
        else {
            setGraphic(null);
        }
    }
}
