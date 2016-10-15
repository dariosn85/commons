package com.snajder.d.commons.kmeans;

import java.util.List;

/**
 * Clustering is done in one or more iterations. Implement this interface to
 * listen to iteration events.
 *
 * @param <T>
 *            the type of data
 */
public interface IterationListener<T> {

	/**
	 * Called after current iteration of clustering is finished.
	 * 
	 * @param clusters
	 *            - The list of the Clusters
	 * @param iteration
	 *            - The sequence number of current iteration
	 */
	public void onIterationFinished(List<Cluster<T>> clusters, int iteration);
}
