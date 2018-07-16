package ure.ui.modals;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.render.URenderer;

import java.util.ArrayList;

/**
 * ModalChoices gives the user a few plaintext choices and returns the one picked.
 *
 */
public class UModalChoices extends UModal {

    ArrayList<String> choices;
    int escapeChoice;
    boolean escapable;
    String[] prompt;
    int selection = 0;
    int hiliteX, hiliteY, hiliteW, hiliteH;
    UColor tempHiliteColor;
    UColor flashColor;

    public UModalChoices(String _prompt, ArrayList<String> _choices, int _escapeChoice, int _defaultChoice,
                         boolean _escapable, UColor bgColor, HearModalChoices _callback, String _callbackContext) {
        super(_callback, _callbackContext, bgColor);
        prompt = splitLines(_prompt);
        choices = _choices;
        escapeChoice = _escapeChoice;
        escapable = _escapable;
        selection = _defaultChoice;
        int width = longestLine(prompt);
        int height = 2;
        if (prompt != null)
            height += prompt.length;
        int cwidth = 0;
        for (String choice : choices) {
            cwidth = (choice.length() + 1);
        }
        if (cwidth > width)
            width = cwidth;
        setDimensions(width, height);
        dismissFrameEnd = 8;
        tempHiliteColor = commander.config.getHiliteColor();
        flashColor = new UColor(commander.config.getHiliteColor());
        flashColor.setAlpha(1f);
    }

    @Override
    public void drawContent(URenderer renderer) {
        if (prompt != null) {
            int i = 0;
            for (String line : prompt) {
                drawString(renderer, line, 0, i);
                i++;
            }
        }
        int xtab = 0;
        int drawSelection = 0;
        for (String choice : choices) {
            if (selection == drawSelection) {
                hiliteX = xtab * gw() + xpos;
                hiliteY = (cellh - 1) * gh() + ypos;
                hiliteW = choice.length() * gw();
                hiliteH = gh();
                renderer.drawRect(hiliteX, hiliteY, hiliteW, hiliteH, tempHiliteColor);
            }
            drawString(renderer, choice, xtab, cellh-1);
            xtab += choice.length() + 1;
            drawSelection++;
        }
    }

    @Override
    public void hearCommand(UCommand command, Character c) {
        if (command.id.equals("MOVE_W") || command.id.equals("MOVE_N"))
            selection--;
        if (command.id.equals("MOVE_E") || command.id.equals("MOVE_S"))
            selection++;
        if (command.id.equals("PASS"))
            pickSelection();
        if (command.id.equals("ESC")) {
            if (escapable)
                escape();
        }

        if (selection < 0) {
            if (commander.config.isWrapSelect())
                selection = choices.size()-1;
            else
                selection = 0;
        } else if (selection >= choices.size()) {
            if (commander.config.isWrapSelect())
                selection = 0;
            else
                selection = choices.size()-1;
        }
    }

    public void pickSelection() {
        dismiss();
        ((HearModalChoices)callback).hearModalChoices(callbackContext, choices.get(selection));
    }

    @Override
    public void animationTick() {
        if (dismissed) {
            if ((dismissFrames % 2) == 0) {
                tempHiliteColor = commander.config.getModalBgColor();
            } else {
                tempHiliteColor = flashColor;
            }
        }
        super.animationTick();
    }
}
