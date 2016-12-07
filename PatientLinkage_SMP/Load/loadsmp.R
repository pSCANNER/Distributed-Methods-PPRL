#Install Packages

install.packages("digest", dependencies=TRUE)
install.packages("homomorpheR", dependencies=TRUE)
install.packages("parallel")
library("digest")
library("homomorpheR")
library("gmp")
library("parallel")

# Load Data

##If implementing be sure to modify the 
##next 14 lines to reflect proper file
##paths and file names

#column types
d.type <- c("numeric", "character", "character", "character", 
            "character", "character", "numeric", "character", 
            "character", "numeric", "character", "character")
alice.data = read.csv("/Users/jndoctor/Documents/Toan/alice.csv", 
                   header = TRUE, stringsAsFactors=F,
                   colClasses = d.type)

d.type <- c("numeric", "character", "character", "character", 
            "character", "character", "numeric", "character", 
            "character", "numeric", "character", "character")
bob.data = read.csv("/Users/jndoctor/Documents/Toan/bob.csv", 
                   header = TRUE, stringsAsFactors=F,
                   colClasses = d.type)
attach(alice.data)
alice2.data <- alice.data[order(ssn),]
attach(bob.data)
bob2.data <- bob.data[order(ssn),]     
