// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.SwitchesWiFi;
import ru.vachok.networker.services.MessageLocal;

import java.io.*;
import java.lang.management.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static ru.vachok.networker.net.enums.ConstantsNet.*;


/**
 Скан диапазона адресов
 
 @since 19.12.2018 (11:35) */
@SuppressWarnings({"ClassWithMultipleLoggers", "MagicNumber", "resource", "IOResourceOpenedButNotSafelyClosed"})
public class DiapazonedScan implements Runnable {
    
    
    /**
     Корень директории.
     */
    private static final String ROOT_PATH_STR = Paths.get("").toAbsolutePath().toString();
    
    /**
     Повторения.
     */
    private static final String FONT_BR_STR = "</font><br>\n";
    
    private static final int MAX_IN_VLAN_INT = 255;
    
    /**
     {@link ConstantsNet#getAllDevices()}
     */
    private static final BlockingDeque<String> ALL_DEVICES_LOCAL_DEQUE = getAllDevices();
    
    private static final MessageToUser messageToUser = new MessageLocal(DiapazonedScan.class.getSimpleName());
    
    /**
     {@link NetScanFileWorker#getI()}
     */
    private static final NetScanFileWorker NET_SCAN_FILE_WORKER_INST = NetScanFileWorker.getI();
    
    /**
     Singleton inst
     */
    private static final DiapazonedScan OUR_INSTANCE = new DiapazonedScan();
    
    private final DiapazonedScan.ExecScan[] runnablesScans;
    
    private final long timeStart = System.currentTimeMillis();
    
    private long stopClassStampLong = NetScanFileWorker.getI().getLastStamp();
    
    private Map<String, File> scanFiles = NET_SCAN_FILE_WORKER_INST.getScanFiles();
    
    private List<String> executionProcessLog = new ArrayList<>();
    
    /**
     Приватный конструктор
     */
    private DiapazonedScan() {
        runnablesScans = new ExecScan[]{
            new ExecScan(10, 20, "10.10.", scanFiles.get(FILENAME_SERVTXT_10SRVTXT)),
            new ExecScan(21, 31, "10.10.", scanFiles.get(FILENAME_SERVTXT_21SRVTXT)),
            new ExecScan(31, 41, "10.10.", scanFiles.get(FILENAME_SERVTXT_31SRVTXT)),
        };
    }
    
    public long getStopClassStampLong() {
        return stopClassStampLong;
    }
    
    /**
     SINGLETON
     
     @return single.
     */
    public static DiapazonedScan getInstance() {
        return OUR_INSTANCE;
    }
    
    /**
     Пингует в 200х VLANах девайсы с 10.200.x.250 по 10.200.x.254
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
    
    @Override
    public void run() {
        startDo();
    }
    
    @SuppressWarnings("StringConcatenation")
    @Override
    public String toString() {
        return theInfoToString();
    }
    
    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    private void theNewLan() {
        Runnable execScan200210 = new DiapazonedScan.ExecScan(200, 210, "10.200.", scanFiles.get(FILENAME_NEWLAN210));
        Runnable execScan210220 = new DiapazonedScan.ExecScan(210, 213, "10.200.", scanFiles.get(FILENAME_NEWLAN213));
        Runnable execScan213220 = new DiapazonedScan.ExecScan(213, 219, "10.200.", scanFiles.get(FILENAME_NEWLAN220));
        
        AppComponents.threadConfig().execByThreadConfig(execScan200210);
        AppComponents.threadConfig().execByThreadConfig(execScan210220);
        AppComponents.threadConfig().execByThreadConfig(execScan213220);
    }
    
    private void startDo() {
        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() == 0) {
            scanFiles.values().stream().forEach(x->{
                String newName = ROOT_PATH_STR + ConstantsFor.FILESYSTEM_SEPARATOR + "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + x.getName()
                    .replace(".txt", "_" + (System.currentTimeMillis() / 1000)) + ".scan";
                File newFile = new File(newName);
                FileSystemWorker.copyOrDelFile(x, newFile.getAbsolutePath(), true);
                messageToUser.info(getClass().getSimpleName() + ".startDo", "newFile", " = " + newFile.getAbsolutePath());
            });
            ALL_DEVICES_LOCAL_DEQUE.clear();
        }
        AppComponents.threadConfig().execByThreadConfig(this::theNewLan);
        AppComponents.threadConfig().execByThreadConfig(this::scanServers);
        AppComponents.threadConfig().execByThreadConfig(this::scanOldLan);
        AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor().scheduleAtFixedRate(this::setScanInMin, 3, 5, TimeUnit.MINUTES);
    }
    
    private void setScanInMin() {
        if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() > 0 && TimeUnit.MILLISECONDS.toMinutes(getRunMin()) > 0 && ALL_DEVICES_LOCAL_DEQUE.size() > 0) {
            long scansItMin = ALL_DEVICES_LOCAL_DEQUE.size() / TimeUnit.MILLISECONDS.toMinutes(getRunMin());
            AppComponents.getProps().setProperty(ConstantsFor.PR_SCANSINMIN, String.valueOf(scansItMin));
            messageToUser.warn(getClass().getSimpleName(), "scansItMin", " = " + scansItMin);
            new AppComponents().updateProps();
        }
    }
    
    /**
     Скан подсетей 10.10.xx.xxx
     */
    private void scanServers() {
        for (DiapazonedScan.ExecScan r : runnablesScans) {
            AppComponents.threadConfig().execByThreadConfig(r);
        }
    }
    
    /**
     Чтобы случайно не уничтожить Overridden {@link #toString()}
     <p>
 
     @return информация о состоянии файлов {@code DiapazonedScan. Start at} ...для {@link ru.vachok.networker.controller.ServiceInfoCtrl} .
     */
    private String theInfoToString() {
        StringBuilder fileTimes = new StringBuilder();
        try {
            String atStr = " size in bytes: ";
            for (String key : scanFiles.keySet()) {
                fileTimes.append(key).append(atStr).append(Paths.get(scanFiles.get(key).getName()).toFile().length()).append("<br>\n");
            }
            ;
        }
        catch (NullPointerException e) {
            messageToUser.info("NO FILES!");
        }
        StringBuilder sb = new StringBuilder("DiapazonedScan. Running ");
        sb.append(TimeUnit.MILLISECONDS.toMinutes(getRunMin()));
        sb.append(" min ");
        sb.append("{ ");
        sb.append("<a href=\"/showalldev\">ALL_DEVICES ");
        sb.append(ALL_DEVICES_LOCAL_DEQUE.size());
        sb.append("/");
        sb.append(IPS_IN_VELKOM_VLAN);
        sb.append("(");
        try {
            sb.append(BigDecimal.valueOf(ALL_DEVICES_LOCAL_DEQUE.size()).divide((BigDecimal.valueOf((IPS_IN_VELKOM_VLAN) / 100)), 3, RoundingMode.HALF_DOWN));
        }
        catch (ArithmeticException e) {
            sb.append((float) (ALL_DEVICES_LOCAL_DEQUE.size()) / (float) (IPS_IN_VELKOM_VLAN / 100));
        }
        sb.append(" %)").append("</a>}");
        sb.append(" ROOT_PATH_STR= ").append(ROOT_PATH_STR);
        sb.append("<br><b>\nfileTimes= </b><br>").append(fileTimes);
        sb.append("<p>").append(new TForms().fromArray(executionProcessLog, true));
        return sb.toString();
    }
    
    /**
     @return {@link DiapazonedScan.ExecScan} (from [10,21,31,41] to [20,31,41,51]) запрос из {@link #theInfoToString()}
     */
    private long getRunMin() {
        List<Long> timeSpend = new ArrayList<>();
        for (DiapazonedScan.ExecScan e : runnablesScans) {
            timeSpend.add(e.getSpend());
        }
        return Collections.max(timeSpend);
    }
    
    /**
     192.168.11-14.254
     */
    @SuppressWarnings("MagicNumber")
    private void scanOldLan() {
        Runnable execScanOld0 = new DiapazonedScan.ExecScan(11, 16, "192.168.", new File(FILENAME_OLDLANTXT0));
        Runnable execScanOld1 = new DiapazonedScan.ExecScan(16, 21, "192.168.", new File(FILENAME_OLDLANTXT1));
        
        AppComponents.threadConfig().execByThreadConfig(execScanOld0);
        AppComponents.threadConfig().execByThreadConfig(execScanOld1);
    }
    
    
    /**
     Да запуска скана из {@link DiapazonedScan}
     
     @since 24.03.2019 (16:01)
     */
    class ExecScan implements Runnable {
        
        
        private static final String PAT_IS_ONLINE = " is online";
    
        private final MessageToUser messageToUser = new MessageLocal(DiapazonedScan.ExecScan.class.getSimpleName());
        
        private long stArt;
        
        private int from;
        
        private int to;
        
        private String whatVlan;
        
        private PrintStream printStream;
    
        private ThreadMXBean threadMXBean;
    
        private OperatingSystemMXBean operatingSystemMXBean;
    
        private RuntimeMXBean runtimeMXBean;
        
        private File vlanFile;
        
        public ExecScan(int from, int to, String whatVlan, File vlanFile) {
            
            this.from = from;
            
            this.to = to;
            
            this.whatVlan = whatVlan;
            
            this.vlanFile = vlanFile;
    
            stArt = LocalDateTime.of(1984, 1, 7, 2, 0).toEpochSecond(ZoneOffset.ofHours(3)) * 1000;
        }
        
        @Override
        public void run() {
            this.threadMXBean = ManagementFactory.getThreadMXBean();
            this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
            this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            
            if (vlanFile.exists()) {
                String newFileName = vlanFile.getAbsolutePath()
                    .replace(vlanFile.getName(), "lan" + ConstantsFor.FILESYSTEM_SEPARATOR + vlanFile.getName().replace(".txt", "_" + (System.currentTimeMillis() / 1000)) + ".scan");
                boolean copyFile = FileSystemWorker.copyOrDelFile(vlanFile, newFileName, true);
                messageToUser.info(vlanFile.getName() + " copied to ", newFileName, " = " + copyFile);
            }
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(vlanFile);
            }
            catch (IOException e) {
                messageToUser.errorAlert(getClass().getSimpleName(), ".ExecScan", e.getMessage());
            }
            this.printStream = new PrintStream(Objects.requireNonNull(outputStream), true);
            if (ALL_DEVICES_LOCAL_DEQUE.remainingCapacity() > 0) {
                boolean execScanB = execScan();
                messageToUser.info("ALL_DEV", "Scan from " + from + " to " + to + " is " + execScanB, "ALL_DEVICES_LOCAL_DEQUE = " + ALL_DEVICES_LOCAL_DEQUE.size());
            }
            else {
                messageToUser.error(getClass().getSimpleName(), String.valueOf(ALL_DEVICES_LOCAL_DEQUE.remainingCapacity()), " ALL_DEVICES_LOCAL_DEQUE remainingCapacity!");
            }
    
        }
        
        @Override public String toString() {
            final StringBuilder sb = new StringBuilder("ExecScan{");
            sb
                .append("from=")
                .append(from);
            sb
                .append(", stArt=")
                .append(new Date(stArt));
            sb
                .append(", to=")
                .append(to);
            sb.append('}');
            sb.append("<br>\n");
            sb.append(getBeansInfo());
            return sb.toString();
        }
    
        private String getBeansInfo() {
            final StringBuilder sb = new StringBuilder();
        
            sb.append(operatingSystemMXBean.getSystemLoadAverage()).append(" avg system load. ");
            sb.append(operatingSystemMXBean.getAvailableProcessors()).append(" Processors. ");
            sb.append(operatingSystemMXBean.getName()).append(" Name. ");
            sb.append(operatingSystemMXBean.getArch()).append(" Arch. ");
            sb.append(operatingSystemMXBean.getVersion()).append(" Version. ");
            sb.append("<br>");
            sb.append(runtimeMXBean.getName()).append(" Name. ");
            sb.append(runtimeMXBean.getUptime()).append(" Time. ");
            sb.append(runtimeMXBean.getInputArguments()).append(" InputArguments. ");
            sb.append(runtimeMXBean.getClassPath()).append(" ClassPath. ");
            sb.append(runtimeMXBean.getLibraryPath()).append(" LibraryPath. ");
            sb.append(runtimeMXBean.getVmVersion()).append(" VmVersion. ");
        
            sb.append("<br>");
            sb.append("<br>");
        
            ThreadInfo infoThisThr = threadMXBean.getThreadInfo(Thread.currentThread().getId());
            sb.append(infoThisThr.toString()).append(" String. ");
            sb.append(infoThisThr.isSuspended()).append(" Suspended. ");
            sb.append(infoThisThr.getThreadName()).append(" ThreadName. ");
            sb.append(infoThisThr.getWaitedTime()).append(" WaitedTime (millis). ");
        
            return sb.toString();
        }
    
        private long getSpend() {
            messageToUser.info(getClass().getSimpleName() + ".getSpend", "new Date(stArt)", " = " + new Date(stArt));
            return System.currentTimeMillis() - stArt;
        }
    
        private boolean execScan() {
            this.stArt = System.currentTimeMillis();
            try {
                ConcurrentMap<String, String> stringStringConcurrentMap = scanLanSegment(from, to, whatVlan, printStream);
                NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
                new ExitApp(from + "-" + to + ".map", stringStringConcurrentMap).writeOwnObject();
                return true;
            }
            catch (Exception e) {
                messageToUser.error(e.getMessage());
                return false;
            }
        }
    
        /**
         @param iThree третий октет vlan
         @param jFour четвертый октет vlan
         @return Example: {@code 192.168.11.0 192.168.11.0} or {@code 10.200.200.1 10.200.200.1 is online}
     
         @throws IOException при записи файла
         */
        private String oneIpScanAndPrintToFile(int iThree, int jFour) throws IOException {
            AppComponents.threadConfig().thrNameSet(String.valueOf(iThree));
    
            int timeOutMSec = (int) ConstantsFor.DELAY;
            byte[] aBytes = InetAddress.getByName(whatVlan + iThree + "." + jFour).getAddress();
            StringBuilder stringBuilder = new StringBuilder();
            InetAddress byAddress = InetAddress.getByAddress(aBytes);
            String hostName = byAddress.getHostName();
            String hostAddress = byAddress.getHostAddress();
            
            if (ConstantsFor.thisPC().equalsIgnoreCase("HOME")) {
                timeOutMSec = (int) (ConstantsFor.DELAY * 2);
                NetScanFileWorker.getI().setLastStamp(System.currentTimeMillis());
            }
            
            if (byAddress.isReachable(timeOutMSec)) {
                NetListKeeper.getI().getOnLinesResolve().put(hostAddress, hostName);
                
                ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"green\">" + hostName + FONT_BR_STR);
                stringBuilder.append(hostAddress).append(" ").append(hostName).append(PAT_IS_ONLINE);
            }
            else {
                NetListKeeper.getI().getOffLines().put(byAddress.getHostAddress(), hostName);
                
                ALL_DEVICES_LOCAL_DEQUE.add("<font color=\"red\">" + hostName + FONT_BR_STR);
                stringBuilder.append(hostAddress).append(" ").append(hostName);
            }
            if (stringBuilder.toString().contains(PAT_IS_ONLINE)) {
                printStream.println(hostAddress + " " + hostName);
                messageToUser.info(getClass().getSimpleName() + ".oneIpScanAndPrintToFile ip online " + whatVlan + iThree + "." + jFour, vlanFile.getName(), " = " + vlanFile
                    .length() + ConstantsFor.STR_BYTES);
            }
            return stringBuilder.toString();
        }
        
        /**
         Сканер локальной сети@param stStMap Запись в лог@param fromVlan начало с 3 октета IP@param toVlan   конец с 3 октета IP
         
         @param whatVlan первый 2 октета, с точкоё в конце.
         */
        private ConcurrentMap<String, String> scanLanSegment(int fromVlan, int toVlan, String whatVlan, PrintStream printStream) {
            ConcurrentMap<String, String> stStMap = new ConcurrentHashMap<>(MAX_IN_ONE_VLAN * (toVlan - fromVlan));
            String theScannedIPHost = "No scan yet. MAP Capacity: ";
    
            for (int i = fromVlan; i < toVlan; i++) {
                StringBuilder msgBuild = new StringBuilder();
                for (int j = 0; j < MAX_IN_VLAN_INT; j++) {
                    AppComponents.threadConfig().thrNameSet(i + "." + j);
                    try {
                        theScannedIPHost = oneIpScanAndPrintToFile(i, j);
                        stStMap.put(theScannedIPHost.split(" ")[0], theScannedIPHost.split(" ")[1]);
                    }
                    catch (IOException e) {
                        stStMap.put(e.getMessage(), new TForms().fromArray(e, false));
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        stStMap.put(theScannedIPHost, e.getMessage());
                    }
                }
                executionProcessLog.add(toString());
                Collections.sort(executionProcessLog);
                if (executionProcessLog.size() >= 8) {
                    FileSystemWorker.writeFile((this.from + "_vlan-to_" + this.to + ".log"), executionProcessLog.stream());
                    executionProcessLog.clear();
                }
            }
    
            return stStMap;
        }
    }
}