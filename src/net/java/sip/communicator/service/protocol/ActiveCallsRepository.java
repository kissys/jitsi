/*
 * SIP Communicator, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.service.protocol;

import java.util.*;

import net.java.sip.communicator.service.protocol.event.*;
import net.java.sip.communicator.util.*;

/**
 * Keeps a list of all calls currently active and maintained by this protocol
 * provider. Offers methods for finding a call by its ID, peer session
 * and others. This class is meant for use by protocol implementations and
 * cannot be accessed from other bundles.
 *
 * @author Emil Ivov
 */
public class ActiveCallsRepository<T extends Call,
                                   U extends AbstractOperationSetBasicTelephony>
    extends CallChangeAdapter
{
    /**
     * The <tt>Logger</tt> used by the <tt>ActiveCallsRepository</tt>
     * class and its instances for logging output.
     */
    private static final Logger logger = Logger
                    .getLogger(ActiveCallsRepository.class.getName());

    /**
     * A table mapping call ids against call instances.
     */
    private Hashtable<String, T> activeCalls
                                    = new Hashtable<String, T>();

    /**
     * The operation set that created us. Instance is mainly used for firing
     * events when necessary.
     */
    private final U parentOperationSet;

    /**
     * Creates a new instance of this repository.
     *
     * @param opSet a reference to the
     * <tt>AbstractOperationSetBasicTelephony</tt> extension that created us.
     */
    public ActiveCallsRepository(U opSet)
    {
        this.parentOperationSet = opSet;
    }

    /**
     * Adds the specified call to the list of calls tracked by this repository.
     * @param call CallSipImpl
     */
    public void addCall(T call)
    {
        activeCalls.put(call.getCallID(), call);
        call.addCallChangeListener(this);
    }

    /**
     * If <tt>evt</tt> indicates that the call has been ended we remove it from
     * the repository.
     * @param evt the <tt>CallChangeEvent</tt> instance containing the source
     * calls and its old and new state.
     */
    public void callStateChanged(CallChangeEvent evt)
    {
        if(evt.getEventType().equals(CallChangeEvent.CALL_STATE_CHANGE)
        && evt.getNewValue().equals(CallState.CALL_ENDED))
        {
            T sourceCall =
                this.activeCalls.remove(evt.getSourceCall().getCallID());

            if (logger.isTraceEnabled())
                logger.trace("Removing call " + sourceCall + " from the list of "
                        + "active calls because it entered an ENDED state");

            this.parentOperationSet.fireCallEvent(
                CallEvent.CALL_ENDED, sourceCall);
        }
    }

    /**
     * Returns an iterator over all currently active (non-ended) calls.
     *
     * @return an iterator over all currently active (non-ended) calls.
     */
    public Iterator<T> getActiveCalls()
    {
        synchronized(activeCalls)
        {
            return new LinkedList<T>(activeCalls.values()).iterator();
        }
    }

    /**
     * Returns the number of calls currently tracked by this repository.
     *
     * @return the number of calls currently tracked by this repository.
     */
    public int getActiveCallCount()
    {
        synchronized (activeCalls)
        {
            return this.activeCalls.size();
        }
    }

}