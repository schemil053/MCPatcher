

package de.schemil053.ui.util;
import java.awt.Component;

import java.util.LinkedList;

import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class KeyListener implements java.awt.event.KeyListener {

	public LinkedList<SimpleKey> keys = new LinkedList<>();
	public LinkedList<KeyStroke> keyStrokes = new LinkedList<>();
	public Component c;

	public KeyListener(Component c){
		this.c = c;
		c.addFocusListener(new FocusAdapter(){
			@Override
			public void focusLost(FocusEvent e){
				resetAll();
			}
		});
	}

	public void resetAll(){
		keyStrokes.forEach(keyStrokeData->keyStrokeData.autoReset());
	}

	public KeyStroke putKeyStroke(KeyStrokeDataListener listener, int... key){
		KeyStroke stroke = new KeyStroke(listener, key);
		keyStrokes.add(stroke);
		return stroke;
	}

	public boolean addKey(SimpleKey key){
		for(SimpleKey kx : keys){
			if(kx.key == key.key)
				return false;
		}
		this.keys.add(key);
		return true;
	}

	public SimpleKey getKey(int key){
		for(SimpleKey kx : keys){
			if(kx.key == key)
				return kx;
		}
		return new SimpleKey(key);
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		synchronized(c){
			keys.forEach(key->key.checkPressed(e.getKeyCode(), true));
			keyStrokes.forEach(keyStrokeData->keyStrokeData.stroke(e));
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		synchronized(c){
			keys.forEach(key->key.checkPressed(e.getKeyCode(), false));
		}
	}

	public synchronized final KeyListener clone(){
		KeyListener listener = new KeyListener(c);
		listener.keys.addAll(keys);
		listener.keyStrokes.addAll(keyStrokes);
		return listener;
	}

	public interface KeyStrokeDataListener {
		void listen(KeyEvent e);
	}

	public class KeyStroke {
		public LinkedList<SimpleKey> keys = new LinkedList<>();
		public LinkedList<SimpleKey> stopKeys = new LinkedList<>();
		public KeyStrokeDataListener listener;
		public volatile boolean useAutoReset = false;

		public KeyStroke(KeyStrokeDataListener listener, int... keys){
			this.listener = listener;
			for(int key : keys){
				this.keys.add(getKey(key));
			}
		}

		public synchronized void stroke(KeyEvent e){
			if(isStrokable()){
				listener.listen(e);
				if(useAutoReset)
					autoReset();
			}
		}

		public void autoReset(){
			for(SimpleKey kx : this.keys){
				getKey(kx.key).setPressed(false);
			}
			for(SimpleKey kx : this.stopKeys){
				getKey(kx.key).setPressed(false);
			}
		}

		public boolean containsStrokeKey(SimpleKey key){
			for(SimpleKey kx : this.keys){
				if(kx.key == key.key)
					return true;
			}
			return false;
		}

		public boolean containsStopKey(SimpleKey key){
			for(SimpleKey kx : this.stopKeys){
				if(kx.key == key.key)
					return true;
			}
			return false;
		}

		public boolean isStrokable(){
			int strokeKeysLength = 0;
			int stopKeysLength = 0;
			for(SimpleKey kx : KeyListener.this.keys){
				if(kx.isPressed()){
					if(containsStrokeKey(kx))
						strokeKeysLength++;
					else if(containsStopKey(kx))
						stopKeysLength++;
				}
			}
			return (strokeKeysLength == this.keys.size()) && stopKeysLength == 0;
		}
	}

	public class SimpleKey {
		public int key;
		public volatile boolean pressed = false;

		public SimpleKey(int key){
			this.key = key;
			addKey(this);
		}

		public void checkPressed(int key, boolean pressed){
			if(this.key == key){
				setPressed(pressed);
			}
		}

		public void setPressed(boolean pressed){
			this.pressed = pressed;
		}

		public boolean isPressed(){
			return pressed;
		}

		@Override
		public String toString(){
			return KeyEvent.getKeyText(key);
		}
	}
}
