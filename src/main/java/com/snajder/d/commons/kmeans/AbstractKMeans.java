package com.snajder.d.commons.kmeans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Abstract implementation of popular machine learning algorithm K-means.
 * <p>
 * Algorithm will organize data into K clusters, where a certain data is placed
 * into cluster with nearest centroid.
 * </p>
 * 
 * <p>
 * Each implementation must provide distance calculation (
 * {@link #calculateDistance(Object, Object)}) and calculation for new center -
 * centroid ({@link #calculateCenter(List, List, int)}).
 * </p>
 * 
 * @see <a href="https://en.wikipedia.org/wiki/K-means_clustering">K-means on
 *      Wiki</a>
 * @param <T>
 *            the type of data on which we will do clustering
 */
public abstract class AbstractKMeans<T> {

	/**
	 * Default minimal cluster difference. See
	 * {@link #getMinClusterDifference()}.
	 */
	private static final Double DEFAULT_MIN_CLUSTER_DIFFERENCE = 0.01;

	/**
	 * Default number of clusters.
	 */
	private static final int DEFAULT_NUMBER_OF_CLUSTERS = 3;

	private Random random;
	private List<Integer> initialCentroids;

	private IterationListener<T> iterationListener;

	private boolean clusterErrorCalculationEnabled = false;
	private Double minClusterDifference = DEFAULT_MIN_CLUSTER_DIFFERENCE;

	/**
	 * Default constructor.
	 */
	public AbstractKMeans() {
		random = new Random(System.currentTimeMillis());
	}

	/**
	 * Constructs k-means using specified {@code random}.
	 * 
	 * @param random
	 *            - the instance of Random
	 */
	public AbstractKMeans(Random random) {
		this.random = random;
	}

	/**
	 * Constructs k-menas using predefined initial centroids.
	 * 
	 * @see AbstractKMeans#getInitialCentroids(List, int)
	 * @param initialCentroids
	 *            - The initial centroids
	 */
	public AbstractKMeans(List<Integer> initialCentroids) {
		this();
		this.initialCentroids = initialCentroids;
	}

	/**
	 * Finds clusters for specified {@code data}.
	 * 
	 * <p>
	 * NOTE: It will try to organize data into 3 clusters (
	 * {@link #DEFAULT_NUMBER_OF_CLUSTERS}).
	 * </p>
	 * 
	 * @param data
	 *            - The data
	 * @return the list of clusters
	 */
	public List<Cluster<T>> findClusters(List<T> data) {
		return findClusters(data, DEFAULT_NUMBER_OF_CLUSTERS);
	}

	/**
	 * Finds clusters for specified {@code data}.
	 * 
	 * @param data
	 *            - The data
	 * @param k
	 *            - The number of desired clusters
	 * @return the list of clusters
	 */
	public List<Cluster<T>> findClusters(List<T> data, int k) {
		List<Cluster<T>> clusters = kmeans(data, k);

		Collections.sort(clusters, new Comparator<Cluster<T>>() {

			public int compare(Cluster<T> o1, Cluster<T> o2) {
				if (o1.size() == o2.size()) {
					return 0;
				}

				if (o2.size() > o1.size()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		return clusters;
	}

	/**
	 * Sets the iteration listener.
	 * 
	 * @param listener
	 *            - the iteration listener
	 */
	public void setIterationListeren(IterationListener<T> listener) {
		this.iterationListener = listener;
	}

	/**
	 * The k-means "core", where we do actual clustering.
	 * 
	 * @param data
	 *            - The data to cluster
	 * @param k
	 *            - The number of clusters
	 * @return the list of found clusters
	 */
	private List<Cluster<T>> kmeans(List<T> data, int k) {
		// find k random centroids (at unique position)
		List<Integer> initialCentroidsIndexes = getInitialCentroids(data, k);

		// generate clusters
		List<Cluster<T>> clusters = new ArrayList<Cluster<T>>();
		for (int i = 0; i < k; i++) {
			// create cluster and place initial centroid into it
			Cluster<T> cluster = new Cluster<T>(data.get(initialCentroidsIndexes.get(i)));

			clusters.add(cluster);
		}

		int iteration = 1;

		// do clustering
		while (true) {

			for (Cluster<T> cluster : clusters) {
				cluster.clearIndexes();
			}

			// for each data
			for (int i = 0; i < data.size(); i++) {
				T value1 = data.get(i);
				// find to which cluster belongs (based on distance)
				int cluster = -1;
				double smallestDistance = Double.MAX_VALUE;
				for (int j = 0; j < k; j++) {
					T value2 = clusters.get(j).getCenter();

					double distance = calculateDistance(value1, value2);
					if (distance < smallestDistance) {
						cluster = j;
						smallestDistance = distance;
					}
				}

				// put data index into cluster
				clusters.get(cluster).addInstanceIndexToCluster(i);
			}

			double diff = 0.0;

			// for each cluster calculate center
			for (int i = 0; i < k; i++) {
				Cluster<T> cluster = clusters.get(i);
				T oldCenter = cluster.getCenter();
				T newCenter = calculateCenter(data, cluster.getDataIndexes(), cluster.getDataIndexes().size());

				cluster.setCenter(newCenter);

				// check distance between old center and new center
				double distance = calculateDistance(oldCenter, newCenter);

				// remember max distance
				diff = diff > distance ? diff : distance;
			}

			// calculate Cluster Error if calculation is enabled
			if (isClusterErrorCalculationEnabled()) {
				calculateClustersError(clusters, data);
			}

			invokeIteraionFinishedListener(clusters, iteration);

			iteration++;

			// if distance is less than MIN CLUSTER DIFFERENCE then stop
			// calculation
			if (diff < getMinClusterDifference()) {
				break;
			}
		}

		return clusters;
	}

	/**
	 * Gets the predefined initial centroids, or pick them randomly.
	 * <p>
	 * NOTE: The default implementation will pick initial centroids randomly.
	 * Override this method to pick initial centroids differently.
	 * </p>
	 * 
	 * @param data
	 *            - The data to cluster
	 * @param k
	 *            - The number of clusters
	 * @return the position indexes of initial centroids from data list
	 */
	protected List<Integer> getInitialCentroids(List<T> data, int k) {
		List<Integer> randomCentroidsIndexes = new ArrayList<Integer>(k);
		if (this.initialCentroids != null) {
			if (k != this.initialCentroids.size()) {
				throw new IllegalArgumentException("Initial centroids length differs from k=" + k);
			}
			randomCentroidsIndexes = initialCentroids;
		} else {
			int count = 0;
			while (count < k) {
				int n = Math.abs(random.nextInt()) % data.size();
				boolean found = false;

				for (int i = 0; i < count; i++) {
					if (n == randomCentroidsIndexes.get(i)) {
						found = true;
						break;
					}
				}

				if (!found) {
					randomCentroidsIndexes.add(n);
					count++;
				}
			}
		}

		return randomCentroidsIndexes;
	}

	/**
	 * Calculates error for all the clusters.
	 * 
	 * @param clusters
	 *            - The clustes
	 * @param data
	 *            - The data
	 */
	private void calculateClustersError(List<Cluster<T>> clusters, List<T> data) {
		for (int i = 0; i < clusters.size(); i++) {
			final Cluster<T> cluster = clusters.get(i);

			final Double error = calculateClusterError(cluster, data);

			cluster.setError(error);
		}
	}

	/**
	 * Calculates cluster error. Default implementation calculates SSE (Sum of
	 * Squared Errors).
	 * <p>
	 * Override this method if you would like to provider your own error
	 * calculation.
	 * </p>
	 * 
	 * @param cluster
	 *            - The cluster for which calculate error
	 * @param data
	 *            - The data
	 * @return the error
	 * 
	 * @see Cluster#getError()
	 */
	protected Double calculateClusterError(Cluster<T> cluster, List<T> data) {
		// default implementation calculates SSE
		return calculateSSE(cluster, data);
	}

	/**
	 * The default cluster error calculation implementation to calculate SSE
	 * (Sum of Squared Errors).
	 * 
	 * @param cluster
	 *            - The cluster
	 * @param data
	 *            - The data
	 * @return the SSE
	 */
	private double calculateSSE(Cluster<T> cluster, List<T> data) {
		double sse = 0.0;
		final T clusterCenter = cluster.getCenter();

		// get it's data
		for (Integer dataIndex : cluster.getDataIndexes()) {
			T instance = data.get(dataIndex);

			// and calculate SSE
			// "error" is distance between cluster mean and certain data
			// instance
			double distance = calculateDistance(clusterCenter, instance);

			// squared value
			sse += distance * distance;
		}

		return sse;
	}

	/**
	 * Invokes iteration finished event.
	 * 
	 * @param clusters
	 *            - The list of clusters
	 * @param iteration
	 *            - The sequence number of the iteration
	 */
	private void invokeIteraionFinishedListener(List<Cluster<T>> clusters, int iteration) {
		if (this.iterationListener != null) {
			this.iterationListener.onIterationFinished(clusters, iteration);
		}
	}

	/**
	 * Indicates whether cluster error calculation is enabled.
	 * 
	 * @return {@code true} if calculation is enabled, otherwise {@code false}
	 */
	public boolean isClusterErrorCalculationEnabled() {
		return this.clusterErrorCalculationEnabled;
	}

	/**
	 * Enabled/disabled cluster error calculation.
	 * 
	 * @param enabled
	 *            if set to {@code true}, calculation is enabled, otherwise it
	 *            is disabled
	 */
	public void enableClusterErrorCalculation(boolean enabled) {
		this.clusterErrorCalculationEnabled = enabled;
	}

	/**
	 * Gets the value of minimal cluster difference.
	 * <p>
	 * Value is used as exit condition. After each iteration new centroid is
	 * calculated for each cluster. If all the distances between new and old
	 * centroids are enough small (less than minimal cluster difference), it
	 * means that we could finish clustering.
	 * </p>
	 * 
	 * @return the minimal cluster difference
	 */
	public Double getMinClusterDifference() {
		return minClusterDifference;
	}

	/**
	 * Sets the value of minimal cluster difference.
	 * <p>
	 * Value is used as exit condition. After each iteration new centroid is
	 * calculated for each cluster. If all the distances between new and old
	 * centroids are enough small (less than minimal cluster difference), it
	 * means that we could finish clustering.
	 * </p>
	 * 
	 * @see #DEFAULT_MIN_CLUSTER_DIFFERENCE
	 * 
	 * @return the minimal cluster difference
	 */
	protected void setMinClusterDifference(Double minClusterDifference) {
		this.minClusterDifference = minClusterDifference;
	}

	/**
	 * Implement this method to provide logic for distance calculation between
	 * two values.
	 * <p>
	 * This method is base for determining which value should be put into which
	 * cluster.
	 * </p>
	 * 
	 * @param value1
	 *            - The first value
	 * @param value2
	 *            - The second value
	 * @return the distance between specified values
	 */
	protected abstract Double calculateDistance(T value1, T value2);

	/**
	 * Implement this method to provide a way for new centroid calculation.
	 * 
	 * @param data
	 *            - The list of all data
	 * @param dataIndexes
	 *            - The list of data indexes (which data should we take into
	 *            account)
	 * @param clusterSize
	 *            - The size of cluster
	 * @return the new centroid
	 */
	protected abstract T calculateCenter(List<T> data, List<Integer> dataIndexes, int clusterSize);

}
