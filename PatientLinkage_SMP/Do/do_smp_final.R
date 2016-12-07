
##If implementing be sure to modify the 
##next 2 lines to reflect proper file
##paths and file names

source("~/Rcode/smp/load_smp.R")
source("~/Rcode/smp/func_smp_final.R")



#create Alice's Encryption Table
#for a data vector
#as a list called a.tab
#ptm1 is proc time to make 
#Alice's encrypted tables
a <- alice2.data$id
b <- bob2.data$id

a.tab <- list()
ptm1 <- list()
for (i in 1:length(a.prime.sort)) {
ptm1[[length(ptm1) + 1]] <- proc.time()
a.tab[[length(a.tab) + 1]] <- alice.1.enc.table(a.prime.sort[i])
gc()
}
ptm1[[length(ptm1)]] <- proc.time() - ptm1[[length(ptm1)]]
print(ptm1)

gc()

ptm2 <- list()
ptm2[[length(ptm2) + 1]] <- proc.time()
gc()
compare(b.prime.sort, 10000, 10000, a.tab, bob.0.encode)

ptm2[[length(ptm2)]] <- proc.time() - ptm2[[length(ptm2)]]
print(ptm2)

gc()
  
      


