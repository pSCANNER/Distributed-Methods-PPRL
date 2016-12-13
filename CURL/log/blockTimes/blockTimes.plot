set terminal png
set out 'blockTimes.png'
set title "Timing Results for Blocks of Encrypted Records"
set xlabel "Block Number Being Processed"
set ylabel "Time in Seconds"

set xrange [0:30]
set yrange [0:20]

set style data linespoints
set grid
set key left top

plot "blockTimes.txt" using 1:2 title "Time"
