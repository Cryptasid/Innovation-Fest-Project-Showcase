import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    public void add(Vehicle v) throws SQLException {
        String sql = "INSERT INTO tbl_vehicle (vehicle_id, vehicle_type, registration_number, vehicle_status) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getId());
            ps.setString(2, v.getType());
            ps.setString(3, v.getRegistrationNumber());
            ps.setString(4, v.getStatus());
            ps.executeUpdate();
        }
    }

    public void update(Vehicle v) throws SQLException {
        String sql = "UPDATE tbl_vehicle SET vehicle_type=?, registration_number=?, vehicle_status=? WHERE vehicle_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, v.getType());
            ps.setString(2, v.getRegistrationNumber());
            ps.setString(3, v.getStatus());
            ps.setString(4, v.getId());
            ps.executeUpdate();
        }
    }

    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM tbl_vehicle WHERE vehicle_id=? AND vehicle_status <> 'Dispatched'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("This vehicle cannot be deleted while it is dispatched.");
            }
        }
    }

    public List<Vehicle> getAll() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT vehicle_id, vehicle_type, registration_number, vehicle_status FROM tbl_vehicle ORDER BY vehicle_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Vehicle(rs.getString("vehicle_id"), rs.getString("vehicle_type"),
                        rs.getString("registration_number"), rs.getString("vehicle_status")));
            }
        }
        return list;
    }

    public List<Vehicle> getAvailable() throws SQLException {
        List<Vehicle> list = new ArrayList<>();
        String sql = "SELECT vehicle_id, vehicle_type, registration_number, vehicle_status FROM tbl_vehicle WHERE vehicle_status='Available' ORDER BY vehicle_id";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Vehicle(rs.getString("vehicle_id"), rs.getString("vehicle_type"),
                        rs.getString("registration_number"), rs.getString("vehicle_status")));
            }
        }
        return list;
    }

    public int count(String status) throws SQLException {
        String sql = status == null ? "SELECT COUNT(*) FROM tbl_vehicle" :
                "SELECT COUNT(*) FROM tbl_vehicle WHERE vehicle_status=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (status != null) ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }
}
