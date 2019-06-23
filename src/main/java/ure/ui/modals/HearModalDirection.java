package ure.ui.modals;

/**
 * Implement this interface to receive callbacks from a ModalDirection.
 *
 */
public interface HearModalDirection extends ModalCallback {

    void hearModalDirection(String context, int xdir, int ydir);

}
