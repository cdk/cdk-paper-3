/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import joptsimple.OptionSet;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.List;

public class Cansmi extends CliExecutable {

  private final SmilesGenerator smigen = new SmilesGenerator(SmiFlavor.Canonical);

  public Cansmi()
  {
    super("cansmi");
  }

  @Override
  boolean processMolecule(BufferedWriter out, IAtomContainer mol) throws CDKException, IOException
  {
    AtomContainerManipulator.suppressHydrogens(mol);
    out.write(smigen.create(mol));

    String title = mol.getProperty(CDKConstants.TITLE);
    if (title != null) {
      out.write(' ');
      out.write(title);
    }
    out.write('\n');
    return true;
  }
}
