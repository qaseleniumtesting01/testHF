// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.componentsrepo.systray.actions;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ad.common.OldBigFilesInfoCollector;
import ru.vachok.networker.componentsrepo.exceptions.InvokeIllegalException;
import ru.vachok.networker.data.enums.FileNames;
import ru.vachok.networker.restapi.message.MessageLocal;
import ru.vachok.networker.restapi.message.MessageToUser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.StringJoiner;
import java.util.concurrent.*;


/**
 Action on Reload Context button
 <p>
 
 @see ActionMakeInfoAboutOldCommonFilesTest
 @since 25.01.2019 (13:30) */
public class ActionMakeInfoAboutOldCommonFiles extends AbstractAction {
    
    
    /**
     {@link MessageLocal}
     */
    private static final MessageToUser messageToUser = MessageToUser.getInstance(MessageToUser.LOCAL_CONSOLE, ActionMakeInfoAboutOldCommonFiles.class.getSimpleName());
    
    private long timeoutSeconds;
    
    private String fileName = FileNames.FILES_OLD;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        this.fileName = fileName + ".t";
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        makeAction();
    }
    
    protected String makeAction() {
        Callable<String> infoCollector = new OldBigFilesInfoCollector();
        Future<String> submit = AppComponents.threadConfig().getTaskExecutor().submit(infoCollector);
        try {
            return submit.get(timeoutSeconds, TimeUnit.SECONDS);
        }
        catch (InterruptedException | ExecutionException e) {
            messageToUser.warn(ActionMakeInfoAboutOldCommonFiles.class.getSimpleName(), "makeAction", e.getMessage() + Thread.currentThread().getState().name());
            Thread.currentThread().checkAccess();
            Thread.currentThread().interrupt();
            throw new InvokeIllegalException(getClass().getSimpleName() + " FAILED");
        }
        catch (TimeoutException e) {
            throw new InvokeIllegalException("TIMEOUT " + timeoutSeconds);
        }
    }
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", ActionMakeInfoAboutOldCommonFiles.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
}
