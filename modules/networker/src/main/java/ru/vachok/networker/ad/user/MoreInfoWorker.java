// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.ad.user;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.abstr.InternetUse;
import ru.vachok.networker.accesscontrol.inetstats.InetUserPCName;
import ru.vachok.networker.exe.runnabletasks.NetScannerSvc;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.InfoWorker;
import ru.vachok.networker.net.enums.ConstantsNet;
import ru.vachok.networker.net.enums.OtherKnownDevices;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.MessageToTray;
import ru.vachok.networker.systray.actions.ActionCloseMsg;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


/**
 Получение более детальной информации о ПК
 <p>
 
 @since 25.01.2019 (11:06) */
public class MoreInfoWorker implements InfoWorker {
    
    
    private static final String TV = "tv";
    
    private static final Pattern COMPILE = Pattern.compile(": ");
    
    private String aboutWhat;
    
    private boolean isOnline;
    
    public MoreInfoWorker(String aboutWhat) {
        this.aboutWhat = aboutWhat;
    }
    
    public void setOnline(boolean online) {
        isOnline = online;
    }
    
    /**
     Достаёт инфо о пользователе из БД
     <p>
     
     @param userInputRaw {@link NetScannerSvc#getThePc()}
     @return LAST 20 USER PCs
     */
    public static String getUserFromDB(String userInputRaw) {
        StringBuilder retBuilder = new StringBuilder();
        final String sql = "select * from pcuserauto where userName like ? ORDER BY whenQueried DESC LIMIT 0, 20";
        List<String> userPCName = new ArrayList<>();
        String mostFreqName = "No Name";
    
        try {
            userInputRaw = COMPILE.split(userInputRaw)[1].trim();
        }
        catch (ArrayIndexOutOfBoundsException e) {
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
            userInputRaw = userInputRaw.split(":")[1].trim();
        }
        try (Connection c = new AppComponents().connection(ConstantsFor.DBPREFIX + ConstantsFor.STR_VELKOM);
             PreparedStatement p = c.prepareStatement(sql)
        ) {
            p.setString(1, "%" + userInputRaw + "%");
            try (ResultSet r = p.executeQuery()) {
                StringBuilder stringBuilder = new StringBuilder();
                String headER = "<h3><center>LAST 20 USER PCs</center></h3>";
                stringBuilder.append(headER);
    
                while (r.next()) {
                    String pcName = r.getString(ConstantsFor.DBFIELD_PCNAME);
                    userPCName.add(pcName);
                    String returnER = "<br><center><a href=\"/ad?" + pcName.split("\\Q.\\E")[0] + "\">" + pcName + "</a> set: " + r
                        .getString(ConstantsNet.DB_FIELD_WHENQUERIED) + ConstantsFor.HTML_CENTER_CLOSE;
                    stringBuilder.append(returnER);
                }
                List<String> collectedNames = userPCName.stream().distinct().collect(Collectors.toList());
                Map<Integer, String> freqName = new HashMap<>();
                for (String x : collectedNames) {
                    int frequency = Collections.frequency(userPCName, x);
                    stringBuilder.append(frequency).append(") ").append(x).append("<br>");
                    freqName.putIfAbsent(frequency, x);
                }
                if (r.last()) {
                    try {
                        MessageToUser messageToUser = new MessageToTray(new ActionCloseMsg(new MessageLocal("NetScanCtr")));
                        messageToUser.info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DB_FIELD_USER));
                    }
                    catch (UnsupportedOperationException e) {
                        new MessageLocal(MoreInfoWorker.class.getSimpleName())
                            .info(r.getString(ConstantsFor.DBFIELD_PCNAME), r.getString(ConstantsNet.DB_FIELD_WHENQUERIED), r.getString(ConstantsFor.DB_FIELD_USER));
                    }
                }
                Collections.sort(collectedNames);
                Set<Integer> integers = freqName.keySet();
                mostFreqName = freqName.get(Collections.max(integers));
                InternetUse internetUse = new InetUserPCName();
                stringBuilder.append("<br>");
                stringBuilder.append(internetUse.getUsage(mostFreqName));
                return stringBuilder.toString();
            }
        }
        catch (SQLException | IOException | NoSuchElementException e) {
            retBuilder.append(e.getMessage()).append("\n").append(new TForms().fromArray(e, false));
        }
        return retBuilder.toString();
    }
    
    @Override public String getInfoAbout() {
        if (aboutWhat.equalsIgnoreCase(TV)) {
            return getTVNetInfo();
        }
        else {
            return getSomeMore(isOnline);
        }
    }
    
    @Override public void setInfo() {
    
    }
    
    private static String getTVNetInfo() {
        File ptvFile = new File(ConstantsFor.FILENAME_PTV);
        
        List<String> readFileToList = FileSystemWorker.readFileToList(ptvFile.getAbsolutePath());
        List<String> onList = new ArrayList<>();
        List<String> offList = new ArrayList<>();
        readFileToList.stream().flatMap(x->Arrays.stream(x.split(", "))).forEach(s->{
            if (s.contains("true")) {
                onList.add(s.split("/")[0]);
            }
            else {
                offList.add(s.split("/")[0]);
            }
        });
        
        String ptv1Str = OtherKnownDevices.PTV1_EATMEAT_RU;
        String ptv2Str = OtherKnownDevices.PTV2_EATMEAT_RU;

//        String ptv3Str = OtherKnownDevices.PTV3_EATMEAT_RU;
        
        int frequencyOffPTV1 = Collections.frequency(offList, ptv1Str);
        int frequencyOnPTV1 = Collections.frequency(onList, ptv1Str);
        int frequencyOnPTV2 = Collections.frequency(onList, ptv2Str);
        int frequencyOffPTV2 = Collections.frequency(offList, ptv2Str);
//        int frequencyOnPTV3 = Collections.frequency(onList, ptv3Str);
//        int frequencyOffPTV3 = Collections.frequency(offList, ptv3Str);
        
        String ptv1Stats = "<br><font color=\"#00ff69\">" + frequencyOnPTV1 + " on " + ptv1Str + "</font> | <font color=\"red\">" + frequencyOffPTV1 + " off " + ptv1Str + "</font>";
        String ptv2Stats = "<font color=\"#00ff69\">" + frequencyOnPTV2 + " on " + ptv2Str + "</font> | <font color=\"red\">" + frequencyOffPTV2 + " off " + ptv2Str + "</font>";
//        String ptv3Stats = "<font color=\"#00ff69\">" + frequencyOnPTV3 + " on " + ptv3Str + "</font> | <font color=\"red\">" + frequencyOffPTV3 + " off " + ptv3Str + "</font>";
        
        return String.join("<br>\n", ptv1Stats, ptv2Stats);
    }
    
    /**
     Поиск имён пользователей компьютера
     <p>
 
     @param isOnlineNow онлайн = true
     @return выдержка из БД (когда последний раз был онлайн + кол-во проверок) либо хранимый в БД юзернейм (для offlines)
 
     @see NetScannerSvc#getPCNamesPref(String)
     */
    private String getSomeMore(boolean isOnlineNow) {
        StringBuilder buildEr = new StringBuilder();
        if (isOnlineNow) {
            buildEr.append("<font color=\"yellow\">last name is ");
            InfoWorker infoWorker = new ConditionChecker("select * from velkompc where NamePP like ?", aboutWhat + ":true");
            AppComponents.netScannerSvc().setOnLinePCsNum(AppComponents.netScannerSvc().getOnLinePCsNum() + 1);
            buildEr.append(infoWorker.getInfoAbout());
            buildEr.append("</font> ");
        }
        else {
            InfoWorker infoWorker = new ConditionChecker("select * from pcuser where pcName like ?", aboutWhat + ":false");
            buildEr.append(infoWorker.getInfoAbout());
        }
        return buildEr.toString();
    }
}
