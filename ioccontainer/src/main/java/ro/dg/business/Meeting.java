package ro.dg.business;

public class Meeting implements IMeeting {

    private IMeetingService meetingService;

    private String[] attendees;

    public Meeting(IMeetingService meetingService) {
        this.meetingService = meetingService;
    }

    public String[] getAttendees() {
        return this.attendees;
    }

    public void setAttendes(String[] attendes) {
        this.attendees=attendes;
    }

    public void getMeeting() {
        attendees = this.meetingService.getAttendees();
    }
}
