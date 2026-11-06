import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class IncidentDAO {
    public int add(Incident i) throws SQLException {
        String sql = "INSERT INTO tbl_incident " +
                "(in_threat,in_street,in_city,incident_date,in_severity,in_status,latitude,longitude) " +
                "VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getThreat());
            ps.setString(2, i.getStreet());
            ps.setString(3, i.getCity());
            ps.setDate(4, java.sql.Date.valueOf(i.getIncidentDate().getDate()));
            ps.setString(5, i.getSeverity().toString());
            ps.setString(6, i.getStatus().toString());
            setNullableDouble(ps, 7, i.getLatitude());
            setNullableDouble(ps, 8, i.getLongitude());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void update(Incident i) throws SQLException {
        String sql = "UPDATE tbl_incident SET in_threat=?,in_street=?,in_city=?," +
                "incident_date=?,in_severity=?,in_status=?,latitude=?,longitude=? WHERE incident_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, i.getThreat());
            ps.setString(2, i.getStreet());
            ps.setString(3, i.getCity());
            ps.setDate(4, java.sql.Date.valueOf(i.getIncidentDate().getDate()));
            ps.setString(5, i.getSeverity().toString());
            ps.setString(6, i.getStatus().toString());
            setNullableDouble(ps, 7, i.getLatitude());
            setNullableDouble(ps, 8, i.getLongitude());
            ps.setInt(9, i.getId());
            ps.executeUpdate();
        }
    }

    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) ps.setNull(index, Types.DOUBLE);
        else ps.setDouble(index, value);
    }

    private Double readNullableDouble(ResultSet rs, String column) throws SQLException {
        double value = rs.getDouble(column);
        return rs.wasNull() ? null : value;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM tbl_incident WHERE incident_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Object[]> getAll() throws SQLException {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT incident_id,in_severity,in_threat,in_street,in_city," +
                "incident_date,in_status,reported_at,latitude,longitude FROM tbl_incident " +
                "ORDER BY FIELD(in_status,'Active','Investigating','Resolved'), " +
                "FIELD(in_severity,'Critical','High','Medium','Low'), reported_at DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("incident_id"), rs.getString("in_severity"),
                        rs.getString("in_threat"), rs.getString("in_street"),
                        rs.getString("in_city"), rs.getDate("incident_date").toLocalDate(),
                        rs.getString("in_status"), rs.getTimestamp("reported_at"),
                        readNullableDouble(rs, "latitude"), readNullableDouble(rs, "longitude")
                });
            }
        }
        return list;
    }

    /** Existing non-resolved incident choices used by the Dispatch form. */
    public List<Incident> getAllObjects() throws SQLException {
        List<Incident> list = new ArrayList<>();
        String sql = "SELECT incident_id,in_threat,in_street,in_city,incident_date," +
                "in_severity,in_status,latitude,longitude FROM tbl_incident WHERE in_status <> 'Resolved' " +
                "ORDER BY FIELD(in_severity,'Critical','High','Medium','Low'), incident_id DESC";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(readIncident(rs));
        }
        return list;
    }

    /** Returns all incidents with valid coordinates, regardless of operational status. */
    public List<Incident> getAllGeolocated() throws SQLException {
    List<Incident> list = new ArrayList<>();

    String sql = "SELECT incident_id,in_threat,in_street,in_city,incident_date," +
            "in_severity,in_status,latitude,longitude FROM tbl_incident " +
            "WHERE latitude IS NOT NULL AND longitude IS NOT NULL " +
            "AND latitude BETWEEN -90 AND 90 " +
            "AND longitude BETWEEN -180 AND 180 " +
            "AND in_status <> 'Resolved' " +
            "ORDER BY incident_id DESC";

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Incident incident = readIncident(rs);

            if (incident.hasCoordinates()) {
                list.add(incident);
            }
        }
    }

    return list;
}

    private Incident readIncident(ResultSet rs) throws SQLException {
        Double latitude = readNullableDouble(rs, "latitude");
        Double longitude = readNullableDouble(rs, "longitude");
        // A partial pair is not a usable location; treat it like an older unmapped record.
        if (latitude == null || longitude == null
                || !Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            latitude = null;
            longitude = null;
        }
        return new Incident(
                rs.getInt("incident_id"), rs.getString("in_threat"),
                rs.getString("in_street"), rs.getString("in_city"),
                new Date(rs.getDate("incident_date").toLocalDate()),
                IncidentSeverity.fromString(rs.getString("in_severity")),
                IncidentStatus.fromString(rs.getString("in_status")), latitude, longitude
        );
    }

    public int countActive() throws SQLException { return count("WHERE in_status='Active'"); }
    public int countCritical() throws SQLException { return count("WHERE in_severity='Critical' AND in_status <> 'Resolved'"); }

    private int count(String filter) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tbl_incident " + filter;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
