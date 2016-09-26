library(ggplot2)
library(reshape2)
x <- read.csv('pubcounts.csv', header=TRUE)
ggplot(x, aes(x=as.factor(year),y=cdk2))+
  geom_bar(stat='identity', fill='beige', colour='black', position='dodge')+
  geom_text(aes(label=cdk2,x=as.factor(year),y=cdk2, ymax=cdk2),vjust=1.25,
            position = position_dodge(width=1))+
  xlab("Year")+ylab("Number of citations")#+ylim(c(0, 60))
ggsave("cdk-paper-citation-growth.png")
