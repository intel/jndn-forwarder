/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.jndn.forwarder.api;

import com.intel.jnfd.deamon.fw.FaceTable;
import com.intel.jnfd.deamon.fw.Forwarder;
import com.intel.jnfd.deamon.table.fib.FibEntry;
import com.intel.jnfd.deamon.table.measurement.MeasurementAccessor;
import com.intel.jnfd.deamon.table.pit.PitEntry;
import net.named_data.jndn.Data;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;

/**
 *
 * @author zht
 */
public abstract class Strategy {

	public Strategy(Forwarder forwarder, Name prefix) {
		this.forwarder = forwarder;
		this.prefix = prefix;
	}

	public Name getName() {
		return name;
	}

	/**
	 * trigger after Interest is received
	 *
	 * The Interest: - does not violate Scope - is not looped - cannot be
	 * satisfied by ContentStore - is under a namespace managed by this strategy
	 *
	 * The strategy should decide whether and where to forward this Interest -
	 * If the strategy decides to forward this Interest, invoke
	 * this->sendInterest one or more times, either now or shortly after - If
	 * strategy concludes that this Interest cannot be forwarded, invoke
	 * this->rejectPendingInterest so that PIT entry will be deleted shortly
	 *
	 * @param inFace
	 * @param interest
	 * @param fibEntry
	 * @param pitEntry
	 */
	public abstract void afterReceiveInterest(Face inFace,
			Interest interest,
			FibEntry fibEntry,
			PitEntry pitEntry);

	/**
	 * trigger before PIT entry is satisfied
	 *
	 * This trigger is invoked when an incoming Data satisfies the PIT entry, It
	 * can be invoked even if the PIT entry has already been satisfied.
	 *
	 * In this base class this method does nothing.
	 *
	 * @param pitEntry
	 * @param inFace
	 * @param data
	 */
	public abstract void beforeSatisfyInterest(PitEntry pitEntry,
			Face inFace, Data data);

	/**
	 * trigger before PIT entry expires
	 *
	 * PIT entry expires when InterestLifetime has elapsed for all InRecords,
	 * and it is not satisfied by an incoming Data.
	 *
	 * This trigger is not invoked for PIT entry already satisfied.
	 *
	 * In this base class this method does nothing.
	 *
	 * @param pitEntry
	 */
	public abstract void beforeExpirePendingInterest(PitEntry pitEntry);

	protected void sendInterest(PitEntry pitEntry, Face outFace,
			boolean wantNewNonce) {
		forwarder.onOutgoingInterest(pitEntry, outFace, wantNewNonce);
	}

	protected void rejectPendingInterest(PitEntry pitEntry) {
		forwarder.onInterestReject(pitEntry);
	}

	protected MeasurementAccessor getMeasurementAccessor() {
		return measurementAccessor;
	}

	protected Face getFace(int faceId) {
		return forwarder.getFace(faceId);
	}

	protected FaceTable getFaceTable() {
		return forwarder.getFaceTable();
	}

	public abstract Face[] determineOutgoingFaces(Interest interest, Forwarder forwarder);

	@Override
	public boolean equals(Object o) {
		return name.equals(((Strategy) o).getName());
	}

	private final Name prefix;
	private Name name;
	private Forwarder forwarder;
	private MeasurementAccessor measurementAccessor;
}
