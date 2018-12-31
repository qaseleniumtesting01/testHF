package ru.vachok.money.other;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.money.ConstantsFor;
import ru.vachok.money.MoneyApplication;
import ru.vachok.money.components.AppVersion;
import ru.vachok.money.config.AppComponents;
import ru.vachok.money.mycar.MyOpel;
import ru.vachok.money.services.sockets.TellNetSRV;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;


/**
 Иконка приложения в System tray.

 @since 29.09.2018 (22:33) */
public class SystemTrayHelper {

    /**
     {@link LoggerFactory}
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTrayHelper.class.getSimpleName());

    /**
     Добавление иконки в трэй.
     <p>
     Задание действия на клик <br>
     Usages: {@link MoneyApplication#main(String[])}
     */
    public void addTrayDefaultMinimum() {
        InitProperties initProperties = new FileProps(ConstantsFor.APP_NAME + SystemTrayHelper.class.getSimpleName());
        try{
            initProperties = new DBRegProperties(ConstantsFor.APP_NAME + SystemTrayHelper.class.getSimpleName());
        }
        catch(ExceptionInInitializerError e){
            LOGGER.error(e.getMessage(), e);
        }
        Properties properties = initProperties.getProps();
        String defaultValue = "/static/images/icons8-монеты-15.png";
        if(ConstantsFor.localPc().equalsIgnoreCase("home")){
            defaultValue = "/static/images/icons8-скучающий-15.png";
        }
        Image image = Toolkit.getDefaultToolkit()
            .getImage(getClass()
                .getResource(properties.getProperty("icon", defaultValue)));
        PopupMenu popupMenu = popMenuSetter();
        TrayIcon trayIcon = new TrayIcon(image, "Money", popupMenu);
        try{
            if(SystemTray.isSupported()){
                SystemTray systemTray = SystemTray.getSystemTray();
                systemTray.add(trayIcon);
            }
            else{
                LOGGER.warn("Tray not supported!");
                Thread.currentThread().interrupt();
            }
        }
        catch(AWTException e){
            LOGGER.warn(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        ActionListener actionListener = e -> {
            try{
                Desktop.getDesktop().browse(URI.create(ConstantsFor.HTTP_LOCALHOST_8881));
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        };
        trayIcon.addActionListener(actionListener);
    }

    /**
     Элементы меню.
     <p>
     Usages: {@link #addTrayDefaultMinimum()}

     @return {@link PopupMenu}
     */
    private PopupMenu popMenuSetter() {
        PopupMenu popupMenu = new PopupMenu();
        MenuItem exitItem = new MenuItem();
        MenuItem openSysInfoPage = new MenuItem();
        MenuItem getInform = new MenuItem();
        MenuItem moneyItem = new MenuItem();
        MenuItem rebootSys = new MenuItem();
        MenuItem offSys = new MenuItem();

        ActionListener exitApp = e -> System.exit(0);
        exitItem.addActionListener(exitApp);
        exitItem.setLabel("Exit");
        openSysInfoPage.addActionListener(e -> {
            try{
                Desktop.getDesktop().browse(URI.create("http://localhost:8881/sysinfo"));
            }
            catch(IOException ex){
                LOGGER.error(ex.getMessage(), ex);
            }
        });
        openSysInfoPage.setLabel("Открыть System Info Page");
        getInform.addActionListener(e -> {
            new MessageSwing().infoNoTitles(new AppVersion().toString() + "\n\n" + Thread.activeCount() + " threads active");
        });
        getInform.setLabel("INFO");
        moneyItem.addActionListener(e -> {
            MyOpel myOpel = MyOpel.getI();
            new MessageSwing().infoNoTitles(myOpel.getCountA107() + " on A107\n" + myOpel.getCountRiga() + "  RIGA\n\n" +
                myOpel.getAvgTime() + " avgTime");
        });
        moneyItem.setLabel("Чекануть дорогу");
        rebootSys.addActionListener(e -> {
            try{
                Runtime.getRuntime().exec("shutdown /r /f");
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        rebootSys.setLabel("REBOOT THIS PC!");
        offSys.addActionListener(e -> {
            try{
                Runtime.getRuntime().exec("shutdown /p /f");
            }
            catch(IOException e1){
                LOGGER.error(e1.getMessage(), e1);
            }
        });
        offSys.setLabel("TURN OFF THIS PC!");
        popupMenu.add(getInform);
        popupMenu.add(moneyItem);
        popupMenu.add(openSysInfoPage);
        popupMenu.addSeparator();
        popupMenu.add(rebootSys);
        popupMenu.add(offSys);
        popupMenu.addSeparator();
        popupMenu.add(exitItem);

        tellNetStarter();
        return popupMenu;
    }

    /**
     Запуск telnet
     <p>

     @see TellNetSRV
     */
    private void tellNetStarter() {
        TellNetSRV tellNetSRV = AppComponents.tellNetSRV();
        new Thread(tellNetSRV::run).start();
    }
}