package sekelsta.engine.render.gui;

import java.util.*;

public class SelectableElementList {
    private static final int INVALID = -1;
    private List<GuiElement> items = new ArrayList<>();
    private int selected = INVALID;
    private int textFocus = INVALID;

    public void add(GuiElement element) {
        if (textFocus == INVALID && element instanceof TextInput) {
            textFocus = items.size();
        }

        items.add(element);
    }

    public void clear() {
        items.clear();
        selected = INVALID;
        textFocus = INVALID;
    }

    public void up() {
        if (selected > 0) {
            selected -= 1;
        }
        else {
            top();
        }
        adjustTextFocus();
    }

    public void down() {
        if (selected + 1 < items.size()) {
            selected += 1;
        }
        adjustTextFocus();
    }

    public void top() {
        if (items.size() > 0) {
            selected = 0;
        }
        adjustTextFocus();
    }

    public void bottom() {
        if (items.size() > 0) {
            selected = items.size() - 1;
        }
        adjustTextFocus();
    }

    private void adjustTextFocus() {
        if (items.get(selected) instanceof TextInput) {
            textFocus = selected;
        }
    }

    public void tab() {
        for (int i = textFocus + 1; i < items.size(); ++i) {
            if (items.get(i) instanceof TextInput) {
                textFocus = i;
                return;
            }
        }
        for (int i = 0; i < textFocus; ++i) {
            if (items.get(i) instanceof TextInput) {
                textFocus = i;
                return;
            }
        }
        // No text inputs in items (except possibly the selected one); do nothing
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

    public boolean focusTextByPointer(double posX, double posY) {
        for (int i = 0; i < items.size(); ++i) {
            GuiElement item = items.get(i);
            if (item instanceof TextInput && item.containsPoint(posX, posY)) {
                textFocus = i;
                return true;
            }
        }
        return false;
    }

    public GuiElement getSelected() {
        if (selected == INVALID) {
            return null;
        }
        return items.get(selected);
    }

    public GuiElement getTextFocus() {
        if (textFocus == INVALID) {
            return null;
        }
        return items.get(textFocus);
    }

    public boolean isLastTextInputFocused() {
        // No item selected
        if (textFocus == INVALID) {
            return false;
        }
        // Selected item is wrong type
        if (!(items.get(textFocus) instanceof TextInput)) {
            return false;
        }
        // Selected text input is not last
        for (int i = textFocus + 1; i < items.size(); ++i) {
            if (items.get(i) instanceof TextInput) {
                return false;
            }
        }
        return true;
    }

    public void setTextFocus(GuiElement newFocus) {
        for (int i = 0; i < items.size(); ++i) {
            if (items.get(i) == newFocus) {
                textFocus = i;
                return;
            }
        }
        throw new IllegalArgumentException("Element is not in selectable list");
    }
}
