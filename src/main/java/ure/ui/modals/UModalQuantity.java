package ure.ui.modals;

import ure.commands.UCommand;
import ure.math.UColor;
import ure.sys.GLKey;
import ure.ui.modals.widgets.WidgetHSlider;
import ure.ui.modals.widgets.WidgetText;

public class UModalQuantity extends UModal {

    int min, max, count;
    int numberX;
    int barwidth;

    WidgetText headerWidget;
    WidgetHSlider sliderWidget;

    public UModalQuantity(String _prompt, int _min, int _max, HearModalQuantity _callback, String _callbackContext) {
        super(_callback,_callbackContext);
        headerWidget = new WidgetText(this,0,0,_prompt);
        sliderWidget = new WidgetHSlider(this,0,headerWidget.h+1,15, _min, _max, true);
        addWidget(headerWidget);
        addWidget(sliderWidget);
        sizeToWidgets();
        centerWidget(headerWidget);
        centerWidget(sliderWidget);
    }

    public void sendInput() {
        dismiss();
        ((HearModalQuantity)callback).hearModalQuantity(callbackContext, count);
    }
}
