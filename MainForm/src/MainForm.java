import javax.swing.*;
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

    private boolean sliderCaptured = false;

    ImageIcon playIcon = new ImageIcon( "rc/play_32x32.png" );
    ImageIcon pauseIcon = new ImageIcon("rc/pause_32x32.png");

    private class UpdateSlider implements SliderUpdater {

        @Override
        public void updateSlider(float value) {
            if ( !sliderCaptured ){
                slider1.setValue( (int)( value * slider1.getMaximum() ) );
            }
        }
    }




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

        UpdateSlider updateSlider = new UpdateSlider();
        // Set callback
        core.setCallBackSlider( updateSlider );


        playButton.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                core.playPause();
                if ( core.getPlayingState() == Core.PlayingStates.PLAY ) {
                    playButton.setIcon( pauseIcon );
                } else {
                    if ( core.getPlayingState() == Core.PlayingStates.PAUSE ) {
                        playButton.setIcon( playIcon );
                    }
                }
            }
        });
        addButton.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                core.open( "/home/oleh/Music/1.wav" );
            }
        });
        slider1.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                sliderCaptured = true;



            }
        });
        slider1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);

                sliderCaptured = false;

                core.seekPlaying( (float)slider1.getValue() / slider1.getMaximum() );



            }
        });
    }
}
