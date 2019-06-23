package ure.ui.modals;

import ure.kotlin.ui.modals.HearModal;
import ure.sys.Entity;

public interface HearModalEntityPick extends HearModal {

    void hearModalEntityPick(String callbackContext, Entity selection);
}
