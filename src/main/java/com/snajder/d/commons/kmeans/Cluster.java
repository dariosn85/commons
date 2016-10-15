package com.snajder.d.commons.kmeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents generic data Cluster.
 * <p>
 * Cluster does not keep the actual data, but keeps indexes from a list of data.
 * </p>
 *
 * @param <T>
 *            the type of data
 */
public class Cluster<T> {
	private List<Integer> indexes;
	private T center;
	private Double clusterError;

	/**
	 * Constructs cluster with specified center.
	 * 
	 * @param center
	 *            - the initial center
	 */
	public Cluster(T center) {
		this.indexes = new ArrayList<Integer>();
		this.center = center;
	}

	/**
	 * Clears data indexes.
	 */
	protected void clearIndexes() {
		this.indexes.clear();
	}

	/**
	 * Adds the instance index (index of data) to the cluster.
	 * 
	 * @param index
	 *            - the index of data
	 */
	public void addInstanceIndexToCluster(int index) {
		this.indexes.add(index);
	}

	/**
	 * Gets the unmodifiable list of data indexes.
	 * 
	 * @return the data indexes
	 */
	public List<Integer> getDataIndexes() {
		return Collections.unmodifiableList(this.indexes);
	}

	/**
	 * Gets the center of the cluster.
	 * 
	 * @return the cluster center
	 */
	public T getCenter() {
		return this.center;
	}

	/**
	 * Sets the center of the cluster.
	 * 
	 * @param center
	 *            - the cluster center
	 */
	protected void setCenter(T center) {
		this.center = center;
	}

	/**
	 * Gets the error of the cluster value. Usually is this SSE (Sum of Squared
	 * Errors).
	 * 
	 * @see AbstractKMeans#enableErrorCalculation(boolean)
	 * @return the error or {@code null} if error was not calculated
	 */
	public Double getError() {
		return clusterError;
	}

	/**
	 * Sets cluster's error. Usually this will be SSE (Sum of Squared Errors).
	 * 
	 * @see AbstractKMeans#enableErrorCalculation(boolean)
	 * @param sse
	 *            - The SSE
	 */
	public void setError(Double error) {
		this.clusterError = error;
	}

	/**
	 * Gets the size of the cluster.
	 * 
	 * @return the cluster size
	 */
	public int size() {
		return this.indexes.size();
	}

	@Override
	public String toString() {
		return "Cluster [" + (this.center == null ? "null" : this.center.toString()) + "]";
	}
}
