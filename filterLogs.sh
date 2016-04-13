for i in logs/*
	do
	echo $i
	awk '$1 ~ /.*<message>report/, $1 ~ "</record>"' $i > $i.summary
	done 
mv logs/*.summary summaries/
find summaries/ -empty -type f -delete
