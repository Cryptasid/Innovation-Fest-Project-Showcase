import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DispatchDAO {

    public void add(String firefighterId, int incidentId, Date dispatchDate) throws SQLException {
        add(firefighterId, incidentId, dispatchDate, null);
    }

    public void add(String firefighterId, int incidentId, Date dispatchDate, String vehicleId) throws SQLException {
        String sql = "INSERT INTO tbl_dispatch (firefighter_id, incident_id, dispatch_date, vehicle_id) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                if (vehicleId != null) {
                    try (PreparedStatement check = con.prepareStatement(
                            "SELECT vehicle_status FROM tbl_vehicle WHERE vehicle_id=? FOR UPDATE")) {
                        check.setString(1, vehicleId);
                        try (ResultSet rs = check.executeQuery()) {
                            if (!rs.next() || !"Available".equals(rs.getString(1))) {
                                throw new SQLException("The selected vehicle is no longer available.");
                            }
                        }
                    }
                }

                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, firefighterId);
                    ps.setInt(2, incidentId);
                    ps.setDate(3, java.sql.Date.valueOf(dispatchDate.getDate()));
                    ps.setString(4, vehicleId);
                    ps.executeUpdate();
                }

                if (vehicleId != null) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE tbl_vehicle SET vehicle_status='Dispatched' WHERE vehicle_id=?")) {
                        ps.setString(1, vehicleId);
                        ps.executeUpdate();
                    }
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String vehicleId = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT vehicle_id FROM tbl_dispatch WHERE dispatch_id=? FOR UPDATE")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Dispatch not found.");
                        vehicleId = rs.getString("vehicle_id");
                    }
                }
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM tbl_dispatch WHERE dispatch_id=?")) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                releaseVehicle(con, vehicleId);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private void releaseVehicle(Connection con, String vehicleId) throws SQLException {
        if (vehicleId == null) return;
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE tbl_vehicle SET vehicle_status='Available' WHERE vehicle_id=? AND vehicle_status='Dispatched'")) {
            ps.setString(1, vehicleId);
            ps.executeUpdate();
        }
    }

    /**
     * Marks a dispatch as Completed.
     * The related incident is set to Resolved ONLY when
     * no other Active dispatches remain for that incident.
     */
    public void complete(int dispatchId) throws SQLException {
        String findSql =
                "SELECT incident_id, vehicle_id FROM tbl_dispatch WHERE dispatch_id = ? FOR UPDATE";

        String updateDispatch =
                "UPDATE tbl_dispatch SET dispatch_status = 'Completed' WHERE dispatch_id = ?";

        String countActiveSql =
                "SELECT COUNT(*) FROM tbl_dispatch " +
                "WHERE incident_id = ? AND dispatch_status = 'Active'";

        String updateIncident =
                "UPDATE tbl_incident SET in_status = 'Resolved' WHERE incident_id = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            try {
                int incidentId = -1;
                String vehicleId = null;

                // 1. Find the incident of this dispatch
                try (PreparedStatement ps = con.prepareStatement(findSql)) {
                    ps.setInt(1, dispatchId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            incidentId = rs.getInt("incident_id");
                            vehicleId = rs.getString("vehicle_id");
                        }
                    }
                }

                if (incidentId == -1) {
                    throw new SQLException("Dispatch not found.");
                }

                // 2. Mark this dispatch as Completed
                try (PreparedStatement ps = con.prepareStatement(updateDispatch)) {
                    ps.setInt(1, dispatchId);
                    ps.executeUpdate();
                }

                // 3. Release the assigned vehicle, if there is one
                releaseVehicle(con, vehicleId);

                // 4. Check how many Active dispatches are still left for this incident
                int remainingActive = 0;
                try (PreparedStatement ps = con.prepareStatement(countActiveSql)) {
                    ps.setInt(1, incidentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            remainingActive = rs.getInt(1);
                        }
                    }
                }

                // 5. Only resolve the incident if no Active dispatches remain
                if (remainingActive == 0) {
                    try (PreparedStatement ps = con.prepareStatement(updateIncident)) {
                        ps.setInt(1, incidentId);
                        ps.executeUpdate();
                    }
                }

                con.commit();

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public List<Object[]> getAll() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT d.dispatch_id, f.ff_name, i.in_threat, i.in_city, i.in_severity, " +
                "COALESCE(d.vehicle_id, 'No Vehicle') AS vehicle_id, d.dispatch_status, d.dispatched_at " +
                "FROM tbl_dispatch d " +
                "JOIN tbl_firefighter f ON d.firefighter_id = f.firefighter_id " +
                "JOIN tbl_incident i ON d.incident_id = i.incident_id " +
                "ORDER BY d.dispatch_status, d.dispatched_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("dispatch_id"),
                        rs.getString("ff_name"),
                        rs.getString("in_threat"),
                        rs.getString("in_city"),
                        rs.getString("in_severity"),
                        rs.getString("vehicle_id"),
                        rs.getString("dispatch_status"),
                        rs.getTimestamp("dispatched_at")
                });
            }
        }
        return list;
    }

    /** Returns only Active dispatches (for the form table). */
    public List<Object[]> getActive() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT d.dispatch_id, f.ff_name, i.in_threat, i.in_city, i.in_severity, " +
                "COALESCE(d.vehicle_id, 'No Vehicle') AS vehicle_id, d.dispatch_status, d.dispatched_at " +
                "FROM tbl_dispatch d " +
                "JOIN tbl_firefighter f ON d.firefighter_id = f.firefighter_id " +
                "JOIN tbl_incident i ON d.incident_id = i.incident_id " +
                "WHERE d.dispatch_status = 'Active' " +
                "ORDER BY d.dispatched_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("dispatch_id"),
                        rs.getString("ff_name"),
                        rs.getString("in_threat"),
                        rs.getString("in_city"),
                        rs.getString("in_severity"),
                        rs.getString("vehicle_id"),
                        rs.getString("dispatch_status"),
                        rs.getTimestamp("dispatched_at")
                });
            }
        }
        return list;
    }

    public int countActive() throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM tbl_dispatch WHERE dispatch_status='Active'");
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
