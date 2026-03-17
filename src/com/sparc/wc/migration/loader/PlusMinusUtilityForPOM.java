package com.sparc.wc.migration.loader;
import java.io.*;
import java.util.*;
public class PlusMinusUtilityForPOM {

	static class PomKey {
		String productId;
		String measurementSetName;
		String pomName;

		PomKey(String productId, String measurementSetName, String pomName) {
			this.productId = productId;
			this.measurementSetName = measurementSetName;
			this.pomName = pomName;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof PomKey))
				return false;
			PomKey other = (PomKey) o;
			return this.productId.equals(other.productId) && this.measurementSetName.equals(other.measurementSetName)
					&& this.pomName.equals(other.pomName);
		}

		@Override
		public int hashCode() {
			return Objects.hash(productId, measurementSetName, pomName);
		}
	}

	static class Tolerance {
		String minusTolerance;
		String plusTolerance;

		Tolerance(String minus, String plus) {
			this.minusTolerance = minus;
			this.plusTolerance = plus;
		}
	}

	public static void normalize(File inputFile, File outputFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(inputFile));
		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		Map<PomKey, Tolerance> firstToleranceMap = new HashMap<>();

		String line;
		String currentProductId = null;
		String currentMeasurementSet = null;
		String currentPOM = null;

		String pendingMinus = null;
		String pendingPlus = null;
		boolean insidePOMBlock = false;
		List<String> pomBlock = new ArrayList<>();

		while ((line = reader.readLine()) != null) {
			// Detect new LCSMeasurementsProduct and extract metadata
			if (line.startsWith("LCSMeasurementsProduct")) {
				String[] parts = line.split("\t");
				if (parts.length >= 3) {
					currentMeasurementSet = parts[1];
					currentProductId = parts[2];
				}
				writer.write(line);
				writer.newLine();
				continue;
			}

			if (line.startsWith("LCSPointsOfMeasure")) {
				// Start of a new POM block
				insidePOMBlock = true;
				pomBlock.clear();
				pomBlock.add(line);
				currentPOM = line.split("\t")[1]; // POM name
				continue;
			}

			if (insidePOMBlock) {
				pomBlock.add(line);

				if (line.startsWith("LCSObjectAttribute\tminusTolerance")) {
					pendingMinus = line.split("\t")[2];
				} else if (line.startsWith("LCSObjectAttribute\tplusTolerance")) {
					pendingPlus = line.split("\t")[2];
				} else if (line.startsWith("LCSObjectAttribute\tEnd")) {
					// End of POM block
					PomKey key = new PomKey(currentProductId, currentMeasurementSet, currentPOM);
					Tolerance standardTol;

					if (!firstToleranceMap.containsKey(key)) {
						// Store the first occurrence
						standardTol = new Tolerance(pendingMinus, pendingPlus);
						firstToleranceMap.put(key, standardTol);
					} else {
						// Get the stored tolerance
						standardTol = firstToleranceMap.get(key);
					}

					// Rewrite the pomBlock with normalized tolerance
					for (String blockLine : pomBlock) {
						if (blockLine.startsWith("LCSObjectAttribute\tminusTolerance")) {
							writer.write("LCSObjectAttribute\tminusTolerance\t" + standardTol.minusTolerance);
						} else if (blockLine.startsWith("LCSObjectAttribute\tplusTolerance")) {
							writer.write("LCSObjectAttribute\tplusTolerance\t" + standardTol.plusTolerance);
						} else {
							writer.write(blockLine);
						}
						writer.newLine();
					}

					// Reset state
					insidePOMBlock = false;
					pomBlock.clear();
					currentPOM = null;
					pendingMinus = null;
					pendingPlus = null;
					continue;
				}
				continue;
			}

			// Default: Write line as-is
			writer.write(line);
			writer.newLine();
		}

		reader.close();
		writer.close();
		System.out.println("Normalization completed successfully.");
	}

	public static void main(String[] args) throws IOException {
		File input = new File("C:\\Reebok\\Measurement\\productMeasurementsExportFinalAug.txt"); // Replace with your input file path
		File output = new File("C:\\Reebok\\Measurement\\productMeasurementsPlusMinusFinal.txt"); // Replace with desired output path
		normalize(input, output);
	}
}



