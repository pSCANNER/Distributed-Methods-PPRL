set terminal png
set out 'linkage.png'

set title "Distribution of Scores"
set xlabel "Score Value"
set ylabel "Appearences"

set yrange [0:55000]
set xrange [0:70]

binwidth=.25
bin(x,width)=width*floor(x/width) + binwidth/2
set boxwidth binwidth

plot 'linkage.dat' using (bin($1,binwidth)):(1.0) title "Scores"\
smooth freq with boxes
