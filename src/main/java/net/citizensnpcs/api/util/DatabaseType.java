/*
 * CitizensAPI
 * Copyright (C) 2012 CitizensDev <http://citizensnpcs.net>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package net.citizensnpcs.api.util;

import net.citizensnpcs.api.util.DatabaseStorage.Table;

public enum DatabaseType {
    H2("org.h2.Driver"),
    MYSQL("com.mysql.jdbc.Driver"),
    POSTGRE("org.postgresql.Driver"),
    SQLITE("org.sqlite.JDBC") {
        @Override
        public String[] prepareForeignKeySQL(Table from, Table to, String columnName) {
            return new String[] { String.format(
                    "ALTER TABLE `%s` ADD COLUMN `%s` %s REFERENCES `%s`(`%s`) ON DELETE CASCADE", from.name,
                    columnName, to.primaryKeyType, to.name, to.primaryKey) };
        }
    };
    private final String driver;
    private boolean loaded = false;

    DatabaseType(String driver) {
        this.driver = driver;
    }

    public boolean load() {
        if (loaded)
            return true;
        if (DatabaseStorage.loadDriver(DatabaseStorage.class.getClassLoader(), driver))
            loaded = true;
        return loaded;
    }

    public String[] prepareForeignKeySQL(Table from, Table to, String columnName) {
        String[] sql = new String[2];
        sql[0] = String.format("ALTER TABLE `%s` ADD `%s` %s", from.name, columnName, to.primaryKeyType);
        sql[1] = String.format("ALTER TABLE `%s` ADD FOREIGN KEY (`%s`) REFERENCES `%s`(`%s`) ON DELETE CASCADE",
                from.name, columnName, to.name, to.primaryKey);
        return sql;
    }

    public static DatabaseType match(String driver) {
        for (DatabaseType type : DatabaseType.values()) {
            if (type.name().toLowerCase().contains(driver)) {
                return type;
            }
        }
        return null;
    }
}
