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
chebi <- which(d$dataset == 'chebi_149');
d$v1.4_mpm[chembl] <- round(60*(1678393/d$v1.4_t_s[chembl]))
d$v2.0_mpm[chembl] <- round(60*(1678393/d$v2.0_t_s[chembl]))
d$v1.4_mpm[chebi] <- round(60*(42704/d$v1.4_t_s[chebi]))
d$v2.0_mpm[chebi] <- round(60*(42704/d$v2.0_t_s[chebi]))

d$improve <- round(d$v1.4_t_s/d$v2.0_t_s, 2)

tab <- d[,c("benchmark", "ifmt", "dataset", "v1.4_skipped", "v1.4_tElap", "v1.4_mpm", "v2.0_skipped", "v2.0_tElap", "v2.0_mpm", "improve")]
tab$ifmt <- factor(tab$ifmt, c('smi', 'sdf'))
tab[order(tab$benchmark, tab$dataset, tab$ifmt), ]

write.table(file="~/workspace/github/cdk/cdk-paper-3/benchmark/results/table.tsv", tab, sep='\t', quote=F, row.names=F);
write.table(file="~/workspace/github/cdk/cdk-paper-3/benchmark/results/table.tex", tab, sep=' & ', quote=F, row.names=F);
