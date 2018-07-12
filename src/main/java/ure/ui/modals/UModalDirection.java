package ure.ui.modals;

import ure.render.URenderer;

public class UModalDirection extends UModal {

    String prompt;
    boolean acceptNull;

    public UModalDirection(String _prompt, boolean _acceptNull, HearModalDirection _callback, String _callbackContext) {
        super(_callback, _callbackContext);
        prompt = _prompt;
        acceptNull = _acceptNull;
    }

    @Override
    public void hearCommand(String command) {
        ((HearModalDirection)callback).hearModalDirection(callbackContext, 0, -1);
        dismiss();
    }

    @Override
    public void draw(URenderer renderer) {
        commander.printScroll(prompt);
    }
}