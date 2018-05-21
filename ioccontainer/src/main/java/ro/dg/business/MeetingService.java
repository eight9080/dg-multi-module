package ro.dg.business;

public class MeetingService implements IMeetingService {
    public String[] getAttendees() {
        return new String[]{"Alice", "Rob"};
    }
}
