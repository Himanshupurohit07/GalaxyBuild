package com.sparc.wc.integration.f21.domain;

import java.util.Objects;

/**
 * Params expected for pulling Colorway Ids as per defined for SCM integration.
 * 
 * @author Acnovate
 */
public class SCMColorwayIdsParams {
	
	private long from;
	private long to;

	public SCMColorwayIdsParams() {
		from = -1;
		to = -1;
	}

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SCMColorwayIdsParams other = (SCMColorwayIdsParams) obj;
		return from == other.from && to == other.to;
	}

	@Override
	public String toString() {
		return "SCMColorwayIdsParams [from=" + from + ", to=" + to + "]";
	}

}
