set terminal png
set out 'encryptionTimes.png'
set title "Timing Results for Record Sets of Varying Sizes"
set xlabel "Number of Records Processed"
set ylabel "Time in Seconds"

set xrange [0:210000]
set yrange [0:1600]

set style data linespoints
set grid
set key left top

plot "encryptionTimes.txt" using 1:2 title "Read", \
"encryptionTimes.txt" using 1:3 title "Clean & Encrypt", \
"encryptionTimes.txt" using 1:4 title "Write"
