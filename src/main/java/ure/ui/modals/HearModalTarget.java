package ure.ui.modals;

import ure.sys.Entity;

public interface HearModalTarget extends ModalCallback {

    void hearModalTarget(String context, Entity target, int targetX, int targetY);

}
