// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.systray.actions;


import org.jetbrains.annotations.Contract;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageSwing;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.componentsrepo.exceptions.UserChangedHisMindException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

import static org.testng.Assert.assertNull;


/**
 @see ActionOpenProgFolder
 @since 12.07.2019 (20:21) */
public class ActionOpenProgFolderTest {
    
    
    private MessageToUser messageToUser = new MessageSwing(this.getClass().getSimpleName());
    
    public ActionOpenProgFolderTest() {
        if (!System.getProperty("os.name").toLowerCase().contains(ConstantsFor.PR_WINDOWSOS)) {
            throw new UnsupportedOperationException(System.getProperty("os.name"));
        }
    }
    
    @Test(enabled = false)
    public void testActionPerformed() {
        Path workingNowPathRoot = Paths.get(".");
        String confirmDo = messageToUser
            .confirm(ConstantsFor.APPNAME_WITHMINUS.replace("-", ""), ActionOpenProgFolder.TITLE_MSG, ActionOpenProgFolder.TITLE_MSG);
        if (confirmDo.equals("ok")) {
            try {
                Process execOpen = Runtime.getRuntime().exec(MessageFormat.format("explorer \"{0}\n", workingNowPathRoot));
            }
            catch (IOException e) {
                assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
            }
        }
        else {
            try {
                thrNewUserChangeExceptionTest();
            }
            catch (UserChangedHisMindException e) {
                Assert.assertTrue(e.getMessage().contains(this.getClass().getSimpleName()));
                System.out.println(MessageFormat.format("{0} = {1}", e.getClass().getSimpleName(), e.getMessage()));
            }
        }
    }
    
    @Contract(" -> fail")
    private void thrNewUserChangeExceptionTest() {
        throw new UserChangedHisMindException("ActionOpenProgFolderTest");
    }
}