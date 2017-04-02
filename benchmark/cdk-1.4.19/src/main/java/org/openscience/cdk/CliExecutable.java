/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import com.google.common.base.Joiner;
import com.google.common.io.CountingInputStream;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class CliExecutable {

  enum FileFormat {
    SDF,
    SMI,
    UNK
  }

  protected final OptionParser       optpar     = new OptionParser();
  protected final OptionSpec<File>   inputSpec  = optpar.nonOptions().ofType(File.class);
  protected final OptionSpec<?>      timeSpec   = optpar.accepts("progress", "Show progress");
  protected final OptionSpec<String> ifmtSpec   = optpar.accepts("ifmt", "Input format")
                                                        .withRequiredArg()
                                                        .ofType(String.class);
  protected final OptionSpec<String> sdfTagSpec = optpar.accepts("title-tag", "Use this SDfile tag as the molecule title instead of the molecule title")
                                                        .withRequiredArg()
                                                        .ofType(String.class);
  protected final OptionSpec<File>   outputSpec = optpar.accepts("out", "Output file")
                                                        .withRequiredArg()
                                                        .ofType(File.class);

  private String name;
  private String[] args = null;

  public CliExecutable(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  static String getExt(String fname)
  {
    int i = fname.length() - 1;
    while (i > 0) {
      if (fname.charAt(i) == '.') {
        break;
      }
      i--;
    }
    if (i > 0) {
      return fname.substring(i + 1);
    } else {
      return "";
    }
  }

  static FileFormat getFmt(String str)
  {
    switch (str.toLowerCase(Locale.ROOT)) {
      case "mol":
      case "mdl":
      case "sdf":
      case "sd":
        return FileFormat.SDF;
      case "smi":
      case "csmi":
      case "cansmi":
      case "ism":
      case "smiles":
      case "cxsmi":
        return FileFormat.SMI;
      default:
        return FileFormat.UNK;
    }
  }

  void configure(OptionSet optset, OutputStream out) throws IOException
  {
    // to override if needed
  }

  abstract boolean processMolecule(BufferedWriter out, IAtomContainer mol) throws CDKException, IOException;

  private static final long PROGRESS_INTERVAL_NS = TimeUnit.MILLISECONDS.toNanos(200);

  private String makeProgressBar(double f, char c, int length)
  {
    char[] cs = new char[length];
    Arrays.fill(cs, ' ');
    Arrays.fill(cs, 0, (int) Math.ceil(f * length), c);
    return new String(cs);
  }

  private int reportProgress(long numBytes, long maxBytes, int numRecords, long tElap)
  {

    final double recordsPerSecond = numRecords / (tElap / 1e9);

    double nanoPerRecord = tElap / numRecords;

    int nextInterval = (int) (PROGRESS_INTERVAL_NS / nanoPerRecord);

    if (numBytes <= maxBytes) {
      double f = numBytes / (double) maxBytes;
      System.err.printf("\r[INFO] %.1f%% [%s] %d (%.0f per second)", 100 * f, makeProgressBar(f, '=', 25), numRecords, recordsPerSecond);
    } else {
      System.err.printf("\r[INFO] %d (%.0f per second)", numRecords, recordsPerSecond);
    }

    return nextInterval;
  }

  private void processStream(OptionSet optset,
                             FileFormat format,
                             BufferedWriter out,
                             InputStream in,
                             long totBytes)
  {

    int numProcessed = 0;
    int numError     = 0;
    int step         = 0;
    int interval     = 10;

    boolean showProgress = optset.has(timeSpec);
    boolean errorNewLine = false;

    CountingInputStream cin = null;
    if (showProgress) {
      in = cin = new CountingInputStream(in);
    }

    IChemObjectBuilder bldr = SilentChemObjectBuilder.getInstance();
    switch (format) {
      case SDF:

        String sdfTag = null;
        if (optset.has(sdfTagSpec))
          sdfTag = optset.valueOf(sdfTagSpec);

        long t0 = System.nanoTime();

          IteratingMDLReader sdfr = new IteratingMDLReader(in, bldr, true);
          while (sdfr.hasNext()) {
            IAtomContainer mol = sdfr.next();
            try {
              if (sdfTag != null) {
                mol.setProperty(CDKConstants.TITLE,
                                mol.getProperty(sdfTag));
              }

              if (!processMolecule(out, mol))
                numError++;

            }
            // pokemon exception (catch them all)
            catch (Exception | Error e) {
              errorNewLine = optionalLineBreak(errorNewLine);
              System.err.println("[ERROR] " + mol.getProperty(CDKConstants.TITLE) + ": " + e.getMessage());
              numError++;
            }

            numProcessed++;
            if (showProgress) {
              step++;
              if (step >= interval) {
                step = 0;
                long t1 = System.nanoTime();
                long td = t1 - t0;
                interval = reportProgress(cin.getCount(), totBytes, numProcessed, td);
                errorNewLine = true;
              }
            }
          }
          if (showProgress) {
            long t1 = System.nanoTime();
            long td = t1 - t0;
            reportProgress(cin.getCount(), totBytes, numProcessed, td);
            System.err.println("");
          }
        System.err.printf("Finished %s %s\n", name, Joiner.on(' ').join(args));
        System.err.printf(" Elapsed Time: %d\n", TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t0));
        System.err.printf(" Records:      %d\n", numProcessed);
        System.err.printf(" Processed:    %d\n", numProcessed-numError);
        System.err.printf(" Skipped:      %d\n", numError);
        break;
      case SMI:
        t0 = System.nanoTime();
        SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
        try (Reader rdr = new InputStreamReader(in, StandardCharsets.UTF_8);
             BufferedReader brdr = new BufferedReader(rdr)) {

          String line;
          while ((line = brdr.readLine()) != null) {
            try {
              String[] cols = splitSmiles(line);

              // empty SMILES are valid, give a bad error error message so we intercept
              if (cols[0].isEmpty()) {
                errorNewLine = optionalLineBreak(errorNewLine);
                System.err.println("[ERROR] Cannot handled empty molecule: " + cols[1]);
                numError++;
                continue;
              }

              IAtomContainer mol = smipar.parseSmiles(cols[0]);
              if (cols[1].isEmpty())
                mol.setProperty(CDKConstants.TITLE, cols[1]);
              if (!processMolecule(out, mol))
                numError++;
            } catch (InvalidSmilesException e) {
              errorNewLine = optionalLineBreak(errorNewLine);
              System.err.println("[ERROR] Bad SMILES: " + e.getMessage());
              numError++;
            } catch (Exception | Error e) {
              errorNewLine = optionalLineBreak(errorNewLine);
              System.err.println("[ERROR] " + line);
              System.err.println("[ERROR] " + e.getMessage());
              numError++;
            }

            numProcessed++;
            if (showProgress) {
              step++;
              if (step >= interval) {
                step = 0;
                long t1 = System.nanoTime();
                long td = t1 - t0;
                interval = reportProgress(cin.getCount(), totBytes, numProcessed, td);
                errorNewLine = true;
              }
            }
          }
        } catch (IOException e) {
          System.err.println("[FATAL] IO Error: " + e.getMessage());
        }

        if (showProgress) {
          long t1 = System.nanoTime();
          long td = t1 - t0;
          reportProgress(cin.getCount(), totBytes, numProcessed, td);
          System.err.print("\n");
        }
        System.err.printf("Finished %s %s\n", name, Joiner.on(' ').join(args));
        System.err.printf(" Elapsed Time: %d\n", TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-t0));
        System.err.printf(" Records       %d\n", numProcessed);
        System.err.printf(" Processed:    %d\n", numProcessed-numError);
        System.err.printf(" Skipped:      %d\n", numError);
        break;
    }
  }

  private boolean optionalLineBreak(boolean errorNewLine)
  {
    if (errorNewLine)
      System.err.print("\n");
    return false;
  }

  private boolean processFile(OptionSet optset, BufferedWriter out, File fname)
  {

    final FileFormat fmt;
    if (optset.has(ifmtSpec)) {
      String ifmt = optset.valueOf(ifmtSpec);
      fmt = getFmt(ifmt);
      if (fmt == FileFormat.UNK)
        System.err.println("Error: Unknown file format: " + ifmt);
    } else {
      String ext = getExt(fname.getName());
      fmt = getFmt(ext);
      if (fmt == FileFormat.UNK)
        System.err.println("Error: Unknown file format: " + ext);
    }

    if (fmt == FileFormat.UNK)
      return false;

    try (InputStream in = new FileInputStream(fname)) {
      processStream(optset, fmt, out, in, fname.length());
    } catch (IOException e) {
      System.err.println("IO Error: " + e.getMessage());
    }

    return true;
  }

  final void process(String[] args)
  {
    this.args = args;
    OptionSet optset = optpar.parse(args);

    try (OutputStream out = openOutput(optset.valueOf(outputSpec), System.out);
         Writer wtr = new OutputStreamWriter(out);
         BufferedWriter bwtr = new BufferedWriter(wtr)) {
      configure(optset, out);
      List<File> files = optset.valuesOf(inputSpec);
      if (files.isEmpty()) {
        System.err.println("Processing STDIN");
        processStdin(optset, bwtr);
      } else {
        for (File file : files)
          processFile(optset, bwtr, file);
      }
    } catch (IOException e) {
      System.err.println("IO Error: " + e.getMessage());
    }
  }

  private boolean processStdin(OptionSet optset, BufferedWriter out)
  {

    final FileFormat fmt;
    if (optset.has(ifmtSpec)) {
      String ifmt = optset.valueOf(ifmtSpec);
      fmt = getFmt(ifmt);
      if (fmt == FileFormat.UNK)
        System.err.println("Error: Unknown file format: " + ifmt);
    } else {
      System.err.println("Format must be specified for stdin: -ifmt=smi");
      return false;
    }

    if (fmt == FileFormat.UNK)
      return false;

    processStream(optset, fmt, out, System.in, 0);

    return true;
  }

  static OutputStream openOutput(File fname, OutputStream out)
  {
    try {
      if (fname != null)
        return new FileOutputStream(fname);
    } catch (FileNotFoundException e) {
      System.err.println("File not found: " + fname);
    }
    return out;
  }


  private static void writeNyble(BufferedWriter out, int x) throws IOException
  {
    switch (x) {
      case 0x0:
      case 0x1:
      case 0x2:
      case 0x3:
      case 0x4:
      case 0x5:
      case 0x6:
      case 0x7:
      case 0x8:
      case 0x9:
        out.write('0' + x);
        break;
      case 0xa:
        out.write('a');
        break;
      case 0xb:
        out.write('b');
        break;
      case 0xc:
        out.write('c');
        break;
      case 0xd:
        out.write('d');
        break;
      case 0xe:
        out.write('e');
        break;
      case 0xf:
        out.write('f');
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  static void writeByte(BufferedWriter out, byte x) throws IOException
  {
    writeNyble(out, x >>> 4 & 0xf);
    writeNyble(out, x & 0xf);
  }

  static void writeFps(BufferedWriter out, BitSet bitset, String title, int numbits) throws IOException
  {
    byte[] words = bitset.toByteArray();
    for (byte word : words)
      writeByte(out, word);
    int bitremain = numbits - 8 * words.length;
    while (bitremain > 0) {
      bitremain -= 8;
      out.write('0');
      out.write('0');
    }
    if (title != null) {
      out.write('\t');
      for (int i = 0; i < title.length(); i++)
        out.write(title.charAt(i));
    }
    out.write('\n');
  }


  static String getDateString()
  {
    Date now = new Date();
    return new SimpleDateFormat("YYYY-MM-DD").format(now) + "T" + new SimpleDateFormat("hh:mm:ss").format(now);
  }

  static void writeFpsHeader(OutputStream out, String key, String val) throws IOException
  {
    out.write('#');
    out.write(key.getBytes(StandardCharsets.UTF_8));
    out.write('=');
    if (val != null)
      out.write(val.getBytes(StandardCharsets.UTF_8));
    out.write('\n');
  }

  static String[] splitSmiles(String smi)
  {
    int i = 0;
    while (i < smi.length()) {
      char c = smi.charAt(i);
      if (c == '\t' || c == ' ')
        break;
      i++;
    }
    if (i < smi.length()) {
      return new String[]{smi.substring(0, i), smi.substring(i + 1)};
    } else {
      return new String[]{smi, ""};
    }
  }

}
