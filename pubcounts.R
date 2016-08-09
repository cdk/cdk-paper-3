library(ggplot2)
library(reshape2)
x <- read.csv('pubcounts.csv', header=TRUE)
ggplot(x, aes(x=as.factor(year),y=cdk2))+
  geom_bar(stat='identity', fill='beige', colour='black', position='dodge')+
  geom_text(aes(label=cdk2,x=as.factor(year),y=cdk2),
            position = position_dodge(width=1))+
  xlab("Year")+ylab("Number of citations")
