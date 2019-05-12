package ru.vachok.networker;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.Address;
import javax.servlet.http.Cookie;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.WatchEvent;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;


/**
 Помощник для {@link Arrays#toString()}
 <p>
 Делает похожие действия, но сразу так, как нужно для {@link ru.vachok.networker.IntoApplication}
 
 @since 06.09.2018 (9:33) */
@SuppressWarnings("ALL")
public class TForms {
    
    
    /**
     {@link LoggerFactory#getLogger(java.lang.String)}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TForms.class.getSimpleName());
    
    private static final String STR_LINE_CLASS = " line, class: ";
    
    private static final String STR_VALUE = ", value: ";
    
    private static final String N_STR = "\n";
    
    private static final String BR_STR = "<br>";
    
    private static final String P_STR = "<p>";
    
    private static final String STR_DISASTER = " occurred disaster!<br>";
    
    private static final String STR_METHFILE = " method.<br>File: ";
    
    private StringBuilder brStringBuilder = new StringBuilder();
    
    private StringBuilder nStringBuilder = new StringBuilder();
    
    public String fromArray(Properties properties) {
        InitProperties initProperties = new FileProps(ConstantsFor.APPNAME_WITHMINUS);
        initProperties.setProps(properties);
        nStringBuilder.append(N_STR);
        properties.forEach((x, y)->{
            String msg = x + " : " + y;
            LOGGER.info(msg);
            nStringBuilder.append(x).append(" :: ").append(y).append(N_STR);
        });
        return nStringBuilder.toString();
    }
    
    /**
     Преобразрвание исключений.
     
     @param e {@link Exception}
     @param isHTML где показывается строка. На ВЕБ или нет.
     @return {@link #brStringBuilder} или {@link #nStringBuilder}
     */
    public String fromArray(Exception e, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
    
        brStringBuilder.append(LocalDateTime.now()).append(BR_STR).append("<h3>").append(e.getMessage()).append(" Exception message.</h3><p>");
    
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            parseTrace(stackTraceElement);
        }
        brStringBuilder.append("<p>");
        nStringBuilder.append(N_STR).append(N_STR).append(N_STR);
    
        if (e.getSuppressed().length > 0) {
            parseThrowable(e);
        }
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromEnum(Enumeration<String> enumStrings, boolean br) {
        nStringBuilder.append(N_STR);
        brStringBuilder.append(P_STR);
        while (enumStrings.hasMoreElements()) {
            String str = enumStrings.nextElement();
            nStringBuilder.append(str).append(N_STR);
            brStringBuilder.append(str).append(BR_STR);
        }
        nStringBuilder.append(N_STR);
        brStringBuilder.append("</p>");
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Queue<String> stringQueue, boolean br) {
        brStringBuilder = new StringBuilder();
        nStringBuilder = new StringBuilder();
        brStringBuilder.append(P_STR);
        while (stringQueue.iterator().hasNext()) {
            brStringBuilder.append(stringQueue.poll()).append(BR_STR);
            nStringBuilder.append(stringQueue.poll()).append(N_STR);
        }
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Cookie[] cookies, boolean br) {
        brStringBuilder.append(P_STR);
        for (Cookie c : cookies) {
            brStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(BR_STR);
            nStringBuilder
                .append(c.getName()).append(" ").append(c.getComment()).append(" ").append(c.getMaxAge()).append(N_STR);
        }
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Address[] mailAddress, boolean br) {
        for (Address address : mailAddress) {
            brStringBuilder
                .append(address)
                .append("br");
            nStringBuilder
                .append(address)
                .append(N_STR);
        }
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Set<?> cacheSet, boolean br) {
        brStringBuilder.append(P_STR);
        nStringBuilder.append(N_STR);
        for (Object o : cacheSet) {
            brStringBuilder
                .append(o)
                .append(BR_STR);
            nStringBuilder
                .append(o)
                .append(N_STR);
        }
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Throwable[] suppressed) {
        nStringBuilder.append("suppressed throwable!\n".toUpperCase());
        for (Throwable throwable : suppressed) {
            nStringBuilder.append(throwable.getMessage());
        }
        return nStringBuilder.toString();
    }
    
    public String fromArrayUsers(ConcurrentMap<String, String> pcUsers, boolean br) {
        pcUsers.forEach((x, y)->{
            nStringBuilder
                .append(N_STR)
                .append(x)
                .append(N_STR)
                .append(y);
            brStringBuilder
                .append("<p><h4>")
                .append(x)
                .append(BR_STR)
                .append(y)
                .append("</p>");
        });
        if (br) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Map<?, ?> mapDefObj, boolean br) {
        brStringBuilder = new StringBuilder();
        nStringBuilder = new StringBuilder();
        
        brStringBuilder.append(P_STR);
        Set<?> keySet = mapDefObj.keySet();
        List<String> list = new ArrayList<>(keySet.size());
        keySet.forEach(x->list.add(x.toString()));
        Collections.sort(list);
        for (String keyMap : list) {
            String valueMap = mapDefObj.get(keyMap).toString();
            brStringBuilder.append(keyMap).append(" ").append(valueMap).append("<br>");
            nStringBuilder.append(keyMap).append(" ").append(valueMap).append("\n");
        }
        if (br) {
            brStringBuilder.append(P_STR);
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(InetAddress[] allByName, boolean b) {
        brStringBuilder.append(BR_STR);
        for (InetAddress inetAddress : allByName) {
            brStringBuilder
                .append(inetAddress)
                .append(BR_STR);
            nStringBuilder
                .append(inetAddress)
                .append(N_STR);
        }
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(List<?> rndList, boolean b) {
        brStringBuilder.append(BR_STR);
        rndList.forEach(x->{
            brStringBuilder
                .append(x)
                .append(BR_STR);
            nStringBuilder
                .append(x)
                .append(N_STR);
        });
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Stream<?> rndStream, boolean b) {
        brStringBuilder.append(BR_STR);
        rndStream.forEach(x->{
            brStringBuilder
                .append(x)
                .append(BR_STR);
            nStringBuilder
                .append(x)
                .append(N_STR);
        });
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Object[] objects, boolean b) {
        brStringBuilder.append(P_STR);
        for (Object o : objects) {
            brStringBuilder
                .append(o)
                .append(BR_STR);
            nStringBuilder
                .append(o)
                .append(N_STR);
        }
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Properties p, boolean b) {
        brStringBuilder.append(P_STR);
        p.forEach((x, y)->{
            String str = "Property: ";
            String str1 = STR_VALUE;
            brStringBuilder
                .append(str).append(x)
                .append(str1).append(y).append(BR_STR);
            nStringBuilder
                .append(str).append(x)
                .append(str1).append(y).append(N_STR);
        });
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(BlockingQueue<Runnable> runnableBlockingQueue, boolean b) {
        this.nStringBuilder = new StringBuilder();
        this.brStringBuilder = new StringBuilder();
        nStringBuilder.append(N_STR);
        Iterator<Runnable> runnableIterator = runnableBlockingQueue.stream().iterator();
        int count = 0;
        while (runnableIterator.hasNext()) {
            Runnable next = runnableIterator.next();
            count++;
            nStringBuilder.append(count).append(") ").append(next).append(N_STR);
            brStringBuilder.append(count).append(") ").append(next).append(BR_STR);
        }
        if (b) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String sshCheckerMapWithDates(Map<String, Long> sshCheckerMap, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
        sshCheckerMap.forEach((x, y)->{
            try {
                byte[] address = InetAddress.getByName(x).getAddress();
                x = InetAddress.getByAddress(address).toString();
            }
            catch (UnknownHostException e) {
                x = x + " no name";
            }
            brStringBuilder.append("<b>").append(x).append("</b><font color=\"gray\"> ").append(y).append("</font> (").append(new Date(y)).append(")<br>");
            nStringBuilder.append(x).append(" ").append(y).append(" (").append(new Date(y)).append(")\n");
        });
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    
    public String fromArray(ResultSetMetaData resultSetMetaData, int colIndex, boolean isHTML) throws SQLException {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
        
        brStringBuilder.append(resultSetMetaData.getColumnCount() + " collumns<br>");
        nStringBuilder.append(resultSetMetaData.getColumnCount() + " collumns\n");
        
        brStringBuilder.append(resultSetMetaData.getCatalogName(colIndex)).append(" getCatalogName").append(BR_STR);
        nStringBuilder.append(resultSetMetaData.getCatalogName(colIndex)).append(" getCatalogName").append(N_STR);
        
        brStringBuilder.append(resultSetMetaData.getColumnName(colIndex)).append(" getColumnName").append(BR_STR);
        nStringBuilder.append(resultSetMetaData.getColumnName(colIndex)).append(" getColumnName").append(N_STR);
        
        brStringBuilder.append(resultSetMetaData.getColumnDisplaySize(colIndex)).append(" getColumnDisplaySize").append(BR_STR);
        nStringBuilder.append(resultSetMetaData.getColumnDisplaySize(colIndex)).append(" getColumnDisplaySize").append(N_STR);
        
        brStringBuilder.append(resultSetMetaData.getColumnType(colIndex)).append(" getColumnType").append(BR_STR);
        nStringBuilder.append(resultSetMetaData.getColumnType(colIndex)).append(" getColumnType").append(N_STR);
        
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(Collection<?> values, boolean isHTML) {
        return fromArray(values.stream(), isHTML);
    }
    
    public String fromArrayW(List<WatchEvent<?>> watchEventlist, boolean isHTML) {
        this.brStringBuilder = new StringBuilder();
        this.nStringBuilder = new StringBuilder();
        watchEventlist.stream().forEach(x->{
            brStringBuilder.append(x.count()).append(" events").append(BR_STR);
            nStringBuilder.append(x.count()).append(" events").append(N_STR);
            
            brStringBuilder.append(x.kind()).append(" ").append(x.context()).append(BR_STR);
            nStringBuilder.append(x.kind()).append(" ").append(x.context()).append(N_STR);
        });
        if (isHTML) {
            return brStringBuilder.toString();
        }
        else {
            return nStringBuilder.toString();
        }
    }
    
    public String fromArray(long[] threads) {
        this.nStringBuilder = new StringBuilder();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        for (long tID : threads) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(tID);
            LockInfo lockInfo = threadInfo.getLockInfo();
            nStringBuilder.append(lockInfo.toString());
        }
        return nStringBuilder.toString();
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TForms{");
        sb.append("STR_LINE_CLASS='").append(STR_LINE_CLASS).append('\'');
        sb.append(", STR_VALUE='").append(STR_VALUE).append('\'');
        sb.append(", N_STR='").append(N_STR).append('\'');
        sb.append(", BR_STR='").append(BR_STR).append('\'');
        sb.append(", P_STR='").append(P_STR).append('\'');
        sb.append(", STR_DISASTER='").append(STR_DISASTER).append('\'');
        sb.append(", STR_METHFILE='").append(STR_METHFILE).append('\'');
        sb.append(", brStringBuilder=").append(brStringBuilder);
        sb.append(", nStringBuilder=").append(nStringBuilder);
        sb.append('}');
        return sb.toString();
    }
    
    /**
     Если {@link Exception} содержит getSuppressed.
     <p>
     Парсит {@link Throwable}
 
     @param e {@link Exception}
     @see TForms#fromArray(java.lang.Exception, boolean)
     */
    @SuppressWarnings("MethodWithMultipleLoops")
    private void parseThrowable(Exception e) {
        Throwable[] eSuppressed = e.getSuppressed();
        nStringBuilder.append("Suppressed.length = ").append(eSuppressed.length).append(N_STR);
        nStringBuilder.append("Suppressed.length = ").append(eSuppressed.length).append(BR_STR);
        for (Throwable throwable : eSuppressed) {
            nStringBuilder.append(throwable.toString()).append(N_STR);
            brStringBuilder.append(throwable.toString()).append(BR_STR);
            for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                parseTrace(stackTraceElement);
            }
            nStringBuilder.append(N_STR).append(N_STR);
            brStringBuilder.append(P_STR);
        }
    }
    
    /**
     Парсинг элемента трэйса
     <p>
     
     @param stackTraceElement {@link StackTraceElement} из {@link Exception} или {@link Throwable}
     @see #parseThrowable(Exception)
     @see #fromArray(Exception, boolean)
     */
    private void parseTrace(StackTraceElement stackTraceElement) {
        nStringBuilder.append(stackTraceElement.getFileName()).append(": ");
        nStringBuilder.append(stackTraceElement.toString()).append(N_STR);
        brStringBuilder.append(stackTraceElement.getFileName()).append(": ");
        brStringBuilder.append(stackTraceElement.toString()).append(BR_STR);
    }
}