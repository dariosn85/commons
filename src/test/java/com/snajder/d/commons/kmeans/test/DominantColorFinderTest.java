package com.snajder.d.commons.kmeans.test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.snajder.d.commons.kmeans.Cluster;
import com.snajder.d.commons.kmeans.IterationListener;

/**
 * Test application for finding dominant colors in an image.
 */
public class DominantColorFinderTest implements IterationListener<Color> {

	public void find() throws IOException {
		final ColorClustering clustering = new ColorClustering();
		clustering.setIterationListeren(this);

		final List<Color> colors = loadImage();

		// calculate clusters (find dominant colors)
		final List<Cluster<Color>> clusters = clustering.findClusters(colors);

		// print clusters
		for (Cluster<Color> cluster : clusters) {
			System.out.println(cluster + ", size=" + cluster.size() + ", sse=" + cluster.getError());
		}
	}

	/**
	 * Loads the image.
	 * 
	 * @return the list of colors
	 * @throws IOException
	 */
	private List<Color> loadImage() throws IOException {
		// load image
		final BufferedImage bufferedImage = ImageIO
				.read(DominantColorFinderTest.class.getResourceAsStream("3colors.png"));

		final List<Color> colors = new ArrayList<Color>();

		// convert image to List of Colors
		for (int x = 0; x < bufferedImage.getWidth(); x++) {
			for (int y = 0; y < bufferedImage.getHeight(); y++) {
				final Color color = new Color(bufferedImage.getRGB(x, y));
				colors.add(color);
			}
		}

		return colors;
	}

	public void onIterationFinished(List<Cluster<Color>> clusters, int iteration) {
		// after each iteration print current results (clusters)
		for (Cluster<Color> cluster : clusters) {
			System.out.println(iteration + ":" + cluster + ", size=" + cluster.size() + ", sse=" + cluster.getError());
		}
	}

	public static void main(String[] args) throws IOException {
		final DominantColorFinderTest test = new DominantColorFinderTest();

		test.find();
	}
}
