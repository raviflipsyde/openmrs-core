package org.openmrs.synchronization.engine;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.api.context.Context;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.SynchronizationService;

/**
 * SyncSource to sync OpenMRS tables based on last_changed_local.
 */
public class SyncSourceJournal implements SyncSource {

    private final Log log = LogFactory.getLog(getClass());

    
    // constructor(s)
    public SyncSourceJournal() {
    }

    // properties

    // Public Methods

    /**
     * Std. method for retrieving last sync local; uses global prop, note in case of journal sync, we could just
     * infer this from the status of the journal: last sync local is the date of the last entry with status of
     * pending, or new
     */
    public SyncPoint<Date> getLastSyncLocal() {
        Date val = null;
        String sVal = Context.getSynchronizationService().getGlobalProperty(
                SyncSource.Constants.LAST_SYNC_LOCAL);
        try {
            val = (sVal == null) ? null : new SimpleDateFormat(SyncSource.Constants.DATETIME_MASK)
                    .parse(sVal);
        } catch (ParseException e) {
            log.error("Error DateFormat parsing " + sVal, e);
            throw new SyncException("Error DateFormat parsing " + sVal, e);
        }
        return new SyncPoint<Date>(val);
    }
    public void setLastSyncLocal(SyncPoint p) {
        String sVal = null;

        sVal = (p.getValue() == null) ? null : new SimpleDateFormat(SyncSourceJournal.Constants.DATETIME_MASK)
                .format(p.getValue());
        // use getSynchronizationService to avoid logging this changes to the journal
        Context.getSynchronizationService().setGlobalProperty(
                SyncSource.Constants.LAST_SYNC_LOCAL, sVal);

        return;
    }

    /*
     * Last sync remote: timestamp of the last data *received* from parent
     */
    public SyncPoint<Date> getLastSyncRemote() {
        Date val = null;

        String sVal = Context.getSynchronizationService().getGlobalProperty(
                SyncSource.Constants.LAST_SYNC_REMOTE);
        try {

            val = (sVal == null) ? null : new SimpleDateFormat(SyncSource.Constants.DATETIME_MASK)
                    .parse(sVal);
        } catch (ParseException e) {
            log.error("error DateFormat parsing " + sVal, e);
        }
        return new SyncPoint<Date>(val);
    }
    public void setLastSyncRemote(SyncPoint p) {
        String sVal = null;

        sVal = (p.getValue() == null) ? null : new SimpleDateFormat(SyncSource.Constants.DATETIME_MASK)
                .format(p.getValue());
        // use getSynchronizationService to avoid logging this changes to the journal
        Context.getSynchronizationService().setGlobalProperty( 
                SyncSource.Constants.LAST_SYNC_REMOTE, sVal);

        return;
    }

    // gets the 'next' SyncPoint: in case of timestamp implementation, just get current date/time
    public SyncPoint<Date> moveSyncPoint() {
        
        return new SyncPoint<Date>(new Date());
    }

    // no op: journal has delete records; get 'changed' returns deleted also
    public List<SyncRecord> getDeleted(SyncPoint from, SyncPoint to)
            throws SyncException {
        List<SyncRecord> deleted = new ArrayList<SyncRecord>();

        return deleted;
    }

    // retrieve journal records > 'from' && <= 'to' && record status = 'new' or
    // 'failed'
    public List<SyncRecord> getChanged(SyncPoint from, SyncPoint to)
            throws SyncException {
        List<SyncRecord> changed = new ArrayList<SyncRecord>();

        try {

            Date fromDate = (Date) from.getValue();
            Date toDate = (Date) to.getValue();
            
            SynchronizationService syncService = Context.getSynchronizationService();           
            changed = syncService.getSyncRecordsBetween(fromDate, toDate);

        } catch (Exception e) {
            // TODO
            log.error("error in getChanged ", e);
        }

        return changed;
    }
    
    /*
     * no-op for journal sync -- all changes (deletes, inserts, updates are received in transactional order
     * via applyChanged
     */
    public void applyDeleted(List<SyncRecord> records) throws SyncException {
        
        return;
    }
    
    public void applyChanged(List<SyncRecord> records) throws SyncException {
        
        //TODO - process the changeset
        
        return;
    }

}
