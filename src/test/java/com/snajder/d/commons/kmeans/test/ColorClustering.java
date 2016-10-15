package com.snajder.d.commons.kmeans.test;

import java.awt.Color;
import java.util.List;

import com.snajder.d.commons.kmeans.AbstractKMeans;

/**
 * Example implementation of {@link AbstractKMeans}. In {@link ColorClustering}
 * we are doing color clustering where we are trying to find dominant colors
 * from picture.
 * <p>
 * Color distance is defined as euclidean distance between RGB components.
 * Cluster center is calculated as mean of all colors (it's RGB component).
 * </p>
 */
public class ColorClustering extends AbstractKMeans<Color> {

	public ColorClustering() {
		// by default we would like to evaluate clusters using SSE (Sum of
		// Squared Errors) calculation
		enableClusterErrorCalculation(true);
	}

	@Override
	protected Double calculateDistance(Color value1, Color value2) {
		int r1 = value1.getRed();
		int g1 = value1.getGreen();
		int b1 = value1.getBlue();
		int r2 = value2.getRed();
		int g2 = value2.getGreen();
		int b2 = value2.getBlue();

		// calculate euclidean distance between two colors
		return Math.sqrt(Math.pow(r1 - r2, 2) + Math.pow(b1 - b2, 2) + Math.pow(g1 - g2, 2));
	}

	@Override
	protected Color calculateCenter(List<Color> data, List<Integer> dataIndexes, int clusterSize) {
		long r = 0, g = 0, b = 0;

		// calculate mean of each color component
		for (Integer index : dataIndexes) {
			final Color color = data.get(index);
			r += color.getRed();
			g += color.getGreen();
			b += color.getBlue();
		}

		float size = (float) clusterSize;

		int red = Math.round(r / size);
		int green = Math.round(g / size);
		int blue = Math.round(b / size);

		return new Color(red, green, blue);
	}
}
