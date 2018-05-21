package ro.dg;


import ro.dg.ioc.Container;
import ro.dg.ioc.IoCException;

public class Main {

    public static void main(String[] args) throws IoCException {
        Container container = new Container("config.json");
        ro.dg.business.IMeeting meeting = container.resolve(ro.dg.business.IMeeting.class);
        meeting.getMeeting();

        for (String attendee : meeting.getAttendees()){
            System.out.println(attendee);
        }


    }
}
