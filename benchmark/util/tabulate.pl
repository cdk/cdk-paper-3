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
			$time =~ m/((?:\d+:)?\d+.\d+)\s*(?:real|user)/i;
			my $tElap = $1;
			$skipped =~ s/\s*Skipped:\s+//i;

			my $tMin = int($tElap / 60);
			$tElap -= (60*$tMin);

			print join("\t", $command, $fmt, $fname, $skipped, sprintf("%dm%.2fs", $tMin, $tElap)) . "\n";

		}
	}
	close $fp or die $!;

}