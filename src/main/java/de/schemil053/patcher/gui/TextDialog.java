package de.schemil053.patcher.gui;

import de.schemil053.ui.component.InputField;
import de.schemil053.ui.component.TextComponent;

import javax.swing.*;

import java.awt.*;
import java.util.function.Consumer;

import static de.schemil053.patcher.gui.FileSelectionDialog.*;

public class TextDialog extends JDialog {
    private TextComponent titleComp;
    private TextComponent applyComp;
    private TextComponent cancelComp;
    public volatile Consumer<String> accepta = null;
    private InputField nameField;
    private String ntext = "";
    public TextDialog(JFrame screen){
        super(screen, true);
        setUndecorated(true);
        setTitle("Texteingabe");
        setIconImage(screen.getIconImage());
        setSize(400, 70);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBackground(c2);
        JPanel panel = new JPanel(null);
        panel.setBackground(c2);
        setContentPane(panel);
        setLayout(null);
        init();
    }

    public void init(){
        titleComp = new TextComponent("Text eingeben", c2, TOOLMENU_GRADIENT, glow, null);
        titleComp.setBounds(0, 0, getWidth() - 120, 30);
        titleComp.setClickable(false);
        titleComp.setFont(PX14);
        titleComp.setArc(0, 0);
        titleComp.attachDragger(this);
        add(titleComp);

        cancelComp = new TextComponent("Abbrechen", TOOLMENU_COLOR1_SHADE, c2, TOOLMENU_COLOR1, () -> {
            System.exit(0);
        });
        cancelComp.setBounds(getWidth() - 120, 0, 60, 30);
        cancelComp.setFont(PX14);
        cancelComp.setArc(0, 0);
        add(cancelComp);

        applyComp = new TextComponent("Fortfahren", TOOLMENU_COLOR2_SHADE, c2, TOOLMENU_COLOR2, this::apply);
        applyComp.setBounds(getWidth() - 60, 0, 60, 30);
        applyComp.setFont(PX14);
        applyComp.setArc(0, 0);
        add(applyComp);

        nameField = new InputField("", "...", TOOLMENU_COLOR3, c2, TOOLMENU_COLOR2) {
            @Override
            public void paint(Graphics graphics) {
                ntext = getText();
                super.paint(graphics);
            }
        };
        nameField.setBounds(10, 40, getWidth() - 20, 30);
        nameField.setFont(PX14);
        nameField.setOnAction(this::apply);
        add(nameField);
        addKeyListener(nameField);
    }

    public void apply(){
        if(ntext == null) {
            return;
        }
        if(ntext.equals("")) {
            return;
        }
        if(accepta == null) {
            return;
        }
        dispose();
        accepta.accept(ntext);
    }


    @Override
    public void setVisible(boolean value){
        super.setVisible(value);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(100, 100);
        frame.setVisible(true);
        TextDialog pw = new TextDialog(frame);
        pw.accepta = s -> {
            System.out.println(s);
            FileSelectionDialog selectionDialog = new FileSelectionDialog(frame);
            selectionDialog.allowDirectories = true;
            selectionDialog.selectFilesAndDirectories().forEach(sa -> {
                System.out.println(sa);
                System.exit(0);
            });
        };
        pw.setVisible(true);
    }
}
