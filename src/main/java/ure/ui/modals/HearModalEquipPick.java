package ure.ui.modals;

import ure.kotlin.ui.modals.HearModal;
import ure.things.UThing;

public interface HearModalEquipPick extends HearModal {
    void hearModalEquipPick(String callbackContext, UThing selection);
}
