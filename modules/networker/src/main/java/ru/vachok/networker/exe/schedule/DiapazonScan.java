// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.schedule;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.controller.ServiceInfoCtrl;
import ru.vachok.networker.exe.ThreadConfig;
import ru.vachok.networker.exe.runnabletasks.ExecScan;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.NetScanFileWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


/**
 Скан диапазона адресов
 
 @since 19.12.2018 (11:35) */
@SuppressWarnings("MagicNumber")
public class DiapazonScan implements Runnable {
    
    
    /**
     {@link ConstantsNet#getAllDevices()}
     */
    private final BlockingDeque<String> allDevLocalDeq = getAllDevices();
    
    private List<String> executionProcessLog= new ArrayList<>();
    
    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();
    
    private final ThreadConfig thrConfig = AppComponents.threadConfig();
    
    /**
     Корень директории.
     */
    private static final String ROOT_PATH_STR = Paths.get("").toAbsolutePath().toString();
    
    private static final MessageToUser messageToUser = new MessageLocal(DiapazonScan.class.getSimpleName());
    
    private Map<String, File> scanFiles = makeFilesMap();
    
    /**
     Singleton inst
     */
    private static DiapazonScan thisInst = new DiapazonScan();
    
    private static final Pattern COMPILE = Pattern.compile(".txt", Pattern.LITERAL);
    
    protected DiapazonScan() {
    }
    
    public Map<String, File> getScanFiles() {
        return Collections.unmodifiableMap(scanFiles);
    }
    
    /**
     SINGLETON
 
     @return single.
     */
    public static DiapazonScan getInstance() {
        return thisInst;
    }
    
    public long getStopClassStampLong() {
        return stopClassStampLong;
    }
    
    /**
     Контролер пинг-экзекуторов
     <p>
     Свичи начала сегментов. Вкл. в оптическое ядро.
     
     @return лист важного оборудования
     
     @throws IllegalAccessException swF.get(swF).toString()
     */
    public static List<String> pingSwitch() throws IllegalAccessException {
        Field[] swFields = SwitchesWiFi.class.getFields();
        List<String> swList = new ArrayList<>();
        for (Field swF : swFields) {
            String ipAddrStr = swF.get(swF).toString();
            swList.add(ipAddrStr);
        }
        return swList;
    }
    
    /**
     Чтобы случайно не уничтожить Overridden {@link #toString()}
     <p>
 
     @return информация о состоянии файлов {@code DiapazonScan. Start at} ...для {@link ServiceInfoCtrl} .
     */
    public String theInfoToString() {
        StringBuilder fileTimes = new StringBuilder();
        try {
            String atStr = " size in bytes: ";
            for (Map.Entry<String, File> entry : scanFiles.entrySet()) {
                fileTimes.append(entry.getKey()).append(atStr).append(Paths.get(entry.getValue().getName()).toFile().length()).append("<br>\n");
            }
        }
        catch (NullPointerException e) {
            messageToUser.info("NO FILES!");
        }
        StringBuilder sb = new StringBuilder("DiapazonScan. Running ");
        sb.append(TimeUnit.MILLISECONDS.toMinutes(getRunMin()));
        sb.append(" min ");
        sb.append("{ ");
        sb.append("<a href=\"/showalldev\">ALL_DEVICES ");
        sb.append(allDevLocalDeq.size());
        sb.append("/");
        sb.append(IPS_IN_VELKOM_VLAN);
        sb.append("(");
        try {
            sb.append(BigDecimal.valueOf(allDevLocalDeq.size()).divide((BigDecimal.valueOf((IPS_IN_VELKOM_VLAN) / 100)), 3, RoundingMode.HALF_DOWN));
        }
        catch (ArithmeticException e) {
            sb.append((float) (allDevLocalDeq.size()) / (float) (IPS_IN_VELKOM_VLAN / 100));
        }
        sb.append(" %)").append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ROOT_PATH_STR);
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes);
        return sb.toString();
    }
    
    @Override public String toString() {
        final StringBuilder sb = new StringBuilder("DiapazonScan{");
        sb.append(theInfoToString()).append("<p>").append(new AppComponents().scanOnline().toString());
        sb.append('}');
        return sb.toString();
    }
    
    protected BlockingDeque<String> getAllDevLocalDeq() {
        return allDevLocalDeq;
    }
    
    @Override
    public void run() {
        startDo();
    }
    
    protected List<String> getExecutionProcessLog() {
        return executionProcessLog;
    }
    
    private ExecScan[] getRunnables() {
        return new ExecScan[]{
            new ExecScan(10, 20, "10.10.", scanFiles.get(FILENAME_SERVTXT_10SRVTXT)),
            new ExecScan(21, 31, "10.10.", scanFiles.get(FILENAME_SERVTXT_21SRVTXT)),
            new ExecScan(31, 41, "10.10.", scanFiles.get(FILENAME_SERVTXT_31SRVTXT)),
        };
    }
    
    private Map<String, File> makeFilesMap() {
        Path absolutePath = Paths.get("").toAbsolutePath();
        Map<String, File> scanMap = new ConcurrentHashMap<>();
        try {
            for (File scanFile : Objects.requireNonNull(new File(absolutePath.toString()).listFiles())) {
                if (scanFile.getName().contains("lan_")) {
                    Objects.requireNonNull(scanFiles).putIfAbsent(scanFile.getName(), scanFile);
                }
            }
        }
        catch (NullPointerException e) {
            messageToUser.error(e.getMessage());
        }
    
        scanMap.putIfAbsent(FILENAME_NEWLAN220, new File(FILENAME_NEWLAN220));
        scanMap.putIfAbsent(FILENAME_NEWLAN210, new File(FILENAME_NEWLAN210));
        scanMap.putIfAbsent(FILENAME_NEWLAN213, new File(FILENAME_NEWLAN213));
        scanMap.putIfAbsent(FILENAME_OLDLANTXT0, new File(FILENAME_OLDLANTXT0));
        scanMap.putIfAbsent(FILENAME_OLDLANTXT1, new File(FILENAME_OLDLANTXT1));
        scanMap.putIfAbsent(FILENAME_SERVTXT_10SRVTXT, new File(FILENAME_SERVTXT_10SRVTXT));
        scanMap.putIfAbsent(FILENAME_SERVTXT_21SRVTXT, new File(FILENAME_SERVTXT_21SRVTXT));
        scanMap.putIfAbsent(FILENAME_SERVTXT_31SRVTXT, new File(FILENAME_SERVTXT_31SRVTXT));
        return scanMap;
    }
    
    private void theNewLan() {
        Runnable execScan200210 = new ExecScan(200, 210, "10.200.", scanFiles.get(FILENAME_NEWLAN210));
        Runnable execScan210220 = new ExecScan(210, 213, "10.200.", scanFiles.get(FILENAME_NEWLAN213));
        Runnable execScan213220 = new ExecScan(213, 219, "10.200.", scanFiles.get(FILENAME_NEWLAN220));
    
        thrConfig.execByThreadConfig(execScan200210);
        thrConfig.execByThreadConfig(execScan210220);
        thrConfig.execByThreadConfig(execScan213220);
    }
    
    private void setScanInMin() {
        if (allDevLocalDeq.remainingCapacity() > 0 && TimeUnit.MILLISECONDS.toMinutes(getRunMin()) > 0 && allDevLocalDeq.size() > 0) {
            
            long scansItMin = allDevLocalDeq.size() / TimeUnit.MILLISECONDS.toMinutes(getRunMin());
            
            AppComponents.getProps().setProperty(ConstantsFor.PR_SCANSINMIN, String.valueOf(scansItMin));
            Preferences pref = AppComponents.getUserPref();
            pref.put(ConstantsFor.PR_SCANSINMIN, String.valueOf(scansItMin));
            
            messageToUser.warn(getClass().getSimpleName(), "scansItMin", " = " + scansItMin);
            try {
                new AppComponents().updateProps();
                pref.sync();
            }
            catch (IOException | BackingStoreException e) {
                messageToUser.error(e.getMessage());
            }
        }
    }
    
    private void startDo() {
        if (allDevLocalDeq.remainingCapacity() == 0) {
            scanFiles.values().stream().forEach(x->{
                String newName = ROOT_PATH_STR + ConstantsFor.FILESYSTEM_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + COMPILE.matcher(x.getName())
                    .replaceAll(Matcher.quoteReplacement("_" + (System.currentTimeMillis() / 1000))) + ".scan";
                File newFile = new File(newName);
                FileSystemWorker.copyOrDelFile(x, newFile.getAbsolutePath(), true);
                messageToUser.info(getClass().getSimpleName() + ".startDo", "newFile", " = " + newFile.getAbsolutePath());
            });
            allDevLocalDeq.clear();
        }
        thrConfig.execByThreadConfig(this::theNewLan);
        thrConfig.execByThreadConfig(this::scanServers);
        thrConfig.execByThreadConfig(DiapazonScan::scanOldLan);
        thrConfig.getTaskScheduler().getScheduledThreadPoolExecutor().scheduleAtFixedRate(this::setScanInMin, 3, 5, TimeUnit.MINUTES);
    }
    
    /**
     @return {@link ExecScan} (from [10,21,31,41] to [20,31,41,51]) запрос из {@link #theInfoToString()}
     */
    private static long getRunMin() {
        Preferences preferences = Preferences.userRoot();
        try {
            preferences.sync();
            return preferences.getLong(ExecScan.class.getSimpleName(), 1);
        }
        catch (BackingStoreException e) {
            InitProperties initProperties = new FileProps(ConstantsFor.PROPS_FILE_JAVA_ID);
            Properties props = initProperties.getProps();
            return Long.parseLong(props.getProperty(ExecScan.class.getSimpleName()));
        }
    }
    
    /**
     192.168.11-14.254
     */
    @SuppressWarnings("MagicNumber")
    private static void scanOldLan() {
        Runnable execScanOld0 = new ExecScan(11, 16, "192.168.", new File(FILENAME_OLDLANTXT0));
        Runnable execScanOld1 = new ExecScan(16, 21, "192.168.", new File(FILENAME_OLDLANTXT1));
        
        AppComponents.threadConfig().execByThreadConfig(execScanOld0);
        AppComponents.threadConfig().execByThreadConfig(execScanOld1);
    }
    
    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        for (ExecScan r : getRunnables()) {
            thrConfig.execByThreadConfig(r);
        }
    }
}