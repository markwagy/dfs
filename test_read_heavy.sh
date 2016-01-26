# to time this test, use
# time ./test_read_heavy.sh


# run read heavy tests

# read 10 words
for i in {1..10}
do
    ./run_client.sh r
done

# write one word
head -1 /usr/share/dict/words | while read line; do ./run_client.sh w $line; done

# read 40 words
for i in {1..40}
do
    ./run_client.sh r
done
# write 4 words
head -4 /usr/share/dict/words | while read line; do ./run_client.sh w $line; done

# read 10 words
for i in {1..10}
do
    ./run_client.sh r
done
# write 5 words
head -5 /usr/share/dict/words | while read line; do ./run_client.sh w $line; done

# read 40 words
for i in {1..40}
do
    ./run_client.sh r
done

# write 4 words
head -4 /usr/share/dict/words | while read line; do ./run_client.sh w $line; done
