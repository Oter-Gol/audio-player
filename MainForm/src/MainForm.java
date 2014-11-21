import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


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

        final Core core = new Core();



        playButton.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                core.open( "G:\\1.wav" );
                core.playPause();
            }
        });
    }
}
