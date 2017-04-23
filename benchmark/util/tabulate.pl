#!/bin/perl


my $results = {};

for my $arg (@ARGV) {

	open my $fp, '<', $arg or die $!;
	my $command;
	my $processed;	
	my $skipped;	
	my $time;	
	while (<$fp>) {
		chomp;
		if (m/^Finished/) {
			$command = $_;
			readline($fp);
			$processed = readline($fp); # num records
			readline($fp); 
			chomp $processed;
			$skipped   = readline($fp);
			chomp $skipped;
			$time = readline($fp); 

			$command =~ s/Finished //;
			$command =~ s/-ifmt=([^ ]+)//;
			my $fmt = $1;
			$command =~ s{../data/([^/]+?)\.(?:smi|sdf)}{}g;
			my $fname = $1;
			$time =~ m/((?:\d+:)*?\d+.\d+)\s*(?:real|elapsed)/i;
			my $tElap = $1;
			$skipped =~ s/\s*Skipped:\s+//i;

			print join("\t", $command, $fmt, $fname, $skipped, $tElap, to_seconds($tElap)) . "\n";

		}
	}
	close $fp or die $!;

}

sub to_seconds {
	my $t = shift;
	my @parts = split ":", $t;
	return $t if (@parts == 1);
	return (60*$parts[0])+$parts[1] if (@parts == 2);
	return (60*60*$parts[0])+(60*$parts[1])+$parts[2] if (@parts == 3);
}