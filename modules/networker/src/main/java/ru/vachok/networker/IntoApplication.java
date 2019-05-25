// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.config.ThreadConfig;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.net.TelnetStarter;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.services.SpeedChecker;
import ru.vachok.networker.statistics.WeekStats;
import ru.vachok.networker.systray.SystemTrayHelper;
import ru.vachok.stats.SaveLogsToDB;

import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.*;
import java.util.concurrent.*;


/**
 Старт
 <p>
 Dependencies:
 {@link ru.vachok.networker} - 5 : {@link AppComponents}, {@link AppInfoOnLoad}, {@link ConstantsFor}, {@link ExitApp}, {@link TForms}<br>
 {@link ru.vachok.networker.config} - 1 : {@link ThreadConfig} <br>
 {@link ru.vachok.networker.fileworks} - 1 : {@link FileSystemWorker} <br>
 {@link ru.vachok.networker.net} - 1 : {@link MyServer} <br>
 {@link ru.vachok.networker.services} - 2 : {@link MessageLocal}, {@link WeekStats} <br>
 {@link ru.vachok.networker.systray} - 1 : {@link SystemTrayHelper}
 <p>
 
 @see AppInfoOnLoad
 @since 02.05.2018 (10:36) */
@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
public class IntoApplication {
    
    
    public static final boolean TRAY_SUPPORTED = SystemTray.isSupported();
    
    private static final Properties LOCAL_PROPS = AppComponents.getProps();
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = new MessageLocal(IntoApplication.class.getSimpleName());
    
    private static final ThreadPoolTaskExecutor EXECUTOR = AppComponents.threadConfig().getTaskExecutor();
    
    private static final ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR = AppComponents.threadConfig().getTaskScheduler().getScheduledThreadPoolExecutor();
    
    private static ConfigurableApplicationContext configurableApplicationContext;
    
    public static ConfigurableApplicationContext getConfigurableApplicationContext() {
        return configurableApplicationContext;
    }
    
    public void setConfigurableApplicationContext(ConfigurableApplicationContext configurableApplicationContext) {
        IntoApplication.configurableApplicationContext = configurableApplicationContext;
    }
    
    /**
     Точка входа в Spring Boot Application
     <p>
     Создает новый объект {@link SpringApplication}. <br>
     Далее создается {@link ConfigurableApplicationContext}, из {@link SpringApplication}.run({@link IntoApplication}.class).
     {@link FileSystemWorker#delFilePatterns(java.lang.String[])}. Удаление останков от предидущего запуска. <br>
     Пытается читать аргументы {@link #readArgs(ConfigurableApplicationContext, String...)}, если они не null и их больше 0. <br>
     В другом случае - {@link #beforeSt(boolean)} до запуска контекста, {@link ConfigurableApplicationContext}.start(), {@link #afterSt()}.
     
     @param args аргументы запуска
     @see SystemTrayHelper
     */
    public static void main(@Nullable String[] args) {
        AppComponents.threadConfig().execByThreadConfig(new TelnetStarter());
        SpringApplication application = new SpringApplication();
        ConfigurableApplicationContext context = SpringApplication.run(IntoApplication.class);
        configurableApplicationContext = context;
        FileSystemWorker.delFilePatterns(ConstantsFor.getStringsVisit());
        if (args != null && args.length > 0) {
            readArgs(context, args);
        }
        else {
            beforeSt(true);
            context.start();
            afterSt();
        }
    }
    
    /**
     Запуск после старта Spring boot app <br> Usages: {@link #main(String[])}
     <p>
     1. {@link AppComponents#threadConfig()}. Управление запуском и трэдами. <br><br>
     <b>Runnable</b><br>
     2. {@link IntoApplication#getWeekPCStats()} собирает инфо о скорости в файл. Если воскресенье, запускает {@link WeekStats} <br><br>
     <b>Далее:</b><br>
     3. {@link AppComponents#threadConfig()} (4. {@link ThreadConfig#getTaskExecutor()}) - запуск <b>Runnable</b> <br>
     5. {@link ThreadConfig#getTaskExecutor()} - запуск {@link AppInfoOnLoad}. <br><br>
     <b>{@link Exception}:</b><br>
     6. {@link TForms#fromArray(java.lang.Exception, boolean)} - искл. в строку. 7. {@link FileSystemWorker#writeFile(java.lang.String, java.util.List)} и
     запишем в файл.
     */
    private static void afterSt() {
        @NotNull Runnable infoAndSched = new AppInfoOnLoad();
        EXECUTOR.submit(infoAndSched);
        EXECUTOR.submit(IntoApplication::getWeekPCStats);
        SaveLogsToDB saveLogsToDB = new AppComponents().saveLogsToDB();
        if (!ConstantsFor.thisPC().toLowerCase().contains("home")) {
            AppComponents.threadConfig().execByThreadConfig(saveLogsToDB::startScheduled);
        }
        else {
            AppComponents.threadConfig().execByThreadConfig(saveLogsToDB::showInfo);
        }
    }
    
    /**
     Статистика по-пользователям за неделю.
     <p>
     Запуск new {@link SpeedChecker.ChkMailAndUpdateDB}, через {@link Executors#unconfigurableExecutorService(java.util.concurrent.ExecutorService)}
     <p>
     Если {@link LocalDate#getDayOfWeek()} equals {@link DayOfWeek#SUNDAY}, запуск new {@link WeekStats}
     */
    private static void getWeekPCStats() {
        if (LocalDate.now().getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
            AppComponents.threadConfig().execByThreadConfig(new WeekStats(ConstantsFor.SQL_SELECTFROM_PCUSERAUTO));
        }
    }
    
    
    /**
     Чтение аргументов {@link #main(String[])}
     <p>
     {@code for} {@link String}:
     {@link ConstantsFor#PR_TOTPC} - {@link Properties#setProperty(java.lang.String, java.lang.String)}.
     Property: {@link ConstantsFor#PR_TOTPC}, value: {@link String#replaceAll(String, String)} ({@link ConstantsFor#PR_TOTPC}, "") <br>
     {@code off}: {@link ThreadConfig#killAll()}
     
     @param args аргументы запуска.
     */
    private static void readArgs(ConfigurableApplicationContext context, @NotNull String... args) {
        boolean isTray = true;
        Runnable exitApp = new ExitApp(IntoApplication.class.getSimpleName());
        List<@NotNull String> argsList = Arrays.asList(args);
        ConcurrentMap<String, String> argsMap = new ConcurrentHashMap<>();
    
        for (int i = 0; i < argsList.size(); i++) {
            String key = argsList.get(i);
            String value = "true";
            try {
                value = argsList.get(i + 1);
            }
            catch (ArrayIndexOutOfBoundsException ignore) {
                //
            }
            if (!value.contains("-")) {
                argsMap.put(key, value);
            }
            else {
                if (!key.contains("-")) {
                    argsMap.put("", "");
                }
                else {
                    argsMap.put(key, "true");
                }
            }
        }
        for (Map.Entry<String, String> stringStringEntry : argsMap.entrySet()) {
            isTray = parseMapEntry(stringStringEntry, exitApp);
        }
        beforeSt(isTray);
        context.start();
        afterSt();
    }
    
    private static boolean parseMapEntry(Map.Entry<String, String> stringStringEntry, Runnable exitApp) {
        boolean isTray = true;
        if (stringStringEntry.getKey().contains(ConstantsFor.PR_TOTPC)) {
            LOCAL_PROPS.setProperty(ConstantsFor.PR_TOTPC, stringStringEntry.getValue());
        }
        if (stringStringEntry.getKey().equalsIgnoreCase("off")) {
            AppComponents.threadConfig().execByThreadConfig(exitApp);
        }
        if (stringStringEntry.getKey().contains("notray")) {
            messageToUser.info("IntoApplication.readArgs", "key", " = " + stringStringEntry.getKey());
            isTray = false;
        }
        if (stringStringEntry.getKey().contains("ff")) {
            Map<Object, Object> objectMap = Collections.unmodifiableMap(AppComponents.getProps());
            LOCAL_PROPS.clear();
            LOCAL_PROPS.putAll(objectMap);
        }
        if (stringStringEntry.getKey().contains(ConstantsFor.PR_LPORT)) {
            LOCAL_PROPS.setProperty(ConstantsFor.PR_LPORT, stringStringEntry.getValue());
        }
        
        return isTray;
    }
    
    
    private static void trayAdd(SystemTrayHelper systemTrayHelper) {
        if (ConstantsFor.thisPC().toLowerCase().contains(ConstantsFor.HOSTNAME_DO213)) {
            systemTrayHelper.addTray("icons8-плохие-поросята-32.png");
        }
        else {
            if (ConstantsFor.thisPC().toLowerCase().contains("home")) {
                systemTrayHelper.addTray("icons8-house-26.png");
            }
            else {
                systemTrayHelper.addTray(ConstantsFor.FILENAME_ICON);
            }
        }
    }
    
    /**
     Запуск до старта Spring boot app <br> Usages: {@link #main(String[])}
     <p>
     {@link Logger#warn(java.lang.String)} - день недели. <br>
     Если {@link ConstantsFor#thisPC()} - {@link ConstantsFor#HOSTNAME_DO213} или "home",
     {@link SystemTrayHelper#addTray(java.lang.String)} "icons8-плохие-поросята-32.png".
     Else - {@link SystemTrayHelper#addTray(java.lang.String)} {@link String} null<br>
     {@link SpringApplication#setMainApplicationClass(java.lang.Class)}
 
     @param isTrayNeed нужен трэй или нет.
     */
    private static void beforeSt(boolean isTrayNeed) {
        if (isTrayNeed) {
            trayAdd(SystemTrayHelper.getI());
        }
        @NotNull StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("updateProps = ").append(new AppComponents().updateProps(LOCAL_PROPS));
        stringBuilder.append(LocalDate.now().getDayOfWeek().getValue());
        stringBuilder.append(" - day of week\n");
        stringBuilder.append(LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
        stringBuilder.append(AppComponents.ipFlushDNS());
        messageToUser.info("IntoApplication.beforeSt", "stringBuilder", stringBuilder.toString());
        System.setProperty("encoding", "UTF8");
        FileSystemWorker.writeFile("system", new TForms().fromArray(System.getProperties()));
        SCHEDULED_THREAD_POOL_EXECUTOR.schedule(()->messageToUser.info(new TForms().fromArray(LOCAL_PROPS, false)), ConstantsFor.DELAY + 10, TimeUnit.MINUTES);
    }
}