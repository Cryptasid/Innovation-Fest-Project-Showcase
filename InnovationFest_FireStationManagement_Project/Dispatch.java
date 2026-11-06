public class Dispatch {
    private int id;
    private String firefighterId;
    private int incidentId;
    private Date dispatchDate;

    public Dispatch(int id, String firefighterId, int incidentId, Date dispatchDate) {
        this.id = id;
        this.firefighterId = firefighterId;
        this.incidentId = incidentId;
        this.dispatchDate = dispatchDate;
    }

    public int getId() { return id; }
    public String getFirefighterId() { return firefighterId; }
    public int getIncidentId() { return incidentId; }
    public Date getDispatchDate() { return dispatchDate; }
}
