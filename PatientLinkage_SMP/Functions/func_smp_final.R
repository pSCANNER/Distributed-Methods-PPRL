##########################################
##
##How to load this program:  source("~/Rcode/smp/{filename}")
##
#####################################


#Paillier Encryption

keys <- PaillierKeyPair$new(1024) # Generate new key pair

encrypt <- function(x) return(keys$pubkey$encrypt(x))

decrypt <- function(x) return(keys$getPrivateKey()$decrypt(x))

###################User Defined Functions#######################

####Alice Table Generation###################
alice.1.enc.table <- function (z) {
rawID.a <- z

#encrypt 32 zeros for later use
x <- rep(list(0),32)
e.x <- vector(mode = 'list', length = 32)
for (k in 1:32) {
e.x[[k]] <- keys$pubkey$encrypt(x[[k]])
}

#encrypt 32 numbers between 1 and 1,000,000
r <- encrypt(sample(1:1000000, 32))

l.r <- as.list(r)

count <- 0

e.z <- vector(mode = 'list', length = 32)
e.r <- vector(mode = 'list', length = 32)
for (i in 1:32) {
count <- count + 1
e.z[[count]] <- e.x[[i]]
e.r[[count]] <- l.r[[i]]
}



#create 3 dataframes for the 32-bit binary table
# alice.0 informs that Alice has zero
# alice.1 informs that alice has one
# bin.table is Alice's encoded table (as an R dataframe)
# A 2 x 32 array of encrypted numbers
# that decrypt to 0 or a random number Lin & Tzeng (2005)

alice.0 <- rbind(e.z,e.r)
alice.1 <- rbind(e.r,e.z)
bin.table <- rbind(vector(mode = 'list', length =32),vector(mode = 'list', length =32))




#create a binary string of the number 
bin.rawID.a <- as.numeric(intToBits(rawID.a))

#embed True set members in with false set members
for (i in 1:32) {
if (bin.rawID.a[i] == 0) {
#encrypt zeros in table
bin.table[1,i] <- as.character(alice.0[[1,i]])
bin.table[2,i] <- as.character(alice.0[[2,i]])
}
if (bin.rawID.a[i] == 1) {
#encrypt zeros in table 1024-bit key
bin.table[1,i] <- as.character(as.bigz(alice.1[[1,i]]))
bin.table[2,i] <- as.character(as.bigz(alice.1[[2,i]]))
}
}
return(bin.table)
}

########Bob 0 Encoding cipher multiplications ###########

## Function for Bob to zero encode his number 
## 'x' is Bob's cleartext number
## cipher is Alice's cipher table
## decrypting these will produce 0's if greater than relation holds
              
bob.0.encode <- function (x, cypher) {
rawID.b <- x
zero.encode.rawID <- list()
c.mult.list <- list()
count <- 1
bin.rawID.b <- as.numeric(intToBits(rawID.b))
for (i in 1:32) {

if (bin.rawID.b[i] == 0) {
bin.rawID.b[i] <- 1
c.mult <- unlist(cypher[2,i])											
for (j in length(bin.rawID.b[1:i]):32) {

if (bin.rawID.b[j] == 0) {
#encrypt zeros in table
c.mult <- c.mult*as.bigz(unlist(cypher[1,j]))
                         }
if (bin.rawID.b[j] == 1) {
#encrypt zeros in table 1024-bit key
c.mult <- c.mult*as.bigz(unlist(cypher[2,j]))
                         }
}
c.mult.list[[length(c.mult.list)+1]] <- as.bigz(c.mult)
                         }
                }  

return(c.mult.list)
		                         }


##########################################



##########################################
##How to load this program:  source("~/Rcode/smp/{filename}")

# Yao's Millionaire Problem
# merge sort portion

#Data for comparison
alice <- c(1, 3, 6, 8, 12)
bob <- c(1, 4, 5, 6,  8, 15)


## Data needed for compare
## alice encryption table for each x
## Bob's y
## With two separate systems this would have to be modified via read/write

compare <- function(b, x, y, r, s) {

# bob's data
# x is length of alice's vector of IDs
# y is length of bob's vector IDs
# r is a.tab (alice.1.enc.table)
# s is "bob.0.encode" here we just call a function
# in real application s would be replaced with an
# encrypted value sent from a database
# this function requires Alice and Bob's
# data be vectors a and b

count.x <- 1
count.y <- 1
count <- 1
position <- 1
stop.x <- 0  #stops evaluating x
stop.y <- 0  #stops evaluating y
xy.length <- x + y
z.sorted <- array(data = rep(NA,xy.length), dim = c(3,xy.length), dimnames = list(c("match","position", "x or y")))
comparisons <- 0
matches <- 0

b.mult <- list()
b.mult[[length(b.mult) + 1]] <- s(b[count.y], r[[count.x]])
gc()
m <- lapply(b.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

bm1.mult <- list()
bm1.mult[[length(bm1.mult) + 1]] <- s(b[count.y]-1, r[[count.x]])
gc()
m1 <- lapply(bm1.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

while ((count <= xy.length) & (stop.x == 0) & (stop.y == 0)) {

# checks x > y
if (is.element(0,lapply(m[[1]], function (x) as.numeric(x)))) {


comparisons <- comparisons + 1
z.sorted[1,count] <- "DR"
z.sorted[2,count] <- position
z.sorted[3,count] <- "y"
z.sorted
count.y <- count.y + 1
position <- position + 1
count <- count + 1

b.mult <- list()
b.mult[[length(b.mult) + 1]] <- s(b[count.y], r[[count.x]])
gc()
m <- lapply(b.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

bm1.mult <- list()
bm1.mult[[length(bm1.mult) + 1]] <- s(b[count.y]-1, r[[count.x]])
gc()
m1 <- lapply(bm1.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

if(count.y > y) {
count.y <- y
stop.y <- 1
												}
								 						}
								 						
# checks y > x
if (!is.element(0,lapply(m[[1]], function (x) as.numeric(x))) && 
    !is.element(0,lapply(m1[[1]], function (x) as.numeric(x)))) {
       

comparisons <- comparisons + 1
z.sorted[1,count] <- "DR"
z.sorted[2,count] <- position
z.sorted[3,count] <- "x"
count.x <- count.x + 1
position <- position + 1
count <- count + 1



if(count.x > x) {
count.x <- x
stop.x <- 1
												}

b.mult <- list()
b.mult[[length(b.mult) + 1]] <- s(b[count.y], r[[count.x]])
gc()
m <- lapply(b.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

bm1.mult <- list()
bm1.mult[[length(bm1.mult) + 1]] <- s(b[count.y]-1, r[[count.x]])
gc()
m1 <- lapply(bm1.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))
								 						}

# checks x = y
if (!is.element(0,lapply(m[[1]], function (x) as.numeric(x))) && 
     is.element(0,lapply(m1[[1]], function (x) as.numeric(x)))) {
     
     
matches <- matches + 1
comparisons <- comparisons + 1
z.sorted[1,count] <- "SR"
z.sorted[2,count] <- position
z.sorted[3,count] <- "x"
count.x <- count.x + 1
position <- position + 1
count <- count + 1



if(count.x > x) {
count.x <- x
stop.x <- 1
												}
if(count.y > y) {
count.y <- y
stop.y <- 1   
												}

b.mult <- list()
b.mult[[length(b.mult) + 1]] <- s(b[count.y], r[[count.x]])
gc()
m <- lapply(b.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

bm1.mult <- list()
bm1.mult[[length(bm1.mult) + 1]] <- s(b[count.y]-1, r[[count.x]])
gc()
m1 <- lapply(bm1.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

														}

											}
# the situation where all x's have been sorted
# and y's still need to be placed in the array											
while ((count <= xy.length) & (stop.x == 1) & (stop.y == 0) ) {	

# checks y > x
if (!is.element(0,lapply(m[[1]], function (x) as.numeric(x))) && 
    !is.element(0,lapply(m1[[1]], function (x) as.numeric(x)))) {
  

comparisons <- comparisons + 1
z.sorted[1,count] <- "END"
z.sorted[2,count] <- position
z.sorted[3,count] <- "y"
count.y <- count.y + 1
position <- position + 1
count <- count + 1		


if(count.y > y) {
count.y <- y
stop.y <- 1   
												}	
												
b.mult <- list()
b.mult[[length(b.mult) + 1]] <- s(b[count.y], r[[count.x]])
gc()
m <- lapply(b.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

bm1.mult <- list()
bm1.mult[[length(bm1.mult) + 1]] <- s(b[count.y]-1, r[[count.x]])
gc()
m1 <- lapply(bm1.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))
																			
														 }
#checks x = y
if (!is.element(0,lapply(m[[1]], function (x) as.numeric(x))) && 
     is.element(0,lapply(m1[[1]], function (x) as.numeric(x)))) {
    

matches <- matches + 1
comparisons <- comparisons + 1
z.sorted[1,count] <- "S"
z.sorted[2,count] <- position
z.sorted[3,count] <- "y"
count.y <- count.y + 1
position <- position + 1


if(count.y > y) {
count.y <- y
stop.y <- 1   
												}

b.mult <- list()
b.mult[[length(b.mult) + 1]] <- s(b[count.y], r[[count.x]])
gc()
m <- lapply(b.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

bm1.mult <- list()
bm1.mult[[length(bm1.mult) + 1]] <- s(b[count.y]-1, r[[count.x]])
gc()
m1 <- lapply(bm1.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

count <- count + 1
														}


																								 }

#sorts x when all y's are sorted

while ((count <= xy.length) & (stop.x == 0) & (stop.y == 1)) {	

# checks x > y
if (is.element(0,lapply(m[[1]], function (x) as.numeric(x)))) {


comparisons <- comparisons + 1
z.sorted[1,count] <- "END"
z.sorted[2,count] <- position
z.sorted[3,count] <- "x"
count.x <- count.x + 1
position <- position + 1
count <- count + 1		

if(count.x > x) {
count.x <- x
stop.x <- 1   
												}	
												
b.mult <- list()
b.mult[[length(b.mult) + 1]] <- s(b[count.y], r[[count.x]])
gc()
m <- lapply(b.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

bm1.mult <- list()
bm1.mult[[length(bm1.mult) + 1]] <- s(b[count.y]-1, r[[count.x]])
gc()
m1 <- lapply(bm1.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))
																			
														 }
# checks x = y
if (!is.element(0,lapply(m[[1]], function (x) as.numeric(x))) && 
     is.element(0,lapply(m1[[1]], function (x) as.numeric(x)))) {
    

matches <- matches + 1
comparisons <- comparisons + 1
z.sorted[1,count] <- "END"
z.sorted[2,count] <- position
z.sorted[3,count] <- "x"
count.x <- count.x + 1
position <- position + 1

if(count.x > x) {
count.x <- x
stop.x <- 1   
												}
												
												b.mult <- list()
b.mult[[length(b.mult) + 1]] <- s(b[count.y], r[[count.x]])
gc()
m <- lapply(b.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))

bm1.mult <- list()
bm1.mult[[length(bm1.mult) + 1]] <- s(b[count.y]-1, r[[count.x]])
gc()
m1 <- lapply(bm1.mult, function(x) lapply(x, function(x) decrypt(unlist(x))))


count <- count + 1
														}


																								 }
																						 
print(paste(c("# of comparisons:", comparisons)))
print(paste(c("# of matches:", matches)))
gc()
return(z.sorted)	
}


g <- c(1,2,3,4,5,6,7,8,9,10)
h <- c(1,2,3,4,5,6,11,12,13,14)


