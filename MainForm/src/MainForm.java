import javax.swing.*;


/**
 * Created by OTER on 11/10/2014.
 */
public class MainForm extends JFrame {
    private JPanel panel1;
    private JButton playButton;
    private JButton prevButton;
    private JButton stopButton;
    private JButton nextButton;
    private JSlider slider1;
    private JList list1;
    private JButton addButton;


    public MainForm() {
        super("Simple Java based player");

        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        pack();

        setVisible(true);
        setSize( 600, 600 );

        DefaultListModel listModel = new DefaultListModel();

        list1.setModel( listModel );
        listModel.add( 0, "Hello" );


        setContentPane( panel1 );

        Core core = new Core();

        core.open( "G:\\1.wav" );

    }
}
