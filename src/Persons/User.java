package Persons;

/**
 *
 * @author harera
 */
import static DataBase.MyConnection.con;
import static Encryption.MyEncryption.encryptPassword;
import static java.lang.System.out;
import java.sql.*;

public class User {

    public static Boolean checkUsername(String username) {
        var query = "select username from user where username = ?";
        PreparedStatement ps;
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            out.println(e);
        }
        return false;
    }

    public static Boolean checkPassword(String username, String password) {
        var encrPassword = encryptPassword(password);
        var query = "select username from user where username = ? and password = ?";
        PreparedStatement ps;
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, encrPassword);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            out.println(e);
        }
        return false;
    }

    public static Boolean insertDoctor(String username, String password, String name) {
        var encrPassword = encryptPassword(password);
        var query = "insert into user values (?,?,?,?)";
        PreparedStatement ps;
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, encrPassword);
            ps.setBoolean(3, true);
            ps.setBoolean(4, false);
            ps.execute();
        } catch (SQLException e) {
            out.println(e);
        }

        query = "insert into doctor (username,name) values (?,?);";
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, name);
            return ps.execute();
        } catch (SQLException e) {
            out.println(e);
        }

        return false;
    }

    public static Boolean insertStudent(String username, String password, String name) {
        var encrPassword = encryptPassword(password);
        var query = "insert  into user values (?,?,?,?)";
        PreparedStatement ps;
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, encrPassword);
            ps.setBoolean(3, false);
            ps.setBoolean(4, false);
            ps.execute();
        } catch (SQLException e) {
            out.println(e);
        }

        query = "insert into student (username,name) values (?,?);";
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, name);
            return ps.execute();
        } catch (SQLException e) {
            out.println(e);
        }

        return false;
    }

    public static Boolean insertTeacher(String username, String password, String name) {
        var encrPassword = encryptPassword(password);
        var query = "insert into user values (?,?,?,?)";
        PreparedStatement ps;
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, encrPassword);
            ps.setBoolean(3, false);
            ps.setBoolean(4, true);
            ps.execute();
        } catch (SQLException e) {
            out.println(e);
        }

        query = "insert into TA (username,name) values (?,?);";
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            ps.setString(2, name);
            return ps.execute();
        } catch (SQLException e) {
            out.println(e);
        }

        return false;
    }

    public static Boolean isTA(String username) {
        var query = "select isTA from user where username = ?";
        PreparedStatement ps;
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("isTA");
            }
        } catch (SQLException e) {
            out.println(e);
        }
        return false;
    }

    public static Boolean isDoctor(String username) {
        var query = "select isdoctor from user where username = ?";
        PreparedStatement ps;
        try {
            ps = con().prepareStatement(query);
            ps.setString(1, username);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean("isdoctor");
            }
        } catch (SQLException e) {
            out.println(e);
        }
        return false;
    }

}
