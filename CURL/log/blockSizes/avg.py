import fileinput

x = 0
count = 1

for line in open('blocks.dat','r'):
	x = x + int(line)
	count = count + 1
print "x=%s" % str(x)
print "count=%s" % str(count)
print "average=%s" % str(x / count)
	
