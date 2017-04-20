/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.Cycles;
import org.openscience.cdk.graph.GraphUtil;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.ringsearch.RingSearch;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

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
        int[][] adjlist = GraphUtil.toAdjList(mol);
        RingSearch ringSearch = new RingSearch(mol, adjlist);
        int count = 0;
        for (int beg = 0; beg < adjlist.length; beg++) {
          if (ringSearch.cyclic(beg)) {
            for (int end : adjlist[beg]) {
              if (end < beg && ringSearch.cyclic(beg, end))
                count++;
            }
          }
        }
        out.write(Integer.toString(count));
        break;
      case SSSR:
        out.write(Integer.toString(Cycles.sssr(mol).numberOfCycles()));
        break;
      case ALL:
        out.write(Integer.toString(Cycles.all(mol, 12).numberOfCycles()));
        break;
    }
    String title = mol.getProperty(CDKConstants.TITLE);
    if (title != null) {
      out.write(' ');
      out.write(title);
    }
    out.write('\n');
    return true;
  }
}
