/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.AtomContainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

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
    String title = mol.getProperty(CDKConstants.TITLE);
    if (title != null) {
      out.write(' ');
      out.write(title);
    }
    out.write('\n');
    return true;
  }
}
