package com.conveyal.gtfs.validator.service.impl;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

import com.conveyal.gtfs.GTFSFeed;
import com.conveyal.gtfs.model.Agency;
import com.conveyal.gtfs.model.Route;
import com.conveyal.gtfs.model.Service;
import com.conveyal.gtfs.model.Stop;
import com.conveyal.gtfs.model.StopTime;
import com.conveyal.gtfs.model.Trip;
import com.conveyal.gtfs.validator.model.Statistic;
import com.conveyal.gtfs.validator.service.StatisticsService;

/**
 * Retrieves a base set of statistics from the GTFS.
 *
 */
public class FeedStats implements StatisticsService {

    private GTFSFeed feed = null;

    public FeedStats(GTFSFeed f) {
        feed = f;
    }

    public Integer getAgencyCount() {
        return feed.agency.size();
    }

    public Integer getRouteCount() {
        return feed.routes.size();
    }

    public Integer getTripCount() {
        return feed.trips.size();
    }

    public Integer getStopCount() {
        return feed.stops.size();
    }

    public Integer getStopTimesCount() {
        return feed.stop_times.size();
    }

    // calendar date range start/end assume a service calendar based schedule
    // returns null for schedules without calendar service schedules

    public LocalDate getCalendarServiceRangeStart() {

        int startDate = 0;
        for (Service service : feed.services.values()) {

            if (startDate == 0
                    || service.calendar.start_date < startDate)
                startDate = service.calendar.start_date;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(String.valueOf(startDate), formatter);
    }

    public LocalDate getCalendarServiceRangeEnd() {

        int endDate = 0;

        for (Service service : feed.services.values()) {

            if (endDate == 0
                    || service.calendar.end_date > endDate)
                endDate = service.calendar.end_date;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(String.valueOf(endDate), formatter);
    }

    public LocalDate getCalendarDateStart() {
        LocalDate startDate = null;
        for (Service service : feed.services.values()) {
            for (LocalDate date : service.calendar_dates.keySet()) {
                if (startDate == null
                        || date.isBefore(startDate))
                startDate = date;
            }
        }
        return startDate;
    }

    public LocalDate getCalendarDateEnd() {
        LocalDate endDate = null;
        for (Service service : feed.services.values()) {
            for (LocalDate date : service.calendar_dates.keySet()) {
                if (endDate == null
                        || date.isAfter(endDate))
                    endDate = date;
            }
        }
        return endDate;
    }

    public Collection<Agency> getAllAgencies() {
        return feed.agency.values();
    }

    public Integer getRouteCount(String agencyId) {
        int count = 0;
        for (Route route : feed.routes.values()) {
            if (agencyId.equals(route.agency.agency_id)) {
                count++;
            }
        }
        return count;
    }

    public Integer getTripCount(String agencyId) {
        int count = 0;
        for (Trip trip : feed.trips.values()) {
            if (agencyId.equals(trip.route.agency.agency_id)) {
                count++;
            }
        }
        return count;
    }

    public Integer getStopCount(String agencyId) {
        int count = 0;
        for (Stop stop : feed.stops.values()) {
//            AgencyAndId id = stop.stop_id;
//            if (agencyId.equals(id.getAgencyId())) {
                count++;
//            }
        }
        return count;
    }

    public Integer getStopTimesCount(String agencyId) {
        int count = 0;
        for (StopTime stopTime : feed.stop_times.values()) {
            if (agencyId.equals(feed.trips.get(stopTime.trip_id).route.agency.agency_id)) {
                count++;
            }
        }
        return count;
    }

    public LocalDate getCalendarServiceRangeStart(String agencyId) {
        int startDate = 0;
        for (Service service : feed.services.values()) {
//            if (agencyId.equals(service.agency_id)) {
                if (startDate == 0
                        || service.calendar.start_date < startDate)
                    startDate = service.calendar.start_date;
//            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(String.valueOf(startDate), formatter);
    }

    public LocalDate getCalendarServiceRangeEnd(String agencyId) {
        int endDate = 0;

        for (Service service : feed.services.values()) {
//            if (agencyId.equals(service.agency_id)) {
                if (endDate == 0
                        || service.calendar.end_date > endDate)
                    endDate = service.calendar.end_date;
//            }
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        return LocalDate.parse(String.valueOf(endDate), formatter);
    }

    public LocalDate getCalendarDateStart(String agencyId) {
        LocalDate startDate = null;
        for (Service service : feed.services.values()) {
            for (LocalDate date : service.calendar_dates.keySet()) {
//                if (agencyId.equals(serviceCalendarDate.getServiceId().getAgencyId())) {
                    if (startDate == null
                            || date.isBefore(startDate))
                        startDate = date;
//                }
            }
        }
        return startDate;
    }

    public LocalDate getCalendarDateEnd(String agencyId) {
        LocalDate endDate = null;
        for (Service service : feed.services.values()) {
            for (LocalDate date : service.calendar_dates.keySet()) {
//                if (agencyId.equals(serviceCalendarDate.getServiceId().getAgencyId())) {
                    if (endDate == null
                            || date.isAfter(endDate))
                        endDate = date;
//                }
            }
        }
        return endDate;
    }

    /**
     * Get the bounding box of this GTFS feed.
     * We use a Rectangle2D rather than a Geotools envelope because GTFS is always in WGS 84.
     * Note that stops do not have agencies in GTFS.
     */
    public Rectangle2D getBounds () {
        Rectangle2D ret = null;

        for (Stop stop : feed.stops.values()) {
            if (ret == null) {
                ret = new Rectangle2D.Double(stop.stop_lon, stop.stop_lat, 0, 0);
            }
            else {
                ret.add(new Point2D.Double(stop.stop_lon, stop.stop_lat));
            }
        }

        return ret;
    }

    public Statistic getStatistic(String agencyId) {
        Statistic gs = new Statistic();
        gs.setAgencyId(agencyId);
        gs.setRouteCount(getRouteCount(agencyId));
        gs.setTripCount(getTripCount(agencyId));
        gs.setStopCount(getStopCount(agencyId));
        gs.setStopTimeCount(getStopTimesCount(agencyId));
        gs.setCalendarStartDate(getCalendarDateStart(agencyId));
        gs.setCalendarEndDate(getCalendarDateEnd(agencyId));
        gs.setCalendarServiceStart(getCalendarServiceRangeStart(agencyId));
        gs.setCalendarServiceEnd(getCalendarServiceRangeEnd(agencyId));
        gs.setBounds(getBounds());
        return gs;
    }

    public String getStatisticAsCSV(String agencyId) {
        Statistic s = getStatistic(agencyId);
        return formatStatisticAsCSV(s);

    }

    public static String formatStatisticAsCSV(Statistic s) {
        StringBuffer buff = new StringBuffer();
        buff.append(s.getAgencyId());
        buff.append(",");
        buff.append(s.getRouteCount());
        buff.append(",");
        buff.append(s.getTripCount());
        buff.append(",");
        buff.append(s.getStopCount());
        buff.append(",");
        buff.append(s.getStopTimeCount());
        buff.append(",");
        buff.append(s.getCalendarServiceStart());
        buff.append(",");
        buff.append(s.getCalendarServiceEnd());
        buff.append(",");
        buff.append(s.getCalendarStartDate());
        buff.append(",");
        buff.append(s.getCalendarEndDate());
        return buff.toString();
    }
}
