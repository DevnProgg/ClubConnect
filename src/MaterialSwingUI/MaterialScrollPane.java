package MaterialSwingUI;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
/**
 * A custom {@code JScrollPane} that applies a material-design inspired dark theme
 * to its scrollbars. It specifically styles the scrollbar's thumb and track and
 * hides the default increase and decrease buttons.
 */
public class MaterialScrollPane extends JScrollPane {
    /**
     * Constructs a {@code MaterialScrollPane} that displays the view component
     * and customizes its vertical scrollbar appearance.
     *
     * @param view The {@code Component} displayed within the scroll pane's viewport.
     */
    public MaterialScrollPane(Component view){
        super(view);
        getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            /**
             * Overrides the default method to set the colors for the thumb (slider)
             * and the track (background of the scrollbar).
             */
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(90, 90, 90);
                this.trackColor = new Color(45, 45, 45);
            }

            /**
             * Creates an invisible button for the decrease (up/left) scroll control.
             *
             * @param orientation The orientation of the scrollbar (ignored).
             * @return An invisible {@code JButton}.
             */
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return invisibleButton();
            }

            /**
             * Creates an invisible button for the increase (down/right) scroll control.
             *
             * @param orientation The orientation of the scrollbar (ignored).
             * @return An invisible {@code JButton}.
             */
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return invisibleButton();
            }

            /**
             * Helper method to create a button that is effectively hidden by setting
             * its preferred size to zero and making it invisible.
             *
             * @return A hidden {@code JButton}.
             */
            private JButton invisibleButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setVisible(false);
                return btn;
            }
        });
    }
}