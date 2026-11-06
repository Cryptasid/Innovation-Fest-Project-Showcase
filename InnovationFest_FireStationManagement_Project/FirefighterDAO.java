import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FirefighterDAO {
    public void add(Firefighter f) throws SQLException {
        String sql = "INSERT INTO tbl_firefighter (firefighter_id,ff_name,ff_contact,ff_expertise) VALUES (?,?,?,?)";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, f.getId()); ps.setString(2, f.getName()); ps.setString(3, f.getContact()); ps.setString(4, f.getExpertise().toString()); ps.executeUpdate();
        }
    }
    public void update(Firefighter f) throws SQLException {
        String sql = "UPDATE tbl_firefighter SET ff_name=?,ff_contact=?,ff_expertise=? WHERE firefighter_id=?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, f.getName()); ps.setString(2, f.getContact()); ps.setString(3, f.getExpertise().toString()); ps.setString(4, f.getId()); ps.executeUpdate();
        }
    }
    public void delete(String id) throws SQLException {
        String sql1 = "DELETE FROM tbl_dispatch WHERE firefighter_id=?";
        String sql2 = "DELETE FROM tbl_firefighter WHERE firefighter_id=?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement p1 = con.prepareStatement(sql1); PreparedStatement p2 = con.prepareStatement(sql2)) {
                p1.setString(1,id); p1.executeUpdate(); p2.setString(1,id); p2.executeUpdate(); con.commit();
            } catch (SQLException e) { con.rollback(); throw e; }
        }
    }
    public List<Object[]> getAll() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT f.firefighter_id,f.ff_name,f.ff_contact,f.ff_expertise," +
                "CASE WHEN EXISTS (SELECT 1 FROM tbl_dispatch d WHERE d.firefighter_id=f.firefighter_id AND d.dispatch_status='Active') THEN 'Dispatched' ELSE 'Available' END AS status " +
                "FROM tbl_firefighter f ORDER BY f.ff_name";
        try (Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while (rs.next()) list.add(new Object[]{rs.getString("firefighter_id"),rs.getString("ff_name"),rs.getString("ff_contact"),rs.getString("ff_expertise"),rs.getString("status")});
        }
        return list;
    }
    public List<Firefighter> getAvailable() throws SQLException {
        List<Firefighter> list = new ArrayList<>();
        String sql = "SELECT f.firefighter_id,f.ff_name,f.ff_contact,f.ff_expertise FROM tbl_firefighter f " +
                "WHERE NOT EXISTS (SELECT 1 FROM tbl_dispatch d WHERE d.firefighter_id=f.firefighter_id AND d.dispatch_status='Active') ORDER BY f.ff_name";
        try (Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement(sql); ResultSet rs=ps.executeQuery()) {
            while (rs.next()) list.add(new Firefighter(rs.getString("firefighter_id"),rs.getString("ff_name"),rs.getString("ff_contact"),Expertise.fromString(rs.getString("ff_expertise"))));
        }
        return list;
    }
    public int countAll() throws SQLException {
        try (Connection con=DBConnection.getConnection(); PreparedStatement ps=con.prepareStatement("SELECT COUNT(*) FROM tbl_firefighter"); ResultSet rs=ps.executeQuery()) { rs.next(); return rs.getInt(1); }
    }
    public int countAvailable() throws SQLException {
        String sql="SELECT COUNT(*) FROM tbl_firefighter f WHERE NOT EXISTS (SELECT 1 FROM tbl_dispatch d WHERE d.firefighter_id=f.firefighter_id AND d.dispatch_status='Active')";
        try(Connection con=DBConnection.getConnection();PreparedStatement ps=con.prepareStatement(sql);ResultSet rs=ps.executeQuery()){rs.next();return rs.getInt(1);}
    }
}
