package jport.gui;

import java.util.Arrays;
import jport.common.StringsUtil_;
import jport.common.gui.ModalDialogFactory;
import jport.common.gui.ModalDialogFactory.EConfirmationChoices;
import jport.common.gui.ModalDialogFactory.EUserResponse;
import jport.ports.PortsMarker;
import jport.type.EPortMark;
import jport.type.Portable;


/**
 *
 * @author sbaber
 */
public class MarkConfirmUi
{
    private MarkConfirmUi() {}

    /**
     * User interaction. Does the marking but can also revert it.
     *
     * @param port that may not end up being marked
     * @param markEnum
     */
    static public void showConfirmation( Portable port, EPortMark markEnum )
    {
        final EPortMark revertPortMark = port.getMark();
        port.setMark( markEnum ); // will notify back to here so do not .setMarkSelection()

        final String title;
        switch( markEnum )
        {
            case Uninstall : case Deactivate :
                    title = "Release these dependants?";
                    break;

            case Install : case Activate :
                    title = "Add these dependencies?";
                    break;

            case Upgrade :
                    title = "Upgrade these dependencies?";
                    break;

            default : throw new IllegalArgumentException();
        }

        final Portable[] portDeps = PortsMarker.gatherDeps( port, markEnum );
        if( portDeps.length != 0 )
        {
            Arrays.sort( portDeps ); // alphabetize

            final EUserResponse response = ModalDialogFactory.showConfirmation
                    ( EConfirmationChoices.OK_CANCEL
                    , TheUiHolder.INSTANCE.getMainFrame()
                    , title
                    , StringsUtil_.htmlTabularize( 6, "<FONT color=gray>\u2192</FONT>", "", portDeps ) +"<BR>" // right arrow
                    );

            if( response == EUserResponse.CANCEL || response == EUserResponse.CLOSED )
            {
                port.setMark( revertPortMark );
            }
        }
    }
}
