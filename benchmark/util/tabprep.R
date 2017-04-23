chebi_149_sdf_size <- 42704
chebi_149_smi_size <- chebi_149_sdf_size-2107
chembl_22_sdf_size <- 1678393
chembl_22_smi_size <- chembl_22_sdf_size

toseconds <- function(str) {
  parts <- unlist(strsplit(as.character(str), ":"))
  if (length(parts) == 3) {
    return (3600*as.numeric(parts[1]) + 60*as.numeric(parts[2]) + as.numeric(parts[3]));
  } else if (length(parts) == 2) {
    return (60*as.numeric(parts[1]) + as.numeric(parts[2]));
  } else if (length(parts) == 1) {
    return (as.numeric(parts[1]));
  }
}

tohms <- function(sec) {
  res <- ""
  if (is.na(sec))
    return (NA);
  decimal <- T
  if (sec > 3600) {
    h <- floor(sec / 3600)
    sec <- sec - (h*3600);
    res <- paste(res, h, "h", sep="")
    decimal <- F
  }
  if (sec > 60) {
    m <- floor(sec / 60)
    sec <- sec - (m*60);
    res <- paste(res, m, "m", sep="")
  }
  if (decimal) {
    res <- paste(res, round(sec, 2), "s", sep="")
  } else {
    res <- paste(res, round(sec), "s", sep="")
  }
  return (res)
}

tohuman <- function(num) {
  if (is.na(num) || is.infinite(num))
    return ("-");
  if (num > 1e6) {
    return (paste(round(num/1e6,1), "M", sep=""))
  }
  if (num > 1e3) {
    return (paste(round(num/1e3,1), "K", sep=""))
  }
  return(num);
}

cdk_1.4 <- read.table("~/workspace/github/cdk/cdk-paper-3/benchmark/results/cdk-1.4.19-results", sep='\t')
cdk_2.0 <- read.table("~/workspace/github/cdk/cdk-paper-3/benchmark/results/cdk-2.0-results", sep='\t')

colnames(cdk_1.4) <- c('benchmark', 'ifmt', 'dataset', 'skipped', 'tElap', 't_s')
colnames(cdk_2.0) <- c('benchmark', 'ifmt', 'dataset', 'skipped', 'tElap', 't_s')

cdk_1.4$benchmark <- as.character(cdk_1.4$benchmark)
cdk_2.0$benchmark <- as.character(cdk_2.0$benchmark)

cdk_1.4$version = "CDK 1.4.19"
cdk_2.0$version = "CDK 2.0"

d <- data.frame();
for (i in 1:nrow(cdk_2.0)) {
  row_2.0 <- cdk_2.0[i,];
  row_1.4 <- subset(cdk_1.4,
         benchmark == row_2.0$benchmark &
         ifmt      == row_2.0$ifmt &
         dataset   == row_2.0$dataset)
  if (nrow(row_1.4) == 1) {
    row <- cbind(row_2.0[-7], row_1.4$skipped, row_1.4$tElap, row_1.4$t_s);
    names(row) <- c('benchmark', 'ifmt', 'dataset', 'v2.0_skipped', 'v2.0_tElap', 'v2.0_t_s', 'v1.4_skipped', 'v1.4_tElap', 'v1.4_t_s')
    d <- rbind(d, row);
  } else {
    row <- cbind(row_2.0[-7], 0, 0, 0);
    names(row) <- c('benchmark', 'ifmt', 'dataset', 'v2.0_skipped', 'v2.0_tElap', 'v2.0_t_s', 'v1.4_skipped', 'v1.4_tElap', 'v1.4_t_s')
    d <- rbind(d, row);
  }
}

d$v2.0_mpm <- c()
d$v1.4_mpm <- c()

chembl <- which(d$dataset == 'chembl_22_1');
chebi_sdf <- which(d$dataset == 'chebi_149' & d$ifmt == 'sdf');
chebi_smi <- which(d$dataset == 'chebi_149' & d$ifmt == 'smi');
d$v1.4_mpm[chembl] <- round(60*(chembl_22_sdf_size/d$v1.4_t_s[chembl]))
d$v2.0_mpm[chembl] <- round(60*(chembl_22_sdf_size/d$v2.0_t_s[chembl]))
d$v1.4_mpm[chebi_sdf] <- round(60*(chebi_149_sdf_size/d$v1.4_t_s[chebi_sdf]))
d$v2.0_mpm[chebi_sdf] <- round(60*(chebi_149_sdf_size/d$v2.0_t_s[chebi_sdf]))
d$v1.4_mpm[chebi_smi] <- round(60*(chebi_149_smi_size/d$v1.4_t_s[chebi_smi]))
d$v2.0_mpm[chebi_smi] <- round(60*(chebi_149_smi_size/d$v2.0_t_s[chebi_smi]))

d$improve <- round(d$v1.4_t_s/d$v2.0_t_s, 2)

tab <- d[,c("benchmark", "dataset", "ifmt", "v1.4_skipped", "v1.4_tElap", "v1.4_mpm", "v2.0_skipped", "v2.0_tElap", "v2.0_mpm", "improve")]
tab$ifmt <- factor(tab$ifmt, c('smi', 'sdf'))
tab <- tab[order(tab$benchmark, tab$dataset, tab$ifmt), ]

sapply(subset(tab, benchmark=="convert--gen2d-ofmtsdf")$v1.4_tElap, 
       function(x) {
         toseconds(x)
       })
subset(tab, benchmark=="convert--gen2d-ofmtsdf")$v1.4_tElap
unlist(strsplit(as.character("3:27:07"), ":"))

# subtract countheavy numbers
base  <- subset(tab, benchmark == 'countheavy')
delta <- tab
delta$v1.4_tElap <- as.character(delta$v1.4_tElap)
delta$v2.0_tElap <- as.character(delta$v2.0_tElap)
for (i in 1:nrow(tab)) {
  row <- tab[i,]  
  tmp <- subset(base, dataset == row$dataset & ifmt == row$ifmt)
  delta[i,]$v1.4_skipped <- tab[i,]$v1.4_skipped - tmp$v1.4_skipped
  delta[i,]$v2.0_skipped <- tab[i,]$v2.0_skipped - tmp$v2.0_skipped
  delta[i,]$v1.4_tElap <- round(toseconds(tab[i,]$v1.4_tElap) - toseconds(tmp$v1.4_tElap), 2)
  delta[i,]$v2.0_tElap <- round(toseconds(tab[i,]$v2.0_tElap) - toseconds(tmp$v2.0_tElap), 2)
  delta[i,]$improve    <- round(as.numeric(delta[i,]$v1.4_tElap) / as.numeric(delta[i,]$v2.0_tElap), 1);
  if (row$dataset == "chebi_149") {
    if (row$ifmt == 'smi') {
      delta[i,]$v1.4_mpm <- round(60*(chebi_149_smi_size/as.numeric(delta[i,]$v1.4_tElap)))
      delta[i,]$v2.0_mpm <- round(60*(chebi_149_smi_size/as.numeric(delta[i,]$v2.0_tElap)))
    } else {
      delta[i,]$v1.4_mpm <- round(60*(chebi_149_sdf_size/as.numeric(delta[i,]$v1.4_tElap)))
      delta[i,]$v2.0_mpm <- round(60*(chebi_149_sdf_size/as.numeric(delta[i,]$v2.0_tElap)))
    }
  } else {
    delta[i,]$v1.4_mpm <- round(60*(chembl_22_sdf_size/as.numeric(delta[i,]$v1.4_tElap)))
    delta[i,]$v2.0_mpm <- round(60*(chembl_22_sdf_size/as.numeric(delta[i,]$v2.0_tElap)))
  }
  delta[i,]$v1.4_tElap <- tohms(as.numeric(delta[i,]$v1.4_tElap))
  delta[i,]$v2.0_tElap <- tohms(as.numeric(delta[i,]$v2.0_tElap))
}
delta$v1.4_mpm <- sapply(delta$v1.4_mpm, tohuman)
delta$v2.0_mpm <- sapply(delta$v2.0_mpm, tohuman)

# reorder
benchmark_ordering <- c("countheavy",
                        "rings-mark",
                        "rings-sssr",
                        "rings-all",
                        "cansmi",
                        "convert-ofmtsmi",
                        "convert-ofmtsdf",
                        "convert--gen2d-ofmtsdf",
                        "fpgen-type=path",
                        "fpgen-type=maccs",
                        "fpgen-type=circ")
delta$benchmark <- factor(delta$benchmark, benchmark_ordering)
tab$benchmark <- factor(tab$benchmark, benchmark_ordering)
delta <- delta[order(delta$benchmark),]
tab <- tab[order(tab$benchmark),]

write.table(file="~/workspace/github/cdk/cdk-paper-3/benchmark/results/table_delta.tsv", delta, sep='\t', quote=F, row.names=F);

delta$benchmark <- as.character(delta$benchmark)
delta$dataset <- as.character(delta$dataset)
delta$dataset[seq(2,length(delta$dataset),2)] <- ""
delta$benchmark[seq(1,length(delta$benchmark),1)[-seq(1,length(delta$benchmark),4)]] <- ""
write.table(file="~/workspace/github/cdk/cdk-paper-3/benchmark/results/table_delta.tex", delta, sep=' & ', quote=F, row.names=F);


tab$v1.4_mpm <- sapply(tab$v1.4_mpm, tohuman)
tab$v2.0_mpm <- sapply(tab$v2.0_mpm, tohuman)
tab$v1.4_tElap <- sapply(tab$v1.4_tElap, function(x) { tohms(toseconds(x)) })
tab$v2.0_tElap <- sapply(tab$v2.0_tElap, function(x) { tohms(toseconds(x)) })

write.table(file="~/workspace/github/cdk/cdk-paper-3/benchmark/results/table.tsv", tab, sep='\t', quote=F, row.names=F);

tab$benchmark <- as.character(tab$benchmark)
tab$dataset <- as.character(tab$dataset)
tab$dataset[seq(2,length(tab$dataset),2)] <- ""
tab$benchmark[seq(1,length(tab$benchmark),1)[-seq(1,length(tab$benchmark),4)]] <- ""

write.table(file="~/workspace/github/cdk/cdk-paper-3/benchmark/results/table.tex", tab, sep=' & ', quote=F, row.names=F);

