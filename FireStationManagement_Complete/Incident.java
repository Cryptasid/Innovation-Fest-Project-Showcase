public class Incident {
    private int id;
    private String threat;
    private String street;
    private String city;
    private Date incidentDate;
    private IncidentSeverity severity;
    private IncidentStatus status;
    // Nullable so incidents created before the map feature remain valid.
    private Double latitude;
    private Double longitude;

    public Incident(int id, String threat, String street, String city,
                    Date incidentDate, IncidentSeverity severity,
                    IncidentStatus status) {
        this(id, threat, street, city, incidentDate, severity, status, null, null);
    }

    public Incident(int id, String threat, String street, String city,
                    Date incidentDate, IncidentSeverity severity,
                    IncidentStatus status, Double latitude, Double longitude) {
        this.id = id;
        this.threat = threat;
        this.street = street;
        this.city = city;
        this.incidentDate = incidentDate;
        this.severity = severity;
        this.status = status;
        setCoordinates(latitude, longitude);
    }

    public int getId() { return id; }
    public String getThreat() { return threat; }
    public String getStreet() { return street; }
    public String getCity() { return city; }
    public Date getIncidentDate() { return incidentDate; }
    public IncidentSeverity getSeverity() { return severity; }
    public IncidentStatus getStatus() { return status; }
    public Double getLatitude() { return latitude; }
    public Double getLongitude() { return longitude; }

    public void setLatitude(Double latitude) {
        if (latitude != null && (!Double.isFinite(latitude) || latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90.");
        }
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        if (longitude != null && (!Double.isFinite(longitude) || longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180.");
        }
        this.longitude = longitude;
    }

    public void setCoordinates(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Latitude and longitude must both be supplied or both be null.");
        }
        if (latitude != null && (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("The selected map coordinates are invalid.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean hasCoordinates() {
        return latitude != null && longitude != null
                && Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }
}
