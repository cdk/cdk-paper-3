/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.SpanningTree;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.ringsearch.AllRingsFinder;
import org.openscience.cdk.ringsearch.SSSRFinder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;

public class Rings extends CliExecutable {

  final OptionSpec<?> markSpec;
  final OptionSpec<?> sssrSpec;
  final OptionSpec<?> allSpec;

  private enum RingSet {
    MARK,
    SSSR,
    ALL;
  }

  RingSet rset = null;

  public Rings()
  {
    super("rings");
    markSpec = optpar.accepts("mark", "Mark ring membership");
    sssrSpec = optpar.accepts("sssr", "Smallest Set of Smallest Ring");
    allSpec  = optpar.accepts("all",  "All rings (size≤12)");
  }

  @Override
  void configure(OptionSet optset, OutputStream out) throws IOException
  {
    if (optset.has(markSpec))
      rset = RingSet.MARK;
    if (optset.has(sssrSpec)) {
      if (rset != null) {
        System.err.println("[ERROR] please specify one of -mark, -sssr, or -all");
        System.exit(1);
      }
      rset = RingSet.SSSR;
    }
    if (optset.has(allSpec)) {
      if (rset != null) {
        System.err.println("[ERROR] please specify one of -mark, -sssr, or -all");
        System.exit(1);
      }
      rset = RingSet.ALL;
    }
    if (rset == null) {
      System.err.println("[ERROR] please specify one of -mark, -sssr, or -all");
      System.exit(1);
    }
  }

  @Override
  boolean processMolecule(BufferedWriter out, IAtomContainer mol) throws CDKException, IOException
  {
    switch (rset) {
      case MARK:
        SpanningTree spanningtree = new SpanningTree(mol);
        out.write(Integer.toString(spanningtree.getBondsAcyclicCount()));
        break;
      case SSSR:
        SSSRFinder finder = new SSSRFinder(mol);
        out.write(Integer.toString(finder.findSSSR().getAtomContainerCount()));
        break;
      case ALL:
        AllRingsFinder arf = new AllRingsFinder();
        arf.setTimeout(1000); // 1 second
        out.write(Integer.toString(arf.findAllRings(mol, 12).getAtomContainerCount()));
        break;
    }
    Object title = mol.getProperty(CDKConstants.TITLE);
    if (title != null) {
      out.write(' ');
      out.write(title.toString());
    }
    out.write('\n');
    return true;
  }
}
