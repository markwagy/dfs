# to time this test, use
# time ./test_write_heavy.sh


# run read heavy tests

# write 10 words
head -10 /usr/share/dict/words | while read line; do ./run_client.sh w $line; done
# read one word
./run_client.sh r
# write 40 words
head -40 /usr/share/dict/words | while read line; do ./run_client.sh w $line; done
# read 4 words
for i in {1..4}
do
    ./run_client.sh r
done
# write 10 words
head -10 /usr/share/dict/words | while read line; do ./run_client.sh w $line; done
# read 5 words
for i in {1..5}
do
    ./run_client.sh r
done
# write 40 words
head -40 /usr/share/dict/words | while read line; do ./run_client.sh w $line; done
# read 4 words
for i in {1..4}
do
    ./run_client.sh r
done
