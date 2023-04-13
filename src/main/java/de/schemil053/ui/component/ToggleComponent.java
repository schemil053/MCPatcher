package de.schemil053.ui.component;

import de.schemil053.ui.util.ToggleHandler;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.image.BufferedImage;
public class ToggleComponent extends TextComponent {

	public ToggleHandler toggleHandler = (value)->{};

	public volatile boolean state = false;
	public volatile boolean toggleEnabled = true;

	public BufferedImage image;
	public int w;
	public int h;

	public ToggleComponent(String text, Color c1, Color c2, Color c3, boolean state){
		super(text, c1, c2, c3, null);
		setArc(6, 6);
		this.state = state;
		addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e){
				if(toggleEnabled){
					ToggleComponent.this.state = !ToggleComponent.this.state;
					repaint();
				}
				if(ToggleComponent.this.toggleHandler != null && e.getButton() == 1)
					ToggleComponent.this.toggleHandler.toggle(ToggleComponent.this.state);
			}
		});
	}


	public ToggleComponent(BufferedImage image, int w, int h, String text, Color c1, Color c2, Color c3, boolean state){
		this(text, c1, c2, c3, state);
		setImage(image);
		setImageWidth(w);
		setImageHeight(h);
	}

	public BufferedImage getImage() {
		return image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		repaint();
	}

	public void setImageWidth(int w){
		this.w = w;
	}

	public void setImageHeight(int h){
		this.h = h;
	}
	public void setOnToggle(ToggleHandler toggleHandler){
		this.toggleHandler = toggleHandler;
	}

	public boolean isOn(){
		return state;
	}

	@Override
	public void paint(Graphics graphics){
		alignX = getHeight() + 4;
		super.paint(graphics);
	}

	@Override
	public void draw(Graphics2D g){
		super.draw(g);
		if(image == null){
			g.setColor(color3);
			g.fillRoundRect(2, 2, getHeight() - 4, getHeight() - 4, arcX, arcY);
		}
		if(image != null){
			g.drawImage(image.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH), getHeight()/2 - w/2, getHeight()/2 - h/2, w, h, this);
		}
		if(state){
			g.fillRect(alignX, getHeight() - 2, g.getFontMetrics().stringWidth(getText()), 2);
			if(image == null){
				g.setColor(color2);
				g.fillRoundRect(8, 8, getHeight() - 16, getHeight() - 16, arcX, arcY);
			}
		}
	}
}

