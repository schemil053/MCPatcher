package de.schemil053.patcher.gui;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;

public class IconManager {
    public static BufferedImage fluentanyfileImage = getFluentIcon("file.png");
    public static BufferedImage fluentlevelupImage = getFluentIcon("up.png");
    public static BufferedImage fluentplainfolderImage = getFluentIcon("folder.png");
    public static BufferedImage fluenthomeImage = getFluentIcon("home.png");
    public static BufferedImage fluentarchiveImage = getFluentIcon("archive.png");
    public static BufferedImage fluentemptyBoxImage = getFluentIcon("empty.png");


    public static BufferedImage getFluentIcon(String name){
        try{
            InputStream in = IconManager.class.getResourceAsStream("/icons/" + name);
            BufferedImage image = ImageIO.read(in);
            in.close();
            return image;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

