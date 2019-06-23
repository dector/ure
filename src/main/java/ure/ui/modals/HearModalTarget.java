package ure.ui.modals;

import ure.kotlin.ui.modals.HearModal;
import ure.sys.Entity;

public interface HearModalTarget extends HearModal {

    void hearModalTarget(String context, Entity target, int targetX, int targetY);

}
