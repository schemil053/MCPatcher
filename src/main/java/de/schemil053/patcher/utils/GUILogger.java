package de.schemil053.patcher.utils;

import de.schemil053.patcher.Main;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;

public class GUILogger {
    public GUILogger() {
        JFrame frame = new JFrame();
        frame.setSize(400, 300);
        JTextPane editor = new JTextPane();
        Color background = new Color(56, 56, 56);
        Color textcolor = new Color(194, 194, 194);
        Color selectcolor = new Color(63,197,95);
        Color cursor = new Color(226, 226, 210);

        Color scrollbarcolor = new Color(150, 150, 150);


        editor.setBackground(background);
        editor.setEditable(false);
        editor.setForeground(textcolor);
        editor.setCaretColor(cursor);
        editor.setSelectionColor(selectcolor);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        editor.setBorder(new LineBorder(background));
        frame.getContentPane().setBackground(background);
        JScrollPane scrollPane = new JScrollPane(editor);
        scrollPane.setForeground(textcolor);
        scrollPane.setBackground(background);
        scrollPane.setViewportBorder(new LineBorder(background));
        scrollPane.setBorder(new LineBorder(background));
        scrollPane.getVerticalScrollBar().setUI(getScrollBarDesign(scrollbarcolor, background));
        scrollPane.getHorizontalScrollBar().setUI(getScrollBarDesign(scrollbarcolor, background));
        frame.getContentPane().add(scrollPane);

        if(Main.class.getResourceAsStream("/settings.econf") == null) {
            frame.setTitle("SchemilPatcher");
        } else {
            sConfig config = new sConfig(new File(".temp"));
            config.readFromInputStream(Main.class.getResourceAsStream("/settings.econf"));
            frame.setTitle(config.getString("title"));
        }

        frame.setVisible(true);

        try {
            BufferedImage bg = ImageIO.read(Objects.requireNonNull(Main.class.getResource("/img/icon.png")));
            frame.setIconImage(bg);
        } catch (Exception e) {
        }

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                try {
                    frame.setVisible(false);
                } catch (Exception exception) {}
            }
        });

        this.pane = editor;
    }

    private final JTextPane pane;

    public void logLine(String t) {
        pane.setText(pane.getText()+t+"\n");
    }

    public void log(String t) {
        pane.setText(pane.getText()+t);
    }


    private static BasicScrollBarUI getScrollBarDesign(Color scrollbarcolor, Color background) {
        return new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = scrollbarcolor;
                this.trackColor = background;
            }

            private JButton createZeroButton() {
                JButton button = new JButton("");
                Dimension zeroDim = new Dimension(0,0);
                button.setPreferredSize(zeroDim);
                button.setMinimumSize(zeroDim);
                button.setMaximumSize(zeroDim);
                return button;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) { return createZeroButton(); }
            @Override
            protected JButton createIncreaseButton(int orientation) { return createZeroButton(); }
        };
    }
}
