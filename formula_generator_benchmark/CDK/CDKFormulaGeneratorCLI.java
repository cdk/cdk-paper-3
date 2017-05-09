import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.formula.MolecularFormulaGenerator;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

public class CDKFormulaGeneratorCLI {

	public static void main(String args[]) {

		if (args.length != 3) {
			System.err.println("Use 3 arguments: <mass_file> <tolerance> <elements>");
			System.err.println("For example:");
			System.err.println("      mass_file: masses.txt");
			System.err.println("      tolerance: 0.005");
			System.err.println("      elements: C0-100H0-100N0-50O0-50S0-10P0-10");
			System.exit(1);
		}

		try {

			double tolerance = Double.parseDouble(args[1]);
			String elements = args[2];

			IsotopeFactory ifac = Isotopes.getInstance();
			Pattern p = Pattern.compile("(\\p{Alpha}+)(\\d+)-(\\d+)");
			Matcher m = p.matcher(elements);

			MolecularFormulaRange mfRange = new MolecularFormulaRange();

			while (m.find()) {
				String element = m.group(1);
				int min = Integer.parseInt(m.group(2));
				int max = Integer.parseInt(m.group(3));
				IIsotope i;
				if (element.equals("D"))
					i = ifac.getIsotope("H", 2);
				else
					i = ifac.getMajorIsotope(element);
				mfRange.addIsotope(i, min, max);
			}

			IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
			final BufferedReader br = new BufferedReader(new FileReader(args[0]));
			String line;

			while ((line = br.readLine()) != null) {

				if (line.trim().length() == 0)
					continue;

				double mass = Double.parseDouble(line);
				MolecularFormulaGenerator gen = new MolecularFormulaGenerator(builder, mass - tolerance,
						mass + tolerance, mfRange);

				IMolecularFormula formula;
				while ((formula = gen.getNextFormula()) != null) {
					String formulaString = MolecularFormulaManipulator.getString(formula);
					System.out.println(formulaString);
				}
			}

			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
