// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.sysinfo;


import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;


/**

 @since 24.09.2018 (9:44) */
@Component(ConstantsFor.STR_VERSIONINFO)
@Scope(ConstantsFor.SINGLETON)
public class VersionInfo {
    
    
    /**
     Билд
     */
    private String appBuild = "null";
    
    /**
     Ссылка на /doc/index.html
     */
    private static final String DOC_URL = "<a href=\"/doc/index.html\">DOC</a>";

    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    /**
     {@link ConstantsFor#thisPC()}
     */
    private final String thisPCNameStr = ConstantsFor.thisPC();

    private static final MessageToUser messageToUser = new MessageLocal(VersionInfo.class.getSimpleName());
    /**
     Версия
     */
    private String appVersion = "No version";
    
    private static final String PR_APP_BUILD = "appBuild";
    
    /**
     Время сборки
     */
    private String buildTime = getBuildTime();
    
    private String propertiesFrom = ConstantsFor.DBPREFIX + ConstantsFor.STR_PROPERTIES;
    
    private static final String ALERT_DNE = "Property does not exists";
    
    private static final String REPLACEPATTERN_VERSION = "version = '";
    
    public String getPropertiesFrom() {
        return propertiesFrom;
    }
    
    public void setPropertiesFrom(String propertiesFrom) {
        this.propertiesFrom = propertiesFrom;
    }
    
    /**
     @return {@link #appBuild}
     */
    public String getAppBuild() {
        return appBuild;
    }

    /**
     @return {@link #appVersion}
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     @return {@link #buildTime}
     */
    public String getBuildTime() {
        String timeStr = String.valueOf(ConstantsFor.START_STAMP);
        if (ConstantsFor.thisPC().toLowerCase().contains("home") || ConstantsFor.thisPC().toLowerCase().contains("do0")) {
            PROPERTIES.setProperty(ConstantsFor.PR_APP_BUILDTIME, timeStr);
            return timeStr;
        }
        else {
            return PROPERTIES.getProperty(ConstantsFor.PR_APP_BUILDTIME, "1");
        }
    }
    
    public String setParams() {
        String rootPathStr = Paths.get(".").toAbsolutePath().normalize().toString();
        StringBuilder stringBuilder = new StringBuilder();
        File file = new File(rootPathStr + ConstantsFor.FILESYSTEM_SEPARATOR + ConstantsFor.FILENAME_BUILDGRADLE);
        if (file.exists()) {
            try {
                stringBuilder.append(setterVersionFromFiles(file)).append(" is SET");
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage());
            }
        } else {
                stringBuilder.append(getParams()).append(" is GET");
        }
        return stringBuilder.toString();
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("VersionInfo{");
        sb.append("appBuild='").append(appBuild).append('\'');
        sb.append(", appVersion='").append(appVersion).append('\'');
        sb.append(", buildTime='").append(buildTime).append('\'');
        sb.append(", propertiesFrom='").append(propertiesFrom).append('\'');
        sb.append(", thisPCNameStr='").append(thisPCNameStr).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    public String getParams() {
        this.appVersion = PROPERTIES.getProperty(ConstantsFor.PR_APP_VERSION, ALERT_DNE);
        this.appBuild = PROPERTIES.getProperty(ConstantsFor.PR_APP_BUILD, ALERT_DNE);
        this.buildTime = PROPERTIES.getProperty(ConstantsFor.PR_APP_BUILDTIME, ALERT_DNE);
        return this.appVersion + " version from props, " + this.buildTime + " " + this.appBuild + " is GET";
    }

    /**
     Usages: {@link #setParams()} <br> Uses: - <br>

     @param file gradle.build
     */
    private String setterVersionFromFiles(File file) throws IOException {
        DateFormat dateFormat = new SimpleDateFormat("E", Locale.ENGLISH);
        for (String s : FileSystemWorker.readFileToList(file.getAbsolutePath())) {
            if (s.toLowerCase().contains(REPLACEPATTERN_VERSION)) {
                this.appVersion = s.replace(REPLACEPATTERN_VERSION, "").replace("\'", "").trim();
            }
            this.buildTime = new Date().toString();
        }
        this.appBuild = ConstantsFor.thisPC() + dateFormat.format(new Date());
        PROPERTIES.setProperty(ConstantsFor.PR_APP_BUILDTIME, this.buildTime);
        PROPERTIES.setProperty(ConstantsFor.PR_APP_BUILD, appBuild);
        PROPERTIES.setProperty(ConstantsFor.PR_APP_VERSION, this.appVersion);
        boolean isUpdate = new AppComponents().updateProps(PROPERTIES);
        return this.appBuild + " build, " + this.buildTime + " time, " + this.appVersion + " version, props saved: " + isUpdate;
    }
}