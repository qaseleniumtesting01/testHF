// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.accesscontrol.common;


import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.TForms;
import ru.vachok.networker.fileworks.FileSystemWorker;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


/**
 @see CommonRightsParsing
 @since 04.07.2019 (9:47) */
public class CommonRightsParsingTest {
    
    
    private long linesLimit = Long.MAX_VALUE;
    
    private int countDirectories;
    
    @Test
    public void realRunTest() {
        CommonRightsParsing commonRightsParsing = new CommonRightsParsing("02", 1000);
        Map<Path, List<String>> pathListMap = commonRightsParsing.rightsWriterToFolderACL();
        pathListMap.forEach((key, value)->{
            System.out.println(key);
            System.out.println(new TForms().fromArray(value, false));
        });
    }
    
    @Test(enabled = false)
    public void rightsWriterToFolderACL() {
        
        List<String> fileRights = readRights();
        
        if (Long.MAX_VALUE > linesLimit) {
            Assert.assertTrue(fileRights.size() == linesLimit);
        }
        
        Map<Path, List<String>> folderRightsMap = mapFoldersRights(fileRights);
        Assert.assertFalse(folderRightsMap.isEmpty());
    }
    
    @Test
    public void writeACLFileTest() {
        CommonRightsParsing commonRightsParsing = new CommonRightsParsing("18_Петровка", 1000);
        Map<Path, List<String>> pathListMap = commonRightsParsing.rightsWriterToFolderACL();
        Assert.assertTrue(new File("\\\\srv-fs\\Common_new\\01_Дирекция\\18_Петровка\\Внутренняя\\Проект реставрации 2 этажа\\ПРОЕКТ свод\\4. Обмерные чертежи_фасады\\folder_acl.txt")
            .lastModified() > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
    }
    
    private Map<Path, List<String>> mapFoldersRights(@NotNull List<String> rights) {
        Map<Path, List<String>> mapRights = new ConcurrentHashMap<>();
        rights.stream().parallel().forEach(line->parseLine(line, mapRights));
        return mapRights;
    }
    
    private void parseLine(@NotNull String line, Map<Path, List<String>> mapRights) {
        try {
            String[] splitRights = line.split("\\Q | ACL: \\E");
            Path folderPath = Paths.get(splitRights[0]);
            splitRights[1] = splitRights[1].replaceFirst("\\Q[\\E", "").replaceFirst("\\Q]\\E", "");
            if (Files.isDirectory(folderPath)) {
                pathIsDirMapping(splitRights, mapRights, folderPath);
            }
        }
        catch (IndexOutOfBoundsException | InvalidPathException | IOException ignore) {
            //
        }
    }
    
    private void pathIsDirMapping(@NotNull String[] splitRights, @NotNull Map<Path, List<String>> mapRights, Path folderPath) throws IndexOutOfBoundsException, IOException {
        String acls = splitRights[1];
        String[] aclsArray = acls.split(", ");
        mapRights.put(folderPath, Arrays.asList(aclsArray));
        writeACLToFile(folderPath, aclsArray);
    }
    
    private void writeACLToFile(Path folderPath, String[] aclsArray) throws IOException {
        String fileFullPath = folderPath + "\\" + ConstantsFor.FILENAME_FOLDERACLTXT;
        Files.deleteIfExists(Paths.get(fileFullPath));
        FileSystemWorker.writeFile(fileFullPath, Arrays.stream(aclsArray));
        Path setAttribute = Files.setAttribute(Paths.get(fileFullPath), ConstantsFor.ATTRIB_HIDDEN, true);
        System.out.println("dos:hidden set " + setAttribute + ".\n total dirs = " + this.countDirectories++);
        Assert.assertTrue(setAttribute.toFile().exists());
    }
    
    private List<String> readRights() {
        List<String> rightsListFromFile = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream("\\\\srv-fs\\Common_new\\14_ИТ_служба\\Внутренняя\\common.rgh");
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream, ConstantsFor.CP_WINDOWS_1251);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            bufferedReader.lines().limit(linesLimit).forEach(rightsListFromFile::add);
        }
        catch (IOException e) {
            Assert.assertNull(e, e.getMessage() + "\n" + new TForms().fromArray(e, false));
        }
        return rightsListFromFile;
    }
}