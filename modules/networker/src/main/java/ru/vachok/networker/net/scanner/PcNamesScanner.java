// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.net.scanner;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.TForms;
import ru.vachok.networker.ad.pc.PCInfo;
import ru.vachok.networker.componentsrepo.UsefulUtilities;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.componentsrepo.fileworks.FileSystemWorker;
import ru.vachok.networker.componentsrepo.htmlgen.HTMLInfo;
import ru.vachok.networker.data.Keeper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.*;
import ru.vachok.networker.net.NetScanService;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageToTray;
import ru.vachok.networker.restapi.props.InitPropertiesAdapter;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static ru.vachok.networker.data.enums.ConstantsFor.STR_P;


/**
 @see ru.vachok.networker.net.scanner.PcNamesScannerTest
 @since 21.08.2018 (14:40) */
@Service(ConstantsFor.BEANNAME_NETSCANNERSVC)
@Scope(ConstantsFor.SINGLETON)
public class PcNamesScanner implements HTMLInfo {
    
    
    /**
     {@link ConstantsFor#DELAY}
     */
    static final int DURATION_MIN = (int) ConstantsFor.DELAY;
    
    /**
     {@link AppComponents#getProps()}
     */
    private static final Properties PROPERTIES = AppComponents.getProps();
    
    private static final String METH_GETPCSASYNC = ".getPCsAsync";
    
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, PcNamesScanner.class.getSimpleName());
    
    private static final File scanTemp = new File("scan.tmp");
    
    private static final TForms T_FORMS = new TForms();
    
    /**
     Время инициализации
     */
    private static final long startClassTime = System.currentTimeMillis();
    
    protected NetScanService getScannerUSR = new ScannerUSR(new Date());
    
    private static List<String> minimessageToUser = new ArrayList<>();
    
    private String thePc = "";
    
    private NetScanCtr classOption;
    
    private Model model;
    
    private HttpServletRequest request;
    
    private long lastSt;
    
    public PcNamesScanner() {
        UsefulUtilities.setPreference(PropertiesNames.PR_ONLINEPC, PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC));
        PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
    }
    
    /**
     @return атрибут модели.
     */
    public String getThePc() {
        return thePc;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("NetScannerSvc{");
        try {
            sb.append(", thePc='").append(thePc).append('\'');
    
            sb.append(", model=").append(classOption.getModel().asMap().size());
            sb.append(", request=").append(classOption.getRequest().getRequestURI());
            sb.append(", lastSt=").append(new Date(lastSt));
        }
        catch (RuntimeException e) {
            sb.append(MessageFormat.format("Exception: {0} in {1}.toString()", e.getMessage(), this.getClass().getSimpleName()));
        }
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String fillAttribute(String attributeName) {
        PCInfo pcInfo = PCInfo.getInstance(attributeName);
        return pcInfo.getInfo();
    }
    
    @Override
    public String fillWebModel() {
        if (classOption == null) {
            throw new InvokeIllegalException("SET CLASS OPTION: " + this.getClass().getSimpleName());
        }
        else {
            try {
                checkScanConditions();
            }
            catch (ExecutionException | InterruptedException | TimeoutException e) {
                return MessageFormat.format("NetScannerSvc.getInfo: {0}, ({1})", e.getMessage(), e.getClass().getName());
            }
        }
        return new ScanMessagesCreator().fillUserPCForWEBModel();
    }
    
    void setThePc(String thePc) {
        this.thePc = thePc;
    }
    
    @Override
    public void setClassOption(Object classOption) {
        if (classOption instanceof NetScanCtr) {
            this.classOption = (NetScanCtr) classOption;
        }
        else {
            this.thePc = (String) classOption;
        }
    }
    
    private void checkScanConditions() throws ExecutionException, InterruptedException, TimeoutException {
        this.model = classOption.getModel();
        this.request = classOption.getRequest();
        this.lastSt = classOption.getLastScan();
        
        int thisTotpc = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_TOTPC, "269"));
        
        if ((scanTemp.isFile() && scanTemp.exists())) {
            isMapSizeBigger(thisTotpc);
        }
        else {
            isTime(thisTotpc - NetKeeper.getNetworkPCs().size(), lastSt / 1000);
        }
    }
    
    private void isMapSizeBigger(int thisTotpc) throws ExecutionException, InterruptedException, TimeoutException {
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(lastSt - System.currentTimeMillis());
        int pcWas = Integer.parseInt(PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"));
        int remainPC = thisTotpc - NetKeeper.getNetworkPCs().size();
        boolean newPSs = remainPC < 0;
        
        String msg = new ScanMessagesCreator().getMsg(timeLeft);
        String title = new ScanMessagesCreator().getTitle(remainPC, thisTotpc, pcWas);
        String pcValue = new ScanMessagesCreator().fillUserPCForWEBModel();
        
        messageToUser.info(msg);
        model.addAttribute("left", msg).addAttribute("pc", pcValue).addAttribute(ModelAttributeNames.TITLE, title);
        
        if (newPSs) {
            newPCCheck(pcValue, remainPC);
        }
        else {
            noNewPCCheck(remainPC);
        }
        
        isTime(remainPC, lastSt / 1000);
    }
    
    protected Set<String> fillSETOfPcNames() {
        Date lastDate = new Date(Long.parseLong(PROPERTIES.getProperty(PropertiesNames.PR_LASTSCAN, "0")));
        return NetKeeper.getPcNamesSet();
    }
    
    private void newPCCheck(String pcValue, double remainPC) {
        FileSystemWorker.writeFile(ConstantsNet.BEANNAME_LASTNETSCAN, pcValue);
        model.addAttribute(ModelAttributeNames.NEWPC, "Добавлены компы! " + Math.abs(remainPC) + " шт.");
        PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getNetworkPCs().size()));
        PROPERTIES.setProperty(ModelAttributeNames.NEWPC, String.valueOf(remainPC));
    }
    
    private void noNewPCCheck(int remainPC) {
        if (remainPC < ConstantsFor.INT_ANSWER) {
            PROPERTIES.setProperty(PropertiesNames.PR_TOTPC, String.valueOf(NetKeeper.getNetworkPCs().size()));
        }
    }
    
    private void isTime(int remainPC, long lastScanEpoch) throws ExecutionException, InterruptedException, TimeoutException {
        long lastScanStamp = lastScanEpoch * 1000;
        Runnable scanRun = new ScannerUSR(new Date(lastScanStamp));
        LocalTime lastScanLocalTime = LocalDateTime.ofEpochSecond(lastScanEpoch, 0, ZoneOffset.ofHours(3)).toLocalTime();
        boolean isSystemTimeBigger = (System.currentTimeMillis() > lastScanStamp);
        if (!(scanTemp.exists())) {
            classOption.getModel().addAttribute(ModelAttributeNames.NEWPC, lastScanLocalTime);
            if (isSystemTimeBigger) {
                Future<?> submitScan = Executors.newSingleThreadExecutor().submit(scanRun);
                submitScan.get(ConstantsFor.DELAY - 1, TimeUnit.MINUTES);
                messageToUser.warn(MessageFormat.format("Scan is Done {0}", submitScan.isDone()));
            }
        }
        else {
            messageToUser.warn(this.getClass().getSimpleName() + ".isTime(last)", " = " + lastScanLocalTime, new Date().toString());
            messageToUser
                    .warn(this.getClass().getSimpleName() + ".isTime(next)", "", " = " + new Date(lastScanStamp + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
            messageToUser.warning(this.getClass().getSimpleName(), "scan.tmp", "is " + new File(FileNames.SCAN_TMP).exists());
        }
        
    }
    
    private @NotNull Set<String> fillSETOfPCNamesPref(String prefixPcName) {
        final long startMethTime = System.currentTimeMillis();
        String pcsString;
        for (String pcName : getCycleNames(prefixPcName)) {
            PCInfo pcInfo = PCInfo.getInstance(pcName);
            pcInfo.getInfoAbout(pcName);
        }
        prefixToMap(prefixPcName);
        pcsString = PCInfo.writeToDB();
        messageToUser.info(pcsString);
        
        String elapsedTime = "<b>Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startMethTime) + " sec.</b> " + LocalTime.now();
        return NetKeeper.getPcNamesSet();
    }
    
    /**
     1. {@link #getNamesCount(String)}
     
     @param namePCPrefix префикс имени ПК
     @return обработанные имена ПК, для пинга
 
     @see #fillSETOfPCNamesPref(String)
     */
    private @NotNull Collection<String> getCycleNames(String namePCPrefix) {
        if (namePCPrefix == null) {
            namePCPrefix = "pp";
        }
        int inDex = getNamesCount(namePCPrefix);
        String nameCount;
        Collection<String> list = new ArrayList<>();
        int pcNum = 0;
        for (int i = 1; i < inDex; i++) {
            if (namePCPrefix.equals("no") || namePCPrefix.equals("pp") || namePCPrefix.equals("do") || namePCPrefix.equals("notd") || namePCPrefix.equals("dotd")) {
                nameCount = String.format("%04d", ++pcNum);
            }
            else {
                nameCount = String.format("%03d", ++pcNum);
            }
            list.add(namePCPrefix + nameCount + ConstantsFor.DOMAIN_EATMEATRU);
        }
        return list;
    }
    
    private void prefixToMap(String prefixPcName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<h4>");
        stringBuilder.append(prefixPcName);
        stringBuilder.append("     ");
        stringBuilder.append(NetKeeper.getPcNamesSet().size());
        stringBuilder.append("</h4>");
        String netMapKey = stringBuilder.toString();
        NetKeeper.getNetworkPCs().put(netMapKey, true);
    }
    
    /**
     @param qer префикс имени ПК
     @return кол-во ПК, для пересичления
     
     @see #getCycleNames(String)
     */
    private int getNamesCount(@NotNull String qer) {
        int inDex = 0;
        if (qer.equals("no")) {
            inDex = ConstantsNet.NOPC;
        }
        if (qer.equals("pp")) {
            inDex = ConstantsNet.PPPC;
        }
        if (qer.equals("do")) {
            inDex = ConstantsNet.DOPC;
        }
        if (qer.equals("a")) {
            inDex = ConstantsNet.APC;
        }
        if (qer.equals("td")) {
            inDex = ConstantsNet.TDPC;
        }
        if (qer.equals("dotd")) {
            inDex = ConstantsNet.DOTDPC;
        }
        if (qer.equals("notd")) {
            inDex = ConstantsNet.NOTDPC;
        }
        return inDex;
    }
    
    private class ScannerUSR implements NetScanService {
        
        
        private Date lastScanDate;
        
        @Contract(pure = true)
        ScannerUSR(Date lastScanDate) {
            this.lastScanDate = lastScanDate;
        }
    
        private void scanIt() {
            if (request != null && request.getQueryString() != null) {
                NetKeeper.getNetworkPCs().clear();
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
                UsefulUtilities.setPreference(PropertiesNames.PR_ONLINEPC, String.valueOf(0));
                getExecution();
                Set<String> pcNames = fillSETOfPCNamesPref(request.getQueryString());
                model
                        .addAttribute(ModelAttributeNames.TITLE, new Date().toString())
                        .addAttribute(ModelAttributeNames.PC, T_FORMS.fromArray(pcNames, true));
            }
            else {
                NetKeeper.getNetworkPCs().clear();
                PROPERTIES.setProperty(PropertiesNames.PR_ONLINEPC, "0");
                UsefulUtilities.setPreference(PropertiesNames.PR_ONLINEPC, String.valueOf(0));
                getExecution();
                Set<String> pCsAsync = fillSETOfPcNames();
                model.addAttribute(ModelAttributeNames.TITLE, lastScanDate).addAttribute(ModelAttributeNames.PC, T_FORMS.fromArray(pCsAsync, true));
                PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, String.valueOf(System.currentTimeMillis()));
            }
        }
        
        @Override
        public String getExecution() {
            try {
                new MessageToTray(this.getClass().getSimpleName())
                        .info("NetScannerSvc started scan", UsefulUtilities.getUpTime(), MessageFormat.format("Last online {0} PCs\n File: {1}",
                                PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC), new File("scan.tmp").getAbsolutePath()));
            }
            catch (NoClassDefFoundError e) {
                messageToUser.error(getClass().getSimpleName(), METH_GETPCSASYNC, T_FORMS.fromArray(e.getStackTrace(), false));
            }
            catch (InvokeIllegalException e) {
                messageToUser.error(MessageFormat
                        .format("Scanner.getPingResultStr {0} - {1}\nStack:\n{2}", e.getClass().getTypeName(), e.getMessage(), new TForms().fromArray(e)));
            }
            AppComponents.threadConfig().execByThreadConfig(this::scanPCPrefix);
            return this.toString();
        }
        
        @Override
        public String getPingResultStr() {
            return FileSystemWorker.readFile(FileNames.LASTNETSCAN_TXT);
        }
    
        @Override
        public String writeLog() {
            runAfterAllScan();
            FileSystemWorker.writeFile(FileNames.LASTNETSCAN_TXT, NetKeeper.getNetworkPCs().navigableKeySet().stream());
            FileSystemWorker.writeFile(PcNamesScanner.class.getSimpleName() + ".mini", minimessageToUser);
            FileSystemWorker.writeFile(FileNames.UNUSED_IPS, NetKeeper.getUnusedNamesTree().stream());
            return scanTemp.exists() + " scan.tmp";
        }
        
        @SuppressWarnings("MagicNumber")
        private void runAfterAllScan() {
            String valueOfPropLastScan = String.valueOf((System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(ConstantsFor.DELAY)));
            PROPERTIES.setProperty(PropertiesNames.PR_LASTSCAN, valueOfPropLastScan);
            minimessageToUser.add(T_FORMS.fromArray(PROPERTIES, false));
            float upTime = (float) (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime)) / ConstantsFor.ONE_HOUR_IN_MIN;
            //todo 23.08.2019 (17:48)
            try {
                boolean isFile = fileScanTMPCreate(false);
                String bodyMsg = MessageFormat.format("Online: {0}.\n{1} min uptime. \n{2} = scan.tmp\n", PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"), upTime, isFile);
                new MessageSwing().infoTimer((int) ConstantsFor.DELAY, bodyMsg);
                UsefulUtilities.setPreference(PropertiesNames.PR_ONLINEPC, PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC));
                InitPropertiesAdapter.setProps(PROPERTIES);
            }
            catch (RuntimeException e) {
                messageToUser.error(MessageFormat.format("ScannerUSR.runAfterAllScan: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            }
        }
        
        private boolean fileScanTMPCreate(boolean create) {
            File file = new File("scan.tmp");
            try {
                if (create) {
                    file = Files.createFile(file.toPath()).toFile();
                }
                else {
                    Files.deleteIfExists(Paths.get("scan.tmp"));
                }
            }
            catch (IOException e) {
                messageToUser.error(e.getMessage() + " see line: 437");
            }
            boolean exists = file.exists() & file.lastModified() > (System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY));
    
            file.deleteOnExit();
            
            return exists;
        }
    
        @Override
        public Runnable getMonitoringRunnable() {
            return this;
        }
    
        @Override
        public String getStatistics() {
            return T_FORMS.fromArray(NetKeeper.getPcNamesSet(), true);
        }
    
        private void scanPCPrefix() {
            for (String pcNamePREFIX : ConstantsNet.getPcPrefixes()) {
                Thread.currentThread().setName(TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + "-sec");
                NetKeeper.getPcNamesSet().clear();
                NetKeeper.getPcNamesSet().addAll(fillSETOfPCNamesPref(pcNamePREFIX));
            }
            String elapsedTime = "Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startClassTime) + " sec.";
            NetKeeper.getPcNamesSet().add(elapsedTime);
            AppComponents.threadConfig().execByThreadConfig(this::runAfterAllScan);
        }
        
        @Override
        public void run() {
            if (fileScanTMPCreate(true)) {
                scanIt();
            }
            else {
                throw new InvokeIllegalException(MessageFormat.format("{0} can't create scan.tmp file!", this.getClass().getSimpleName()));
            }
        }
    
        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Scanner{");
            sb.append("lastScanDate=").append(lastScanDate);
            sb.append('}');
            return sb.toString();
        }
    }
    
    
    
    private class ScanMessagesCreator implements Keeper {
        
        
        private @NotNull String getTitle(int remainPC, int thisTotpc, int pcWas) {
            StringBuilder titleBuilder = new StringBuilder();
            titleBuilder.append(remainPC);
            titleBuilder.append("/");
            titleBuilder.append(thisTotpc);
            titleBuilder.append(" PCs (");
            titleBuilder.append(PROPERTIES.getProperty(PropertiesNames.PR_ONLINEPC, "0"));
            titleBuilder.append("/");
            titleBuilder.append(pcWas);
            titleBuilder.append(") Next run ");
            titleBuilder.append(new Date(lastSt));
            return titleBuilder.toString();
        }
        
        private @NotNull String getMsg(long timeLeft) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(timeLeft);
            stringBuilder.append(" seconds (");
            stringBuilder.append((float) timeLeft / ConstantsFor.ONE_HOUR_IN_MIN);
            stringBuilder.append(" min) left<br>Delay period is ");
            stringBuilder.append(DURATION_MIN);
            return stringBuilder.toString();
        }
        
        private @NotNull String fillUserPCForWEBModel() {
            StringBuilder brStringBuilder = new StringBuilder();
            brStringBuilder.append(STR_P);
            Set<String> keySet = NetKeeper.getNetworkPCs().keySet();
            List<String> list = new ArrayList<>(keySet.size());
            list.addAll(keySet);
            
            Collections.sort(list);
            
            for (String keyMap : list) {
                String valueMap = String.valueOf(NetKeeper.getNetworkPCs().get(keyMap));
                brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
            }
            return brStringBuilder.toString();
            
        }
    }
    
}