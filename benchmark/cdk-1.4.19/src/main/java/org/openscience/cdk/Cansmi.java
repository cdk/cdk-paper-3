/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.BufferedWriter;
import java.io.IOException;

public class Cansmi extends CliExecutable {

  SmilesGenerator smigen = new SmilesGenerator();

  public Cansmi()
  {
    super("cansmi");
  }

  @Override
  boolean processMolecule(BufferedWriter out, IAtomContainer mol) throws CDKException, IOException
  {
    AtomContainerManipulator.removeHydrogens(mol);
    out.write(smigen.createSMILES(mol));

    Object title = mol.getProperty(CDKConstants.TITLE);
    if (title != null) {
      out.write(' ');
      out.write(title.toString());
    }
    out.write('\n');
    return true;
  }

}
