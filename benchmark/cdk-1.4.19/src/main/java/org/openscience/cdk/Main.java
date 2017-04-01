/*
 * Copyright (c) 2017. NextMove Software Ltd.
 */

package org.openscience.cdk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Main {

  private static final Map<String, CliExecutable> map = new HashMap<>();

  static {
    add(new Cansmi());
    add(new FpGen());
    add(new HeavyAtomCount());
    add(new Rings());
    add(new Convert());
  }

  private static void add(CliExecutable exec) {
    map.put(exec.getName(), exec);
  }

  public static void main(String[] args)
  {
    if (args.length == 0) {
      System.err.println("Usage: java -jar cdk-1.4-demo.jar " + map.keySet() + " {input} -out {output}");
      return;
    }

    String        cmd   = args[0];
    CliExecutable bmark = map.get(cmd.toLowerCase(Locale.ROOT));
    if (bmark == null) {
      System.err.println("No such command: " + cmd);
      System.err.println("Usage: ./cdk " + map.keySet() + " {input} -out {output}");
      return;
    }

    bmark.process(Arrays.copyOfRange(args, 1, args.length));
  }

}
