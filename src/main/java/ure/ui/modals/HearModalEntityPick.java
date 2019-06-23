package ure.ui.modals;

import ure.sys.Entity;

public interface HearModalEntityPick extends ModalCallback {

    void hearModalEntityPick(String callbackContext, Entity selection);
}
