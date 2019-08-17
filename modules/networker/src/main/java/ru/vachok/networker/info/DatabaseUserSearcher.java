// Copyright (c) all rights. http://networker.vachok.ru 2019.

package ru.vachok.networker.info;


import ru.vachok.networker.AppComponents;
import ru.vachok.networker.ConstantsFor;
import ru.vachok.networker.accesscontrol.NameOrIPChecker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 @since 16.08.2019 (10:32) */
public class DatabaseUserSearcher extends DatabaseInfo {
    
    
    private String aboutWhat;
    
    public DatabaseUserSearcher(String userOrPc) {
        this.aboutWhat = userOrPc;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DatabaseUserSearcher{");
        sb.append("aboutWhat='").append(aboutWhat).append('\'');
        sb.append('}');
        return sb.toString();
    }
    
    @Override
    public String getUserByPCNameFromDB(String pcName) {
        InformationFactory informationFactory = InformationFactory.getInstance(InformationFactory.TYPE_PCINFO);
        return informationFactory.getInfoAbout(pcName);
    }
    
    @Override
    public String getInfoAbout(String aboutWhat) {
        this.aboutWhat = aboutWhat;
        String retStr;
        if (new NameOrIPChecker(aboutWhat).isLocalAddress()) {
            retStr = new CurrentPCUser().getInfoAbout(aboutWhat);
        }
        else {
            retStr = getInfo();
        }
        return retStr;
    }
    
    @Override
    public String getInfo() {
        StringBuilder stringBuilder = new StringBuilder();
        try (Connection connection = new AppComponents().connection(ConstantsFor.DBBASENAME_U0466446_VELKOM)) {
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `pcuser` WHERE `userName` LIKE ? LIMIT 20")) {
                preparedStatement.setString(1, new StringBuilder().append("%").append(aboutWhat).append("%").toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        stringBuilder.append(resultSet.getString(ConstantsFor.DBFIELD_PCNAME)).append(" : ").append(resultSet.getString(ConstantsFor.DB_FIELD_USER))
                            .append("\n");
                    }
                }
            }
        }
        catch (SQLException e) {
            stringBuilder.append(e.getMessage());
        }
        return stringBuilder.toString();
    }
}
