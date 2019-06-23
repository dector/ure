package ure.ui.modals;

import ure.sys.Entity;

import java.util.ArrayList;

public interface HearModalEntityPickMulti extends ModalCallback {

    void hearModalEntityPickMulti(String context, ArrayList<Entity> things);
}
