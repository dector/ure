package ure.ui.modals;

import ure.ui.modals.widgets.Widget;

import java.util.ArrayList;
import java.util.HashMap;

public class UModalTabs extends UModal {

    HashMap<String,ArrayList<Widget>> tabWidgetSets;
    String tab;

    public UModalTabs(HearModal _callback, String _callbackContext) {
        super(_callback, _callbackContext);
        tabWidgetSets = new HashMap<>();
    }

    @Override
    public void addWidget(Widget widget) {
        super.addWidget(widget);
        if (tab != null) {
            tabWidgetSets.get(tab).add(widget);
        }
    }

    @Override
    public void removeWidget(Widget widget) {
        super.removeWidget(widget);
        if (tab != null) {
            tabWidgetSets.get(tab).remove(widget);
        }
    }

    public void changeTab(String newtab) {
        if (newtab != null)
            if (newtab.equals(tab)) return;
        if (tab != null) {
            for (Widget w : tabWidgetSets.get(tab))
                super.removeWidget(w);
        }
        tab = newtab;
        if (newtab != null) {
            if (tabWidgetSets.get(tab) == null)
                tabWidgetSets.put(tab, new ArrayList<>());
            for (Widget w : tabWidgetSets.get(tab))
                super.addWidget(w);
        }
    }

    public ArrayList<String> tabList() {
        ArrayList<String> tablist = new ArrayList<>();
        for (String s : tabWidgetSets.keySet()) {
            tablist.add(s);
        }
        return tablist;
    }
}
