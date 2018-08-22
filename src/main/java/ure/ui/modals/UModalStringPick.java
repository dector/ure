package ure.ui.modals;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.sys.GLKey;

import java.util.ArrayList;

public class UModalStringPick extends UModal {

    UColor tempHiliteColor, flashColor;

    WidgetText headerWidget;
    WidgetListVert choicesWidget;

    public UModalStringPick(String _header, String[] _choices,
                            HearModalStringPick _callback, String _callbackContext) {
        super(_callback, _callbackContext);

        headerWidget = new WidgetText(0,0,_header);
        addWidget(headerWidget);
        choicesWidget = new WidgetListVert(0, headerWidget.h+1, _choices);
        addWidget(choicesWidget);
        sizeToWidgets();
        tempHiliteColor = commander.config.getHiliteColor();
        flashColor = new UColor(commander.config.getHiliteColor());
        flashColor.setAlpha(1f);
        dismissFrameEnd = 8;
    }

    @Override
    public void widgetClick(Widget widget, int mousex, int mousey) {
        if (widget == choicesWidget)
            selectChoice(choicesWidget.choice());
    }

    public void selectChoice(String choice) {
        dismiss();
        ((HearModalStringPick)callback).hearModalStringPick(callbackContext, choice);
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
