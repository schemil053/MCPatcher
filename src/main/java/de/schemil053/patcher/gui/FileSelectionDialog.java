package de.schemil053.patcher.gui;
import de.schemil053.ui.component.InputField;
import de.schemil053.ui.component.TextComponent;
import de.schemil053.ui.component.ToggleComponent;
import de.schemil053.ui.util.KeyListener;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.image.BufferedImage;
import java.io.File;

import java.util.LinkedList;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JPanel;
import javax.swing.JFrame;

import static de.schemil053.ui.animation.AnimationManager.*;
import static java.awt.event.KeyEvent.*;

public class FileSelectionDialog extends JDialog {
	public static final Color c2 = Color.decode("#1e1e1e");
	public static final Color TOOLMENU_COLOR1 = Color.decode("#f0b40f");
	public static final Color TOOLMENU_COLOR1_SHADE = new Color(TOOLMENU_COLOR1.getRed(), TOOLMENU_COLOR1.getGreen(), TOOLMENU_COLOR1.getBlue(), 40);
	public static final Color TOOLMENU_COLOR2 = Color.decode("#D34D42");
	public static final Color TOOLMENU_COLOR2_SHADE = new Color(TOOLMENU_COLOR2.getRed(), TOOLMENU_COLOR2.getGreen(), TOOLMENU_COLOR2.getBlue(), 40);
	public static final Color TOOLMENU_COLOR3 = Color.decode("#22d5d5");
	public static final Color TOOLMENU_COLOR3_SHADE = new Color(TOOLMENU_COLOR3.getRed(), TOOLMENU_COLOR3.getGreen(), TOOLMENU_COLOR3.getBlue(), 40);
	public static final Color TOOLMENU_COLOR4 = Color.decode("#EB7201");
	public static final Color TOOLMENU_COLOR5 = Color.decode("#7f6021");
	public static final Color TOOLMENU_COLOR5_SHADE = new Color(TOOLMENU_COLOR5.getRed(), TOOLMENU_COLOR5.getGreen(), TOOLMENU_COLOR5.getBlue(), 40);
	public static final Color TOOLMENU_GRADIENT = new Color(51, 51, 51, 140);

	public static final Color back1 = Color.decode("#252526");
	public static final Color back2 = Color.decode("#262626");
	public static final Color back3 = Color.decode("#303030");
	public static final Color glow = Color.decode("#343434");

	public static boolean home = false;
	public static String fontName = "Ubuntu Mono";
	public static final Font PX14 = new Font(fontName, Font.BOLD, 14);
	public static final Font PX20 = new Font(fontName, Font.BOLD, 20);
	public static final Font UBUNTU_PX14 = new Font("Ubuntu", Font.BOLD, 14);
	private TextComponent titleComp;
	private JScrollPane scrollPane;
	private JPanel panel;
	private InputField selectionField;

	private final LinkedList<ToggleComponent> items = new LinkedList<>();
	private final LinkedList<File> selections = new LinkedList<>();

	public static final String ALL_EXTENSIONS = ".*";

	private int state = 0;

	public volatile boolean allowDirectories = true;

	private File currentDir = new File(System.getProperty((home ? "user.home" : "user.dir")));

	private String[] extensions;

	private int pressX;
	private int pressY;

	private int block = 0;

	public FileSelectionDialog(JFrame f){
		super(f, true);
		setUndecorated(true);
		JPanel panel = new JPanel(null);
		panel.setBackground(c2);
		setContentPane(panel);
		setSize(500, 400);
		setLocationRelativeTo(null);
		setBackground(c2);
		setLayout(null);
		init();
	}

	public void init(){
		titleComp = new TextComponent("", back2, back2, glow, null);
		titleComp.setBounds(0, 0, getWidth() - 50 - 60, 30);
		titleComp.setFont(PX14);
		titleComp.setClickable(false);
		titleComp.setArc(0, 0);
		titleComp.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(MouseEvent e){
				pressX = e.getX();
				pressY = e.getY();
			}
		});
		titleComp.addMouseMotionListener(new MouseAdapter(){
			@Override
			public void mouseDragged(MouseEvent e){
				setLocation(e.getXOnScreen() - pressX, e.getYOnScreen() - pressY);
			}
		});
		add(titleComp);

		TextComponent cancelComp = new TextComponent("Abbrechen", TOOLMENU_COLOR2_SHADE, back2, TOOLMENU_COLOR2, () -> {
			selections.clear();
			state = -1;
			dispose();
			System.exit(0);
		});
		cancelComp.setBounds(getWidth() - 50, 0, 50, 30);
		cancelComp.setFont(PX14);
		cancelComp.setArc(0, 0);
		add(cancelComp);

		TextComponent levelComp = new TextComponent(IconManager.fluentlevelupImage, 20, 20, "Nach oben bewegen", TOOLMENU_COLOR1_SHADE, back2, TOOLMENU_COLOR1, () -> {
			if (currentDir == null)
				return;
			String path = currentDir.getAbsolutePath();
			if (File.pathSeparator.equals(":") && !path.equals("/")) {
				if (count('/', path) != 1)
					currentDir = new File(path.substring(0, path.lastIndexOf('/')));
				else
					currentDir = new File("/");
			} else if (File.pathSeparator.equals(";")) {
				if (path.contains("\\")) {
					if (path.endsWith(":\\")) {
						showWindowsRoots();
						return;
					} else {
						path = path.substring(0, path.lastIndexOf('\\'));
						if (path.endsWith(":"))
							path += "\\";
						currentDir = new File(path);
					}
				}
			}
			if (state == 0)
				selectFiles();
			else if (state == 1)
				selectDirectories();
			else if (state == 2)
				selectFilesAndDirectories();
		});
		levelComp.setBounds(getWidth() - 50 - 60, 0, 30, 30);
		levelComp.setFont(PX14);
		levelComp.setArc(0, 0);
		add(levelComp);

		TextComponent homeComp = new TextComponent(IconManager.fluenthomeImage, 20, 20, "Home", TOOLMENU_COLOR1_SHADE, back2, TOOLMENU_COLOR1, () -> {
			currentDir = new File(System.getProperty((home ? "user.home" : "user.dir")));
			if (state == 0)
				selectFiles();
			else if (state == 1)
				selectDirectories();
			else if (state == 2)
				selectFilesAndDirectories();
		});
		homeComp.setBounds(getWidth() - 50 - 30, 0, 30, 30);
		homeComp.setFont(PX14);
		homeComp.setArc(0, 0);
		add(homeComp);

		selectionField = new InputField("", "Pfad manuell eingeben", TOOLMENU_COLOR2, back2, TOOLMENU_COLOR3);
		selectionField.setBounds(0, getHeight() - 30, getWidth() - 50, 30);
		selectionField.setOnAction(()->{
			if(selectionField.getText().equals("") && !selections.isEmpty()){
				dispose();
				return;
			}
			File file = new File(selectionField.getText());
			if(!file.exists()){
				file = new File(currentDir.getAbsolutePath(), selectionField.getText());
			}
			if(file.exists()) {
				if(state == 0){
					if(file.isDirectory()){
						currentDir = file;
						selectFiles();
					}
					else{
						selections.add(file);
						dispose();
					}
				}
				else if(state == 1){
					if(file.isDirectory()){
						selections.add(file);
						dispose();
					}
					else
						selectionField.notify("Ordner erwartet!");
				}
				else if(state == 2){
					selections.add(file);
					dispose();
				}
			}
			else{
				Toolkit.getDefaultToolkit().beep();
			}
		});
		selectionField.setFont(PX14);
		add(selectionField);
		addKeyListener(selectionField);

		initKeyStrokeListener();

		TextComponent selectComp = new TextComponent("Fertig", TOOLMENU_COLOR1_SHADE, c2, TOOLMENU_COLOR3, this::dispose);
		selectComp.setBounds(getWidth() - 50, getHeight() - 30, 50, 30);
		selectComp.setFont(PX14);
		selectComp.setArc(0, 0);
		add(selectComp);

		scrollPane = new JScrollPane(panel = new JPanel(null){
			GradientPaint paint = new GradientPaint(0, 0, TOOLMENU_COLOR2, 500, 310, TOOLMENU_COLOR3);
			String hint = "Nichts zum anzeigen";
			@Override
			public void paint(Graphics graphics){
				if(items.isEmpty()){
					Graphics2D g = (Graphics2D)graphics;
					g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
					g.setColor(c2);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setPaint(paint);
					g.setFont(PX20);
					g.drawString(hint, getWidth()/2 - g.getFontMetrics().stringWidth(hint)/2, getHeight()/2 - g.getFontMetrics().getHeight()/2 + g.getFontMetrics().getAscent() - g.getFontMetrics().getDescent() + 1);
				}
				else
					super.paint(graphics);
			}
		});
		scrollPane.setBounds(0, 30, getWidth(), getHeight() - 60);
		scrollPane.setBackground(c2);
		scrollPane.setBorder(null);
		add(scrollPane);
		panel.setBackground(c2);
		panel.setPreferredSize(new Dimension(490, 290));
		panel.setBorder(null);

		putAnimationLayer(levelComp, getImageSizeAnimationLayer(25, 5, true), ACTION_MOUSE_ENTERED);
		putAnimationLayer(homeComp, getImageSizeAnimationLayer(25, 5, true), ACTION_MOUSE_ENTERED);
	}

	private void initKeyStrokeListener(){
		KeyListener listener = new KeyListener(this);
		listener.putKeyStroke((e)->{
			if(state == 0){

				items.forEach(item->{
					File file = (File)item.getExtras().get(0);
					if(!file.isDirectory()){
						item.toggleHandler.toggle(!item.isOn());
					}
				});
			}
			else if(state == 1){

				items.forEach(item->{
					File file = (File)item.getExtras().get(0);
					if(file.isDirectory()){
						item.toggleHandler.toggle(!item.isOn());
					}
				});
			}
			else {

				items.forEach(item->{
					File file = (File)item.getExtras().get(0);
					item.toggleHandler.toggle(!item.isOn());
				});
			}
		}, VK_CONTROL, VK_A);
		addKeyListener(listener);
	}

	@Override
	public void setTitle(String title){
		super.setTitle(title);
		titleComp.setText(title);
	}

	public void setFileExtensions(String... extensions){
		this.extensions = extensions;
	}

	public boolean isDirectoriesAllowed() {
		return allowDirectories;
	}


	public boolean isExtentionAllowed(File file){
		if(extensions == null || extensions.length == 0 || extensions[0].equals(ALL_EXTENSIONS))
			return true;
		if(file.isDirectory() && isDirectoriesAllowed()) {
			return true;
		}
		for(String ext : extensions){
			if(file.getName().endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	public LinkedList<File> showWindowsRoots(){
		selections.clear();
		items.forEach(panel::remove);
		items.clear();
		block = 0;

		currentDir = new File("\\");

		File[] F = File.listRoots();
		if(F == null || F.length == 0)
			return selections;

		LinkedList<File> files = new LinkedList<>();
		for(File f : F){
			files.add(f);
		}

		F = null;
		sort(files);

		Color c1 = null;
		Color c2 = Color.decode("#1e1e1e");
		Color c3 = null;
		for(File file : files){
			if(isExtentionAllowed(file)){
				if(file.isDirectory()){
					c1 = TOOLMENU_COLOR1_SHADE;
					c3 = TOOLMENU_COLOR1;
				}
				else{
					c1 = TOOLMENU_COLOR2_SHADE;
					c3 = TOOLMENU_COLOR2;
				}
				String meta = (file.listFiles() != null && file.listFiles().length != 0) ? "" : " - Empty";
				if(!file.isDirectory())
					meta = "";
				ToggleComponent comp = new ToggleComponent(getPreferredImageForFile(file), 25, 25, file.toString() + meta, c1, c2, c3, false);
				if(!file.isDirectory()){
					comp.setOnToggle((value)->{
						comp.state = value;
						comp.setColors(comp.color1, comp.color3, comp.color2);
						if(value)
							selections.add(file);
						else
							selections.remove(file);
					});
				}
				else
					comp.toggleEnabled = false;
				comp.setBounds(0, block, 490, 25);
				if(file.isDirectory()){
					comp.addMouseListener(new MouseAdapter(){
						@Override
						public void mousePressed(MouseEvent e){
							if(file.isDirectory() && file.listFiles() != null && file.listFiles().length != 0){
								currentDir = file;
								selectFiles();
							}
						}
					});
				}
				comp.setFont(PX14);
				comp.setArc(0, 0);
				comp.getExtras().add(file);
				panel.add(comp);
				items.add(comp);
				block += 25;
			}
		}
		files.clear();
		panel.setPreferredSize(new Dimension(490, block < 290 ? 290 : block));
		triggerRepaint();
		setVisible(true);
		return selections;
	}

	public LinkedList<File> selectFiles(){
		state = 0;
		selections.clear();
		items.forEach(panel::remove);
		items.clear();
		block = 0;

		if(currentDir == null)
			currentDir = new File(System.getProperty((home ? "user.home" : "user.dir")));

		File[] F = currentDir.listFiles();
		if(F == null || F.length == 0)
			return selections;

		LinkedList<File> files = new LinkedList<>();
		for(File f : F)
			files.add(f);

		F = null;
		sort(files);

		Color c1 = null;
		Color c2 = this.c2;
		Color c3 = null;
		for(File file : files){
			if(isExtentionAllowed(file)){
				if(file.isDirectory()){
					c1 = TOOLMENU_COLOR1_SHADE;
					c3 = TOOLMENU_COLOR1;
				}
				else{
					c1 = TOOLMENU_COLOR2_SHADE;
					c3 = TOOLMENU_COLOR2;
				}
				String meta = (file.listFiles() != null && file.listFiles().length != 0) ? "" : " - Empty";
				if(!file.isDirectory())
					meta = "";
				ToggleComponent comp = new ToggleComponent(getPreferredImageForFile(file), 25, 25, file.getName() + meta, c1, c2, c3, false);
				if(!file.isDirectory()){
					comp.setOnToggle((value)->{
						comp.state = value;
						comp.setColors(comp.color1, comp.color3, comp.color2);
						if(value)
							selections.add(file);
						else
							selections.remove(file);
					});
				}
				else
					comp.toggleEnabled = false;
				comp.setBounds(0, block, 490, 25);
				comp.addMouseListener(new MouseAdapter(){
					@Override
					public void mousePressed(MouseEvent e){
						if(!file.isDirectory() && e.getClickCount() == 2){
							selections.clear();
							selections.add(file);
							dispose();
						}
						if(file.isDirectory() && file.listFiles() != null && file.listFiles().length != 0){
							currentDir = file;
							selectFiles();
						}
					}
				});
				comp.setFont(PX14);
				comp.setArc(0, 0);
				comp.getExtras().add(file);
				panel.add(comp);
				items.add(comp);
				block += 25;
			}
		}
		files.clear();
		panel.setPreferredSize(new Dimension(490, block < 290 ? 290 : block));
		triggerRepaint();
		setVisible(true);
		return selections;
	}

	public LinkedList<File> selectDirectories(){
		state = 1;
		setFileExtensions(ALL_EXTENSIONS);
		selections.clear();
		items.forEach(panel::remove);
		items.clear();
		block = 0;

		if(currentDir == null)
			currentDir = new File(System.getProperty((home ? "user.home" : "user.dir")));

		File[] F = currentDir.listFiles();
		if(F == null || F.length == 0)
			return selections;

		LinkedList<File> files = new LinkedList<>();
		for(File f : F)
			files.add(f);

		F = null;
		sort(files);

		Color c1 = null;
		Color c2 = this.c2;
		Color c3 = null;
		for(File file : files){
			if(isExtentionAllowed(file)){
				if(file.isDirectory()){
					c1 = TOOLMENU_COLOR1_SHADE;
					c3 = TOOLMENU_COLOR1;
				}
				else{
					c1 = TOOLMENU_COLOR2_SHADE;
					c3 = TOOLMENU_COLOR2;
				}
				String meta = (file.listFiles() != null && file.listFiles().length != 0) ? "" : " - Empty";
				if(!file.isDirectory())
					meta = "";
				ToggleComponent comp = new ToggleComponent(getPreferredImageForFile(file), 25, 25, file.getName() + meta, c1, c2, c3, false);
				if(file.isDirectory()){
					comp.setOnToggle((value)->{
						comp.state = value;
						comp.setColors(comp.color1, comp.color3, comp.color2);
						if(value)
							selections.add(file);
						else
							selections.remove(file);
					});
				}
				else
					comp.toggleEnabled = false;
				comp.setBounds(0, block, 490, 25);
				if(file.isDirectory()){
					comp.addMouseListener(new MouseAdapter(){
						@Override
						public void mousePressed(MouseEvent e){
							if(e.getButton() == 1 && e.getClickCount() == 2 && file.isDirectory() && file.listFiles() != null && file.listFiles().length != 0){
								currentDir = file;
								selectDirectories();
							}
						}
					});
				}
				comp.setFont(PX14);
				comp.setArc(0, 0);
				comp.getExtras().add(file);
				panel.add(comp);
				items.add(comp);
				block += 25;
			}
		}
		files.clear();
		panel.setPreferredSize(new Dimension(490, block < 290 ? 290 : block));
		triggerRepaint();
		setVisible(true);
		return selections;
	}

	public LinkedList<File> selectFilesAndDirectories(){
		state = 2;
		selections.clear();
		items.forEach(panel::remove);
		items.clear();
		block = 0;

		if(currentDir == null)
			currentDir = new File(System.getProperty((home ? "user.home" : "user.dir")));

		File[] F = currentDir.listFiles();
		if(F == null || F.length == 0)
			return selections;

		LinkedList<File> files = new LinkedList<>();
		for(File f : F)
			files.add(f);

		F = null;
		sort(files);

		Color c1 = null;
		Color c2 = this.c2;
		Color c3 = null;
		for(File file : files){
			if(isExtentionAllowed(file)){
				if(file.isDirectory()){
					c1 = TOOLMENU_COLOR1_SHADE;
					c3 = TOOLMENU_COLOR1;
				}
				else{
					c1 = TOOLMENU_COLOR2_SHADE;
					c3 = TOOLMENU_COLOR2;
				}
				String meta = (file.listFiles() != null && file.listFiles().length != 0) ? "" : " - Empty";
				if(!file.isDirectory())
					meta = "";
				ToggleComponent comp = new ToggleComponent(getPreferredImageForFile(file), 25, 25, file.getName() + meta, c1, c2, c3, false);
				comp.setOnToggle((value)->{
					comp.state = value;
					comp.setColors(comp.color1, comp.color3, comp.color2);
					if(value)
						selections.add(file);
					else
						selections.remove(file);
				});
				comp.setBounds(0, block, 490, 25);
				if(file.isDirectory()){
					comp.addMouseListener(new MouseAdapter(){
						@Override
						public void mousePressed(MouseEvent e){
							if(e.getButton() == 1 && e.getClickCount() == 2 && file.isDirectory() && file.listFiles() != null && file.listFiles().length != 0){
								currentDir = file;
								selectFilesAndDirectories();
							}
						}
					});
				}
				comp.setFont(PX14);
				comp.setArc(0, 0);
				comp.getExtras().add(file);
				panel.add(comp);
				items.add(comp);
				block += 25;
			}
		}
		files.clear();
		panel.setPreferredSize(new Dimension(490, block < 290 ? 290 : block));
		triggerRepaint();
		setVisible(true);
		return selections;
	}

	@Override
	public void paint(Graphics g){
		super.paint(g);
		scrollPane.repaint();
		panel.repaint();
		items.forEach(item->item.repaint());
	}

	public static synchronized void sort(LinkedList<File> files){
		try{
			final LinkedList<File> tempFiles = new LinkedList<>();
			final LinkedList<File> tempDirs = new LinkedList<>();
			files.forEach(f->{
				if(f.isDirectory()) tempDirs.add(f);
				else tempFiles.add(f);
				});
			files.clear();
			File[] F = new File[tempFiles.size()];
			int k = -1;
			for(File fx : tempFiles)
				F[++k] = fx;
			File[] D = new File[tempDirs.size()];
			k = -1;
			for(File fx : tempDirs)
				D[++k] = fx;
			sort(F);
			sort(D);
			LinkedList<File> dots = new LinkedList<>();
			for(File f : D){
				if(f.getName().startsWith(".")) dots.add(f);
				else files.add(f);
				}
			for(File f : dots){
				files.add(f);
			}
			dots.clear();
			for(File f : F){
				if(f.getName().startsWith(".")) dots.add(f);
				else files.add(f);
				}
			for(File f : dots){
				files.add(f);
			}
			tempFiles.clear();
			tempDirs.clear();
			dots.clear();
		}
		catch(Exception exception){

		}
	}

	private static void sort(File[] files){
		for(int i = 0; i < files.length; i++){
			for(int j = 0; j < files.length - 1 - i; j++){
				File x = files[j];
				File y = files[j + 1];
				if(x.getName().compareTo(y.getName()) > 0){
					files[j] = y;
					files[j + 1] = x;
				}
			}
		}
	}

	private static int count(char ch, String line){
		int c = 0;
		for(char cx : line.toCharArray()){
			if(cx == ch)
				c++;
		}
		return c;
	}

	public synchronized void triggerRepaint(){
		new Thread(()->{
			try{
				scrollPane.repaint();
				scrollPane.getVerticalScrollBar().setValue(0);
				panel.repaint();
				items.forEach(item->item.repaint());
			}
			catch(Exception e){

			}
		}).start();
	}

	@Override
	public void setVisible(boolean value){
		super.setVisible(value);
	}

	@Override
	public void setSize(int width, int height){
		super.setSize(width, height);
		setShape(new RoundRectangle2D.Double(0, 0, width, height, 20, 20));
	}

	@Override
	public void dispose(){
		if(state != -1 && selections.isEmpty()){
			String text = selectionField.getText();
			File file = new File(text);
			if(file.exists()){
				switch(state){
					case 0:
						if(!file.isDirectory())
							selections.add(file);
						break;
					default:
						selections.add(file);
				}
			}
			else if(currentDir != null && currentDir.exists()){
				file = new File(currentDir.getAbsolutePath() + File.separator + text);
				if(file.exists()){
					switch(state){
						case 0:
							if(!file.isDirectory())
								selections.add(file);
							break;
						default:
							selections.add(file);
					}
				}
				else if(state != 0)
					selections.add(currentDir);
			}
		}
		super.dispose();
	}


	public static BufferedImage getPreferredImageForFile(File file){
		if(file.isDirectory()){
			File[] files = file.listFiles();
			if (files == null || files.length == 0) {
				return IconManager.fluentemptyBoxImage;
			}
			return IconManager.fluentplainfolderImage;
		}
		if(file.getName().contains(".")){
			String ext = file.getName().substring(file.getName().lastIndexOf('.'));
			if(file.getName().endsWith(".zip") || file.getName().endsWith(".jar") || ext.equals(".json")) {
				return IconManager.fluentarchiveImage;
			}
		}
		return IconManager.fluentanyfileImage;
	}
}

