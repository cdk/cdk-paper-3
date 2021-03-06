/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;

public class Convert extends CliExecutable {

  FileFormat fmt = null;

  protected final OptionSpec<String> ofmtSpec;
  protected final OptionSpec<?> gen2d;

  private SmilesGenerator smigen = null;
  private SDFWriter       sdfw   = null;

  private StructureDiagramGenerator sdg = null;

  public Convert()
  {
    super("convert");
    ofmtSpec = optpar.accepts("ofmt", "Output format")
                     .withRequiredArg()
                     .ofType(String.class);
    gen2d = optpar.accepts("gen2d", "Generate 2D coordinates");
  }

  @Override
  void configure(OptionSet optset, OutputStream out) throws IOException
  {
    if (optset.has(ofmtSpec)) {
      fmt = getFmt(optset.valueOf(ofmtSpec));
      if (fmt == FileFormat.UNK) {
        System.err.println("[FATAl] Unknown output format: " + optset.valuesOf(ofmtSpec));
        System.exit(1);
      }
    } else if (optset.has(outputSpec)){
      String ext = getExt(optset.valueOf(outputSpec).getName());
      fmt = getFmt(ext);
      if (fmt == FileFormat.UNK) {
        System.err.println("[FATAl] Unknown output format: " + ext);
        System.exit(1);
      }
    }
    if (fmt == FileFormat.UNK || fmt == null) {
      System.err.println("[FATAl] No format specified");
      System.exit(1);
    }
    if (optset.has(gen2d)) {
      sdg = new StructureDiagramGenerator();
    }
  }

  @Override
  boolean processMolecule(BufferedWriter out, IAtomContainer mol) throws CDKException, IOException
  {
    // Gen 2D
    if (sdg != null)
      sdg.generateCoordinates(mol);

    switch (fmt) {
      case SMI:
        if (smigen == null)
          smigen = new SmilesGenerator(SmiFlavor.CxSmiles |
                                       SmiFlavor.Stereo |
                                       SmiFlavor.AtomicMass);
        out.write(smigen.create(mol));
        String title = mol.getProperty(CDKConstants.TITLE);
        if (title != null) {
          out.write(' ');
          out.write(title);
          out.write('\n');
        }
        break;
      case SDF:
        if (sdfw == null)
          sdfw = new SDFWriter(out);
        sdfw.write(mol);
        break;
    }

    return true;
  }
}
