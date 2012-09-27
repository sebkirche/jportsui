package jport.gui.window;

import javax.swing.JDialog;
import jport.PortsConstants;
import jport.gui.TheUiHolder;


/**
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
class JDialog_AboutApp extends JDialog
{
    JDialog_AboutApp()
    {
        super
            ( TheUiHolder.INSTANCE.getMainFrame() // stay on top
            , PortsConstants.APP_NAME +" designed and coded by Stephen Baber"
            , ModalityType.APPLICATION_MODAL
            );


//... animate icons from logo cache
    }

}
