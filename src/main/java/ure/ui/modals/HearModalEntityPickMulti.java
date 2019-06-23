package ure.ui.modals;

import ure.kotlin.ui.modals.HearModal;
import ure.sys.Entity;

import java.util.ArrayList;

public interface HearModalEntityPickMulti extends HearModal {

    void hearModalEntityPickMulti(String context, ArrayList<Entity> things);
}
