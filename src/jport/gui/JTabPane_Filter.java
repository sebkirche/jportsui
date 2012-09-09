package jport.gui;

import java.awt.Component;
import javax.swing.JTabbedPane;
import jport.common.gui.EmbeddedCreatorFactory_;
import jport.common.Interfacing_.Creatable;
import jport.common.Util;
import jport.gui.panel.JPanel_Histogram;
import jport.gui.panel.JPanel_StatusFilter;


/**
 * Contains controls for filtering the Port inventory table.
 * On the left side of the window.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JTabPane_Filter extends JTabbedPane
{
    public JTabPane_Filter()
    {
        super
            ( ( Util.isOnMac() == true ) ? JTabbedPane.RIGHT : JTabbedPane.TOP // Metal-PLAF doesn't do small numbers of LEFT & RIGHT tabs
            , JTabbedPane.WRAP_TAB_LAYOUT
            );

//?        final SingleSelectionModel ssm = this.getModel(); // manages which tab

//        final HistogramUiFactory histogramUi = new HistogramUiFactory();
        
        this.addTab( "Status"        , EmbeddedCreatorFactory_.create( new Creatable<Component>() { @Override public Component create() { return new JPanel_StatusFilter(); } } ) );
        this.addTab( "Totals"        , EmbeddedCreatorFactory_.create( new Creatable<Component>() { @Override public Component create() { return new JPanel_Histogram(); } } ) );

//        if( HistogramUiFactory.NEED_BREAK_OUT_CATEGORIES )
//        this.addTab( "Categories"    , EmbeddedCreatorFactory.create( new Creatable<Component>() { @Override public Component create() { return histogramUi.createCategoriesComponent(); } } ) );

//...        this.addTab( "Search Results", new JLabel( "SHOW HERE..." ) ); //...

        // doen't have expected declutter effect on Mac -> this.setBorder( null );
    }
}
