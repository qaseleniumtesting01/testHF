// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.configuretests;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


public class TestConfigureThreadsLogMaker implements TestConfigure {
    
    
    private final long startTime;
    
    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    
    private static final MessageToUser MESSAGE_TO_USER = new MessageLocal("TESTS");
    
    private PrintStream printStream;
    
    private String callingClass;
    
    private ThreadInfo threadInfo;
    
    public TestConfigureThreadsLogMaker(String callingClass, final long startNANOTime) {
        this.startTime = startNANOTime;
        this.callingClass = callingClass;
    }
    
    @Override
    public PrintStream getPrintStream() {
        return printStream;
    }
    
    @Override
    public void beforeClass() {
        threadMXBean.setThreadCpuTimeEnabled(true);
        threadMXBean.setThreadContentionMonitoringEnabled(true);
    
        for (long threadId : threadMXBean.getAllThreadIds()) {
            String threadName = threadMXBean.getThreadInfo(threadId).getThreadName();
            if (callingClass.contains(threadName)) {
                this.threadInfo = threadMXBean.getThreadInfo(threadId);
            }
        }
        try {
            String fileSeparator = System.getProperty(ConstantsFor.PRSYS_SEPARATOR);
            String absoluteExecutionRootTestFolder = Paths.get(".").toAbsolutePath().normalize() + fileSeparator + "tests" + fileSeparator;
            Files.createDirectories(Paths.get(absoluteExecutionRootTestFolder));
            OutputStream outputStream = new FileOutputStream(absoluteExecutionRootTestFolder + callingClass + ".log", true);
            this.printStream = new PrintStream(outputStream, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void afterClass() {
        long cpuTime = threadMXBean.getThreadCpuTime(threadInfo.getThreadId());
        long realTime = System.nanoTime() - startTime;
        String startInfo = "*** Starting " + threadInfo + " at " + LocalDateTime.now();
        String rtInfo = MessageFormat
            .format("TIMERS: realTime in seconds = {0} (in seconds)\ncpuTime = {1} (in NANOSECONDS)", TimeUnit.NANOSECONDS.toSeconds(realTime), cpuTime);
    
        printStream.println(startInfo);
        printStream.println();
        printStream.println(rtInfo);
        printStream.println("cpuTime in nanos = " + cpuTime);
        printStream.println("End ***");
        printStream.println();
        printStream.println();
        printStream.close();
        MESSAGE_TO_USER.info("test", startInfo, rtInfo);
        Thread.currentThread().checkAccess();
        Thread.currentThread().interrupt();
    }
    
}