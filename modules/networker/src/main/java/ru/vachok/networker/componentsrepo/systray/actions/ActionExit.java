// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray.actions;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ExitApp;
import ru.vachok.networker.componentsrepo.systray.SystemTrayHelper;
import ru.vachok.networker.data.NetKeeper;
import ru.vachok.networker.data.enums.ConstantsFor;
import ru.vachok.networker.data.enums.FileNames;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileOutputStream;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


/**
 Action Exit App
 <p>
 
 @see SystemTrayHelper
 @since 25.01.2019 (9:59) */
@SuppressWarnings("ClassHasNoToStringMethod")
public class ActionExit extends AbstractAction {
    
    
    private String reason;
    
    private transient MessageToUser messageToUser = ru.vachok.networker.restapi.message.MessageToUser
            .getInstance(ru.vachok.networker.restapi.message.MessageToUser.LOCAL_CONSOLE, ActionExit.class.getSimpleName());
    
    public ActionExit(String reason) {
        this.reason = reason;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        messageToUser.infoNoTitles(getClass().getSimpleName() + ConstantsFor.STR_ACTIONPERFORMED);
        try (FileOutputStream fileOutputStream = new FileOutputStream(FileNames.ALLDEV_MAP)) {
            Future<?> submit = AppComponents.threadConfig().getTaskExecutor().submit(new ExitApp(reason, fileOutputStream, NetKeeper.class));
            submit.get(ConstantsFor.DELAY, TimeUnit.SECONDS);
        }
        catch (Exception ex) {
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            System.exit(ConstantsFor.EXIT_STATUSBAD);
        }
    }
}
