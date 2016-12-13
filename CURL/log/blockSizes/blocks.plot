set terminal png linewidth 1
set out 'blocks.png'

set title "Distribution of Block Sizes"
set xlabel "Block Size"
set ylabel "Appearences"

set yrange [0:500]
set xrange [0:700]

binwidth=5
bin(x,width)=width*floor(x/width) + binwidth/2
set boxwidth binwidth

plot 'blocks.dat' using (bin($1,binwidth)):(1.0) title "Sizes"\
smooth freq with boxes
