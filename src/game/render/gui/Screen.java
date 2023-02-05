package sekelsta.game.render.gui;

import java.util.*;
import sekelsta.engine.render.gui.*;

public class Screen {
    protected SelectableElementList selectable = new SelectableElementList();
    protected List<GuiElement> items = new ArrayList<>();

    public void refresh() {
        // Do nothing
    }

    public void onEscape() {
        // Do nothing
    }

    protected void addSelectableItem(GuiElement item) {
        items.add(item);
        selectable.add(item);
    }

    public boolean pausesGame() {
        return false;
    }

    public void positionPointer(double xPos, double yPos) {
        selectable.clearSelection();
        selectable.selectByPointer(xPos, yPos);

        GuiElement selected = selectable.getSelected();
        if (selected instanceof TextChoice) {
            ((TextChoice)selected).hover(xPos, yPos);
        }
    }

    public boolean trigger() {
        GuiElement selected = selectable.getSelected();
        if (selected != null) {
            return selected.trigger();
        }
        return false;
    }

    public boolean click(double xPos, double yPos) {
        positionPointer(xPos, yPos);
        GuiElement selected = selectable.getSelected();
        if (selected != null) {
            selected.click(xPos, yPos);
            return true;
        }
        return false;
    }

    public void holdLeftMouseButton(double xPos, double yPos) {
        GuiElement selected = selectable.getSelected();
        if (selected instanceof Slider) {
            ((Slider)selected).holdLeftMouseButton(xPos, yPos);
        }
    }

    public void up() {
        selectable.up();
    }

    public void down() {
        selectable.down();
    }

    public void left() {
        GuiElement selected = selectable.getSelected();
        if (selected != null) {
            selected.left();
        }
    }

    public void right() {
        GuiElement selected = selectable.getSelected();
        if (selected != null) {
            selected.right();
        }
    }

    public void top() {
        selectable.top();
    }

    public void bottom() {
        selectable.bottom();
    }

    public void backspace() {
        GuiElement textFocus = selectable.getTextFocus();
        if (textFocus != null) {
            textFocus.backspace();
        }
    }

    public void inputCharacter(char character) {
        GuiElement textFocus = selectable.getTextFocus();
        if (textFocus != null) {
            textFocus.inputCharacter(character);
        }
    }

    public void tab() {
        selectable.tab();
    }

    public void blit(double screenWidth, double screenHeight) {
        int height = 0;
        for (int i = 0; i < items.size(); ++i) {
            int h = items.get(i).getHeight();
            height += h;
            if (i + 1 != items.size()) {
                height += h / 4;
            }
        }
        int yPos = ((int)screenHeight - height) / 2;
        GuiElement selected = selectable.getSelected();
        for (GuiElement item : items) {
            item.position(((int)screenWidth - item.getWidth()) / 2, yPos);
            yPos += (int)(1.25 * item.getHeight());
            item.blit(null, item == selected);
        }
    }
}
