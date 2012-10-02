package oz.zomg.jport.common.gui;

import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JOptionPane;


/**
 * Force user response with a modal GUI.
 *
 * @see javax.swing.JOptionPane
 *
 * @author sbaber
 */
public class ModalDialogFactory
{
    static public enum EConfirmationChoices
            { YES_NO        // JOptionPane.YES_NO_OPTION        =0
            , YES_NO_CANCEL // JOptionPane.YES_NO_CANCEL_OPTION =1
            , OK_CANCEL     // JOptionPane.OK_CANCEL_OPTION     =2
            , OK            // user has been informed
            , CANCEL        // user has been warned
            }

    static public enum EUserResponse
            { CLOSED // JOptionPane.CLOSED_OPTION =-1 dialog dismissed
            , YES_OK // JOptionPane.YES_OPTION    =0
            , NO     // JOptionPane.NO_OPTION     =1
            , CANCEL // JOptionPane.CANCEL_OPTION =2
            }

    static // initializer block
    {}

    private ModalDialogFactory() {}

    /**
     * Force operator response with a modal GUI.
     *
     * @param confirmationEnum
     * @param parent 'null' is acceptable but does not seem to properly use the default parent component of the root frame
     * @param title can -not- be HTML in heavyweight, window title bars. Ex. "Station H1"
     * @param message can be HTML.  Ex. <code"<HTML><FONT size=+1>Application is already running.<BR><BR>Request quit?<BR><BR>"</code>
     * @return
     */
    static public EUserResponse showConfirmation
            ( final EConfirmationChoices confirmationEnum
            , final Component parent
            , final String title
            , final String message
            )
    {
        switch( confirmationEnum )
        {
            case OK : case CANCEL :
                {   JOptionPane.showMessageDialog
                        ( parent
                        , message
                        , title
                        , ( confirmationEnum == EConfirmationChoices.OK )
                                ? JOptionPane.INFORMATION_MESSAGE
                                : JOptionPane.WARNING_MESSAGE
                        );
                    return ( confirmationEnum == EConfirmationChoices.OK ) // as expected
                            ? EUserResponse.YES_OK
                            : EUserResponse.CANCEL;
                }

            default :
                {   final int userPick = JOptionPane.showConfirmDialog
                        ( parent
                        , message // nothing internally unexpected, does Object.toString()
                        , title
                        , confirmationEnum.ordinal()
                        , JOptionPane.WARNING_MESSAGE
                        );
                    return EUserResponse.values()[ userPick + 1 ];
                }
        }
    }

    /**
     * Force operator response with a modal GUI which asks a fill-in-the-blank question.
     *
     * @param parentComponent 'null' is acceptable but does not seem to properly use the default parent component of the root frame
     * @param questionText can be HTML
     * @param defaultResponse can be ""
     * @return 'null' if canceled or dismissed
     */
    static public String showFillInTheBlankQuestion
            ( final Component parentComponent
            , final String    questionText
            , final String    defaultResponse
            )
    {
        final String response = JOptionPane.showInputDialog
                ( parentComponent
                , questionText
                , defaultResponse
                );
        return response;
    }

    /**
     * Force operator response with a modal GUI which asks a fill-in-the-blank question.
     *
     * @param parentComponent 'null' is acceptable but does not seem to properly use the default parent component of the root frame
     * @param dialogTitle 'null' is acceptable but can -not- be HTML
     * @param icon can be 'null'
     * @param questionText can be HTML
     * @param defaultResponse can be ""
     * @return 'null' if canceled or dismissed
     */
    static public String showFillInTheBlankQuestion
        ( final Component parentComponent
        , final Icon      icon
        , final String    dialogTitle
        , final String    questionText
        , final String    defaultResponse
        )
    {
        final String response = (String)JOptionPane.showInputDialog // cast required
                ( parentComponent
                , questionText // nothing internally unexpected, does Object.toString()
                , dialogTitle
                , JOptionPane.QUESTION_MESSAGE
                , icon
                , null // array of choices that fill in a JComboBox but without editable text capability
                , defaultResponse // nothing internally unexpected, does Object.toString()
                );
        return response;
    }

    //... incomplete, see JDialog_GoTo
//...for an implementation or use the JComboBox based JOptionPane.showInputDialog(...)
    static private <T> T showMultipleChoiceQuestion
            ( final Component parentComponent
            , final String    questionText
            , final T...      answers
            )
    {
        return null;
    }
}
