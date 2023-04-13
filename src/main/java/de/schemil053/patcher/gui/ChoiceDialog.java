package de.schemil053.patcher.gui;

import de.schemil053.ui.component.TextComponent;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;


import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import static de.schemil053.patcher.Main.frame;
import static de.schemil053.patcher.gui.FileSelectionDialog.*;

public class ChoiceDialog extends JDialog {

	private TextComponent textComponent;
	private TextComponent choice1Comp;
	private TextComponent choice2Comp;
	private TextComponent cancelComp;

	public static final int CHOICE1 = 0;
	public static final int CHOICE2 = 1;
	public static final int CANCEL = 2;
	public int choice = CANCEL;

	public static ChoiceDialog choiceDialog;

	public ChoiceDialog(JFrame frame){
		super(frame, true);
		setTitle("Sicher?");
		setLayout(null);
		setUndecorated(true);
		JPanel panel = new JPanel(null);
		setContentPane(panel);
		panel.setBackground(back3);
		init();
		setSize(500, 300);
	}

	public void init(){
		textComponent = new TextComponent("Frage?", back1, back1, glow, null);
		textComponent.setFont(PX14);
		textComponent.setArc(0, 0);
		textComponent.setClickable(false);
		textComponent.attachDragger(this);
		add(textComponent);

		choice1Comp = new TextComponent("Wahl 1", TOOLMENU_COLOR5_SHADE, back1, TOOLMENU_COLOR3, ()->{
			choice = CHOICE1;
			dispose();
		});
		choice1Comp.setArc(5, 5);
		choice1Comp.setFont(UBUNTU_PX14);
		add(choice1Comp);

		choice2Comp = new TextComponent("Wahl 2", TOOLMENU_COLOR5_SHADE, back1, TOOLMENU_COLOR3, ()->{
			choice = CHOICE2;
			dispose();
		});
		choice2Comp.setArc(5, 5);
		choice2Comp.setFont(UBUNTU_PX14);
		add(choice2Comp);

		cancelComp = new TextComponent("Abbruch", TOOLMENU_COLOR3_SHADE, TOOLMENU_COLOR1_SHADE, TOOLMENU_COLOR1, this::dispose);
		cancelComp.setFont(PX14);
		cancelComp.setArc(5, 5);
		add(cancelComp);
	}

	public static int makeChoice(String question, String choice1, String choice2){
		if(choiceDialog == null) {
			choiceDialog = new ChoiceDialog(frame);
		}
		choiceDialog.choice = CANCEL;
		choiceDialog.textComponent.setText(question);
		choiceDialog.choice1Comp.setText(choice1);
		choiceDialog.choice2Comp.setText(choice2);

		choiceDialog.choice1Comp.setSize(computeWidth(choice1, UBUNTU_PX14) + 10, 25);
		choiceDialog.choice2Comp.setSize(computeWidth(choice2, UBUNTU_PX14) + 10, 25);

		choiceDialog.choice1Comp.setLocation(choiceDialog.getWidth() - 10 - choiceDialog.choice1Comp.getWidth() - 10 - choiceDialog.choice2Comp.getWidth(), choiceDialog.getHeight() - 40);
		choiceDialog.choice2Comp.setLocation(choiceDialog.getWidth() - 10 - choiceDialog.choice2Comp.getWidth(), choiceDialog.getHeight() - 40);

		choiceDialog.setVisible(true);
		return choiceDialog.choice;
	}

	@Override
	public void setSize(int w, int h){
		super.setSize(w, h);
		setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
		setLocationRelativeTo(null);
		textComponent.setBounds(0, 150, getWidth(), 30);
		cancelComp.setBounds(getWidth()/2 - 90/2, 200, 90, 25);
	}

	public static int computeWidth(String name, Font font){
		if(font == null)
			return 8;
		Graphics2D g = (Graphics2D)new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB).getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(font);
		return g.getFontMetrics().stringWidth(name);
	}
}
