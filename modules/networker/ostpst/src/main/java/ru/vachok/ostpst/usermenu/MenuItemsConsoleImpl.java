// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.ostpst.usermenu;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.ostpst.ConstantsFor;
import ru.vachok.ostpst.MakeConvert;
import ru.vachok.ostpst.fileworks.ConverterImpl;
import ru.vachok.ostpst.utils.FileSystemWorker;
import ru.vachok.ostpst.utils.TForms;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;


/**
 @since 16.05.2019 (12:06) */
class MenuItemsConsoleImpl implements MenuItems {
    
    
    private static UserMenu userMenu = new MenuConsoleLocal();
    
    private static MakeConvert makeConvert = null;
    
    private static long folderID = 0L;
    
    private MessageToUser messageToUser = new MessageCons(getClass().getSimpleName());
    
    private String fileName;
    
    public MenuItemsConsoleImpl(String fileName) {
        this.fileName = fileName;
    }
    
    @Override public void askUser() {
        try (Scanner scanner = new Scanner(System.in)) {
            showSecondStage();
            askUser(scanner, fileName);
        }
        catch (Exception e) {
            messageToUser.error(FileSystemWorker.error(getClass().getSimpleName() + ".askUser", e));
            new MenuConsoleLocal(null).showMenu();
        }
    }
    
    @Override public void showSecondStage() {
        System.out.println("What should I do with this file?");
        System.out.println("1. Save contacts to csv");
        System.out.println("2. Show contacts");
        System.out.println("3. Show folders");
        System.out.println("4. Write folder names to disk");
        System.out.println("5. Parse object");
        System.out.println("6. Show message subjects");
        System.out.println("7. Copy file");
        System.out.println("8. Search message");
        System.out.println("0. Exit");
        System.out.println("Choose: ");
    }
    
    private void ansIsOneSaveContToCSV() {
        System.out.println("Enter name of csv, for contacts save:");
        
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String csvFileName = scanner.nextLine();
                MakeConvert converter = new ConverterImpl(fileName);
                String saveContacts = converter.saveContacts(csvFileName);
                messageToUser.warn(saveContacts);
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    private static void askUser(Scanner scanner, String fileName) throws IOException {
        makeConvert = new ConverterImpl(fileName);
        while (scanner.hasNextInt()) {
            int userAns = scanner.nextInt();
            if (userAns == 1) {
                new MenuItemsConsoleImpl(fileName).ansIsOneSaveContToCSV();
            }
            else if (userAns == 2) {
                MakeConvert makeConvert = new ConverterImpl(fileName);
                makeConvert.showContacts();
                new MenuConsoleLocal(fileName).showMenu();
            }
            else if (userAns == 3) {
                MakeConvert makeConvert = new ConverterImpl(fileName);
                System.out.println(makeConvert.showListFolders());
                new MenuConsoleLocal(fileName).showMenu();
            }
            else if (userAns == 4) {
                MakeConvert makeConvert = new ConverterImpl(fileName);
                makeConvert.getDequeFolderNamesAndWriteToDisk();
                new MenuConsoleLocal(fileName).showMenu();
            }
            else if (userAns == 5) {
                new MenuItemsConsoleImpl(fileName).ansIsFiveParseByID();
            }
            else if (userAns == 6) {
                new MenuItemsConsoleImpl(fileName).ansIsSixGetListMSGSubj(folderID);
            }
            else if (userAns == 7) {
                new MenuItemsConsoleImpl(fileName).ansSevenCopy();
            }
            else if (userAns == 8) {
                new MenuItemsConsoleImpl(fileName).ansEightSearch();
            }
            else if (userAns == 0) {
                userMenu.exitProgram(fileName);
            }
            else if (userAns == 10) {
                userMenu.showMenu();
            }
            else {
                System.out.println("Incorrect choice!");
                new MenuItemsConsoleImpl(fileName).askUser();
            }
        }
        userMenu.showMenu();
    }
    
    private void ansEightSearch() {
        ansEightSearch(0);
    }
    
    private void ansEightSearchSecondStage(Scanner scanner, long folderID) {
        System.out.println("Another message? (0 for back, 6 - show subjects)");
        scanner.reset();
        if (scanner.hasNextLong()) {
            long messageID = scanner.nextLong();
            if (messageID == 0) {
                new MenuConsoleLocal(fileName).showMenu();
            }
            if (messageID == 6) {
                new MenuItemsConsoleImpl(fileName).ansIsSixGetListMSGSubj(folderID);
            }
            System.out.println(makeConvert.searchMessages(folderID, messageID));
            this.ansEightSearchSecondStage(scanner, folderID);
        }
        else if (scanner.hasNextLine()) {
            String subj = scanner.nextLine();
            System.out.println(makeConvert.searchMessages(folderID, subj));
            this.ansEightSearchSecondStage(scanner, folderID);
        }
        else {
            new MenuConsoleLocal(fileName).showMenu();
        }
    }
    
    private void ansIsSixGetListMSGSubj(long folderID) {
        System.out.println("Enter folder id: ");
        if (folderID == 0) {
            try (Scanner scanner = new Scanner(System.in)) {
                folderID = scanner.nextLong();
                List<String> subjectWithID = makeConvert.getListMessagesSubjectWithID(folderID);
                System.out.println(new TForms().fromArray(subjectWithID));
                new MenuItemsConsoleImpl(fileName).askUser();
            }
            catch (Exception e) {
                System.out.println(e.getMessage() + "\n\n" + new TForms().fromArray(e));
                new MenuItemsConsoleImpl(fileName).askUser();
            }
        }
        List<String> subjectWithID = makeConvert.getListMessagesSubjectWithID(folderID);
        System.out.println(new TForms().fromArray(subjectWithID));
        this.ansEightSearch(folderID);
    }
    
    private void ansEightSearch(long id) {
        System.out.println("Enter folder ID or 0 to return :");
        try (Scanner scanner = new Scanner(System.in)) {
            if (id > 0) {
                ansEightSearchSecondStage(scanner, id);
            }
            long folderID = -1;
            try {
                folderID = Long.parseLong(scanner.nextLine());
            }
            catch (NumberFormatException e) {
                System.out.println("NumberFormat incorrect:\n");
                System.out.println(e.getMessage());
                new MenuConsoleLocal(fileName).showMenu();
            }
            if (folderID == 0) {
                new MenuConsoleLocal(fileName).showMenu();
            }
            System.out.println("...and message ID or Subject:");
            while (scanner.hasNextLine()) {
                ansEightSearchSecondStage(scanner, folderID);
            }
        }
        catch (NumberFormatException e) {
            System.out.println(e);
            System.out.println(new TForms().fromArray(e));
            new MenuConsoleLocal(fileName).showMenu();
        }
    }
    
    private void ansIsFiveParseByID() {
        System.out.println("Enter object ID: ");
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLong()) {
                long objID = scanner.nextLong();
                MakeConvert converter = new ConverterImpl(fileName);
                String itemsByID = converter.getObjectItemsByID(objID);
                System.out.println(itemsByID);
                new MenuConsoleLocal(fileName).showMenu();
            }
        }
    }
    
    private void ansSevenCopy() {
        System.out.println("New copy? (y/n) (e - exit)");
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(ConstantsFor.FILENAME_PROPERTIES));
            this.fileName = properties.getProperty(ConstantsFor.PR_TMPFILE);
            System.out.println("Your last copy: " + fileName);
            System.out.println("c - continue last");
        }
        catch (IOException e) {
            messageToUser.error(e.getMessage());
            new MenuItemsConsoleImpl(fileName).askUser();
        }
        try (Scanner scanner = new Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String newCP = scanner.nextLine();
                System.out.println(makeConvert.copyierWithSave(newCP));
                this.fileName = Paths.get(".").normalize().toAbsolutePath() + ConstantsFor.SYSTEM_SEPARATOR + "tmp_" + fileName;
                new MenuItemsConsoleImpl(fileName).askUser();
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage() + "\n" + new TForms().fromArray(e));
            new MenuConsoleLocal(fileName).showMenu();
        }
    }
}