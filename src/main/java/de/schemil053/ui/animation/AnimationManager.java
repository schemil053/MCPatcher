package de.schemil053.ui.animation;

import de.schemil053.ui.component.TextComponent;

import java.awt.image.BufferedImage;

import java.util.LinkedList;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
public class AnimationManager {
	public static volatile boolean animationsOn = true;
	
	public static final String ANIMATION_STATE = "Animation Running";
	
	public static final int ACTION_MOUSE_ENTERED = 0;
	public static final int ACTION_MOUSE_EXITED = 1;
	public static final int ACTION_MOUSE_PRESSED = 2;
	public static final int ACTION_MOUSE_CLICKED = 3;
	public static final int ACTION_MOUSE_DOUBLE_CLICKED = 4;

	public static final LinkedList<TextComponent> comps = new LinkedList<>();

	public static boolean isAnimationsOn() {
		return animationsOn;
	}

	public static void prepareTextComp(TextComponent comp){
		comp.map.put(ANIMATION_STATE, false);
	}
	
	public static void putAnimationLayer(TextComponent comp, AnimationLayer layer, int action){
		if(!isActionApplicable(action) || !isAnimationsOn())
			return;
		prepareTextComp(comp);
		if(action == ACTION_MOUSE_ENTERED){
			comp.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseEntered(MouseEvent e){
					if(!isAnimationsOn())
						return;
					layer.animate(comp);
				}
			});
		}
		else if(action == ACTION_MOUSE_EXITED){
			comp.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseExited(MouseEvent e){
					if(!isAnimationsOn())
						return;
					layer.animate(comp);
				}
			});
		}
		else if(action == ACTION_MOUSE_PRESSED){
			comp.addMouseListener(new MouseAdapter(){
				@Override
				public void mousePressed(MouseEvent e){
					if(!isAnimationsOn())
						return;
					layer.animate(comp);
				}
			});
		}
		else if(action == ACTION_MOUSE_CLICKED){
			comp.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseExited(MouseEvent e){
					if(!isAnimationsOn())
						return;
					if(e.getClickCount() == 1)
						layer.animate(comp);
				}
			});
		}
		else if(action == ACTION_MOUSE_DOUBLE_CLICKED){
			comp.addMouseListener(new MouseAdapter(){
				@Override
				public void mouseExited(MouseEvent e){
					if(!isAnimationsOn())
						return;
					if(e.getClickCount() == 2)
						layer.animate(comp);
				}
			});
		}
	}
	
	public static AnimationLayer getImageSizeAnimationLayer(int rate, int distance, boolean useClear){
		return new ImageTransition(){
			@Override
			public void animate(TextComponent component){
				if(!isAnimationsOn())
					return;
				boolean animationRunning = (boolean)component.getValue(ANIMATION_STATE);
				if(animationRunning || !component.canDrawImage())
					return;
				
				new Thread(()->{
					this.prepareImages(component, distance, useClear);
					
					Graphics2D g = (Graphics2D)component.getGraphics();
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					
					component.map.put(ANIMATION_STATE, true);
					
					for(BufferedImage image : images){
						
						if(!component.isMouseEntered()) {
							break;
						}
						
						g.drawImage(image, 0, 0, null);
						
						try{
							Thread.sleep(rate);
						}
						catch(Exception e){
							e.printStackTrace();
						}
					}
					
					g.dispose();
					
					component.map.put(ANIMATION_STATE, false);
					
					if(!component.enter) {
						component.repaint();
					}
				}).start();
			}
		};
	}
	
	public static boolean isActionApplicable(int action){
		return action >= ACTION_MOUSE_ENTERED && action <= ACTION_MOUSE_DOUBLE_CLICKED;
	}
}
