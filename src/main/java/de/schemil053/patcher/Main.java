package de.schemil053.patcher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.schemil053.patcher.gui.ChoiceDialog;
import de.schemil053.patcher.gui.FileSelectionDialog;
import de.schemil053.patcher.gui.TextDialog;
import de.schemil053.patcher.utils.*;
import io.sigpipe.jbsdiff.Diff;
import io.sigpipe.jbsdiff.Patch;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class Main {
    static volatile AtomicReference<String> n = new AtomicReference<>(null);
    public static JFrame frame = new JFrame();
    public static File f;
    public static void main(String[] args) throws Throwable {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        try {
            BufferedImage bg = ImageIO.read(Objects.requireNonNull(Main.class.getResource("/img/icon.png")));
            frame.setIconImage(bg);
        } catch (Exception e) {
        }
        frame.setTitle("Do not close");
        frame.setVisible(true);
        f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation()
                .toURI());
        if(Main.class.getResourceAsStream("/settings.econf") == null) {
            AtomicInteger c = new AtomicInteger(ChoiceDialog.makeChoice("Wähle deinen Minecraft-Client bitte aus.", "Weiter", "Abbruch"));
            if(c.get() != ChoiceDialog.CHOICE1) {
                System.exit(0);
                return;
            }
            FileSelectionDialog dialog = new FileSelectionDialog(frame);
            List<File> client = dialog.selectFiles();
            if(client.size() != 1) {
                System.exit(0);
                return;
            }
            File clientjar = client.get(0);

            c.set(ChoiceDialog.makeChoice("Wähle die Minecraft-Config aus", "Weiter", "Abbruch"));
            if(c.get() != ChoiceDialog.CHOICE1) {
                System.exit(0);
                return;
            }
            List<File> config = dialog.selectFiles();
            if(config.size() != 1) {
                System.exit(0);
                return;
            }

            c.set(ChoiceDialog.makeChoice("Soll der Client geupdatet werden?", "Ja", "Nein"));
            if(c.get() == ChoiceDialog.CANCEL) {
                System.exit(0);
                return;
            }
            boolean update = (c.get() == ChoiceDialog.CHOICE1);

            c.set(ChoiceDialog.makeChoice("Soll der Installer ein Log-Fenster öffnen?", "Ja", "Nein"));
            if(c.get() == ChoiceDialog.CANCEL) {
                System.exit(0);
                return;
            }
            boolean debug = (c.get() == ChoiceDialog.CHOICE1);


            c.set(ChoiceDialog.makeChoice("Soll der Installer den Client zum Minecraft-Launcher hinzufügen?", "Ja", "Nein"));
            if(c.get() == ChoiceDialog.CANCEL) {
                System.exit(0);
                return;
            }
            boolean addmc = (c.get() == ChoiceDialog.CHOICE1);

            File configfile = config.get(0);
            c.set(ChoiceDialog.makeChoice("Wähle den Namen im versions-Ordner", "Weiter", "Abbruch"));
            if(c.get() != ChoiceDialog.CHOICE1) {
                System.exit(0);
                return;
            }




            TextDialog dialog2 = new TextDialog(frame);


            dialog2.accepta = sbb -> {

                n.set(sbb);

                c.set(ChoiceDialog.makeChoice("Wähle den Titel vom Patcher aus", "Weiter", "Abbruch"));
                if (c.get() != ChoiceDialog.CHOICE1) {
                    System.exit(0);
                    return;
                }

                AtomicReference<String> title = new AtomicReference<>(null);

                TextDialog dialog3 = new TextDialog(frame);
                dialog3.accepta = title::set;

                dialog3.setVisible(true);

                while (title.get() == null) {
                    if(title.get() != null) {
                        break;
                    }
                }


                c.set(ChoiceDialog.makeChoice("Wähle die Minecraft-Version aus", "Weiter", "Abbruch"));
                if (c.get() != ChoiceDialog.CHOICE1) {
                    System.exit(0);
                    return;
                }

                TextDialog dialog1 = new TextDialog(frame);


                dialog1.accepta = s -> {
                    File mc = new File(Util.getWorkingDirectory(), "versions/" + s);
                    if (!mc.isDirectory()) {
                        System.exit(0);
                        return;
                    }
                    int ca = ChoiceDialog.makeChoice("Das builden kann eine Weile dauern!", "Weiter", "Abbruch");
                    if (ca != ChoiceDialog.CHOICE1) {
                        System.exit(0);
                        return;
                    }
                    GUILogger logger = new GUILogger();
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {

                    }
                  new Thread(() -> {
                      try {
                          long start = System.currentTimeMillis();
                          logger.log("Reading Minecraft...  ");
                          byte[] orig = new FileInputStream(new File(mc, s + ".jar")).readAllBytes();
                          logger.logLine("[OK]");

                          logger.log("Reading Client...  ");
                          byte[] clientd = new FileInputStream(clientjar).readAllBytes();
                          logger.logLine("[OK]");


                          logger.log("Creating Patch...  ");

                          File patch = new File("patch.temp");
                          patch.delete();
                          patch.deleteOnExit();
                          patch.createNewFile();


                          Diff.diff(orig, clientd, new FileOutputStream(patch));
                          logger.logLine("[OK]");

                          logger.log("Creating Installerjar...  ");

                          File installer = new File("installer.jar");

                          FileCopy.copyFile(f, installer);

                          sConfig co = sConfig.getSConfig("settings.econf");
                          co.clearAll();
                          co.set("version", s);
                          co.set("name", n.get());
                          co.set("update", update);
                          co.set("debug", debug);
                          co.set("title", title.get());
                          co.set("install", addmc);
                          co.save();

                          ZipFile war = new ZipFile(f);
                          ZipOutputStream append = new ZipOutputStream(new FileOutputStream(installer));
                          Enumeration<? extends ZipEntry> entries = war.entries();
                          while (entries.hasMoreElements()) {
                              ZipEntry e = entries.nextElement();
                              append.putNextEntry(e);
                              if (!e.isDirectory()) {
                                  copy(war.getInputStream(e), append);
                              }
                              append.closeEntry();
                          }

                          ZipEntry entry = new ZipEntry("settings.econf");
                          append.putNextEntry(entry);
                          append.write(new FileInputStream(co.getFile()).readAllBytes());

                          entry = new ZipEntry("mcdata.json");
                          append.putNextEntry(entry);
                          append.write(new FileInputStream(configfile).readAllBytes());

                          entry = new ZipEntry("patch.temp");
                          append.putNextEntry(entry);
                          append.write(new FileInputStream(patch).readAllBytes());


                          append.close();


                          logger.logLine("[OK]");
                          logger.logLine("Jar in " + installer.getAbsolutePath());
                          logger.logLine("Done ("+(System.currentTimeMillis()-start)+"ms)");
                          Thread.sleep(10000);
                          System.exit(0);
                      } catch (Exception exception) {
                          logger.logLine("");
                          logger.logLine("ERROR");
                          logger.logLine(exception.toString());
                      }
                  }).start();
                };
                dialog1.setVisible(true);
            };
            dialog2.setVisible(true);
        } else {
            sConfig config = new sConfig(new File("a"));
            config.readFromInputStream(Main.class.getResourceAsStream("/settings.econf"));
            String v = config.getString("version");
            String name = config.getString("name");
            File mc = new File(Util.getWorkingDirectory(), "versions/"+config.getString("version"));
            boolean debug = false;
            if(config.isBoolean("debug")) {
                debug = config.getBoolean("debug");
            }
            boolean install = false;
            if(config.isBoolean("install")) {
                install = config.getBoolean("install");
            }



            GUILogger guiLogger = null;

            if(debug) {
                guiLogger = new GUILogger();
            }

            GUILogger finalGuiLogger = guiLogger;
            boolean finalInstall = install;
            new Thread(() -> {
                try {
                    if(!mc.isDirectory()) {
                        if(finalGuiLogger != null) {
                            finalGuiLogger.logLine(config.getString("version")+" not found.");
                        }
                        JOptionPane.showMessageDialog(frame, "Spiele bitte die Version "+config.getString("version")+" mindestens ein Mal!");
                        System.exit(0);
                        return;
                    }

                    try {

                        JsonElement element = JsonParser.parseReader(new FileReader(
                                new File(Util.getWorkingDirectory(), "launcher_profiles.json")
                        ));

                        JsonObject profiles = element.getAsJsonObject().get("profiles").getAsJsonObject();
                        boolean installed = false;
                        if(profiles.get(name) != null) {
                            installed = !profiles.get(name).isJsonNull();
                        }

                        if((!installed) && finalInstall) {

                            if (finalGuiLogger != null) {
                                finalGuiLogger.log("Loading image...  ");
                            }


                            String img = "Furnace";

                            if (Main.class.getResourceAsStream("/img/icon.png") != null) {
                                img = "data:image/png;base64," + Base64.getEncoder().encodeToString(
                                        Main.class.getResourceAsStream("/img/icon.png").readAllBytes());
                            }

                            if (finalGuiLogger != null) {
                                finalGuiLogger.logLine("[OK]");
                            }

                            MojangProfile.install(new File(Util.getWorkingDirectory(), "launcher_profiles.json"), name, img);


                        }
                    } catch (Exception exception) {

                    }

                    boolean update = false;


                    if(config.isBoolean("update")) {
                        update = config.getBoolean("update");
                    }

                    if(finalGuiLogger != null) {
                        finalGuiLogger.logLine("Update-Mode: "+update);
                    }


                    File cl = new File(Util.getWorkingDirectory(), "versions/"+config.getString("name"));
                    if(cl.exists() && !update) {
                        if(finalGuiLogger != null) {
                            finalGuiLogger.logLine(name+" already satisfied.");
                        }
                        JOptionPane.showMessageDialog(frame, name+" bereits installiert!");
                        System.exit(0);
                        return;
                    }

                    if(finalGuiLogger != null) {
                        finalGuiLogger.log("Creating folder...  ");
                    }
                    cl.mkdirs();
                    if(finalGuiLogger != null) {
                        finalGuiLogger.logLine("[OK]");
                    }
                    if(finalGuiLogger != null) {
                        finalGuiLogger.log("Copy json-file...  ");
                    }
                    new FileCopy().copyFromJar("mcdata.json", new File(cl, name+".json").toPath());
                    if(finalGuiLogger != null) {
                        finalGuiLogger.logLine("[OK]");
                    }
                    if(finalGuiLogger != null) {
                        finalGuiLogger.log("Patching...  ");
                    }
                    Patch.patch(new FileInputStream(new File(mc, v+".jar")).readAllBytes(),
                            Main.class.getResourceAsStream("/patch.temp").readAllBytes(), new FileOutputStream(new File(cl, name+".jar")));
                    if(finalGuiLogger != null) {
                        finalGuiLogger.logLine("[OK]");
                    }
                    finalGuiLogger.logLine("");
                    finalGuiLogger.logLine("Installation done.");
                    JOptionPane.showMessageDialog(frame, name+" erfolgreich installiert!");
                    System.exit(0);
                    return;
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(frame, "Fehler: "+exception.toString());
                    System.exit(0);
                    return;
                }
            },name+" installer").start();
        }
    }
    private static final byte[] BUFFER = new byte[4096 * 1024];
    public static void copy(InputStream input, OutputStream output) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(BUFFER))!= -1) {
            output.write(BUFFER, 0, bytesRead);
        }
    }
}
