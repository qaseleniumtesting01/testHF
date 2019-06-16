// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.exe.runnabletasks;


import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.fileworks.FileSystemWorker;
import ru.vachok.networker.services.MessageLocal;
import ru.vachok.networker.systray.actions.ActionOnAppStart;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;


/**
 Обновление инфо о скорости и дороге.
 
 @see ru.vachok.networker.controller.ServiceInfoCtrl#infoMapping(Model, HttpServletRequest, HttpServletResponse)
 @see ActionOnAppStart#actionPerformed(ActionEvent)
 @since 22.08.2018 (9:36) */
public class SpeedChecker implements Callable<Long>, Runnable {
    
    
    /**
     Логер. {@link LoggerFactory}
     */
    private static final MessageToUser LOGGER = new MessageLocal(SpeedChecker.class.getSimpleName());
    
    private final Properties APP_PR = AppComponents.getProps();
    
    /**
     Выходной день
     */
    private static boolean isWeekEnd = (LocalDate.now().getDayOfWeek().equals(SUNDAY) || LocalDate.now().getDayOfWeek().equals(SATURDAY));
    
    public Long getRtLong() {
        return rtLong;
    }
    
    public void setRtLong(Long rtLong) {
        this.rtLong = rtLong;
    }
    
    /**
     Time as long
     <p>
     Время из Базы. Берется из {@link AppComponents#getProps()}
     */
    private Long rtLong = Long.valueOf(APP_PR.getProperty(ConstantsFor.PR_LASTWORKSTART, "2"));
    
    /**
     Запуск.
     <p>
     Если прошло 20 часов, с момента {@link #rtLong} или не {@link #isWeekEnd}, запуск {@link #setRtLong()}.
     Иначе {@link #rtLong} = {@link AppComponents#getProps()}
     */
    @Override
    public void run() {
        long l = rtLong + TimeUnit.HOURS.toMillis(20);
        boolean is20HRSSpend = System.currentTimeMillis() > l;
        if (is20HRSSpend || !isWeekEnd) {
            setRtLong();
        }
        else {
            this.rtLong = Long.valueOf(APP_PR.getProperty(ConstantsFor.PR_LASTWORKSTART));
        }
    }
    
    /**
     @return {@link #rtLong}
     */
    @Override
    public Long call() {
        return rtLong;
    }
    
    /**
     Метрика метода.
     <p>
     
     @param stArt время начала отсчёта.
     */
    private static void methMetr(long stArt) {
        float f = (float) (System.currentTimeMillis() - stArt) / 1000;
        String msgTimeSp = new StringBuilder()
            .append("SpeedChecker.chkForLast: ")
            .append(f)
            .append(ConstantsFor.STR_SEC_SPEND)
            .toString();
        LOGGER.info(msgTimeSp);
    }
    
    /**
     Время прихода на работу.
     <p>
     Для рассчёта в {@link ru.vachok.networker.controller.ServiceInfoCtrl}.
     <p>
     this.{@link #rtLong} = таймстэм, полученый этим методом из БД.
     <b>{@link SQLException}:</b><br>
     {@link FileSystemWorker#error(java.lang.String, java.lang.Exception)} в файл. <br><br>
     <b>Далее:</b><br>
     {@link #methMetr(long)}. Метрика метода.
     */
    private void setRtLong() {
        String classMeth = "SpeedChecker.chkForLast";
        final String sql = ConstantsFor.DBQUERY_SELECTFROMSPEED;
        Properties properties = APP_PR;
        final long stArt = System.currentTimeMillis();
    
        new ChkMailAndUpdateDB(this).runCheck();
        
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBPREFIX + "liferpg");
             PreparedStatement p = connection.prepareStatement(sql);
             ResultSet r = p.executeQuery()
        ) {
            while (r.next()) {
                if (r.last()) {
                    double timeSpend = r.getDouble(ConstantsFor.DBFIELD_TIMESPEND);
                    long timeStamp = r.getTimestamp(ConstantsFor.DBFIELD_TIMESTAMP).getTime();
                    String msg = timeSpend + " time spend;\n" + new Date(timeStamp);
                    this.rtLong = timeStamp + TimeUnit.SECONDS.toMillis(90);
                    properties.setProperty(ConstantsFor.PR_LASTWORKSTART, rtLong + "");
                    LOGGER.info(msg);
                }
            }
        }
        catch (SQLException | IOException e) {
            FileSystemWorker.error(classMeth, e);
        }
        methMetr(stArt);
    }
    
}