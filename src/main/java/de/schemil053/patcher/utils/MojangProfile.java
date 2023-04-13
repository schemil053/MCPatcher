package de.schemil053.patcher.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MojangProfile {

    private static final SimpleDateFormat TIMEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss:SSS'Z'", Locale.GERMANY);

    static {
        TIMEFORMAT.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    private static String getShortUUID(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
    }

    public static JsonObject create(String name, String icon) {
        JsonObject object = new JsonObject();
        object.addProperty("created", TIMEFORMAT.format(new Date(System.currentTimeMillis())));
        object.addProperty("lastUsed", TIMEFORMAT.format(new Date(System.currentTimeMillis())));
        object.addProperty("icon", icon);
        object.addProperty("javaArgs", "-Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M");
        object.addProperty("lastVersionId", name);
        object.addProperty("name", name);
        object.addProperty("type", "custom");
        return object;
    }

    public static void install(File file, String name, String icon) {
        if(!file.exists())
            throw new IllegalStateException("Profile File does not exists!");
        try {
            FileReader reader = new FileReader(file);
            JsonObject parsed = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject profiles = new JsonObject();
            if(parsed.has("profiles")) {
                profiles = parsed.getAsJsonObject("profiles");
                parsed.remove("profiles");
            }
            if(profiles.has(getShortUUID(name))) {
                profiles.remove(getShortUUID(name));
            }
            profiles.add(getShortUUID(name), create(name, icon));
            parsed.add("profiles", profiles);
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            String output = gson.toJson(parsed);

            FileWriter writer = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
            bufferedWriter.write(output);
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
