import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;


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

    ArrayList<String> fileList = new ArrayList<String>();

    private boolean sliderCaptured = false;

    ImageIcon playIcon = new ImageIcon( "rc/play_32x32.png" );
    ImageIcon pauseIcon = new ImageIcon("rc/pause_32x32.png");

    DefaultListModel listModel = new DefaultListModel();

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

        list1.setModel( listModel );

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
        slider1.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                slider1.setValue( (int)( (float)e.getX() / slider1.getWidth() * slider1.getMaximum() )  );
                core.seekPlaying( (float)slider1.getValue() / slider1.getMaximum() );
            }
        });


        slider1.addMouseMotionListener(new MouseMotionAdapter() {
            /**
             * Invoked when a mouse button is pressed on a component and then
             * dragged.  Mouse drag events will continue to be delivered to
             * the component where the first originated until the mouse button is
             * released (regardless of whether the mouse position is within the
             * bounds of the component).
             *
             * @param e
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);

                if ( sliderCaptured ) {
                    slider1.setValue( (int)( (float)e.getX() / slider1.getWidth() * slider1.getMaximum() )  );
                }
            }
        });

        new FileDrop( list1, new FileDrop.Listener()
        {   public void  filesDropped( java.io.File[] files )
            {
                for ( File file : files ) {

                    listModel.addElement(file.getName());
                    fileList.add( file.getPath() );
                }
            }   // end filesDropped
        }); // end FileDrop.Listener

        list1.addKeyListener(new KeyAdapter() {
            /**
             * Invoked when a key has been released.
             *
             * @param e
             */
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);

                if ( e.getKeyCode() == KeyEvent.VK_DELETE ){
                    int index = list1.getSelectedIndex();
                    if ( index >= 0 ){
                        listModel.remove( index );
                        fileList.remove( index );
                    }
                }
            }
        });


        list1.addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             *
             * @param e
             */
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (e.getClickCount() == 2) {
                    int index = list1.locationToIndex(e.getPoint()); // Double click action with click point
                    core.stop();
                    core.open( fileList.get( index ));
                    playButton.doClick( 100 );

                }
            }
        });


        playButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e
             */
            @Override
            public void actionPerformed(ActionEvent e) {
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
    }
}
