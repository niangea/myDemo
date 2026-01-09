package com.nian.myddemoadmin;// DatabaseTest.java
import java.sql.*;

public class DatabaseTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://192.168.206.247:3306/";
        String user = "lihenian";
        String password = "asdASD10086.";

        try {
            // 测试MySQL服务器连接
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ 成功连接到MySQL服务器");

            // 检查数据库是否存在
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet rs = meta.getCatalogs();

            boolean userDbExists = false;
            boolean goodsDbExists = false;

            while (rs.next()) {
                String dbName = rs.getString(1);
                if ("myDemo_user".equals(dbName)) {
                    userDbExists = true;
                }
                if ("myDemo_goods".equals(dbName)) {
                    goodsDbExists = true;
                }
            }

            System.out.println("myDemo_user 数据库: " + (userDbExists ? "✅ 存在" : "❌ 不存在"));
            System.out.println("myDemo_goods 数据库: " + (goodsDbExists ? "✅ 存在" : "❌ 不存在"));

            rs.close();
            conn.close();

        } catch (SQLException e) {
            System.out.println("❌ 连接失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}