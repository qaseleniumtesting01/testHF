// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.vachok.networker.accesscontrol.inetstats.InternetUse;
import ru.vachok.networker.ad.PCUserNameHTMLResolver;
import ru.vachok.networker.ad.user.ADUser;
import ru.vachok.networker.componentsrepo.exceptions.TODOException;
import ru.vachok.networker.restapi.MessageToUser;
import ru.vachok.networker.restapi.message.MessageLocal;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.StringJoiner;


/**
 @see ru.vachok.networker.info.PCInfoTest
 @since 13.08.2019 (17:15) */
public abstract class PCInfo implements InformationFactory {
    
    
    private static final MessageToUser messageToUser = new MessageLocal(PCInfo.class.getSimpleName());
    
    @Contract("_ -> new")
    public static @NotNull PCInfo getDatabaseInfo(String userOrPc) {
        return new PCUserNameHTMLResolver(userOrPc);
    }
    
    public List<ADUser> getADUsers(@NotNull File csvFile) {
        throw new TODOException("19.08.2019 (18:54)");
    }
    
    @Contract("_ -> new")
    public static @NotNull PCInfo getLocalInfo(String type) {
        return LocalPCInfo.getInstance(type);
    }
    
    public long getStatsFromDB(String userCred, String sql, String colLabel) throws UnknownHostException {
        long result = 0;
        InetAddress address = InetAddress.getByName(userCred);
        userCred = address.getHostAddress();
        try (Connection connection = InternetUse.MYSQL_DATA_SOURCE.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, userCred);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        result = result + resultSet.getLong(colLabel);
                    }
                    return result;
                }
            }
        }
        catch (SQLException e) {
            messageToUser.error(MessageFormat.format("DatabaseInfo.getStatsFromDB: {0}, ({1})", e.getMessage(), e.getClass().getName()));
            return -1;
        }
    }
    
    @Override
    public abstract String getInfoAbout(String aboutWhat);
    
    @Override
    public abstract void setClassOption(Object classOption);
    
    public abstract String getInfo();
    
    @Override
    public String toString() {
        return new StringJoiner(",\n", PCInfo.class.getSimpleName() + "[\n", "\n]")
            .toString();
    }
    
    public abstract String getUserByPCNameFromDB(String pcName);
}
