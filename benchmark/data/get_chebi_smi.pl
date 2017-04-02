#/bin/perl

use strict;
use warnings;

my $smi;
my $id;

while (<>) {
    if ($_ eq "\$\$\$\$\n") {
	print $smi . " " . $id if $id;
	$smi = "";
	$id  = "";
    }
    elsif ($_=~ m/> <SMILES>/) {
	$smi = readline;
	chomp $smi
    }
    elsif ($_ =~ m/> <ChEBI ID>/) {
	$id = readline;
    }
}
print $smi . " " . $id if $id;
