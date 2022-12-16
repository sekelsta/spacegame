package sekelsta.engine.render.gui;

import java.util.*;

public class SelectableElementList {
    private static final int INVALID = -1;
    private List<GuiElement> items = new ArrayList<>();
    private int selected = INVALID;

    public void add(GuiElement element) {
        items.add(element);
    }

    public void clear() {
        items.clear();
        selected = INVALID;
    }

    public void up() {
        if (selected > 0) {
            selected -= 1;
        }
        else {
            top();
        }
    }

    public void down() {
        if (selected + 1 < items.size()) {
            selected += 1;
        }
    }

    public void top() {
        if (items.size() > 0) {
            selected = 0;
        }
    }

    public void bottom() {
        if (items.size() > 0) {
            selected = items.size() - 1;
        }
    }

    public void clearSelection() {
        selected = INVALID;
    }

    public void selectByPointer(double posX, double posY) {
        for (int i = 0; i < items.size(); ++i) {
            if (items.get(i).containsPoint(posX, posY)) {
                selected = i;
            }
        }
    }

    public GuiElement getSelected() {
        if (selected == INVALID) {
            return null;
        }
        return items.get(selected);
    }
}
