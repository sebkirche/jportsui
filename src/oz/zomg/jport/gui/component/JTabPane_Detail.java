package oz.zomg.jport.gui.component;

import java.awt.Component;
import javax.swing.JTabbedPane;
import oz.zomg.jport.common.Interfacing_.Creatable;
import oz.zomg.jport.common.gui.EmbeddedCreatorFactory_;
import oz.zomg.jport.common.gui.JScrollPaneFactory_.EScrollPolicy;
import oz.zomg.jport.gui.component.AJLabel_PortInfo.JLabel_Dependants;
import oz.zomg.jport.gui.component.AJLabel_PortInfo.JLabel_Dependencies;
import oz.zomg.jport.gui.component.AJLabel_PortInfo.JLabel_Description;
import oz.zomg.jport.gui.component.AJLabel_PortInfo.JLabel_FilesInstalled;
import oz.zomg.jport.gui.panel.JPanel_CommonInfo;
import oz.zomg.jport.gui.table.TableModel_Variants;
import oz.zomg.jport.type.Portable;


/**
 * Show various details of the lead selection from the Ports inventory.
 * At the bottom-right of the main UI or in an external frame.
 *
 * @author sbaber
 */
@SuppressWarnings("serial")
public class JTabPane_Detail extends JTabbedPane
{
    static
    {}

    /**
     * Follows main table selections.
     */
    public JTabPane_Detail()
    {
        this( Portable.NONE );
    }

    /**
     * Also constructor for externally framed port details.
     *
     * @param port assign NONE if waiting to be notified by user selections
     */
    public JTabPane_Detail( final Portable port )
    {
        super( JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT );

        this.addTab( "Info"           , EmbeddedCreatorFactory_.create( new Creatable<Component>() { @Override public Component create() { return new JPanel_CommonInfo    ( port ); } } ) );

        this.addTab( "Description"    , EmbeddedCreatorFactory_.create( new Creatable<Component>() { @Override public Component create() { return new JLabel_Description   ( port ); } } ) );

        this.addTab( "Variants"       , EmbeddedCreatorFactory_.create( EScrollPolicy.VERT_AS_NEEDED__HORIZ_NONE
                                                                      , new Creatable<Component>() { @Override public Component create() { return new TableModel_Variants  ( port ).getJTable(); } } ) );

        this.addTab( "Dependencies"   , EmbeddedCreatorFactory_.create( EScrollPolicy.VERT_AS_NEEDED__HORIZ_NONE
                                                                      , new Creatable<Component>() { @Override public Component create() { return new JLabel_Dependencies  ( port ); } } ) );

        this.addTab( "Dependants"     , EmbeddedCreatorFactory_.create( EScrollPolicy.VERT_AS_NEEDED__HORIZ_NONE
                                                                      , new Creatable<Component>() { @Override public Component create() { return new JLabel_Dependants    ( port ); } } ) );

        this.addTab( "Installed Files", EmbeddedCreatorFactory_.create( EScrollPolicy.VERT_AS_NEEDED__HORIZ_NONE
                                                                      , new Creatable<Component>() { @Override public Component create() { return new JLabel_FilesInstalled( port ); } } ) );

//        this.addTab( "Versions"       , EmbeddedCreatorFactory.create( EScrollPolicy.VERT_AS_NEEDED__HORIZ_NONE
  //                                                                   , new Creatable<Component>() { @Override public Component create() { return new JLabel_Versioning    ( port ); } } ) );

//        this.addTab( "<>"             , EmbeddedCreatorFactory_.create( new Creatable<Component>() { @Override public Component create() { return new JLabel_PortFields    ( port ); } } ) );
    }
}
