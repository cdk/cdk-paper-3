/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

import java.io.BufferedWriter;
import java.io.IOException;

public class HeavyAtomCount extends CliExecutable {

  public HeavyAtomCount()
  {
    super("countheavy");
  }

  @Override
  boolean processMolecule(BufferedWriter out, IAtomContainer mol) throws CDKException, IOException
  {
    int numHeavy = 0;
    for (IAtom atom : mol.atoms()) {
      if (atom.getAtomicNumber() != null && atom.getAtomicNumber() != 1)
        numHeavy++;
    }
    out.write(Integer.toString(numHeavy));
    Object title = mol.getProperty(CDKConstants.TITLE);
    if (title != null) {
      out.write(' ');
      out.write(title.toString());
    }
    out.write('\n');
    return true;
  }
}
